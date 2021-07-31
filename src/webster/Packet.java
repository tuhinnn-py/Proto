package webster;

import java.net.DatagramPacket;

/**This class provides a Single LinkedList Implementation
 * A node includes the datagram packet it is associated with, the sequence number and a pointer to the next node
 * @author tuhin
 */

public class Packet
{
    DatagramPacket sp;
    Packet next;
    int seqNo;
    
    public Packet()
    {
        seqNo = -1;
        sp = null;
        next = null;
    }
    
    public Packet(DatagramPacket s, int sNo)
    {
        sp = s;
        seqNo = sNo;
        next = null;
    }
    
    public Packet(DatagramPacket s, int sNo, Packet n)
    {
        sp = s;
        seqNo = sNo;
        next = n;
    }
}