package bg.bas.iit.weboptim.solver.fiem;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;

public class BaseStep {
    
    protected final OptimModel model;
    protected final OptimModelInterpreter interpreter;
    
    protected BaseStep(OptimModel model, OptimModelInterpreter interpreter) {
        this.model = model;
        this.interpreter = interpreter;
    }
    
    protected Logger getLogger(Class<?> c, Boolean threadedLogging) {
        if (null == threadedLogging || !threadedLogging) {
            return Logger.getLogger(c.getName());
        }
        return Logger.getLogger(Thread.currentThread().getName() + "." + c.getName());
    }
}
