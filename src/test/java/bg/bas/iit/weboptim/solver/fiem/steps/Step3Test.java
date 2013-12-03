package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.steps.Step3;
import bg.bas.iit.weboptim.solver.fiem.util.Cluster;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step3Test {

    private static Logger logger = Logger.getLogger(Step3Test.class);
    
    private OptimModel model;
    private OptimModelInterpreter interpreter;
    private Config cfg;
    
    @Before
    public  void setUpBefore() throws Exception {
        InputStream is = getClass().getResourceAsStream("base_problem.mod");
        try {
            model = new AmplParser().parse(is);
            interpreter = new OptimModelInterpreter(model);
            FiemSolver fiemSolver = new FiemSolver();
            Whitebox.invokeMethod(fiemSolver, "initModelConfig", new Object[] { interpreter, model });
            cfg = Whitebox.getInternalState(fiemSolver, "cfg");
        } finally {
            is.close();
        }
    }

    @Test
    public final void testExecute() {
        logger.debug("Test execution of step3");
        Step3 step3 = new Step3(model, interpreter, cfg);
        List<double[]> population = new ArrayList<double[]>() {{
            add(new double[] {334, 40});
            add(new double[] {339, 31});
            add(new double[] {362, 33});
            add(new double[] {378, 38});
            add(new double[] {343, 32});
            add(new double[] {365, 30});
            add(new double[] {317, 39});
            add(new double[] {390, 37});
            add(new double[] {355, 35});
            add(new double[] {332, 36});
        }};
        Util.dumpPopulation(logger, population, "population for step3 execution");
        List<ObjectivesValuesForPoint> res = step3.execute(population);
        //TODO verify res
        Util.dumpObjectivesValuesForPoint(logger, res, "Step3 execution result");
    }

    @Test
    public final void testMergeClosestClusters() throws Exception {
        logger.debug("Test merging of closest clusters");
        Step3 step3 = new Step3(model, interpreter, cfg);
        List<double[]> population = new ArrayList<double[]>() {{
            add(new double[] {10, 10});
            add(new double[] {10, 5});
            add(new double[] {20, 5});
            add(new double[] {20, 10});
            add(new double[] {10, 70});
            add(new double[] {10, 80});
            add(new double[] {20, 70});
            add(new double[] {800, 70});
            add(new double[] {800, 80});
            add(new double[] {780, 80});
        }};
        Util.dumpPopulation(logger, population, "population");
        List<Cluster> clusters = Whitebox.invokeMethod(step3, "initClusters", new Object[] { population });
        assertEquals(10, clusters.size());
        Util.dumpClusters(logger, clusters, "Initial clusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(9, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(8, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(7, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(6, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(5, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(4, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        Whitebox.invokeMethod(step3, "mergeClosestClusters", new Object[] { clusters });
        assertEquals(3, clusters.size());
        Util.dumpClusters(logger, clusters, "Clusters after invocation to mergeClosestClusters");
        
        assertEquals(clusters.get(0).getPoint(0), population.get(0));
        assertEquals(clusters.get(0).getPoint(1), population.get(1));
        assertEquals(clusters.get(0).getPoint(2), population.get(2));
        assertEquals(clusters.get(0).getPoint(3), population.get(3));
        assertEquals(clusters.get(1).getPoint(0), population.get(4));
        assertEquals(clusters.get(1).getPoint(1), population.get(5));
        assertEquals(clusters.get(1).getPoint(2), population.get(6));
        assertEquals(clusters.get(2).getPoint(0), population.get(7));
        assertEquals(clusters.get(2).getPoint(1), population.get(8));
        assertEquals(clusters.get(2).getPoint(2), population.get(9));
        
    }
}
