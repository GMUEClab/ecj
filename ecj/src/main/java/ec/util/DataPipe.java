/*
  Copyright 2009 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.*;

/*
  DataPipe is a mechanism which allows you to pipe data from an OutputStream to an InputStream within a single thread.
  This differs from a PipedInputStream/PipedOutputSteam pair in that it permits a single thread (in fact requires it
  due to lack of synchronization for speed).  To do this, the DataPipe maintains an extensible buffer which gets as large
  as necessary when filled with data from the InputStream, then expels it out the OutputStream when called.  The
  default Input and Output streams of a DataPipe are DataInputStream and DataOutputStream, since reading and writing
  binary objects is its primary function.

  <p>The procedure is as follows: create the DataPipe, then start writing to its DataInputStream.  When you are done,
  start reading from its DataOutputStream. DataPipe is meant to be one-shot: write a bunch of stuff, then read <i>all</i> of it.
  You shouldn't read and write piecemeal as the DataPipe is not a true circular buffer and will grow without bound with wasted
  space equal to the amount you've read so far.

  <p>You <i>can</i>, however, reuse the DataPipe by calling reset() on it.  
  Note that this retains the current buffer however large it has grown to.
*/

public class DataPipe
    {
    // an extensible array which can be pushed into, then pulled out of
    byte[] buffer = new byte[8192];

    // the number of bytes in the array -- in reality this is the number of bytes written to the array so far
    int size = 0;
    
    // the number of bytes read from the array.  pull will always trail size
    int pull = 0;
    
    // Double the size of the buffer
    void resize()
        {
        byte[] newbuffer = new byte[buffer.length * 2];
        System.arraycopy(buffer, 0, newbuffer, 0, buffer.length);
        buffer = newbuffer;
        }
    
    // Add a byte to the buffer
    void push(byte b)
        {
        if (size >= buffer.length) resize();
        buffer[size++] = b;
        }
    
    // Add bytes to the buffer, read from b[offset]... b[offset+length-1]
    void push(byte[] b, int offset, int length)
        {
        if (size + length > buffer.length) resize();
        System.arraycopy(b, offset, buffer, size, length);
        size += length;
        }
        
    // Return an unsigned byte read from the buffer.
    // If there are no bytes left to read, -1 is returned.
    int pull()
        {
        if (pull==size) return -1;  // EOF
        byte b = buffer[pull++];
        if (b < 0) return b + 256;
        return b;
        }
    
    // Provide up to *length* bytes from the buffer.  They are
    // placed into b[offset] ... b[offset + length - 1].
    // If there aren't *length* bytes in the buffer, some number
    // less than that is actually provided.  The actual number
    // of bytes provided is returned.
    // If there are no bytes left to read at all, -1 is returned.
    int pull(byte[] b, int offset, int length)
        {
        if (pull==size) return -1;
        if (length > size-pull) length = size-pull;
        System.arraycopy(buffer, pull, b, offset, length);
        pull += length;
        return length;
        }
    
    /** The input stream */
    public DataInputStream input;
    /** The output stream */
    public DataOutputStream output;
    
    public DataPipe()
        {
        OutputStream outStream = new OutputStream()
            {
            public void write(int b) throws IOException { push((byte)b); }
            public void write(byte[] b, int off, int len) throws IOException { push(b, off, len); }
            public void write(byte[] b) throws IOException { push(b, 0, b.length); }
            };
        output = new DataOutputStream(outStream);
        
        InputStream inStream = new InputStream()
            {
            public int read() throws IOException { return pull(); }
            public int read(byte[] b, int off, int len) throws IOException { return pull(b, off, len); }
            public int read(byte[] b) throws IOException { return pull(b, 0, b.length); }
            };
        
        input = new DataInputStream(inStream);
        }
    
    /** Reset the buffer.  Does not resize it back to a smaller size -- if it has ballooned it will
        stay large, though it will no longer have wasted space in it.  If you wish to make the buffer
        a more manageable size, create a new DataPipe instead. */
    public void reset()
        {
        pull = size = 0;
        }
        
    /** Returns the total size of the buffer. */
    public int size()
        {
        return buffer.length;
        }
    
    /** Returns the number of elements written to the buffer so far (after the last reset()). */
    public int numWritten()
        {
        return size;
        }
        
    /** Returns the number of elements read from the buffer so far (after the last reset()). */
    public int numRead()
        {
        return pull;
        }
        
    /** A poor-man's clone for serializable but not cloneable objects:
        serializes an object to the pipe, then deserializes it. */
    public static Object copy(Serializable obj) throws IOException, ClassNotFoundException
        {
        DataPipe pipe = new DataPipe();
        ObjectOutputStream s = new ObjectOutputStream(pipe.output);
        ObjectInputStream u = new ObjectInputStream(pipe.input);
        s.writeObject(obj);
        return u.readObject();
        }

    public String toString() { return "DataPipe(" + numWritten() + ", " + numRead() + ", " + size() + ")"; }
    }
