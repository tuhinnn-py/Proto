package webster;
/**This class provides a Single LinkedList Implementation of a Queue
 * Simulates a window associated with the Sender
 * @author tuhin
 */

public class PacketWindow
{
    private final int size;
    private int length;
    private Packet head, tail;
    
    public PacketWindow(int s)
    {
        size = s;
        length = 0;
        
        head = tail = null;
    }
    
    public int getLength()
    {
        return length;
    }
    
    public boolean isEmpty()
    {
        return length == 0;
    }
    
    public boolean isFull()
    {
        return length == size;
    }
    
    public void addPacket(Packet packet)
    {
        if(head == null)
            head = packet;
        if(tail != null)
            tail.next = packet;
        
        tail = packet;
        length += 1;
    }
    
    public void deleteHead()
    {
        if(head == tail)
            tail = null;
        head = head.next;
        length -= 1;
    }
    
    public Packet getHead()
    {
        return head;
    }
    
}
