package bg.bas.iit.weboptim.solver.fiem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.model.SymbolDeclaration;
import net.vatov.ampl.model.SymbolDeclaration.SymbolType;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.Solver;
import net.vatov.ampl.solver.io.UserIO;
import bg.bas.iit.weboptim.solver.fiem.steps.Step1;
import bg.bas.iit.weboptim.solver.fiem.steps.Step2;
import bg.bas.iit.weboptim.solver.fiem.steps.Step3;
import bg.bas.iit.weboptim.solver.fiem.steps.Step4;
import bg.bas.iit.weboptim.solver.fiem.steps.Step5;
import bg.bas.iit.weboptim.solver.fiem.steps.Step6;
import bg.bas.iit.weboptim.solver.fiem.steps.Step7;
import bg.bas.iit.weboptim.solver.fiem.steps.Step8;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;

public class FiemSolver extends Solver {

    public static class Config {
        public Integer improveStep = new Integer(1);
        public Integer popSize = new Integer(10);
        public Double popSizeExtCoef = new Double(0.2);
        public Double popDeltaCoef = new Double(0.05);
        public static final String IMPROVE_STEP_PARAM = "improveStep";
        public static final String POP_SIZE_PARAM = "popSize";
        public static final String POP_DELTA_COEF_PARAM = "popDeltaCoef";
        public static final String POP_SIZE_EXT_PARAM = "popSizeExtCoef";
        public Boolean threadedLogging = null;
    }

    public static final String NAME = "EmooSolver";
    public static final String DESCRIPTION =
    		"     Institute of Information and Communication Technologies – Bulgarian Academy of Sciences     \n" +
    		"                                    (IICT – BAS), www.iict.bas.bg                                \n" +
            "                                                                                                 \n" +
            "                          FIEM (Fast Interactive Evolutionary Method)                            \n" +
            "                                                                                                 \n" +
            "                                       Version 1.2 / 2013                                        \n" +
            "                                                                                                 \n" +
            "  ------------------------------------------------------------------------------------------------\n" +
            "  |        The method FIEM is designed to solve multicriteria linear integer and                 |\n" +
            "  |                multicriteria nonlinear integer programming problems.                         |\n" +
            "  |                                                                                              |\n" +
            "  |     Research and development team:  Vassil Guliashki, Leonid Kirilov, Daniel Vatov           |\n" +
            "  |     IICT-BAS, Acad. G. Bonchev str., block 2, 1113 Sofia, Bulgaria, www.iict.bas.bg          |\n" +
            "  |        e-mail: vggul@yahoo.com, lkirilov@iinf.bas.bg, daniel.vatov@gmail.com                 |\n" +
            "  |                                                                                              |\n" +
            "  |         This work is partially supported by the Bulgarian National Science Fund,             |\n" +
            "  |  Grant # DTK02/71 'Web-Based Interactive System, Supporting the Building Models and Solving  |\n" +
            "  |                       Optimization and Decision Making Problems'.                            |\n" +
            "  |                                                                                              |\n" +
            "  ------------------------------------------------------------------------------------------------\n";
    private Config cfg = new Config();
        
    public FiemSolver() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public void setThreadedLogging(Boolean threadedLogging) {
        cfg.threadedLogging = threadedLogging;
    }
    private void initModelConfig(OptimModelInterpreter interpreter, OptimModel model) {
        ArrayList<SymbolDeclaration> symbols = model.getSymbolDeclarations();
        for (SymbolDeclaration sd : symbols) {
            if (sd.getType() == SymbolType.PARAM) {
                if (sd.getName().equals(Config.IMPROVE_STEP_PARAM)) {
                    cfg.improveStep = interpreter.evaluateExpression(sd.getValue()).intValue();
                } else if (sd.getName().equals(Config.POP_SIZE_PARAM)) {
                    cfg.popSize = interpreter.evaluateExpression(sd.getValue()).intValue();
                } else if (sd.getName().equals(Config.POP_DELTA_COEF_PARAM)) {
                    cfg.popDeltaCoef = interpreter.evaluateExpression(sd.getValue());
                } else if (sd.getName().equals(Config.POP_SIZE_EXT_PARAM)) {
                    cfg.popSizeExtCoef = interpreter.evaluateExpression(sd.getValue());
                }
            }
        }

    }

    @Override
    public Map<String, String> solve(InputStream input, UserIO userInput) {
        userInput.message(DESCRIPTION);
        OptimModel model = parse(input);
        OptimModelInterpreter interpreter = new OptimModelInterpreter(model);
        initModelConfig(interpreter, model);

        Step1 step1 = new Step1(model, interpreter, cfg, userInput);
        double[] chebyshevCenter = step1.getChebyshevCenter();
        List<double[]> initialPopulation = step1.execute();

        Step2 step2 = new Step2(model, interpreter, cfg);
        List<double[]> population = step2.execute(initialPopulation);

        boolean loop;
        do {
            Step3 step3 = new Step3(model, interpreter, cfg);
            List<ObjectivesValuesForPoint> clusterCandidates = step3.execute(population);

            Step4 step4 = new Step4(model, interpreter, userInput, cfg);
            ObjectivesValuesForPoint point = step4.execute(clusterCandidates);

            Step5 step5 = new Step5(model, interpreter, cfg);
            point = step5.execute(point, chebyshevCenter);

            Step6 step6 = new Step6(cfg);
            population = step6.execute(point.getPoint(), population);

            Step7 step7 = new Step7(model, interpreter, cfg);
            population = step7.execute(population, chebyshevCenter);

            Step8 step8 = new Step8(model, interpreter, userInput, cfg);
            loop = !step8.execute(population, point);

        } while (loop);
        return null;
    }    
}