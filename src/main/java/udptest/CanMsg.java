package udptest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CanMsg {
    public static final int MASK_IDLEN = 0X80000000;
    public static final int VAL_IDLEN_29 = 0X80000000;
    public static final int VAL_IDLEN_11 = 0X00000000;

    public static final int CAN_ID_HEARTBEAT = 0X80045670;

    protected int id;
    protected int len;
    protected ByteBuffer data = ByteBuffer.allocate(8);

    public int getId() {
        return id;
    }

    public int getLen() {
        return len;
    }

    public ByteBuffer getData() {
        return data;
    }
    public byte getData(int index) {
        return data.get(index);
    }


    public CanMsg() {
        data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void toBB(final ByteBuffer bb) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(id);
        bb.putInt(8);
        data.clear(); // put all data; reset pointer
        bb.put(data);
    }

    public void fromBB(final ByteBuffer bb) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        id = bb.getInt();
        len = bb.getInt();
        data.clear(); // put all data; reset pointer
        for (int i = 0; i < 8; i++) {
            data.put(bb.get());
        }
    }
}
