package udptest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class WorkaroundMulticastJoin {
    /*
     * multicast 224.0.0.1 must be joined in Java
     * 
     * 
     */
    private enum JOIN_METHOD {
        ALL, //
        GET_BY_HOST, //
        SELECTION
    }

    private final JOIN_METHOD joinMethod;

    private static final Logger logger = Logger.getLogger(WorkaroundMulticastJoin.class.getName());

    private final InetAddress allSystemsMulticast;
    private final DatagramChannel datagramChannel;

    private final Map<NetworkInterface, MembershipKey> joins = new LinkedHashMap<>();

    public WorkaroundMulticastJoin(DatagramChannel datagramChannel) throws IOException {

        this.datagramChannel = datagramChannel;
//        joinMethod = JOIN_METHOD.GET_BY_HOST;
        joinMethod = JOIN_METHOD.GET_BY_HOST;

        allSystemsMulticast = InetAddress.getByName("224.0.0.1");

        datagramChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
        datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    }

    public void join() throws SocketException {
        switch (joinMethod) {
        case ALL: {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            joinAll(e);
        }
            break;
        case GET_BY_HOST:
            joinByHost();
            break;
        case SELECTION:
            break;
        }
    }

    private void joinByHost() {
        NetworkInterface networkInterface = null;
        try {
            final MembershipKey membershipKey;

            networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
            membershipKey = datagramChannel.join(allSystemsMulticast, networkInterface);

            joins.put(networkInterface, membershipKey);

            System.out.println("Joined " + networkInterface.getDisplayName());
        } catch (Exception e) {
            logger.info("Join Interface " + //
                    networkInterface.getDisplayName() + //
                    " Failed: " + //
                    e.getMessage());
        }
    }

    // have function without exception
    private void joinAll(final Enumeration<NetworkInterface> interfaces) {
        while (interfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = interfaces.nextElement();
            try {
                final MembershipKey membershipKey;

                membershipKey = datagramChannel.join(allSystemsMulticast, networkInterface);

                // DONT set this value or no outgoing packages
//                datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
                datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, null);

                joins.put(networkInterface, membershipKey);

                System.out.println("Joined " + networkInterface.getDisplayName());
            } catch (Exception e) {
                logger.info("Join Interface " + //
                        networkInterface.getDisplayName() + //
                        " Failed: " + //
                        e.getMessage());
            }
        }
    }

    public void dropAll() {
        for (final NetworkInterface networkInterface : joins.keySet()) {
            final MembershipKey membershipKey = joins.get(networkInterface);
            try {
                membershipKey.drop();
            } catch (Exception e) {
                logger.info("Drop Interface " + //
                        networkInterface.getDisplayName() + //
                        " Failed: " + //
                        e.getMessage());
            }
        }
    }
}
