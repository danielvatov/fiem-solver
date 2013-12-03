package bg.bas.iit.weboptim.solver.fiem.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.vatov.ampl.model.ObjectiveDeclaration;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;


public class Util {

    public static void bindVars(OptimModel model, double[] point) {
        List<SymbolDeclaration> varRefs = model.getVarRefs();
        for (int i = 0; i < point.length; ++i) {
            varRefs.get(i).setBindValue(point[i]);
        }
    }

    public static boolean constraintsSatisfied(OptimModel model, OptimModelInterpreter interpreter, double[] point) {
        bindVars(model, point);
        int constraintsNum = model.getConstraints().size();
        for (int i = 0; i < constraintsNum; ++i) {
            if (!interpreter.evaluateConstraint(i)) {
                return false;
            }
        }
        for (SymbolDeclaration sd : model.getVarRefs()) {
            Double lowerBound = interpreter.evaluateExpression(sd.getLowerBound());
            if (sd.getBindValue() < lowerBound) {
                return false;
            }
            Double upperBound = interpreter.evaluateExpression(sd.getUpperBound());
            if (sd.getBindValue() > upperBound) {
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
        List<Tuple<ObjectiveDeclaration,Double>> goals = point.getGoals();
        double[] ret = new double[point.getGoals().size()];
        for (int i=0; i<goals.size(); i++) {
            ret[i] = goals.get(i).getB();
        }
        return ret;
    }
    
    public static double[] round(final double[] point) {
        double[] ret = new double[point.length];
        for (int i = 0; i < point.length; ++i) {
            ret[i] = Precision.round(point[i], 0, BigDecimal.ROUND_DOWN);
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
}
