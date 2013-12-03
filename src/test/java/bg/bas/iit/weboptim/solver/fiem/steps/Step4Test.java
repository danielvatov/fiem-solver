package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;

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
import bg.bas.iit.weboptim.solver.fiem.steps.Step3;
import bg.bas.iit.weboptim.solver.fiem.steps.Step4;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step4Test {

    private static Logger logger = Logger.getLogger(Step4Test.class);

    private OptimModel model;
    private OptimModelInterpreter interpreter;
    private Config cfg;
    private List<ObjectivesValuesForPoint> candidates;

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
        Step3 step3 = new Step3(model, interpreter, cfg);
        List<double[]> population = new ArrayList<double[]>() {
            {
                add(new double[] { 10, 10 });
                add(new double[] { 10, 5 });
                add(new double[] { 20, 5 });
                add(new double[] { 20, 10 });
                add(new double[] { 10, 70 });
                add(new double[] { 10, 80 });
                add(new double[] { 20, 70 });
                add(new double[] { 800, 70 });
                add(new double[] { 800, 80 });
                add(new double[] { 780, 80 });
            }
        };
        candidates = step3.execute(population);
    }

    @Test
    public final void testExecute() {
        logger.debug("Testing execution of step4");
        UserIO input = new UserIO() {
            private int nosBeforeYes = 1;

            public Integer getChoice(List<String> options, Integer defaultOption, String question) {
                if (question.equals("Choose best candidate")) {
                    int choice = 2;
                    logger.debug("For '" + question + "' chosing " + options.get(choice - 1));
                    logger.debug("Options are " + options);
                    return choice;
                } else {
                    int choice = 1;
                    logger.debug("For '" + question + "' chosing " + options.get(choice - 1));
                    logger.debug("Options are " + options);
                    return choice;
                }
            }

            public Integer getInt(Integer defaultOption, String question) {
                int probeStep = 10;
                logger.debug("Proble step is " + probeStep);
                return probeStep;
            }

            public Boolean getYesNo(Boolean defaultValue, String question) {
                if (0 == nosBeforeYes) {
                    return true;
                }
                nosBeforeYes--;
                return false;
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
        Step4 step4 = new Step4(model, interpreter, input, cfg);
        Util.dumpObjectivesValuesForPoint(logger, candidates, "Input for step4");
        ObjectivesValuesForPoint point = step4.execute(candidates);
        Util.dumpObjectivesValuesForPoint(logger, point, "Good enough point");
        assertEquals(0.0909, point.getGoals().get(0).getB(), 0.0001);
        assertEquals(-0.00084, point.getGoals().get(1).getB(), 0.00001);
        //TODO
        //assertEquals(0.00078, point.getGoals().get(1).getB(), 0.00001);
        assertEquals(10, point.getPoint()[0],0);
        assertEquals(-1190, point.getPoint()[1],0);
        //TODO
        //assertEquals(1270, point.getPoint()[1],0);
    }

}
