package udptest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class CanMsgHeartbeat extends CanMsg {

    private static final Logger logger = Logger.getLogger( CanMsgHeartbeat.class.getName());
    private static final long VALID_TIMEOUT_MS = 3000l;
    private short counter = 1;
    
    private long lastRec = 0l;
    private short lastSeen;
    
    public void toBB(final ByteBuffer bb)
    {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(CAN_ID_HEARTBEAT);
        bb.putInt(8);
        bb.putShort(counter);
        if( System.currentTimeMillis() <= lastRec + VALID_TIMEOUT_MS)
        {
           bb.putShort(lastSeen);
        }
        else
        {
            bb.putShort( (short)0XFFFF);
        }
        bb.putShort((short)0);
        bb.putShort((short)0);
        counter++;
    }

    public void fromBB(final ByteBuffer bb)
    {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        if( bb.getInt() != CAN_ID_HEARTBEAT)
        {
            logger.warning("Wrong CAN ID");
        }
        if( bb.getInt() != 8)
        {
            logger.warning("Wrong CAN LEN");
        }
        lastSeen = bb.getShort();
        lastRec = System.currentTimeMillis();
        logger.info("[01]" + ((int)lastSeen & 0XFFFF));
        logger.info("[23]" + ((int)bb.getShort() & 0XFFFF));
        
    }
}
