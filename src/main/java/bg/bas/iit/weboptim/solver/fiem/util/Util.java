package bg.bas.iit.weboptim.solver.fiem.util;

import bg.bas.iit.weboptim.solver.fiem.FiemException;
import net.vatov.ampl.model.ConstraintDeclaration;
import net.vatov.ampl.model.Expression;
import net.vatov.ampl.model.ObjectiveDeclaration;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.solver.OptimModelInterpreter;
import org.apache.commons.math3.ml.distance.ChebyshevDistance;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Util {

    private static Double EPS;

    public static void bindVars(OptimModel model, double[] point) {
        List<SymbolDeclaration> varRefs = model.getVarRefs();
        for (int i = 0; i < point.length; ++i) {
            varRefs.get(i).setBindValue(point[i]);
        }
    }

    public static boolean constraintsSatisfied(OptimModel model, OptimModelInterpreter interpreter, double[] point) {
        if (null != point) {
            bindVars(model, point);
        }
        int constraintsNum = model.getConstraints().size();
        for (int i = 0; i < constraintsNum; ++i) {
            if (!interpreter.evaluateConstraint(i)) {
                return false;
            }
        }
        return true;
    }

    public static double clusterDistance(Cluster c1, Cluster c2) {
        double d = 1.0 / (c1.size() * c2.size());
        double sum = 0;
        for (int i = 0; i < c1.size(); ++i) {
            for (int j = 0; j < c2.size(); ++j) {
                sum += euclidDistance(c1.getPoint(i), c2.getPoint(j));
            }
        }
        return d * sum;
    }

    public static double euclidDistance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; ++i) {
            sum += Math.pow(p2[i] - p1[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static String toString(double[] p) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < p.length; ++i) {
            sb.append(p[i]).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }

    public static List<Tuple<ObjectiveDeclaration, Double>> bindAndEvaluateGoals(double[] b, OptimModel model,
                                                                                 OptimModelInterpreter interpreter) {
        bindVars(model, b);
        List<Tuple<ObjectiveDeclaration, Double>> ret = new ArrayList<Tuple<ObjectiveDeclaration, Double>>();
        for (int i = 0; i < model.getObjectives().size(); ++i) {
            Double val = interpreter.evaluateGoal(i);
            ret.add(new Tuple<ObjectiveDeclaration, Double>(model.getObjectives().get(i), val));
        }
        return ret;
    }

    public static double[] translate(double[] point, long step, double[] translationVector) {
        double[] ret = new double[point.length];
        for (int i = 0; i < point.length; ++i) {
            ret[i] = point[i] + step * translationVector[i];
        }
        return ret;
    }

    public static double[] getGoalVector(ObjectivesValuesForPoint point) {
        List<Tuple<ObjectiveDeclaration, Double>> goals = point.getGoals();
        double[] ret = new double[point.getGoals().size()];
        for (int i = 0; i < goals.size(); i++) {
            ret[i] = goals.get(i).getB();
        }
        return ret;
    }

    public static void dumpUnsatisfiedConstraints(Logger logger, final double[] point, OptimModel model, OptimModelInterpreter interpreter) {
        List<ConstraintDeclaration> unsatisfiedConstraints = Util.getUnsatisfiedConstraints(model, interpreter, point);
        if (unsatisfiedConstraints.isEmpty()) {
            return;
        }
        logger.error("Unsatisifed constraints at point:");
        if (null != point) {
            dumpPoint(logger, point, null);
        } else {
            dumpVars(logger, model.getVarRefs(), null);
        }
        for (ConstraintDeclaration cd : unsatisfiedConstraints) {
            logger.error(cd.getName() + ": " + interpreter.evaluateExpression(cd.getaExpr()) + " "
                    + cd.getRelop() + " " + interpreter.evaluateExpression(cd.getbExpr()));
        }
    }


    public static void checkConstraints(List<double[]> pop, OptimModel model, OptimModelInterpreter interpreter, Logger logger) {
        for (double[] p : pop) {
            checkConstraints(p, model, interpreter, logger);
        }
    }

    public static void checkConstraints(double[] p, OptimModel model, OptimModelInterpreter interpreter, Logger logger) {
        if (!Util.constraintsSatisfied(model, interpreter, p)) {
            Util.dumpUnsatisfiedConstraints(logger, p, model, interpreter);
            throw new FiemException("Infeasible point found!");
        }
    }

    public static double[] roundAndCheck(Logger logger, final double[] point, OptimModel model, OptimModelInterpreter interpreter) {
        double[] ret = Arrays.copyOf(point, point.length);
        if (!constraintsSatisfied(model, interpreter, point)) {
            dumpUnsatisfiedConstraints(logger, point, model, interpreter);
            throw new FiemException("Rounding unfeasible value");
        }
        for (int i = 0; i < point.length; ++i) {
            ret[i] = Precision.round(point[i], 0, BigDecimal.ROUND_DOWN);
            if (!constraintsSatisfied(model, interpreter, ret)) {
                ret[i] = Precision.round(point[i], 0, BigDecimal.ROUND_UP);
                if (!constraintsSatisfied(model, interpreter, ret)) {
                    throw new FiemException("Rounding unfeasible value");
                }
            }
        }
        return ret;
    }

    public static double[] round(final double[] point) {
        double[] ret = Arrays.copyOf(point, point.length);
        for (int i = 0; i < point.length; ++i) {
            ret[i] = Precision.round(point[i], 0, BigDecimal.ROUND_DOWN);
        }
        return ret;
    }

    public static List<ConstraintDeclaration> getUnsatisfiedConstraints(OptimModel model,
                                                                        OptimModelInterpreter interpreter, double[] point) {
        if (null != point) {
            bindVars(model, point);
        }
        List<ConstraintDeclaration> ret = new ArrayList<ConstraintDeclaration>();
        int constraintsNum = model.getConstraints().size();
        for (int i = 0; i < constraintsNum; ++i) {
            if (!interpreter.evaluateConstraint(i)) {
                ret.add(model.getConstraints().get(i));
            }
        }
        return ret;
    }

    public static double[] computeWeightCenter(List<double[]> population) {
        int length = population.get(0).length;
        double[] ret = new double[length];
        for (int i = 0; i < length; i++) {
            double sum = 0;
            for (double[] p : population) {
                sum += p[i];
            }
            ret[i] = sum / population.size();
        }
        return ret;
    }

    public static void dumpPopulation(Logger logger, List<double[]> p, String header) {
        if (null != header) {
            logger.debug(header);
        }
        for (double[] e : p) {
            dumpPoint(logger, e, null);
        }
    }

    public static void dumpPoints(Logger logger, Map<String, double[]> points) {
        for (String header : points.keySet()) {
            dumpPoint(logger, points.get(header), header);
        }
    }

    public static void dumpPoint(Logger logger, double[] p, String header) {
        StringBuilder sb = new StringBuilder();
        if (null != header) {
            sb.append(header).append(" ");
        }
        sb.append("[");
        for (int i = 0; i < p.length; ++i) {
            sb.append(p[i]).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        logger.debug(sb.toString());
    }

    public static void dumpObjectivesValuesForPoint(Logger logger, List<ObjectivesValuesForPoint> p, String header) {
        if (null != header) {
            logger.debug(header);
        }
        for (ObjectivesValuesForPoint v : p) {
            dumpObjectivesValuesForPoint(logger, v, null);
        }
    }

    public static void dumpObjectivesValuesForPoint(Logger logger, ObjectivesValuesForPoint p, String header) {
        if (null != header) {
            logger.debug(header + " " + p.toString());
        } else {
            logger.debug(p.toString());
        }
    }

    public static void dumpClusters(Logger logger, List<Cluster> p, String header) {
        if (null != header) {
            logger.debug(header);
        }
        for (Cluster v : p) {
            logger.debug(v.toString());
        }
    }

    public static void dumpVars(Logger logger, List<SymbolDeclaration> vars, String header) {
        if (null != header) {
            logger.debug(header);
        }
        for (SymbolDeclaration sd : vars) {
            logger.debug(sd);
        }
    }

    public static double[] getPointFromVars(List<SymbolDeclaration> vars) {
        double[] ret = new double[vars.size()];
        int i = 0;
        for (SymbolDeclaration sd : vars) {
            ret[i++] = sd.getBindValue();
        }
        return ret;
    }

    public static double[] getContraintCoefficients(OptimModel model, OptimModelInterpreter interpreter, ConstraintDeclaration cd) {
        double[] ret = new double[model.getVarRefs().size() + 1];
        Expression aExpr = cd.getaExpr();
        for (SymbolDeclaration sd : model.getVarRefs()) {
            sd.setBindValue(0d);
        }
        Double constFactor = interpreter.evaluateExpression(aExpr);
        int sign = 0;
        switch (cd.getRelop()) {
            case GE:
                sign = -1;
                break;
            case EQ:
                throw new FiemException("Invalid constraint relation 'EQ' for " + cd.getName());
            case LE:
                sign = 1;
                break;
        }

        Double b = sign * (interpreter.evaluateExpression(cd.getbExpr()) - constFactor);
        int i = 0;
        for (SymbolDeclaration sd : model.getVarRefs()) {
            sd.setBindValue(1d);
            ret[i++] = sign * (interpreter.evaluateExpression(aExpr) - constFactor);
            sd.setBindValue(0d);
        }
        ret[i] = b;
        return ret;
    }

    public static Map<String, double[]> getConstraintsCoefficients(OptimModel model, OptimModelInterpreter interpreter) {
        Map<String, double[]> ret = new HashMap<String, double[]>(model.getConstraints().size());
        for (ConstraintDeclaration cd : model.getConstraints()) {
            ret.put(cd.getName(), getContraintCoefficients(model, interpreter, cd));
        }
        return ret;
    }

    public static double[] computeChebyshevCenter(LinearObjectiveFunction goal, List<LinearConstraint> constraints) {
        PointValuePair pair = new SimplexSolver().optimize(goal, GoalType.MAXIMIZE, new LinearConstraintSet(constraints));
        return pair.getPoint();
    }

    public static List<LinearConstraint> getLinearConstraints(
            Map<String, double[]> constraintCoefficients) {
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        for (double[] c : constraintCoefficients.values()) {
            double value = c[c.length - 1];
            c[c.length - 1] = computeUniformNorm(Arrays.copyOf(c, c.length - 1));
            constraints.add(new LinearConstraint(c, Relationship.LEQ, value));
        }

        double[] rConstraint = new double[constraintCoefficients.size()];
        Arrays.fill(rConstraint, 0d);
        rConstraint[rConstraint.length - 1] = 1;
        constraints.add(new LinearConstraint(rConstraint, Relationship.GEQ, 0));
        return constraints;
    }

    public static LinearObjectiveFunction getLinearObjectiveFunction(
            Map<String, double[]> constraintCoefficients) {
        int varsNum = constraintCoefficients.values().iterator().next().length;
        double[] goalCoeff = new double[varsNum];
        Arrays.fill(goalCoeff, 0d);
        goalCoeff[varsNum - 1] = 1;
        return new LinearObjectiveFunction(goalCoeff, 0);
    }

    public static double computeUniformNorm(double[] points) {
        ChebyshevDistance d = new ChebyshevDistance();
        return d.compute(new double[points.length], points);
    }

    public static Double epsilon() {
        if (EPS != null) {
            return EPS;
        }
        EPS = 1.0;
        while ((1.0 + 0.5 * EPS) != 1.0) {
            EPS = 0.5 * EPS;
        }
        EPS = Math.sqrt(EPS);
        return EPS;
    }

    public static void dumpMath3Model(Logger logger, LinearObjectiveFunction goal,
            List<LinearConstraint> constraints) {
        logger.debug(String.format("Goal: %s  const %f", goal.getCoefficients(), goal.getConstantTerm()));
        for (LinearConstraint c : constraints) {
            logger.debug(String.format("Constraint: %s %s %f", c.getCoefficients(), c.getRelationship(), c.getValue()));
        }
    }
}