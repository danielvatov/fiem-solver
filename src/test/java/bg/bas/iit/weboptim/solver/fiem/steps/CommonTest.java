package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class CommonTest {

    private static Logger logger = Logger.getLogger(CommonTest.class);

    @Test
    public final void testEuclidDistance() {
        logger.debug("Test euclid distance computation");
        double[] p1 = new double[] { 334, 40 };
        double[] p2 = new double[] { 339, 31 };
        Util.dumpPoint(logger, p1, "p1");
        Util.dumpPoint(logger, p2, "p2");
        double euclidDistance = Util.euclidDistance(p1, p2);
        logger.debug("computed distance is " + euclidDistance);
        assertEquals(10.295, euclidDistance, 0.001);
    }

    @Test
    public final void testComputeWeightCenter() throws Exception {
        logger.debug("Test computation of weight center of a population");
        List<double[]> population = new ArrayList<double[]>() {
            {
                add(new double[] { 10, 10 });
                add(new double[] { 10, 5 });
                add(new double[] { 20, 5 });
                add(new double[] { 20, 10 });
            }
        };
        Util.dumpPopulation(logger, population, "population");
        double[] center = Util.computeWeightCenter(population);
        Util.dumpPoint(logger, center, "weight center");
        assertEquals(2, center.length);
        assertEquals(15, center[0], 0);
        assertEquals(7.5, center[1], 0.1);
    }
}
