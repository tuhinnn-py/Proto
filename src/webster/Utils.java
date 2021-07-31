package webster;

import java.net.DatagramPacket;
import java.util.Random;

/**This class provides utilities common to the most of the classes in the package
 * Includes constants and other factors
 * @author hp
 */

public class Utils 
{
    // WINDOW_SIZE = 1 signifies Stop-And-Wait Protocol
    // WINDOW_SIZE = N signifies Go-Back-N Protocol
    
    public static int WINDOW_SIZE = 100;
    public static int BIT_LENGTH = (1 << (int) Math.ceil(Math.log(WINDOW_SIZE) / Math.log(2) + 0.1));
    
    // the bit length in case Selective Repeat is used
    public static final int SR_BIT_LENGTH = 32;
    
    // the probability of dropping a packet
    public static final double DROP_PROBABILITY = 0.1;   
    
    // timeout constant for Stop-And-Wait    
    public static final int TIMEOUT = 30;
    
    // simulates an event of probability probability and returns its outcome    
    public static boolean try_event(double probability)
    {
        Random random = new Random();
        return probability >= random.nextDouble();
    }
    
    public static void useSR()
    {
        BIT_LENGTH = SR_BIT_LENGTH;
        WINDOW_SIZE = BIT_LENGTH / 2;   
    }
    
    // removes junk from a byte[]    
    public static byte[] trim(DatagramPacket packet)
    {
        int length = packet.getLength();
        byte[] bytePacket = new byte[length];
        
        System.arraycopy(packet.getData(), 0, bytePacket, 0, length);
        
        return bytePacket;
    }
}
