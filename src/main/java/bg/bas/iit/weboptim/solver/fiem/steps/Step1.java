package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.*;

import net.vatov.ampl.model.ConstraintDeclaration;
import net.vatov.ampl.model.ConstraintDeclaration.RelopType;
import net.vatov.ampl.model.Expression;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.UserIO;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemException;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step1 extends BaseStep {

    private Logger logger;
    private List<SymbolDeclaration> vars = new ArrayList<SymbolDeclaration>();
    private List<double[]> initialPopulation;
    private Config cfg;
    private double[] chebyshevCenter;
    private final UserIO input;

    private class CyclicIterator implements Iterator<SymbolDeclaration> {

        private final List<SymbolDeclaration> sds;
        private Iterator<SymbolDeclaration> it;
        private SymbolDeclaration markedElement;
        private SymbolDeclaration currentElement;
        private boolean passedFullCycle = false;

        public CyclicIterator(List<SymbolDeclaration> sds) {
            this.sds = sds;
            it = sds.iterator();
        }

        public boolean hasNext() {
            return sds.size() != 0;
        }

        public SymbolDeclaration next() {
            if (it.hasNext()) {
                currentElement = it.next();
            } else {
                it = sds.iterator();
                currentElement = it.next();
            }
            if (currentElement == markedElement) {
                passedFullCycle = true;
            }
            if (null == markedElement) {
                markedElement = currentElement;
            }
            return currentElement;
        }

        public void remove() {
            throw new FiemException("Operation not supported");
        }

        public void markCurrentElement() {
            markedElement = currentElement;
            passedFullCycle = false;
        }

        public Boolean hasPassedFullCycle() {
            return passedFullCycle;
        }
    }

    private interface ConstraintComputation {
        double compute();

        boolean isBetter(double newVal, double oldVal);
    }

    private class InitailVectorComputation implements ConstraintComputation {

        public double compute() {
            double S = 0;
            for (ConstraintDeclaration c : model.getConstraints()) {
                double tmp = 0;
                if (c.getRelop().equals(RelopType.LE)) {
                    tmp = interpreter.evaluateExpression(c.getaExpr()) - interpreter.evaluateExpression(c.getbExpr());
                } else if (c.getRelop().equals(RelopType.GE)) {
                    tmp = interpreter.evaluateExpression(c.getbExpr()) - interpreter.evaluateExpression(c.getaExpr());
                } else if (c.getRelop().equals(RelopType.EQ)) {
                    tmp = Math.abs(interpreter.evaluateExpression(c.getbExpr()) - interpreter.evaluateExpression(c.getaExpr()));
                }
                if (tmp > 0) {
                    S += tmp;
                }
            }
            return S;
        }

        public boolean isBetter(double newVal, double oldVal) {
            return newVal < oldVal;
        }
    }

    public Step1(OptimModel model, OptimModelInterpreter interpreter, Config cfg, UserIO input) {
        super(model, interpreter);
        this.input = input;
        this.cfg = cfg;
        logger = getLogger(Step1.class, cfg.threadedLogging);
        vars = model.getVarRefs();
        for (SymbolDeclaration sd : vars) {
            if (!isBounded(sd)) {
                throw new FiemException("variable " + sd.getName() + " is unbounded");
            }
        }
        chebyshevCenter = new double[vars.size()];
    }

    private boolean isBounded(SymbolDeclaration sd) {
        return sd.getLowerBound() != null && sd.getUpperBound() != null;
    }

    private void generateRandomInitialX() {
        for (SymbolDeclaration sd : vars) {
            Expression lbExpr = sd.getLowerBound();
            Expression ubExpr = sd.getUpperBound();
            Double lb = interpreter.evaluateExpression(lbExpr);
            Double ub = interpreter.evaluateExpression(ubExpr);
            double random = Math.random() * (ub - lb) + lb;
            if (sd.isInteger()) {
                random = Math.rint(random);
                if (random > ub) {
                    random = ub - 1;
                }
                if (random < lb) {
                    random = lb + 1;
                }
            }
            // TODO: gi(x) eval
            sd.setBindValue(random);
        }
    }

    private void gotoChebyshevCenter() {
        Map<String, double[]> constraintsCoefficients = Util.getConstraintsCoefficients(model, interpreter);
        logger.debug("Chebyshev center constraint coefficients");
        Util.dumpPoints(logger, constraintsCoefficients);
        LinearObjectiveFunction goal = Util.getLinearObjectiveFunction(constraintsCoefficients);
        List<LinearConstraint> constraints = Util.getLinearConstraints(constraintsCoefficients);
        logger.debug("Chebyshev center model (Convex Optimization, Boyd and Vandenberghe):");
        Util.dumpMath3Model(logger, goal, constraints);
        double[] p = Util.computeChebyshevCenter(goal, constraints);

        int i = 0;
        double[] rounded = Util.roundAndCheck(logger, p, model, interpreter);
        for (SymbolDeclaration sd : vars) {
            if (sd.isInteger()) {
                sd.setBindValue(rounded[i]);
            } else {
                sd.setBindValue(p[i]);
            }
            chebyshevCenter[i] = sd.getBindValue();
            i++;
        }
    }

    private Double minimizeS() {
        InitailVectorComputation ic = new InitailVectorComputation();
        double S = ic.compute();
        CyclicIterator it = new CyclicIterator(vars);
        while (0 < S && !it.hasPassedFullCycle()) {
            SymbolDeclaration sd = it.next();
            double newS = moveUpIfPossible(sd, S, ic);
            if (ic.isBetter(newS, S)) {
                S = newS;
                it.markCurrentElement();
            } else {
                newS = moveDownIfPossible(sd, S, ic);
                if (ic.isBetter(newS, S)) {
                    S = newS;
                    it.markCurrentElement();
                }
            }
        }
        if (!Util.constraintsSatisfied(model, interpreter, null)) {
            logger.error("S is " + S);
            logger.error("variables vector is: " + vars);
            logger.error("Broken constraints are: ");
            for (int i = 0; i < model.getConstraints().size(); i++) {
                if (!interpreter.evaluateConstraint(i)) {
                    logger.error(model.getConstraints().get(i).getName());
                }
            }
            throw new FiemException("Found vector does not satisfies constraints");
        }
        return S;
    }

    private double moveDownIfPossible(SymbolDeclaration sd, double goal, ConstraintComputation cc) {
        Double value = sd.getBindValue();
        double newValue = value - cfg.improveStep;
        if (null != sd.getLowerBound()) {
            Double lowerBound = interpreter.evaluateExpression(sd.getLowerBound());
            if (newValue >= lowerBound) {
                return hasImprovementOfGoalWithRestore(sd, goal, value, newValue, cc);
            }
            return goal;
        } else {
            return hasImprovementOfGoalWithRestore(sd, goal, value, newValue, cc);
        }
    }

    private double moveUpIfPossible(SymbolDeclaration sd, double goal, ConstraintComputation cc) {
        Double value = sd.getBindValue();
        double newValue = value + cfg.improveStep;
        if (null != sd.getUpperBound()) {
            Double upperBound = interpreter.evaluateExpression(sd.getUpperBound());
            if (newValue <= upperBound) {
                return hasImprovementOfGoalWithRestore(sd, goal, value, newValue, cc);
            }
            return goal;
        } else {
            return hasImprovementOfGoalWithRestore(sd, goal, value, newValue, cc);
        }
    }

    private double hasImprovementOfGoalWithRestore(SymbolDeclaration sd, double goal, double oldValue, double newValue,
                                                   ConstraintComputation cc) {
        sd.setBindValue(newValue);
        double newGoal = cc.compute();
        if (cc.isBetter(newGoal, goal)) {
            return newGoal;
        } else {
            sd.setBindValue(oldValue);
        }
        return goal;
    }

    private void generateInitialPopulation() {
        int extPopSize = (int) Math.floor(cfg.popSize * cfg.popSizeExtCoef);
        int populationSize = cfg.popSize + extPopSize;
        logger.debug(String.format("Will generate initial population of size %d (base: %d, ext: %d)", populationSize,
                cfg.popSize, extPopSize));
        initialPopulation = new ArrayList<double[]>(populationSize);
        long unfeasibleInstances = 0;
        for (int i = 0; i < populationSize; ++i) {
            double[] point = new double[vars.size()];
            initialPopulation.add(point);
            for (int j = 0; j < vars.size(); ++j) {
                SymbolDeclaration sd = vars.get(j);
                double random = Math.random();
                double val = sd.getBindValue();
                double ub = interpreter.evaluateExpression(sd.getUpperBound());
                double lb = interpreter.evaluateExpression(sd.getLowerBound());
                double delta = cfg.popDeltaCoef * (ub - lb);
                for (int k = 0; k < 3; ++k) {
                    point[j] = Math.rint(val + delta * (2 * random - 1));
                    if (point[j] < lb) {
                        point[j] = lb + lb - point[j];
                    }
                    if (point[j] > ub) {
                        point[j] = ub - (point[j] - ub);
                    }
                    if (point[j] < lb || point[j] > ub) {
                        logger.warn(String.format(
                                "Can not fit initial random component lb/ub with val %f in %f +/- %f. Leaving as is.",
                                point[j], val, delta));
                        point[j] = val;
                    }
                    if (!evaluateConstraintsWithRestore(sd, val, point[j])) {
                        point[j] = val;
                        continue;
                    }
                }
                if (point[j] == val) {
                    unfeasibleInstances++;
                    if (unfeasibleInstances == Long.MAX_VALUE) {
                        logger.debug(unfeasibleInstances + " unfeasible candidates generated for instance " + (i + 1)
                                + "and counting ...");
                        unfeasibleInstances = 0;
                    }
                }
            }
            logger.debug(unfeasibleInstances + " unfeasible candidates generated for instance " + (i + 1));
        }
    }

    public List<double[]> execute() {
        logger.debug("executing step 1");
        List<String> options = new ArrayList<String>();
        options.add("Minimize constraint violation");
        options.add("Enter point manually");

        boolean chebyshevCenterComputed = true;
        try {
            gotoChebyshevCenter();
            options.add("Start from Chebyshev center: " + Arrays.toString(chebyshevCenter));
        } catch (Exception e) {
            logger.warn(String.format("Can not compute Chebyshev center: %s", e.getMessage()));
            chebyshevCenterComputed = false;
        }

        Integer choice = input.getChoice(options, 1, "Select method for initial point initialization");
        switch (choice) {
            case 1:
                generateRandomInitialX();
                Util.dumpVars(logger, vars, "generated initial random X");
                Double S = minimizeS();
                logger.debug("S minimized to " + S);
                if (S > 0) {
                    logger.error("S > 0 - can not find feasible initial point");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Constraints' right and left parts after minimization of constraints violations");
                    for (ConstraintDeclaration cd : model.getConstraints()) {
                        logger.debug(cd.getName() + ": " + interpreter.evaluateExpression(cd.getaExpr()) + " "
                                + cd.getRelop() + " " + interpreter.evaluateExpression(cd.getbExpr()));
                    }
                }
                break;
            case 2:
                StringBuilder sb = new StringBuilder();
                for (SymbolDeclaration sd : vars) {
                    sb.append(sd.getName()).append(" ");
                }
                if (sb.length() > 2) {
                    sb.delete(sb.length() - 1, sb.length());
                }
                String vectorString = input.getString(null, "Enter values delimited with single space for variables " + sb.toString());
                String[] elementsString = vectorString.split("\\s+");
                for (int i = 0; i < elementsString.length; i++) {
                    vars.get(i).setBindValue(Double.valueOf(elementsString[i]));
                }
                Util.dumpVars(logger, vars, "Initial variables");
                break;
            case 3:
                //We are already at chebyshev center
                break;
            default:
                throw new FiemException("Unknown option " + choice);
        }
        Util.checkConstraints((double[]) null, model, interpreter, logger);
        if (chebyshevCenterComputed == false) {
            chebyshevCenter = Util.getPointFromVars(model.getVarRefs());
            Util.dumpPoint(logger, chebyshevCenter, "Chebyshev center forced to point");
        }
        generateInitialPopulation();
        Util.dumpPopulation(logger, initialPopulation, "initial population");
        return initialPopulation;
    }

    boolean evaluateConstraintsWithRestore(SymbolDeclaration sd, Double origValue, Double newValue) {
        List<ConstraintDeclaration> constraints = model.getConstraints();
        sd.setBindValue(newValue);
        for (int i = 0; i < constraints.size(); ++i) {
            if (!interpreter.evaluateConstraint(i)) {
                logger.trace(String.format("Evaluation of constraint %s failed for %s", model.getConstraints().get(i)
                        .getName(), sd));
                sd.setBindValue(origValue);
                return false;
            }
        }
        return true;
    }

    boolean evaluateConstraints() {
        List<ConstraintDeclaration> constraints = model.getConstraints();
        for (int i = 0; i < constraints.size(); ++i) {
            if (!interpreter.evaluateConstraint(i)) {
                return false;
            }
        }
        return true;
    }

    public double[] getChebyshevCenter() {
        return chebyshevCenter;
    }
}
