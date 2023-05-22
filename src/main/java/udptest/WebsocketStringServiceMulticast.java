package udptest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.logging.Logger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

// TODO add logger
public class WebsocketStringServiceMulticast extends Service<Void> {
    private static final Logger logger = Logger.getLogger(WebsocketStringServiceMulticast.class.getName());

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
            final CanMsgHeartbeat canMsgHeartbeat = new CanMsgHeartbeat();
            updateMessage("Starting");
            int counter = 0;

//            DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET);
            dc = DatagramChannel.open(StandardProtocolFamily.INET);
            dc.bind(new InetSocketAddress(General.MULTICAST_PORT));

            final WorkaroundMulticastJoin workaroundMulticastJoin = new WorkaroundMulticastJoin(dc);

            workaroundMulticastJoin.join();

            Selector selector = Selector.open();
            dc.configureBlocking(false);
            SelectionKey key = dc.register(selector, SelectionKey.OP_READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(General.BUFFER_SIZE);

            Ticker ticker = new Ticker(1200);
            updateMessage("Running");
//            int transmitCounter = 0;
            while (!isCancelled()) {
                int channels;
                ticker.test();
                if (ticker.isBehind()) {
                    channels = selector.selectNow();
                } else {
                    channels = selector.select(ticker.nextTimeout());
                }
                if (channels == 0) {
                    // timeout
//                    byteBuffer.clear();
//                    canMsgHeartbeat.toBB(byteBuffer);
//                    byteBuffer.flip();
//                    dc.send(byteBuffer, General.FINAL_DESTINATION);
//                    ticker.tick();
//                    System.out.println("TRansmittCounter" + (++transmitCounter));
                } else {
                    for (SelectionKey skey : selector.selectedKeys()) {
                        skey.channel();
                        byteBuffer.clear();
                        SocketAddress isa = dc.receive(byteBuffer);
                        byteBuffer.flip();
                        {
                            int size = byteBuffer.limit() - byteBuffer.position();
                            if(size != 16)
                            {
                                logger.info("Receive Size " + size);
                            }
                        }
//                        canMsgHeartbeat.fromBB(byteBuffer);
                        {
                            final CanMsg canMsg = new CanMsg();
                            canMsg.fromBB(byteBuffer);
//                            sourceQueue.add(canMsg);
                            nonFXThreadEventReciever.xonNewText(canMsg);
                        }
                        updateMessage("Got data from: " + isa + "  Count: " + counter++);
                        selector.selectedKeys().remove(skey); // nercessary ????
                    }
                }
            }
            selector.close();
            key.cancel();

            workaroundMulticastJoin.dropAll();
            dc.close();

            updateMessage("Bye!");
            return null;
        }
    }

//    public LinkedBlockingQueue<CanMsg> getSourceQueue() {
//        return sourceQueue;
//    }
//
//    public LinkedBlockingQueue<CanMsg> getSinkQueue() {
//        return sinkQueue;
//    }

//    private final LinkedBlockingQueue<CanMsg> sourceQueue = new LinkedBlockingQueue<>(General.QUEUE_DEPTH);
//    private final LinkedBlockingQueue<CanMsg> sinkQueue = new LinkedBlockingQueue<>(General.QUEUE_DEPTH);
    private final NonFXThreadEventReciever nonFXThreadEventReciever;

    public WebsocketStringServiceMulticast(final NonFXThreadEventReciever nonFXThreadEventReciever) {
        this.nonFXThreadEventReciever = nonFXThreadEventReciever;
    }

    @Override
    protected Task<Void> createTask() {

        return new WebsocketTask();
    }

    // TODO best Way to transmitt
    public void sendMsg(CanMsg canMsg) {
        // TODO Auto-generated method stub
        ByteBuffer bb = ByteBuffer.allocate(16);
        canMsg.toBB(bb);
        bb.flip();
        try {
            // TODO chech dc valid
            dc.send(bb, General.FINAL_DESTINATION);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }
}
