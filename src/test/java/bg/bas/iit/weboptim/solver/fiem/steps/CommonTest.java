package bg.bas.iit.weboptim.solver.fiem.steps;

import bg.bas.iit.weboptim.solver.fiem.util.Util;
import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.ConstraintDeclaration;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.xml.AmplXmlPersister;
import net.vatov.ampl.solver.OptimModelInterpreter;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

    @Test
    public final void testGetConstraintCoefficients() throws Exception {
        InputStream is = getClass().getResourceAsStream("lin_constraints.mod");
        OptimModel model;
        OptimModelInterpreter interpreter;
        try {
            model = new AmplParser().parse(is);
            AmplXmlPersister persister = new AmplXmlPersister();
            persister.write(model, System.out);
            interpreter = new OptimModelInterpreter(model);
        } finally {
            is.close();
        }
        ConstraintDeclaration cd = model.getConstraint("c1");
        double[] contraintCoefficients = Util.getContraintCoefficients(model, interpreter, cd);
        assertArrayEquals(new double[] { 1.0, 10.0, -12.0, 0.0, 1000000.0 }, contraintCoefficients,
                0.1);

        cd = model.getConstraint("c2");
        contraintCoefficients = Util.getContraintCoefficients(model, interpreter, cd);
        assertArrayEquals(new double[] { -6.0, -4.0, 64.0, 0.0, -14.0 }, contraintCoefficients,
                0.1);
    }

    /**
     * x1 + x2 < 10; x1>2; x2>3
     */
    @Test
    public final void testGetContraintCoefficients() throws IOException {
        InputStream is = new ByteArrayInputStream(
                "var x1 >= 2;\nvar x2 >= 3;\nminimize f1: 1/(x1 + x2);\nc1: x1 + x2 <= 10;\n"
                        .getBytes(Charset.forName("UTF-8")));
        OptimModel model;
        OptimModelInterpreter interpreter;
        try {
            model = new AmplParser().parse(is);
            AmplXmlPersister persister = new AmplXmlPersister();
            persister.write(model, System.out);
            interpreter = new OptimModelInterpreter(model);
        } finally {
            is.close();
        }
        ConstraintDeclaration cd = model.getConstraint("c1");
        double[] contraintCoefficients = Util.getContraintCoefficients(model, interpreter, cd);
        assertArrayEquals(new double[] { 1.0, 1.0, 10.0 }, contraintCoefficients,
                0.1);

        cd = model.getConstraint("var_x1_lower");
        contraintCoefficients = Util.getContraintCoefficients(model, interpreter, cd);
        assertArrayEquals(new double[] { -1, 0, -2 }, contraintCoefficients,
                0.1);

        cd = model.getConstraint("var_x2_lower");
        contraintCoefficients = Util.getContraintCoefficients(model, interpreter, cd);
        assertArrayEquals(new double[] { 0, -1, -3 }, contraintCoefficients,
                0.1);
    }

    @Test
    public final void testComplexChebyshevCenter() throws IOException {
        InputStream is = getClass().getResourceAsStream("lin_constraints.mod");
        OptimModel model;
        OptimModelInterpreter interpreter;
        try {
            model = new AmplParser().parse(is);
            AmplXmlPersister persister = new AmplXmlPersister();
            persister.write(model, System.out);
            interpreter = new OptimModelInterpreter(model);
        } finally {
            is.close();
        }
        Map<String, double[]> constraintsCoefficients = Util
                .getConstraintsCoefficients(model, interpreter);

        LinearObjectiveFunction goal = Util.getLinearObjectiveFunction(constraintsCoefficients);
        List<LinearConstraint> constraints = Util.getLinearConstraints(constraintsCoefficients);
        Util.dumpMath3Model(logger, goal, constraints);
        double[] chebyshevCenter = Util.computeChebyshevCenter(goal, constraints);
        assertArrayEquals(new double[] {953.7, 53.7, 46.3, 46.3}, chebyshevCenter, 0.1);
    }

    /**
     * x1 + x2 < 10; x1>2; x2>3
     */
    @Test
    public final void testComputeChebyshevCenter() {
        Map<String, double[]> p = new HashMap<String, double[]>();
        p.put("c", new double[] { 1, 1, 10 });
        p.put("x1", new double[] { -1, 0, -2 });
        p.put("x2", new double[] { 0, -1, -3 });
        LinearObjectiveFunction goal = Util.getLinearObjectiveFunction(p);
        List<LinearConstraint> constraints = Util.getLinearConstraints(p);
        double[] chebyshevCenter = Util.computeChebyshevCenter(goal, constraints);
        Util.dumpMath3Model(logger, goal, constraints);
        assertArrayEquals(new double[] { 3.67, 4.67 }, chebyshevCenter, 0.01);
    }

    @Test
    public final void testComputeUniformNorm() {
        assertEquals(20, Util.computeUniformNorm(new double[] { -10, 3, 0, 0, 15, -20, 1, 19 }), 0.1);
    }
}
