package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.ConstraintGoldenSplit;
import bg.bas.iit.weboptim.solver.fiem.util.Line;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

public class Step7 extends BaseStep {

    private Logger logger;

    public Step7(OptimModel model, OptimModelInterpreter interpreter, Config cfg) {
        super(model, interpreter);
        logger  = getLogger(Step7.class, cfg.threadedLogging);
    }

    public List<double[]> execute(List<double[]> population, double[] chebyshevCenter) {
        logger.debug("executing step 7");
        List<double[]> ret = new ArrayList<double[]>(population.size());
        ConstraintGoldenSplit goldenSplit = new ConstraintGoldenSplit(model, interpreter, logger);
        for (double[] p : population) {
            if (Util.constraintsSatisfied(model, interpreter, p)) {
                ret.add(p);
                continue;
            }
            ret.add(goldenSplit.spit(new Line(chebyshevCenter, p)));
        }
        Util.dumpPopulation(logger, ret, "population translated to feasiblility");
        return ret;
    }
}
