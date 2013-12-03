package bg.bas.iit.weboptim.solver.fiem.steps;

import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.ConstraintGoldenSplit;
import bg.bas.iit.weboptim.solver.fiem.util.Line;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

public class Step5 extends BaseStep {
    private Logger logger;
            
    public Step5(OptimModel model, OptimModelInterpreter interpreter, Config cfg) {
        super(model, interpreter);
        logger = getLogger(Step5.class, cfg.threadedLogging);
    }
    
    public ObjectivesValuesForPoint execute(ObjectivesValuesForPoint bestPoint, double[] chebyshevCenter) {
        logger.debug("executing step 5");
        double[] point = bestPoint.getPoint();
        if (Util.constraintsSatisfied(model, interpreter, point)) {
            Util.dumpObjectivesValuesForPoint(logger, bestPoint, "reference point is feasible");
            return bestPoint;
        }
        Util.dumpObjectivesValuesForPoint(logger, bestPoint, "reference point is NOT feasible");
        ConstraintGoldenSplit goldenSplit = new ConstraintGoldenSplit(model, interpreter);
        Line translation = new Line(chebyshevCenter, bestPoint.getPoint());
        Util.dumpPoint(logger, translation.getVector(), "computed translation vector for reference point");
        point = goldenSplit.spit(translation);
        ObjectivesValuesForPoint feasiblePoint = new ObjectivesValuesForPoint(point, Util.bindAndEvaluateGoals(point, model, interpreter));
        Util.dumpObjectivesValuesForPoint(logger, feasiblePoint, "reference point after translation");
        return feasiblePoint;
    }
}
