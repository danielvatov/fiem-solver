package bg.bas.iit.weboptim.solver.fiem;

public class FiemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FiemException(String message) {
        super(message);
    }
    
    public FiemException(Throwable e) {
        super(e);
    }
}
