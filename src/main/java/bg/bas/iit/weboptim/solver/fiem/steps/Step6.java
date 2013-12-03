package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.Line;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step6 extends BaseStep {

    private Logger logger;

    public Step6(Config cfg) {
        super(null, null);
        logger = getLogger(Step6.class, cfg.threadedLogging);
    }

    public List<double[]> execute(double[] point, List<double[]> population) {
        logger.debug("executing step 6");
        List<double[]> ret = new ArrayList<double[]>(population.size());
        double[] center = Util.computeWeightCenter(population);
        Util.dumpPoint(logger, center, "computed wight center of the population");
        double[] translationVector = new Line(center, point).getVector();
        Util.dumpPoint(logger, translationVector, "computed translation vector");
        for (double[] p : population) {
            ret.add(Util.round(Util.translate(p, 1, translationVector)));
        }
        Util.dumpPopulation(logger, population, "translated population");
        return ret;
    }
}
