package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.UserIO;
import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesComparator;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step8 extends BaseStep {

    private Logger logger;

    private UserIO input;

    public Step8(OptimModel model, OptimModelInterpreter interpreter, UserIO input, Config cfg) {
        super(model, interpreter);
        this.input = input;
        logger  = getLogger(Step8.class, cfg.threadedLogging);
    }

    public boolean execute(List<double[]> population, ObjectivesValuesForPoint refPoint) {
        logger.debug("executing step 8");
        List<ObjectivesValuesForPoint> pop = new ArrayList<ObjectivesValuesForPoint>(population.size());
        for (double[] p : population) {
            pop.add(new ObjectivesValuesForPoint(p, Util.bindAndEvaluateGoals(p, model, interpreter)));
        }
        Collections.sort(pop, new ObjectivesValuesComparator(Util.getGoalVector(refPoint)));
        StringBuilder sb = new StringBuilder();
        for (ObjectivesValuesForPoint o : pop) {
            sb.append(o).append("\n");
        }
        sb.append("Is the desired (best) satisfactory solution among the presented ones? If yes then STOP the calculations, else continue the search procedure.");
        return input.getYesNo(false, sb.toString());
    }
}
