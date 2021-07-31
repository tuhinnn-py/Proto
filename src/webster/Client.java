package webster;
/**Client
 * @author tuhin
 */

import java.io.*;
import java.net.*;

public class Client 
{
    //variable to denote the current packet expected
    public static byte seqNo;
    
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
        
        seqNo = 0x0;
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
        
        try
        {
            cs = new DatagramSocket();
            
            //300 milliseconds 
            cs.setSoTimeout(1000 * Utils.TIMEOUT);
            byte[] rd, sd;
            
            DatagramPacket rp, sp;
            
            init(cs, args);
            boolean last = false;
            
            while(!last)
            {
                rd = new byte[1080];
                rp = new DatagramPacket(rd, rd.length);
                
                cs.receive(rp);
                rd = Utils.trim(rp);
                
                int currSeqNo = MessageExtractor.getSeqNo(rd);
                
                //in-order packet gets delivered
                if(currSeqNo == MessageFactory.unsignedToBytes(seqNo))
                {
                    System.out.println("Received Consignment # " + currSeqNo);
                    seqNo = (byte) (seqNo + 1);
                    if(MessageFactory.unsignedToBytes(seqNo) >= Utils.BIT_LENGTH)
                        seqNo = 0x0;
                    
                    last = MessageExtractor.writeStream(rd);
                    if(!last)
                        sd = MessageFactory.concatenateByteArrays(MessageFactory.ACK, new byte[] { seqNo }, MessageFactory.CRLF);
                    else
                        sd = MessageFactory.concatenateByteArrays(MessageFactory.ACK, new byte[] { (byte) -1 }, MessageFactory.CRLF);
                    
                    sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));

                    cs.send(sp);
                    if(!last)
                        System.out.println("Sent ACK = " + MessageFactory.unsignedToBytes(seqNo));
                    else
                        System.out.println("Sent ACK = " + 255);
                }
            }
        }
        catch(SocketTimeoutException ex)
        {
            System.out.println(ex.getMessage() + "\nNo response from server...");
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
