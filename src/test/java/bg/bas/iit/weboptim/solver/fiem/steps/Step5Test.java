package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.steps.Step5;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step5Test {

    private static Logger logger = Logger.getLogger(Step5Test.class);

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
        logger.debug("Testing step5 executions");
        Step5 step5 = new Step5(model, interpreter, cfg);
        double[] chebyshevCenter = new double[] {250, 25};
        Util.dumpPoint(logger, chebyshevCenter, "Chebishev center");
        double[] bestPoint = new double[] {1000, 100};
        Util.dumpPoint(logger, bestPoint, "Best point");
        ObjectivesValuesForPoint p = new ObjectivesValuesForPoint(bestPoint, null);
        ObjectivesValuesForPoint point = step5.execute(p,chebyshevCenter);
        Util.dumpObjectivesValuesForPoint(logger, point, "Step5 execution result");
        assertEquals(0.0014, point.getGoals().get(0).getB(), 0.0001);
        assertEquals(0.0140, point.getGoals().get(1).getB(), 0.0001);
        assertEquals(707, point.getPoint()[0],0);
        assertEquals(70, point.getPoint()[1],0);
    }

}
