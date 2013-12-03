package bg.bas.iit.weboptim.solver.fiem.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class FiemServerHandler extends IoHandlerAdapter {

    private Map<IoSession, FiemExecution> execs = new HashMap<IoSession, FiemExecution>();
    
    private Logger logger = Logger.getLogger(FiemServerHandler.class);

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        session.write(cause.getClass().getName() + " " + cause.getMessage());
        cause.printStackTrace();
    }

    public void messageReceived(IoSession session, Object msg) throws Exception {
        if (!execs.containsKey(session)) {
            logger.error("Session " + session + "is unknown");
        } else {
            FiemExecution e = execs.get(session);
            e.recv(msg);
        }        
    }

    @Override
    public void sessionCreated(IoSession session) {
        if (!execs.containsKey(session)) {
            FiemExecution e;
            try {
                e = new FiemExecution(session);
                execs.put(session, e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            logger.error("Session " + session + " already exists!");
        }        
    }

    public void sessionClosed(IoSession session) throws Exception {
        if (execs.containsKey(session)) {
            FiemExecution e = execs.remove(session);
            e.stop();
        }
    }

}
