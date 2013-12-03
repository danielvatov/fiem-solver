package bg.bas.iit.weboptim.solver.fiem.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bg.bas.iit.weboptim.solver.fiem.net.FiemTCPServer;

public class FiemExecutionTest {

    private static Thread serverThread;
    private Logger logger = Logger.getLogger(FiemExecutionTest.class);

    @BeforeClass
    public static void init() throws IOException {
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    FiemTCPServer.main(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    @AfterClass
    public static void clean() {
        serverThread.stop();
    }

    //@Test
    public void testNetworkInterface() throws InterruptedException {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(2*1000);

        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        connector.getFilterChain().addLast("logger", new LoggingFilter());

        FiemTestSessionHandler handler = new FiemTestSessionHandler();
        connector.setHandler(handler);
        connector.getSessionConfig().setReadBufferSize(2048);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        IoSession session;

        for (;;) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(
                        "localhost", 9191));
                future.awaitUninterruptibly();
                session = future.getSession();
                session.write("ala\nbala\nMMODEL END");
                break;
            } catch (RuntimeIoException e) {
                System.err.println("Failed to connect");
                e.printStackTrace();
                Thread.sleep(5000);
            }
        }

        // wait until the summation is done
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
    }

}
