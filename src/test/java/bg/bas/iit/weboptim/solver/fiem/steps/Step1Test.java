package bg.bas.iit.weboptim.solver.fiem.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.vatov.ampl.AmplParser;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.UserIO;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.steps.Step1;

public class Step1Test {

    private static Logger logger = Logger.getLogger(Step1Test.class);

    private OptimModel model;
    private OptimModelInterpreter interpreter;
    private Config cfg;

    @Before
    public void setUp() throws Exception {
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
    public void testGenerateRandomInitialX() throws Exception {
        logger.debug("Test generation of random initial X");
        Step1 step1 = new Step1(model, interpreter, cfg, null);
        Whitebox.invokeMethod(step1, "generateRandomInitialX", new Object[] {});
        List<SymbolDeclaration> vars = Whitebox.getInternalState(step1, "vars");
        assertNotNull("variables not initialized", vars);
        for (SymbolDeclaration sd : vars) {
            assertNotNull(String.format("variable %s has no initial value", sd.getName()));
        }
    }

    private Object createInnerClass(Step1 step1, String innerClassName) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<Object> innerClassType = Whitebox.getInnerClassType(Step1.class, innerClassName);
        Constructor<Object> ctor = innerClassType.getDeclaredConstructor(new Class[] { Step1.class });
        ctor.setAccessible(true);
        Object computation = ctor.newInstance(new Object[] { step1 });
        return computation;
    }

    @Test
    public void testComputeS() throws Exception {
        logger.debug("Testing computation of S");
        Step1 step1 = new Step1(model, interpreter, cfg, null);
        model.getVarRef("x1").setBindValue(1000.0);
        model.getVarRef("x2").setBindValue(100.0);
        Double S = Whitebox.<Double> invokeMethod(createInnerClass(step1, "InitailVectorComputation"), "compute",
                new Object[] {});
        assertNotNull(S);
        assertEquals(Double.valueOf(1000000.0), S);
        logger.debug(String.format("max S value is %f", S));
    }

    @Test
    public void testMinimizeS() throws Exception {
        logger.debug("Test minimization of S");
        Step1 step1 = new Step1(model, interpreter, cfg, null);
        model.getVarRef("x1").setBindValue(1000.0);
        model.getVarRef("x2").setBindValue(100.0);
        Double S = Whitebox.<Double> invokeMethod(step1, "minimizeS", new Object[] {});
        List<SymbolDeclaration> vars = Whitebox.getInternalState(step1, "vars");
        for (SymbolDeclaration sd : vars) {
            logger.debug(String.format("%s : %f", sd.getName(), sd.getBindValue()));
        }
        assertEquals(Double.valueOf(0.0), S);
        logger.debug(String.format("Calculated S value after worst case initialization is %f", S));
    }

    @Test
    public void testGenerateInitialPopulation() throws Exception {
        logger.debug("Test generation of random initial population");
        Step1 step1 = new Step1(model, interpreter, cfg, null);
        model.getVarRef("x1").setBindValue(935.0);
        model.getVarRef("x2").setBindValue(35.0);
        //Whitebox.<Double> invokeMethod(step1, "gotoChebyshevCenter", new Object[] {});
        Whitebox.<Void> invokeMethod(step1, "generateInitialPopulation", new Object[] {});
        List<double[]> initialPopulation = Whitebox.<List<double[]>> getInternalState(step1, "initialPopulation");
        assertEquals(12, initialPopulation.size());
        assertEquals(2, initialPopulation.get(0).length);
        logger.debug("initial population:");
        StringBuilder sb = new StringBuilder();
        for (double[] point : initialPopulation) {
            for (double c : point) {
                assertTrue(c >= 0);
                sb.append(Double.valueOf(c).intValue()).append(",");
            }
            logger.debug(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    @Test
    public void testExecute() {
        logger.debug("Test execution of step1");
        UserIO userInput = new UserIO() {
            
            public void refreshData(Object data) {
            }
            
            public void pause(String msg) {
            }
            
            public void message(String paramString) {
            }
            
            public Boolean getYesNo(Boolean defaultValue, String question) {
                return null;
            }
            
            public Integer getInt(Integer defaultValue, String question) {
                return null;
            }
            
            public Integer getChoice(List<String> options, Integer defaultOption, String question) {
                return 1;
            }
            
            public void close() {
            }

            public String getString(String defaultValue, String question) {
                return null;
            }
        };
        Step1 step1 = new Step1(model, interpreter, cfg, userInput);
        List<double[]> initialPopulation = step1.execute();
        assertEquals(12, initialPopulation.size());
        assertEquals(2, initialPopulation.get(0).length);
        logger.debug("initial population:");
        StringBuilder sb = new StringBuilder();
        for (double[] point : initialPopulation) {
            for (double c : point) {
                assertTrue(c >= 0);
                sb.append(Double.valueOf(c).intValue()).append(",");
            }
            logger.debug(sb.toString());
            sb.delete(0, sb.length());
        }
    }
}
