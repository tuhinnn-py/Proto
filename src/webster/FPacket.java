package webster;
/**
 *
 * @author tuhin
 */

import java.net.*;

public class FPacket
{
    final int seqNo;
    final DatagramPacket packet;

    FPacket prev, next;

    public FPacket(int sNo, DatagramPacket p)
    {
        seqNo = sNo;
        packet = p;
        prev = next = null;
    }

    public FPacket(int sNo, DatagramPacket p, FPacket previous, FPacket n)
    {
        seqNo = sNo;
        packet = p;

        prev = previous;
        next = n;
    }

    public void deleteSelf()
    {
        if(prev != null)
            prev.next = next;
        if(next != null)
            next.prev = prev;
    }
}
