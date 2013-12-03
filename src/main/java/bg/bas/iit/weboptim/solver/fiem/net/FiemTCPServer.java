package bg.bas.iit.weboptim.solver.fiem.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class FiemTCPServer {

    private static final int PORT = 9191;

    public static void main(String[] args) throws IOException {
        IoAcceptor acceptor = new NioSocketAcceptor();

        // acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        ProtocolCodecFactory codecFactory = new ProtocolCodecFactory() {
            
            private ProtocolEncoder encoder = new ProtocolEncoder() {
                
                public void encode(IoSession session, Object message,
                        ProtocolEncoderOutput out) throws Exception {

                    String value = (message == null ? "" : message.toString());
                    IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
                    buf.putString(value, Charset.forName("UTF-8").newEncoder());
                    buf.flip();
                    out.write(buf);
                }
                
                public void dispose(IoSession session) throws Exception {
                }
            };
            
            private ProtocolDecoder decoder = new ProtocolDecoder() {
                
                public void finishDecode(IoSession session, ProtocolDecoderOutput out)
                        throws Exception {
                }
                
                public void dispose(IoSession session) throws Exception {
                }
                
                public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
                        throws Exception {
                    out.write(in.getString(Charset.forName("UTF-8").newDecoder()));
                }
            };
            
            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return encoder; 
            }
            
            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return decoder;
            }
        };
//        TextLineCodecFactory codecFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(codecFactory));

        acceptor.setHandler(new FiemServerHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        try {
            acceptor.bind(new InetSocketAddress(PORT));
        } finally {
            // acceptor.dispose(false);
        }
    }
}
