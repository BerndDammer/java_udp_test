package udptest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javafx.util.Duration;

public class General {
    public static final Duration COMMAND_DELAY_MS = Duration.millis(1000);
    public static final int BUFFER_SIZE = 4096;
    public static final Duration DEFAULT_SPEED = Duration.millis(1000.0);
    public static final int POLLING_DELAY_MS = 150;
    public static final int LOG_AUTODELETE = 17;
    public static final java.time.Duration CONNECT_TIMEOUT = java.time.Duration.ofSeconds(3l);
    public static final int QUEUE_DEPTH = 20;

    public static final String IF_NAME = "eth3";
    public static final int MULTICAST_PORT = 50100;
    public static final int SELECT_TIMEOUT = 2500;
    public static final InetSocketAddress FINAL_DESTINATION;

    static {
        InetSocketAddress isa;
        try {
            isa = new InetSocketAddress(InetAddress.getByName("224.0.0.1"), 50000);
        } catch (UnknownHostException e) {
            isa = null; // this should never happen
        }
        FINAL_DESTINATION = isa;
    }
}
