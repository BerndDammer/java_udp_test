package udptest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.logging.Logger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

// TODO add logger
public class WebsocketStringServiceBroadcast extends Service<Void> {
    private static final Logger logger = Logger.getLogger(WebsocketStringServiceBroadcast.class.getName());

    static class Ticker {

        private long nextEvent;
        private final long delayMs;
        private long thisTimeout;

        Ticker(int delayMs) {
            this.delayMs = (long) delayMs;
            long now = System.currentTimeMillis();
            nextEvent = delayMs + now;
            thisTimeout = now - nextEvent;
        }

        void test() {
            thisTimeout = nextEvent - System.currentTimeMillis();
        }

        boolean isBehind() {
            return thisTimeout <= 0l;
        }

        long nextTimeout() {
            return thisTimeout;
        }

        void tick() {
            nextEvent += delayMs;
        }
    }

    public interface NonFXThreadEventReciever {
        public void xonNewText(final CanMsg canMsg);
    }

    // TODO better Sync
    DatagramChannel dc = null;

    public class WebsocketTask extends Task<Void> {

        public WebsocketTask() {
        }

        @Override
        protected Void call() throws Exception {
            updateMessage("Starting");
            int counter = 0;

            dc = DatagramChannel.open(StandardProtocolFamily.INET);
            dc.bind(new InetSocketAddress(General.MULTICAST_PORT));

            Selector selector = Selector.open();
            dc.configureBlocking(false);
            SelectionKey key = dc.register(selector, SelectionKey.OP_READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(General.BUFFER_SIZE);

            Ticker ticker = new Ticker(1200);
            updateMessage("Running");
            while (!isCancelled()) {
                int channels;
                ticker.test();
                if (ticker.isBehind()) {
                    channels = selector.selectNow();
                } else {
                    channels = selector.select(ticker.nextTimeout());
                }
                if (channels == 0) {
                    //
                } else {
                    for (SelectionKey skey : selector.selectedKeys()) {
                        skey.channel();
                        byteBuffer.clear();
                        SocketAddress isa = dc.receive(byteBuffer);
                        byteBuffer.flip();
                        {
                            int size = byteBuffer.limit() - byteBuffer.position();
                            if (size != 16) {
                                logger.info("Receive Size " + size);
                            }
                        }
                        {
                            final CanMsg canMsg = new CanMsg();
                            canMsg.fromBB(byteBuffer);
                            nonFXThreadEventReciever.xonNewText(canMsg);
                        }
                        updateMessage("Got data from: " + isa + "  Count: " + counter++);
                        selector.selectedKeys().remove(skey); // nercessary ????
                    }
                }
            }
            selector.close();
            key.cancel();

            dc.close();

            updateMessage("Bye!");
            return null;
        }
    }

    private final NonFXThreadEventReciever nonFXThreadEventReciever;

    InetSocketAddress broadcastDestination;

    public WebsocketStringServiceBroadcast(final NonFXThreadEventReciever nonFXThreadEventReciever) {
        this.nonFXThreadEventReciever = nonFXThreadEventReciever;

        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
            final NetworkInterface mainIF = NetworkInterface.getByInetAddress(localhost);
            final InterfaceAddress if4 = mainIF.getInterfaceAddresses().get(0);
            final InetAddress if4broadcast = if4.getBroadcast();
            broadcastDestination = new InetSocketAddress(if4broadcast, General.FINAL_DESTINATION.getPort());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            broadcastDestination = null;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            broadcastDestination = null;
        }

    }

    @Override
    protected Task<Void> createTask() {

        return new WebsocketTask();
    }

    // TODO best Way to transmitt
    public void sendMsg(CanMsg canMsg) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        canMsg.toBB(bb);
        bb.flip();
        try {
            // TODO check dc valid
            dc.send(bb, broadcastDestination);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }
}
