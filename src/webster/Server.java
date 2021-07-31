package webster;
/**Server
 * @author tuhin
 */

import java.io.*;
import java.net.*;

public class Server
{
    // A LinkedList Implementation of a Queue which simuates the Sender Window
    private static PacketWindow window;
    // flag to determine whether the contents of the window need to be flushed
    private static boolean flush;
    // variable to store the most recent acknowledgment number received
    private static int ackNo;
    
    // initialises the MessageFactory class with the file to be sent
    
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
        
        flush = false;
        window = new PacketWindow(Utils.WINDOW_SIZE);
        
        ackNo = 0;
        return true;
    }
    
    public static void close()
    {
        MessageFactory.close();
    }
    
    public static void send(DatagramSocket ss, InetAddress ip, int port)
    {
        byte[] rd, sd;
        DatagramPacket rp, sp;
        
        while(ackNo != 255)
        {
            try
            {
                // go-back-N
                if(flush)
                {
                    Packet head = window.getHead();
                    //iterate through the window
                    while(head != null)
                    {
                        //don't send the packet incase an event simulated with the probability of dropping a packet return true
                        if(!Utils.try_event(Utils.DROP_PROBABILITY))
                        {
                            System.out.println("Resent Consignment # " + head.seqNo);
                            ss.send(head.sp);
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
                        Packet packet = new Packet(sp, MessageFactory.seqNo);

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

                ackNo = MessageExtractor.getSeqNo(rp.getData());
                System.out.println("Client ACK = " + ackNo);

                //reuse address space
                if(ackNo == 0)
                    ackNo = Utils.BIT_LENGTH;

                //cumulative acknowledgment
                if(!window.isEmpty() && window.getHead().seqNo <= ackNo)
                {
                    window.deleteHead();
                }
            }
            catch(SocketTimeoutException ex)
            {
                System.out.println("Timed Out\nResending all frames");
                flush = true;
                //go-back-N
                //recurse to get back into the while loop
            }
            catch(IOException | NumberFormatException ex)
            {
                System.out.println("Server #2 : " + ex.getMessage());
            }
            
        }        
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
        
        try
        {
            ss = new DatagramSocket(Integer.parseInt(args[0]));
            if(Utils.WINDOW_SIZE == 1)
                System.out.println("Stop-And-Wait Automatic Repeat Request.");
            else
                System.out.println("Go-Back-N Automatic Repeat Request.");
            
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
            System.out.println("Server #2 : " + ex.getMessage());
        }
        finally
        {
            close();
            if(ss != null)
                ss.close();
        }
    }
    
}
