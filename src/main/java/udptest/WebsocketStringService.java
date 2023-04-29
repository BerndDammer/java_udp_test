package udptest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Enumeration;
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

		void test()
		{
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
			updateMessage("Starting");
			int counter = 0;

			DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
					.setOption(StandardSocketOptions.SO_BROADCAST, true)
					.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true)
					.setOption(StandardSocketOptions.SO_REUSEADDR, true)
					.bind(new InetSocketAddress(General.MULTICAST_PORT));

			MembershipKey mkkey;
			{
				{
					MulticastSocket ms = new MulticastSocket();
					NetworkInterface ni = ms.getNetworkInterface();
					System.out.println(ni.getDisplayName());
					System.out.println(ni.getName());
				}
				Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

				// NetworkInterface ni = NetworkInterface.getByName(General.IF_NAME);
				// NetworkInterface ni =
				// NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.178.20"));
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				dc.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
				mkkey = dc.join(InetAddress.getByName("224.0.0.1"), ni);
			}

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
				if( ticker.isBehind())
				{
					channels = selector.selectNow();
				}
				else
				{
					channels = selector.select(ticker.nextTimeout());
				}
				if (channels == 0) {
					// timeout
					byteBuffer.clear();
					byteBuffer.put("jfewofjewofjew".getBytes());
					byteBuffer.flip();
					dc.send(byteBuffer, General.FINAL_DESTINATION);
					ticker.tick();
					System.out.println("TRansmittCounter" + (++transmitCounter));
				} else {
					for (SelectionKey skey : selector.selectedKeys()) {
						skey.channel();
						byteBuffer.clear();
						SocketAddress isa = dc.receive(byteBuffer);
						updateMessage("Got data from: " + isa + "  Count: " + counter++);
						selector.selectedKeys().remove(skey); // nercessary ????
					}
				}
			}
			selector.close();
			key.cancel();

			mkkey.drop();
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
