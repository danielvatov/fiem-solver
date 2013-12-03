package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.UserIO;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.steps.Step8;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step8Test {

    private static Logger logger = Logger.getLogger(Step8Test.class);

    private List<double[]> population = new ArrayList<double[]>() {
        {
            add(new double[] { 10, 10 });
            add(new double[] { 10, 5 });
            add(new double[] { 20, 5 });
            add(new double[] { 20, 10 });
        }
    };
    
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
        UserIO input = new UserIO() {

            public Integer getChoice(List<String> options, Integer defaultOption, String question) {
                return null;
            }

            public Integer getInt(Integer defaultOption, String question) {
                return null;
            }

            public Boolean getYesNo(Boolean defaultValue, String question) {
                System.out.println(question);
                return true;
            }

            public void pause(String msg) {
                throw new RuntimeException("Not implemented");
            }

            public void refreshData(Object data) {
                throw new RuntimeException("Not implemented");
            }
            
            public void message(String msg){
                throw new RuntimeException("Not implemented");                
            }

            public void close() {
                throw new RuntimeException("Not implemented");
            }

            public String getString(String defaultValue, String question) {
                throw new RuntimeException("Not implemented");
            }
        };
        Step8 step8 = new Step8(model, interpreter, input, cfg);
        double[] p = new double[] {0,0};
        ObjectivesValuesForPoint refPoint = new ObjectivesValuesForPoint(p, Util.bindAndEvaluateGoals(p, model, interpreter)); 
        assertTrue(step8.execute(population, refPoint));
    }
}
