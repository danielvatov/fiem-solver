package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.vatov.ampl.model.ObjectiveDeclaration;
import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;
import net.vatov.ampl.solver.io.InvalidUserInputException;
import net.vatov.ampl.solver.io.UserIO;
import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.Line;
import bg.bas.iit.weboptim.solver.fiem.util.ObjectivesValuesForPoint;
import bg.bas.iit.weboptim.solver.fiem.util.Tuple;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step4 extends BaseStep {

    private Logger logger;

    private UserIO input;

    public Step4(OptimModel model, OptimModelInterpreter interpreter, UserIO input, Config cfg) {
        super(model, interpreter);
        this.input = input;
        logger = getLogger(Step4.class, cfg.threadedLogging);
    }

    public ObjectivesValuesForPoint execute(List<ObjectivesValuesForPoint> candidates) {
        logger.debug("executing step 4");
        List<String> options = buildOptions(candidates);
        Integer choice = getCorrectChoice(options, "Choose the most preferred cluster, please!");
        ObjectivesValuesForPoint best = candidates.get(choice - 1);
        Util.dumpObjectivesValuesForPoint(logger, best, "user selected most preferred cluster");
        candidates.remove(best);
        options = buildOptions(candidates);
        choice = getCorrectChoice(options, "Chose the least preferred cluster, please!");
        ObjectivesValuesForPoint worst = candidates.get(choice - 1);
        Util.dumpObjectivesValuesForPoint(logger, worst, "user selected least preferred cluster");
        Integer probeStep = getCorrectInt(1000, "Enter moving step size as an integer number");
        logger.debug("Moving step size is " + probeStep);
        double[] translationVector = new Line(worst.getPoint(), best.getPoint()).getVector();
        Util.dumpPoint(logger, translationVector, "computed translation vector");
        ObjectivesValuesForPoint newPoint = best;
        Boolean userInput;
        do {
            double[] t = Util.translate(newPoint.getPoint(), probeStep, translationVector);
            List<Tuple<ObjectiveDeclaration, Double>> goals = Util.bindAndEvaluateGoals(t, model, interpreter);
            newPoint = new ObjectivesValuesForPoint(t, goals);
            Util.dumpObjectivesValuesForPoint(logger, newPoint, "new reference point computed");
            userInput = input.getYesNo(true, "Do you accept the current point as reference point " + newPoint);
        } while (!userInput);
        Util.dumpObjectivesValuesForPoint(logger, newPoint, "reference point set");
        return newPoint;
    }

    private Integer getCorrectChoice(List<String> options, String question) {
        while (true) {
            try {
                return input.getChoice(options, null, question);
            } catch (InvalidUserInputException e) {
                input.message(e.getMessage());
            }
        }
    }
    
    private Integer getCorrectInt(Integer defaultValue, String question) {
        while (true) {
            try {
                return input.getInt(defaultValue, question);
            } catch (InvalidUserInputException e) {
                input.message(e.getMessage());
            }
        }
    }

    private List<String> buildOptions(List<ObjectivesValuesForPoint> candidates) {
        List<String> options = new ArrayList<String>(candidates.size());
        for (ObjectivesValuesForPoint o : candidates) {
            options.add(o.toString());
        }
        return options;
    }
}
