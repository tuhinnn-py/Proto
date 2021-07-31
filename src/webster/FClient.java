package webster;
/**Client
 * @author tuhin
 */

import java.io.*;
import java.net.*;
import java.util.*;

class ReceiverWindow
{
    Map<Integer, DatagramPacket> packets;
    int headSeqNo, size;
    
    public ReceiverWindow(int capacity)
    {
        size = capacity;
        headSeqNo = 0;
        
        packets = new HashMap<>(capacity);
    }
    
    public boolean hasSeen(int seqNo)
    {
        return packets.containsKey(seqNo);
    }
    
    public boolean isEmpty()
    {
        return packets.isEmpty();
    }
    
    public boolean isFull()
    {
        return packets.size() == size;
    }
    
    public boolean inWindow(int seqNo)
    {
        int endSeqNo = (headSeqNo + size) % Utils.BIT_LENGTH;
        if(endSeqNo > headSeqNo)
            return seqNo >= headSeqNo && seqNo <= endSeqNo;
        else
            return seqNo >= headSeqNo || seqNo <= endSeqNo;
    }
    
    public void addPacket(int seqNo, DatagramPacket packet)
    {
        packets.put(seqNo, packet);    
    }
    
    public void deleteHead()
    {
        if(!packets.isEmpty())
        {
            packets.remove(headSeqNo);
            headSeqNo = (headSeqNo + 1) % Utils.BIT_LENGTH;
        }
    }
}

public class FClient 
{
    // receiver window
    private static ReceiverWindow window;
    private static boolean nakSent;
    
    //REQUEST filename CLRF
    public static boolean init(DatagramSocket cs, String[] args)
    {
        byte[] request = MessageFactory.concatenateByteArrays(MessageFactory.REQUEST, args[2].getBytes(), MessageFactory.CRLF);
        DatagramPacket sp;
        
        try
        {
            MessageExtractor.init("client\\" + args[2]);
            sp = new DatagramPacket(request, request.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
            cs.send(sp);
        }
        
        catch(IOException | NumberFormatException ex)
        {
            System.out.println("FClient #1 : " + ex.getMessage());
            return false;
        }
        
        window = new ReceiverWindow(Utils.WINDOW_SIZE);
        nakSent = false;
        
        return true;
    }
    
    public static void close()
    {
        MessageExtractor.close();
    }
    
    public static void main(String[] args)
    {
        //incase the commandline arguments are empty
        if(args.length == 0)
            args = new String[] {"localhost", "40000", "Sheldon.pdf"};
        
        DatagramSocket cs = null;
        Utils.useSR();
        
        try
        {
            cs = new DatagramSocket();
            
            //300 milliseconds 
            cs.setSoTimeout(100 * Utils.TIMEOUT);
            byte[] rd, sd;
            
            DatagramPacket rp, sp;
            
            init(cs, args);
            boolean last = false;
            
            while(!last)
            {
                rd = new byte[1080];
                rp = new DatagramPacket(rd, rd.length);
                
                cs.receive(rp);
                int currSeqNo = MessageExtractor.getSeqNo(rd);
                
                //out-of-order packet and NAK has previously not been sent
                if(currSeqNo != window.headSeqNo && (!nakSent))
                {
                    sd = MessageFactory.concatenateByteArrays(MessageFactory.NAK, new byte[] { (byte) window.headSeqNo }, MessageFactory.CRLF);
                    sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));

                    cs.send(sp);
                    System.out.println("Sent NAK = " + window.headSeqNo);
                    nakSent = true;
                }
                
                if(window.inWindow(currSeqNo))
                {
                    window.addPacket(currSeqNo, rp);
                    System.out.println("Received Consignment # " + currSeqNo);
                    
                    while(window.hasSeen(window.headSeqNo))
                    {
                        last = MessageExtractor.writeStream(Utils.trim(window.packets.get(window.headSeqNo)));
                        window.deleteHead();
                        nakSent = false;
                    }
                    
                    if(!last)
                        sd = MessageFactory.concatenateByteArrays(MessageFactory.ACK, new byte[] { (byte) currSeqNo }, MessageFactory.CRLF);
                    else
                        sd = MessageFactory.concatenateByteArrays(MessageFactory.ACK, new byte[] { (byte) -1 }, MessageFactory.CRLF);
                    
                    sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                    cs.send(sp);
                    
                    if(!last)
                        System.out.println("Sent ACK = " + currSeqNo);
                    else
                        System.out.println("Sent ACK = " + 255);
                }
                
            }
        }
        catch(SocketTimeoutException ex)
        {
            System.out.println(ex.getMessage() + "\nNo response from server...\nConnection Terminated");
        }
        catch(IOException ex)
        {
            System.out.println("FClient #2 : " + ex.getMessage());
        }
        finally
        {
            close();
            if(cs != null)
                cs.close();
        }
    }
}
