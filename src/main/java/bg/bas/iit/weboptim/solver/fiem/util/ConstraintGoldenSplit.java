package bg.bas.iit.weboptim.solver.fiem.util;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

public class ConstraintGoldenSplit {

    private OptimModelInterpreter interpreter;
    private OptimModel model;
    private static final double PHI = (Math.sqrt(5) - 1) / 2;

    public ConstraintGoldenSplit(OptimModel model, OptimModelInterpreter interpreter) {
        this.model = model;
        this.interpreter = interpreter;
    }

    public double[] spit(Line line) {
        double[] left = line.getLeft();
        double[] right = line.getRight();
        do {
            double[] middle = goldenSplit(left, right);
            if (Util.constraintsSatisfied(model, interpreter, middle)) {
                left = middle;
            } else {
                right = middle;
            }
        } while (Util.euclidDistance(left, right) > Math.sqrt(line.dim()));
        return Util.round(left, model, interpreter);
    }

    public double[] spit(Line line, int constraintIdx) {
        double[] left = line.getLeft();
        double[] right = line.getRight();
        do {
            double[] middle = goldenSplit(left, right);
            Util.bindVars(model, middle);
            if (interpreter.evaluateConstraint(constraintIdx)) {
                left = middle;
            } else {
                right = middle;
            }
        } while (Util.euclidDistance(left, right) > Math.sqrt(line.dim()));
        return Util.round(left, model, interpreter);
    }

    private double[] goldenSplit(double[] left, double[] right) {
        double[] ret = new double[left.length];
        for (int i = 0; i < left.length; ++i) {
            ret[i] = left[i] + PHI * (right[i] - left[i]);
        }
        return ret;
    }
}
