package webster;
/**File Server
 * @author tuhin
 */

import java.io.*;
import java.net.*;

public class FServer
{
    private static FPacketWindow window;
    private static int ackNo;
    private static boolean flush;
    
    public static void send(DatagramSocket ss, InetAddress ip, int port)
    {
        byte[] rd, sd;
        DatagramPacket rp, sp;
        
        while(ackNo != 255)
        {
            try
            {
                // Selective Repeat
                if(flush)
                {
                    FPacket head = window.head;
                    //iterate through the window
                    while(head != null)
                    {
                        //don't send the packet incase an event simulated with the probability of dropping a packet return true
                        if(!Utils.try_event(Utils.DROP_PROBABILITY))
                        {
                            System.out.println("Resent Consignment # " + head.seqNo);
                            ss.send(head.packet);
                        }
                        else
                        {
                            System.out.println("Dropped Consignment # " + head.seqNo);
                        }
                        
                        head = head.next;
                    }
                    flush = false;
                }
                
                //Fill up the Sender's window in case it's not completely filled up
                while(!window.isFull())
                {
                    sd = MessageFactory.readStream();
                    if(sd != null)
                    {
                        sp = new DatagramPacket(sd, sd.length, ip, port);
                        FPacket packet = new FPacket(MessageFactory.seqNo, sp);
                        
                        window.addPacket(packet);
                        if(!Utils.try_event(Utils.DROP_PROBABILITY))
                        {
                            System.out.println("Sent Consignment # " + MessageFactory.seqNo);
                            ss.send(sp);
                        }
                        else
                        {
                            System.out.println("Dropped Consignment # " + MessageFactory.seqNo);
                        }
                    }
                    else
                        break;
                }
                
                rd = new byte[1080];
                rp = new DatagramPacket(rd, rd.length);
                ss.receive(rp);
                
                if(!MessageExtractor.isNAK(rp.getData()))
                {
                    ackNo = MessageExtractor.getSeqNo(rp.getData());
                    System.out.println("Client ACK = " + ackNo);
                      
                    //selective acknowledgment
                    if(!window.isEmpty())
                    {
                        window.remove(ackNo);
                    }
                }
                else
                {
                    int nakNo = MessageExtractor.getSeqNo(rp.getData());
                    System.out.println("Client NAK = " + nakNo);
                    
                    if(!Utils.try_event(Utils.DROP_PROBABILITY))
                    {
                        ss.send(window.packets.get(nakNo).packet);
                        System.out.println("Sent Consignment # " + nakNo);
                    }
                    else
                    {
                        System.out.println("Dropped Consignment # " + nakNo);
                    }
                }
            }
            catch(SocketTimeoutException ex)
            {
                System.out.println("Timed Out.\nResending all frames");
                flush = true;
                //Selective-Repeat
                //recurse to get back into the while loop
            }
            catch(IOException | NumberFormatException ex)
            {
                System.out.println("FServer #2 : " + ex.getMessage());
            }
        }
    }
    
    public static boolean init(String filename)
    {
        try
        {
            MessageFactory.init(filename);
            System.out.println("Received request for file " + filename + "...");
        }
        catch(Exception ex)
        {
            System.out.println("Server #1 : " + ex.getMessage());
            return false;
        }
        
        ackNo = 0;
        window = new FPacketWindow(Utils.WINDOW_SIZE);
        
        flush = false;
        return true;
    }
    
    public static void close()
    {
        MessageFactory.close();
    }
    
    public static void main(String args[])
    {
        if(args.length == 0)
            args = new String[] {"40000"};
        
        DatagramSocket ss = null;
        DatagramPacket rp;
        
        byte[] rd;
        InetAddress ip;
        
        int port;
        Utils.useSR();
        
        try
        {
            ss = new DatagramSocket(Integer.parseInt(args[0]));
            System.out.println("Selective Repeat Automatic Repeat Request.");
            System.out.println("Server is up...");
            rd = new byte[1080];
            
            rp = new DatagramPacket(rd, rd.length);
            ss.receive(rp);
            
            //remove junk
            rd = Utils.trim(rp);
            
            ip = rp.getAddress();
            port = rp.getPort();
            
            String filename = new String(rd, 7, rd.length - 9);
            init(filename);
            
            ss.setSoTimeout(Utils.TIMEOUT);
            send(ss, ip, port);

        }
        catch(IOException | NumberFormatException ex)
        {
            System.out.println("FServer #2 : " + ex.getMessage());
        }
        finally
        {
            close();
            if(ss != null)
                ss.close();
        }
    }
    
}
