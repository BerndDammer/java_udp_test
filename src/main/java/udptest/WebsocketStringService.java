package udptest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

// TODO add logger
public class WebsocketStringService extends Service<Void> {
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
        public void xonNewText();
    }

    public class WebsocketTask extends Task<Void> {

        public WebsocketTask() {
        }

        @Override
        protected Void call() throws Exception {
            final CanMsgHeartbeat canMsgHeartbeat = new CanMsgHeartbeat();
            updateMessage("Starting");
            int counter = 0;

            DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET);
            dc.bind(new InetSocketAddress(General.MULTICAST_PORT));

            final WorkaroundMulticastJoin workaroundMulticastJoin = new WorkaroundMulticastJoin(dc);

            workaroundMulticastJoin.join();

            Selector selector = Selector.open();
            dc.configureBlocking(false);
            SelectionKey key = dc.register(selector, SelectionKey.OP_READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(General.BUFFER_SIZE);

            Ticker ticker = new Ticker(1200);
            updateMessage("Running");
            int transmitCounter = 0;
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
                    byteBuffer.clear();
                    canMsgHeartbeat.toBB(byteBuffer);
                    byteBuffer.flip();
                    dc.send(byteBuffer, General.FINAL_DESTINATION);
                    ticker.tick();
                    System.out.println("TRansmittCounter" + (++transmitCounter));
                } else {
                    for (SelectionKey skey : selector.selectedKeys()) {
                        skey.channel();
                        byteBuffer.clear();
                        SocketAddress isa = dc.receive(byteBuffer);
                        byteBuffer.flip();
                        canMsgHeartbeat.fromBB(byteBuffer);
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

    public LinkedBlockingQueue<String> getSourceQueue() {
        return sourceQueue;
    }

    public LinkedBlockingQueue<String> getSinkQueue() {
        return sinkQueue;
    }

    private final LinkedBlockingQueue<String> sourceQueue = new LinkedBlockingQueue<>(General.QUEUE_DEPTH);
    private final LinkedBlockingQueue<String> sinkQueue = new LinkedBlockingQueue<>(General.QUEUE_DEPTH);
//	private final NonFXThreadEventReciever nonFXThreadEventReciever;
//	private URI uri;

    public WebsocketStringService() {
    }

    @Override
    protected Task<Void> createTask() {

        return new WebsocketTask();
    }
}
