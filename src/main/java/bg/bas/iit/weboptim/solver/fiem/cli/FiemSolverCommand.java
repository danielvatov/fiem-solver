package bg.bas.iit.weboptim.solver.fiem.cli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.vatov.ampl.solver.io.UserIO;

import bg.bas.iit.weboptim.solver.fiem.FiemException;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = FiemSolverCommand.NAME, commandDescription = "Solve optimization problem")
public class FiemSolverCommand implements BaseCommand {

    public final static String NAME = "solve";

    @Parameter(names = "--modelFile", description = "The file path of the ampl optimization problem", required = true)
    public String modelFile;

    public String getName() {
        return NAME;
    }

    public void execute() {
        FiemSolver fiemSolver = new FiemSolver();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(modelFile);
        } catch (FileNotFoundException e) {
            throw new FiemException(e);
        }
        fiemSolver.solve(inputStream, UserIO.Factory.createStdUserIO());
    }

}
