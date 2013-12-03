package bg.bas.iit.weboptim.solver.fiem.cli;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class FiemSolverCli {

    public static void main(String[] args) throws Exception {

        FiemSolverCli cli = new FiemSolverCli();
        JCommander jc = new JCommander(cli);
        Map<String, BaseCommand> commands = new HashMap<String, BaseCommand>();
        commands.put(FiemSolverCommand.NAME, new FiemSolverCommand());
        for (BaseCommand c : commands.values()) {
            jc.addCommand(c);
        }

        try {
            jc.setProgramName(FiemSolverCli.class.getName());
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            if (null == jc.getParsedCommand()) {
                jc.usage();
            } else {
                jc.usage(jc.getParsedCommand());
            }
            return;
        }

        if (commands.containsKey(jc.getParsedCommand())) {
            commands.get(jc.getParsedCommand()).execute();
        } else {
            jc.usage();
        }
    }
}
