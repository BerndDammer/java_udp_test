package netinf;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public class NetworkInterfaceView {
    private interface ICellFunction {
        String cellFunction(NetworkInterface networkInterface) throws SocketException;
    }

    private class CellFactoryInterface
            implements Callback<TableColumn.CellDataFeatures<NetworkInterface, String>, ObservableValue<String>> {

        final ICellFunction cf;

        CellFactoryInterface(final ICellFunction cf) {
            this.cf = cf;
        }

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<NetworkInterface, String> param) {
            final NetworkInterface ni = param.getValue();
            String s;
            try {
                s = cf.cellFunction(ni);
            } catch (Exception e) {
                s = e.getMessage();
            }
            return new SimpleStringProperty(s);
        }
    }

    private abstract class CellFactoryBase
            implements Callback<TableColumn.CellDataFeatures<NetworkInterface, String>, ObservableValue<String>> {
        abstract String cellFunction(final NetworkInterface networkInterface) throws Exception;

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<NetworkInterface, String> param) {
            final NetworkInterface ni = param.getValue();
            String s;
            try {
                s = cellFunction(ni);
            } catch (Exception e) {
                s = e.getMessage();
            }
            return new SimpleStringProperty(s);
        }
    }

    private class CellFacIPCnt extends CellFactoryBase {
        @Override
        String cellFunction(NetworkInterface networkInterface) throws SocketException {
            int i = 0;
            Enumeration<InetAddress> ips = networkInterface.getInetAddresses();
            while (ips.hasMoreElements()) {
                ips.nextElement();
                i++;
            }
            return Integer.toString(i);
        }
    }

    private class CellFacIPIndex extends CellFactoryBase {
        private final int index;

        CellFacIPIndex(int index) {
            this.index = index;
        }

        @Override
        String cellFunction(NetworkInterface networkInterface) throws SocketException {
            int i = 0;
            Enumeration<InetAddress> ips = networkInterface.getInetAddresses();
            while (ips.hasMoreElements()) {
                InetAddress ia = ips.nextElement();
                if (i == index) {
                    return ia.getHostAddress();
                }
                i++;
            }
            return "nix";
        }
    }
    private class CellFacIAIndex extends CellFactoryBase {
        private final int index;

        CellFacIAIndex(int index) {
            this.index = index;
        }

        @Override
        String cellFunction(NetworkInterface networkInterface) throws SocketException {
            List<InterfaceAddress> ias = networkInterface.getInterfaceAddresses();
            if(index < ias.size())
            {
//                return ias.get(index).getAddress().getHostAddress();
                return ias.get(index).getAddress().getHostAddress();
            }
            return "nix";
        }
    }

    private class CellFacMAC extends CellFactoryBase {
        @Override
        String cellFunction(NetworkInterface networkInterface) throws SocketException {
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null)
                return ("No HW Addr");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i]));
                if (i != mac.length - 1)
                    sb.append(":");
            }
            return sb.toString();
        }
    }

    TableView<NetworkInterface> table;

    public void doit() {
        final Stage stage = new Stage();

        LinkedList<NetworkInterface> interfaceList = new LinkedList<NetworkInterface>();
        ObservableList<NetworkInterface> interfaces = FXCollections.observableList(interfaceList);
        Enumeration<NetworkInterface> e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }
        while (e.hasMoreElements()) {
            interfaceList.add(e.nextElement());
        }

        table = new TableView<>(interfaces);
        {
            addColumn("Name", (ni) -> ni.getName());
            addColumn("Display Name", (ni) -> ni.getDisplayName());

            addColumn("multicast", (n) -> Boolean.toString(n.supportsMulticast()));
            addColumn("local", (n) -> Boolean.toString(n.isLoopback()));
            addColumn("mac", new CellFacMAC());
            addColumn("virtual", (n) -> Boolean.toString(n.isVirtual()));
            addColumn("up", (n) -> Boolean.toString(n.isUp()));
            addColumn("up", (n) -> Boolean.toString(n.isUp()));
            addColumn("mtu", (n) -> Integer.toString(n.getMTU()));
            addColumn("hasParent", (n) -> Boolean.toString(n.getParent() != null));
            addColumn("IPCnt", new CellFacIPCnt());
            addColumn("IP 1", new CellFacIPIndex(0));
            addColumn("IP 2", new CellFacIPIndex(1));
            addColumn("IAcnt", (n)-> Integer.toString(n.getInterfaceAddresses().size()));
            addColumn("IA 1", new CellFacIAIndex(0));
            addColumn("IA 2", new CellFacIAIndex(1));
        }

        Scene scene = new Scene(table, Color.ROSYBROWN);
        stage.setScene(scene);
        stage.setTitle("Network INterfaces");
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.show();
    }

    private void addColumn(final String name, final CellFactoryBase cfni) {
        TableColumn<NetworkInterface, String> column = new TableColumn<>();
        column.setText(name);
        column.setCellValueFactory(cfni);
        table.getColumns().add(column);
    }

    private void addColumn(final String name, final ICellFunction cf) {
        TableColumn<NetworkInterface, String> column = new TableColumn<>();
        column.setText(name);
        column.setCellValueFactory(new CellFactoryInterface(cf));
        table.getColumns().add(column);
    }
}
