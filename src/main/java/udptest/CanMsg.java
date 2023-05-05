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
    
    public CanMsg()
    {
        data.order(ByteOrder.LITTLE_ENDIAN);
    }
}
