package webster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class reads bytes from a stream, creates consignment and 
 * puts each consignment into a message indexed by an incrementing sequence number
 * 
 * Message Format (without in-between space)
 * RDT sequence_number payload CRLF, or
 * RDT sequence_number payload END_CRLF, or
 * @author - eekian | tuhin
 */

public class MessageFactory {
    
    public static int CONSIGNMENT = 1024;
    
    //byte arrays to store headers
    
    public static byte[] REQUEST = new byte[] { 0x52, 0x45, 0x51, 0x55, 0x45, 0x53, 0x54 };
    public static byte[] ACK = new byte[] { 0x41, 0x43, 0x4b };
    public static byte[] NAK = new byte[] { 0x4e, 0x41, 0x4b };
    
    public static byte[] RDT = new byte[] { 0x52, 0x44, 0x54 };
    public static byte[] SEQ = new byte[] { 0x0 };
    
    public static byte[] END = new byte[] { 0x45, 0x4e, 0x44 };
    public static byte[] CRLF = new byte[] { 0x0a, 0x0d };
    
    private static FileInputStream myFIS = null;
    public static int bytesRead;
    public static int seqNo = -1;
    
    //initializes the file to be streamed
    
    public static void init(String filename)
    {
        bytesRead = 0;
        try
        {
            myFIS = new FileInputStream(filename);
        }
        catch(FileNotFoundException ex1)
        {
            System.out.println("MessageFactory #1 : " + ex1.getMessage());
        }
    }
    
    // closes the file to be streamed
    
    public static void close()
    {
        try 
        {
            if(myFIS != null)
                myFIS.close();
        }
        catch(IOException ex)
        {
            System.out.println("MessageFactory #2 : " + ex.getMessage());
        }
    }
    
    // provides a byte[] iterator
    // similar to the funtion of yield keyword in Python
    
    public static byte[] readStream() 
    {
        byte[] myData = new byte[CONSIGNMENT];
        byte[] myLastData;
        byte[] myMsg = null;
       
        int i; // counter for copying bytes in array
        
        try 
        {
            bytesRead = myFIS.read(myData);
            seqNo = unsignedToBytes(SEQ[0]);

            if(bytesRead > -1) 
            {  
                if(bytesRead < CONSIGNMENT) 
                {
                    // last consignment
                    // make a special byte array that exactly fits the number of bytes read 
                    // otherwise, the consignment may be padded with junk data
                    myLastData = new byte[bytesRead];
                    for (i = 0; i < bytesRead; i++) 
                    {
                        myLastData[i] = myData[i];
                    }
                    myMsg = concatenateByteArrays(RDT, SEQ, myLastData, END, CRLF);
                    SEQ[0] = 0x0;
                    bytesRead = -1;
                } 
                else 
                {
                    myMsg = concatenateByteArrays(RDT, SEQ, myData, CRLF);
                    SEQ[0] = (byte) (SEQ[0] + 1);
                    
                    // reuse sequence numbers
                    if(unsignedToBytes(SEQ[0]) >= Utils.BIT_LENGTH)
                        SEQ[0] = 0x0;
                }
            }
        }
                      
        catch(IOException ex)
        {
            System.out.println("MessageFactory #3 : " + ex.getMessage());
        }
        
        return myMsg;
    }
    
    // overloaded methods to copy byte[]s into a single byte[]
    
    public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c) 
    {
        byte[] result = new byte[a.length + b.length + c.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length+b.length, c.length);
        return result;
    }
    
    public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d) 
    {
        byte[] result = new byte[a.length + b.length + c.length + d.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length+b.length, c.length);
        System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
        return result;
    }
    
    public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) 
    {
        byte[] result = new byte[a.length + b.length + c.length + d.length + e.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length+b.length, c.length);
        System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
        System.arraycopy(e, 0, result, a.length+b.length+c.length+d.length, e.length);
        return result;
    }
    
    // byte in Java are signed in nature
    // this operation removes the sign but introduces an integer overhead
    
    public static int unsignedToBytes(byte a)
    {
        return a & 0xFF;
    }

}
