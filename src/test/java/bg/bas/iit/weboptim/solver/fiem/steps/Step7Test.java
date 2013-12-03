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
import bg.bas.iit.weboptim.solver.fiem.steps.Step7;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step7Test {

    private static Logger logger = Logger.getLogger(Step7Test.class);

    private List<double[]> population1 = new ArrayList<double[]>() {
        {
            add(new double[] { 10, 10 });
            add(new double[] { 10, 5 });
            add(new double[] { 20, 5 });
            add(new double[] { 20, 10 });
        }
    };

    private List<double[]> population2 = new ArrayList<double[]>() {
        {
            add(new double[] { 801, 70 });
            add(new double[] { 150, 5 });
            add(new double[] { 990, 90 });
            add(new double[] { 11, 10 });
        }
    };
    
    private double[] chebyshevCenter = new double[] {250, 25};

    private OptimModel model;
    private OptimModelInterpreter interpreter;
    private Config cfg;

    @Before
    public void setUpBefore() throws Exception {
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
        logger.debug("Test step7 execution for feasible population");
        Step7 step7 = new Step7(model, interpreter, cfg);
        Util.dumpPopulation(logger, population1, "population");
        Util.dumpPoint(logger, chebyshevCenter, "chebyshev center");
        List<double[]> newPop = step7.execute(population1, chebyshevCenter);
        Util.dumpPopulation(logger, newPop, "resulting population");
        assertEquals(4, newPop.size());
        assertEquals(10, newPop.get(0)[0], 0);
        assertEquals(10, newPop.get(0)[1], 0);
        assertEquals(10, newPop.get(1)[0], 0);
        assertEquals(5, newPop.get(1)[1], 0);
        assertEquals(20, newPop.get(2)[0], 0);
        assertEquals(5, newPop.get(2)[1], 0);
        assertEquals(20, newPop.get(3)[0], 0);
        assertEquals(10, newPop.get(3)[1], 0);
        
    }
    
    @Test
    public final void testExecuteUnfeasible() {
        logger.debug("Test step7 execution for unfeasible population");
        Step7 step7 = new Step7(model, interpreter, cfg);
        Util.dumpPopulation(logger, population2, "population");
        Util.dumpPoint(logger, chebyshevCenter, "chebyshev center");
        List<double[]> newPop = step7.execute(population2, chebyshevCenter);
        Util.dumpPopulation(logger, newPop, "resulting population");
        assertEquals(4, newPop.size());
        assertEquals(751, newPop.get(0)[0], 0);
        assertEquals(65, newPop.get(0)[1], 0);
        assertEquals(150, newPop.get(1)[0], 0);
        assertEquals(5, newPop.get(1)[1], 0);
        assertEquals(736, newPop.get(2)[0], 0);
        assertEquals(67, newPop.get(2)[1], 0);
        assertEquals(11, newPop.get(3)[0], 0);
        assertEquals(10, newPop.get(3)[1], 0);

    }
}
