package webster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * This class extracts the message from a byte[] and stores it in a file
 * Provides output stream utility
 */

/**
 *
 * @author eekian | tuhin
 */

public class MessageExtractor 
{
    public static byte[] MESSAGE_START = { 0x52, 0x44, 0x54 }; // "RDT "
    public static byte[] MESSAGE_END = { 0x45, 0x4e, 0x44, 0xa, 0xd }; //" END CRLF"
    
    public static int MESSAGE_FRONT_OFFSET = 4; //"RDT#"
    public static int MESSAGE_BACK_OFFSET = 2; //"CRLF"
    public static int MESSAGE_LAST_BACK_OFFSET = 5; //"ENDCRLF"
    
    private static File myFile;
    private static FileOutputStream myFOS;
    
    // initialises the file to be streamed into
    
    public static void init(String filename)
    {
        try
        {
            myFile = new File(filename);
            myFOS = new FileOutputStream(myFile);
        }
        catch(FileNotFoundException ex) 
        {
            System.out.println("MessageExtractor #1 : " + ex.getMessage());
        }
    }
    
    // closes the file to be streamed into
    
    public static void close()
    {
        try 
        {
            if(myFOS != null)
                myFOS.close();
        }
        catch(IOException ex)
        {
            System.out.println("MessageExtractor #2 : " + ex.getMessage());
        }
    }
    
    // strips the 1 byte sequence number from the header
    
    public static int getSeqNo(byte[] msg)
    {
        if(msg == null)
            return -1;
        return MessageFactory.unsignedToBytes(msg[3]);
    }
    
    public static boolean isNAK(byte[] msg)
    {
        return msg[0] == MessageFactory.NAK[0];
    }
    
    // writes the stream of byte[] to the file
    
    public static boolean writeStream(byte[] msg) 
    {
        if(msg == null)
            return true;
        
        try
        {
            // get last message
            if (!matchByteSequence(msg, msg.length-MESSAGE_END.length , MESSAGE_END.length, MESSAGE_END)) 
            {
                myFOS.write(msg, MESSAGE_FRONT_OFFSET, msg.length-MESSAGE_FRONT_OFFSET-MESSAGE_BACK_OFFSET);
                return false;
            }
            else 
            {
                myFOS.write(msg, MESSAGE_FRONT_OFFSET, msg.length-MESSAGE_FRONT_OFFSET-MESSAGE_LAST_BACK_OFFSET);
                return true;
            } 
        }
        catch(IOException ex)
        {
            System.out.println("MessageExtractor #3 : " + ex.getMessage());
        }
        
        return false;
    }
           
    static public boolean matchByteSequence(byte[] input, int offset, int length, byte[] ref) {
        
        boolean result = true;
        
        if (length == ref.length) {
            for (int i=0; i<ref.length; i++) {
                if (input[offset+i] != ref[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}