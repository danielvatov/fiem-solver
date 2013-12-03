package bg.bas.iit.weboptim.solver.fiem.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import net.vatov.ampl.solver.io.UserIO;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.mina.core.session.IoSession;

import bg.bas.iit.weboptim.solver.fiem.FiemSolver;

public class FiemExecution {

    private static final String MODEL_END = "MODEL END";
    private final IoSession session;
    private Thread solverThread;
    private Thread stdoutThread;
    private final PipedOutputStream in = new PipedOutputStream();
    private final PipedInputStream out = new PipedInputStream();
    private final PrintStream pipe_out = new PrintStream(new PipedOutputStream(out));
    private UserIO userIO = UserIO.Factory
            .createStreamUserIO(new PipedInputStream(in),
                    pipe_out);
    private StringBuilder modelBuffer = new StringBuilder(4 * 1024);
    private boolean waitModel = true;
    
    FiemExecution(IoSession session) throws IOException {
        this.session = session;
    }

    synchronized void recv(Object msg) throws IOException {
        if (waitModel) {
            modelBuffer.append(msg);
            if (modelReceived()) {
                waitModel = false;
                final String threadName = "fiem_solver_" + session.getId();
                solverThread = new Thread(new Runnable() {
                    public void run() {
                        FiemSolver solver = new FiemSolver();
                        solver.setThreadedLogging(true);
                        try {
                            session.write("MODEL RECEIVED\n");
                            PatternLayout layout = new PatternLayout("%-5p %c{2} %x - %m%n");
                            WriterAppender wa = new WriterAppender(layout, pipe_out);
                            wa.setName(threadName);
                            Logger logger = Logger.getLogger(threadName + ".bg.bas.iit.weboptim.solver");
                            logger.addAppender(wa);
                            logger.setAdditivity(false);

                            solver.solve(new ByteArrayInputStream(modelBuffer
                                    .toString().getBytes()), userIO);
                            
                            logger.removeAppender(wa);

                            FiemExecution.this.solverFinished();                               
                        } catch (IOException e) {
                            e.printStackTrace();
                            session.write(e.getMessage());
                        }

                    }
                });
                solverThread.setName(threadName);
                solverThread.start();
                stdoutThread = new Thread(new Runnable() {
                    public void run() {
                        byte[] b = new byte[100];
                        try {
                            while(out.read(b) != -1){
                                session.write(new String(b));
                                Arrays.fill(b, (byte)0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            session.write(e.getMessage());
                        }
                    }
                });
                stdoutThread.setName("userio " + session.getId());
                stdoutThread.start();
            }
        } else {
            in.write((msg).toString().getBytes());
        }
    }

    protected void solverFinished() throws IOException {
/*        in.close();
        out.close();
*/
        session.close(false);
        userIO.close();
        stop();
    }

    private boolean modelReceived() throws IOException {
        int pos = modelBuffer.indexOf(MODEL_END);
        if (-1 == pos) {
            if (modelBuffer.length() > 1024*512) {
                session.write("Model too big");
                solverFinished();
                throw new RuntimeException("Model too big " + modelBuffer);
            }
        } else {
            modelBuffer.delete(pos, modelBuffer.length());
            return true;
        }
        return false;
    }

    void stop() {
        if (null != solverThread) {
            solverThread.stop();
        }
        if (null != stdoutThread) {
            stdoutThread.stop();
        }
    }

}
