package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.vatov.ampl.model.ConstraintDeclaration;
import net.vatov.ampl.model.ConstraintDeclaration.RelopType;
import net.vatov.ampl.model.Expression;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.model.SymbolDeclaration.SymbolType;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.UserIO;

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
        ArrayList<SymbolDeclaration> symbols = model.getSymbolDeclarations();
        for (SymbolDeclaration sd : symbols) {
            if (sd.getType() == SymbolType.VAR) {
                if (!isBounded(sd)) {
                    throw new FiemException("variable " + sd.getName() + " is unbounded");
                }
                vars.add(sd);
            }
        }
        chebyshevCenter = new double[vars.size()];

    }

    private boolean isBounded(SymbolDeclaration sd) {
        return sd.getLowerBound() != null && sd.getUpperBound() != null;
    }

    private void generateRandomInitialX() {
        for (SymbolDeclaration sd : vars) {
            if (null != sd.getBindValue()) {
                continue;
            }
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
        int i = 0;
        for (SymbolDeclaration sd : vars) {
            Expression lbExpr = sd.getLowerBound();
            Expression ubExpr = sd.getUpperBound();
            Double lb = interpreter.evaluateExpression(lbExpr);
            Double ub = interpreter.evaluateExpression(ubExpr);
            double mid = 0.5 * (ub - lb) + lb;
            if (sd.isInteger()) {
                mid = Math.round(mid);
            }
            sd.setBindValue(Double.valueOf(mid));
            chebyshevCenter[i++] = sd.getBindValue();
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
        if (!evaluateConstraints()) {
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
                    logger.warn("New random value component does not satisfy constraints. Leaving as is.");
                }
            }
        }
    }

    public List<double[]> execute() {
        logger.debug("executing step 1");
        gotoChebyshevCenter();
        List<String> options = new ArrayList<String>();
        options.add("Start from Chebyshev center: " + Arrays.toString(chebyshevCenter));
        options.add("Minimize constraint violation");
        options.add("Enter point manually");
        Integer choice = input.getChoice(options, 1, "Select method for initial point initialization");
        switch (choice) {
        case 1:
            //We are already at chebyshev center
            break;
        case 2:
            generateRandomInitialX();
            Util.dumpVars(logger, vars, "generated initial random X");
            Double S = minimizeS();
            logger.debug("S minimized to " + S);
            if (S > 0) {
                logger.error("S > 0 - can not find feasible initial point");
            }
            break;
        case 3:
            StringBuilder sb = new StringBuilder();
            for (SymbolDeclaration sd : vars) {
                sb.append(sd.getName()).append(" ");
            }
            if (sb.length() > 2) {
                sb.delete(sb.length() - 1, sb.length());
            }
            String vectorString = input.getString(null, "Enter values delimited with single space for variables " + sb.toString());
            String[] elementsString = vectorString.split("\\s+");
            for (int i=0; i<elementsString.length; i++) {
                vars.get(i).setBindValue(Double.valueOf(elementsString[i]));
            }
            Util.dumpVars(logger, vars, "Initial variables");
            break;
        default:
            throw new FiemException("Unknown option " + choice);
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