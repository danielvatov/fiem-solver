package bg.bas.iit.weboptim.solver.fiem.util;

import java.util.ArrayList;
import java.util.List;


import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;
import org.apache.log4j.Logger;

public class ChebyshevCenterComputation {

    private final OptimModelInterpreter interpreter;
    private final OptimModel model;
    private final ConstraintGoldenSplit goldenSplit;
    
    public ChebyshevCenterComputation(OptimModel model, OptimModelInterpreter interpreter, Logger logger) {
        this.model = model;
        this.interpreter = interpreter;
        goldenSplit = new ConstraintGoldenSplit(model, interpreter, logger);
    }

    public double[] compute() {
        Line diag = computeDiag();
        List<double[]> intersections = new ArrayList<double[]>(model.getConstraints().size());
        for (int i = 0; i < model.getConstraints().size(); ++i) {
            intersections.add(computeConstaintIntersection(diag, i));
        }
        //sort intersections by coordinates
        //evaluate feasibility for left and right 
        //when found feasible left and right divide by two and this is Chebyshev center
        //TODO
        return null;
    }

    private double[] computeConstaintIntersection(Line diag, int constraintIdx) {
        return goldenSplit.spit(diag, constraintIdx);
    }

    private Line computeDiag() {
        return null;
    }
}