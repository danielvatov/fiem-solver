package bg.bas.iit.weboptim.solver.fiem.net;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class FiemTestSessionHandler extends IoHandlerAdapter {

    private Logger logger = Logger.getLogger(FiemTestSessionHandler.class);

    public void messageReceived(IoSession session, Object message)
            throws Exception {
        logger.info(message);
        if ("MESSAGE RECEIVED".equals(message)) {
            session.write("opa");
        }
    }
}
