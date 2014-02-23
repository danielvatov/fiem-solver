package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;
import org.junit.Test;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step6Test {

    private static Logger logger = Logger.getLogger(Step6Test.class);
    
    @SuppressWarnings("serial")
    private List<double[]> population = new ArrayList<double[]>() {
        {
            add(new double[] { 10, 10 });
            add(new double[] { 10, 5 });
            add(new double[] { 20, 5 });
            add(new double[] { 20, 10 });
        }
    };
    
    @Test
    public final void testExecute() {
        OptimModel model = new OptimModel();
        model.addSymbolDeclaration(SymbolDeclaration.createVarDeclaration("x1"));
        model.addSymbolDeclaration(SymbolDeclaration.createVarDeclaration("x2"));
        logger.debug("Test step6 execution");
        Config cfg = new FiemSolver.Config();
        cfg.threadedLogging = false;
        Step6 step6 = new Step6(model, new OptimModelInterpreter(model), cfg);
        double[] p = new double[] {1000, 100};
        Util.dumpPoint(logger, p, "reference point");
        Util.dumpPopulation(logger, population, "population");
        List<double[]> newPop = step6.execute(p, population);
        Util.dumpPopulation(logger, newPop, "Resulting population");
        assertEquals(4, newPop.size());
        assertEquals(995, newPop.get(0)[0], 0);
        assertEquals(102, newPop.get(0)[1], 0);
        assertEquals(995, newPop.get(1)[0], 0);
        assertEquals(97, newPop.get(1)[1], 0);
        assertEquals(1005, newPop.get(2)[0], 0);
        assertEquals(97, newPop.get(2)[1], 0);
        assertEquals(1005, newPop.get(3)[0], 0);
        assertEquals(102, newPop.get(3)[1], 0);
    }
}
