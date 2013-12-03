package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.steps.Step2;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step2Test {

    private static Logger logger = Logger.getLogger(Step2Test.class);
    
    private static Step2 step2;
    private static List<double[]> ptot;

    @SuppressWarnings("serial")
    @BeforeClass
    public static void setUpClass() {
        Config cfg = new Config();
        cfg.popSize = 10;
        cfg.popSizeExtCoef = 0.2;
        step2 = new Step2(null, null, cfg);
        ptot = new ArrayList<double[]>() {
            {
                for (int x = 0; x < 4; ++x) {
                    for (int y = 0; y < 3; ++y) {
                        add(new double[] { x, y });
                    }
                }
            }
        };
        Util.dumpPopulation(logger, ptot, "Population used for step2 tests");
    }

    @Test
    public final void testFindUndominated() throws Exception {
        logger.debug("Find undominated members of the population");
        Whitebox.invokeMethod(step2, "findUndominated", new Object[] { ptot });
        List<double[]> p = Whitebox.getInternalState(step2, "p");
        List<double[]> pe = Whitebox.getInternalState(step2, "pe");
        assertEquals(10, p.size());
        Util.dumpPopulation(logger, p, "Internal population");
        assertEquals(2, pe.size());
        Util.dumpPopulation(logger, pe, "External population");
        assertEquals(3, Double.valueOf(pe.get(1)[0]).intValue());
        assertEquals(2, Double.valueOf(pe.get(1)[1]).intValue());
        assertEquals(2, Double.valueOf(pe.get(0)[0]).intValue());
        assertEquals(2, Double.valueOf(pe.get(0)[1]).intValue());
        
    }
    
    @Test
    public final void testComputeWeightCenters() throws Exception {
        logger.debug("Test compute weight centers");
        Whitebox.invokeMethod(step2, "computeWeightCenters", new Object[] {});
        double[] ci = Whitebox.getInternalState(step2, "ci");
        Util.dumpPoint(logger, ci, "Ci weight center");
        double[] ce = Whitebox.getInternalState(step2, "ce");
        Util.dumpPoint(logger, ce, "Ce weight center");
        assertEquals(1.3, ci[0], 0.1);
        assertEquals(0.8, ci[1], 0.1);
        assertEquals(2.5, ce[0], 0.1);
        assertEquals(2.0, ce[1], 0.1);
    }

    @Test
    public final void testComputeMoveVector() throws Exception {
        logger.debug("Test computation of translation vector");
        Whitebox.invokeMethod(step2, "computeMoveVector", new Object[] {});
        double[] t = Whitebox.getInternalState(step2, "t");
        Util.dumpPoint(logger, t, "Translation vector is");
        assertEquals(1.0, t[0], 0.1);
        assertEquals(1.0, t[1], 0.1);
    }
    
    @Test
    public final void testFindUndominated1() throws Exception {
        logger.debug("Find undominated members of the population");
        List<double[]> ptotLocal = new ArrayList<double[]>() {
            {
                add(new double[] {507,53});
                add(new double[] {510,52});
                add(new double[] {534,53});
                add(new double[] {563,48});
                add(new double[] {581,44});
                add(new double[] {621,48});
                add(new double[] {598,44});
                add(new double[] {627,49});
                add(new double[] {610,44});
                add(new double[] {600,42});
                add(new double[] {631,42});
                add(new double[] {618,43});
            }
        };
        Whitebox.invokeMethod(step2, "findUndominated", new Object[] { ptotLocal });
        List<double[]> p = Whitebox.getInternalState(step2, "p");
        List<double[]> pe = Whitebox.getInternalState(step2, "pe");
        assertEquals(10, p.size());
        Util.dumpPopulation(logger, p, "Internal population");
        assertEquals(2, pe.size());
        Util.dumpPopulation(logger, pe, "External population");
        assertEquals(627, Double.valueOf(pe.get(1)[0]).intValue());
        assertEquals(49, Double.valueOf(pe.get(1)[1]).intValue());
        assertEquals(621, Double.valueOf(pe.get(0)[0]).intValue());
        assertEquals(48, Double.valueOf(pe.get(0)[1]).intValue());
        
    }

}
