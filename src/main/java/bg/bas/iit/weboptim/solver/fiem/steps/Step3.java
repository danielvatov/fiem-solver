package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemException;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.Cluster;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

public class Step3 extends BaseStep {

    private Logger logger;

    public Step3(OptimModel model, OptimModelInterpreter interpreter, Config cfg) {
        super(model, interpreter);
        logger  = getLogger(Step3.class, cfg.threadedLogging);
    }

    public List<ObjectivesValuesForPoint> execute(List<double[]> population) {
        logger.debug("executing step 3");
        int maxClusters = model.getObjectives().size() + 1;
        List<Cluster> clusters = initClusters(population);
        Util.dumpClusters(logger, clusters, "initial clusters");
        while (clusters.size() > maxClusters) {
            mergeClosestClusters(clusters);
        }
        Util.dumpClusters(logger, clusters, "merged clusters");
        List<ObjectivesValuesForPoint> ret = new ArrayList<ObjectivesValuesForPoint>();
        for (Cluster c : clusters) {
            double[] point = c.getPoint(0);
            ret.add(new ObjectivesValuesForPoint(point, Util.bindAndEvaluateGoals(point, model, interpreter)));
        }
        Util.dumpObjectivesValuesForPoint(logger, ret, "selected cluster candidates");
        return ret;
    }

    private void mergeClosestClusters(List<Cluster> clusters) {
        Cluster minC1 = null;
        Cluster minC2 = null;
        double distance = Double.MAX_VALUE;
        if (clusters.size() < 2) {
            throw new FiemException("Not enough clusters");
        }
        for (int i = 0; i < clusters.size() - 1; ++i) {
            Cluster c1 = clusters.get(i);
            for (int j = i + 1; j < clusters.size(); ++j) {
                Cluster c2 = clusters.get(j);
                double d = Util.clusterDistance(c1, c2);
                if (d < distance) {
                    minC1 = c1;
                    minC2 = c2;
                    distance = d;
                }
            }
        }
        minC1.addAllPoints(minC2);
        clusters.remove(minC2);
    }

    private List<Cluster> initClusters(List<double[]> population) {
        List<Cluster> ret = new ArrayList<Cluster>(population.size());
        for (double[] p : population) {
            ret.add(new Cluster(p));
        }
        return ret;
    }
}