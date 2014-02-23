package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.ConstraintDeclaration;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
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
                add(new double[] { 507, 53 });
                add(new double[] { 510, 52 });
                add(new double[] { 534, 53 });
                add(new double[] { 563, 48 });
                add(new double[] { 581, 44 });
                add(new double[] { 621, 48 });
                add(new double[] { 598, 44 });
                add(new double[] { 627, 49 });
                add(new double[] { 610, 44 });
                add(new double[] { 600, 42 });
                add(new double[] { 631, 42 });
                add(new double[] { 618, 43 });
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

    @Test
    public final void testMovePoint() throws Exception {
        logger.debug("Test translation of a point");
        InputStream is = getClass().getResourceAsStream("big_problem.mod");
        try {
            OptimModel model = new AmplParser().parse(is);
            OptimModelInterpreter interpreter = new OptimModelInterpreter(model);
            FiemSolver fiemSolver = new FiemSolver();
            Whitebox.invokeMethod(fiemSolver, "initModelConfig", new Object[] { interpreter, model });
            Object cfg = Whitebox.getInternalState(fiemSolver, "cfg");
            Step2 step2 = new Step2(model, interpreter, (Config) cfg);
            double[] expectedPoint = new double[] { 265.0, 12.0, 28.0, 27.0, 209.0, 91.0, 45.0, 66.0, 47.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 65.0, 51.0, 16.0, 1.0, 17.0, 81.0, 160.0, 9.0, 0.0, 9.0, 0.0, 0.0, 0.0, 3.0,
                    10.0, 101.0, 23.0, 121.0, 251.0, 0.0, 0.0, 306.0, 168.0, 0.0, 66.0, 41.0, 0.0, 0.0, 0.0, 34.0,
                    222.0, 143.0, 0.0, 0.0, 0.0, 0.0, 82.0, 90.0, 111.0, 55.0, 90.0, 34.0, 30.0, 9.0, 58.0, 72.0, 44.0,
                    52.0, 36.0, 0.0, 17.0, 21.0, 178.0, 236.0, 10.0, 0.0, 100.0, 110.0, 279.0, 183.0, 0.0, 86.0, 71.0,
                    0.0, 7181.0, 4682.0, 4969.0, 9560.0 };
            double[] t = new double[] { -0.01154507492016701, -0.013387374109555393, 0.0459346597887497,
                    0.009629083763203141, -0.007393760746745251, 0.012650454433800048, -0.01522967329894375,
                    -2.1617013900389472E-4, 9.69448788899923E-4, 0.0, 0.0, 0.0, -4.796608396453538E-4, 0.0, 0.0, 0.0,
                    0.0, -2.8750259123108137E-4, -2.671988986606216E-4, 1.999349148442337E-4, -9.721558016026312E-5,
                    -3.490752816310982E-17, 0.0, 0.0, -0.0035617784328174904, -6.554691309131039E-5,
                    6.764156228433774E-5, 0.0, 0.0, -0.0103168754605748, 0.0043638254086010445, 0.0,
                    0.005748556602919215, 0.0025599384799824794, 0.0, 0.0, 0.0035617784328174622,
                    -0.014738393515106857, 0.0, 6.95501559908465E-4, 0.0, -2.2666145561825958E-4, 0.0, 0.0,
                    -0.012404814541881606, 0.0, -3.490752816310982E-17, 2.61671580270732E-4, -5.421251819290663E-4,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.502326271301012E-4, -1.3237346652167836E-4,
                    0.0, 1.745376408155491E-17, -1.169754342818633E-4, 0.0, 0.0, 0.0, 3.490752816310982E-17, 0.0, 0.0,
                    0.0, 0.0, -1.6368232157294133E-4, -0.013264554163596185, -6.14099729796119E-4,
                    0.001228199459592238, 0.0, 0.0, 0.0, -0.7119872267256202, -0.2926799312208305, 1.0,
                    -1.0445836403831985 };
            double[] point = new double[] { 267.0, 14.0, 23.0, 26.0, 210.0, 90.0, 47.0, 66.17600572717697,
                    47.421349592155366, 0.25598263289896295, 0.23366389406753285, 0.7561277082944002,
                    0.781079711278494, 0.20539681685374944, 0.9661492123693733, 65.81931543873463,/*!!!*/ 50.48654129377496,
                    16.292605762225435, 1.2719416791118476, 17.053161417619748, 81.27915292536647, 160.5318218879878,
                    9.054734670748417, 0.6026458099061074, 10.0, 0.5336829663894491, 0.8898524799761844,
                    0.009655248182752985, 3.0218687386214924, 12.0, 101.0782889205284, 22.98121193714951,
                    121.03905042780633, 251.02616348266372, 0.11986738056731383, 0.2444477592766816, 306.0, 170.0,
                    0.12692447397967044, 65.37080292213614, 41.15382897642269, 0.30757959527397816, 0.7414638863218954,
                    0.381266584716343, 36.0, 221.33873985388163, 143.88049390224606, 0.5738939986871401,
                    0.0689972052110761, 0.4208458815121503, 0.7417422376252034, 81.78974054860328, 90.44677374863579,
                    111.04566666769722, 55.08045709743817, 90.38412728615754, 34.32829978125187, 30.332261260509654,
                    9.92528177526981, 58.694201487476676, 72.13472309555243, 44.0, 52.204562525341004,
                    36.31747132864098, 0.6664909655461315, 17.358127919542255, 21.453565676948642, 177.51553844616467,
                    236.9622964662098, 10.389751185454728, 0.31306552739579274, 100.57945088064491, 110.14807794024966,
                    281.0, 184.0, 0.0, 86.0, 71.0, 0.0, 7270.0, 4719.0, 4845.0, 9691.0 };
            Whitebox.setInternalState(step2, "t", t);
            Whitebox.invokeMethod(step2, "movePoint", point);
//            Util.dumpPoint(logger, point, "Translated point is");
            Assert.assertArrayEquals(expectedPoint, point, 0);
            List<ConstraintDeclaration> unsatisfiedConstraints = Util.getUnsatisfiedConstraints(model, interpreter, point);
            for (ConstraintDeclaration cd : unsatisfiedConstraints) {
                logger.error(cd);
            }
            Assert.assertTrue(unsatisfiedConstraints.isEmpty());
        } finally {
            is.close();
        }
    }

}
