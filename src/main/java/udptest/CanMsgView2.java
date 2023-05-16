package udptest;

import java.net.SocketException;

import javafx.scene.control.TableColumn;

public class CanMsgView2 extends CanMsgView {

    private class CellFacID extends CellFactoryBase {
        @Override
        String cellFunction(CanMsg canMsg) throws SocketException {
            int id = canMsg.getId();
            switch (id & CanMsg.MASK_IDLEN) {
            case CanMsg.VAL_IDLEN_11:
                return String.format("%03X", id & ~CanMsg.MASK_IDLEN);
            case CanMsg.VAL_IDLEN_29:
                return String.format("%08X", id & ~CanMsg.MASK_IDLEN);
            }
            return "OOPs";
        }
    }

    public CanMsgView2() {
        addColumn("ID", new CellFacID());
        addColumn("len", (msg) -> Integer.toString(msg.getLen()));
        addColumn("D0", (msg) -> String.format("%02X", msg.getData(0)));
        addColumn("D1", (msg) -> String.format("%02X", msg.getData(1)));
        addColumn("D2", (msg) -> String.format("%02X", msg.getData(2)));
        addColumn("D3", (msg) -> String.format("%02X", msg.getData(3)));
        addColumn("D4", (msg) -> String.format("%02X", msg.getData(4)));
        addColumn("D5", (msg) -> String.format("%02X", msg.getData(5)));
        addColumn("D6", (msg) -> String.format("%02X", msg.getData(6)));
        addColumn("D7", (msg) -> String.format("%02X", msg.getData(7)));
        
        adjColumns();
    }

    private void adjColumns() {
        for (TableColumn<CanMsg, ?> col : getColumns()) {
            if( !col.getText().equals("ID") )
            {
                col.setPrefWidth(33.0);
            }
        }
    }
}
