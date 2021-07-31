package webster;
/**
 *
 * @author tuhin
 */

import java.util.*;

public class FPacketWindow
{
    final int size;
    int length;

    Map<Integer, FPacket> packets;
    FPacket head, tail;

    public FPacketWindow(int capacity)
    {
        size = capacity;
        length = 0;

        packets = new HashMap<>();
        head = tail = null;
    }

    public void addPacket(FPacket packet)
    {
        packets.put(packet.seqNo, packet);
        if(head == null)
            head = packet;
        if(tail != null)
        {
            tail.next = packet;
            packet.prev = tail;
        }

        tail = packet;
        length += 1;
    }

    public void remove(int seqNo)
    {
        if(!packets.containsKey(seqNo))
            return;
        
        FPacket packet = packets.get(seqNo);
        if(head == packet)
            head = packet.next;

        if(tail == packet)
            tail = packet.prev;

        packet.deleteSelf();
        packets.remove(seqNo);
        length -= 1;
    }

    public boolean isEmpty()
    {
        return  length == 0;
    }

    public boolean isFull()
    {
        return length == size;
    }
}
