/*
 * LEDataInputStream.java
 *
 * Copyright (c) 1998
 * Roedy Green
 * Canadian Mind Products
 * #208 - 525 Ninth Street
 * New Westminster, BC Canada V3M 5T9
 * tel: (604) 777-1804
 * mailto:roedy@mindprod.com
 * http://mindprod.com
 *
 *
 * Version 1.0 1998 January 6
 *         1.1 1998 January 7 - officially implements DataInput
 *         1.2 1998 January 9 - add LERandomAccessFile
 *         1.3 1998 August 27 - fix bug, readFully instead of read.
 *         1.4 1998 November 10 - add address and phone.
 *         1.5 1999 October 8 - use cmp.LEDataStream package name.
 *         1.5.x 2000 Febuary 15 - Modifed by James Macgill to include LE/BE switch
 *
 * Very similar to DataInputStream except it reads little-endian instead of
 * big-endian binary data.
 * We can't extend DataInputStream directly since it has only final methods.
 * This forces us implement LEDataInputStream with a DataInputStream object,
 * and use wrapper methods.
 */
package cmp.LEDataStream;
import java.io.*;

public class LEDataInputStream implements DataInput {

    private static final String EmbeddedCopyright =
        "Copyright 1998 Roedy Green, Canadian Mind Products, http://mindprod.com";

    /**
    	* constructor
    	*/
    public LEDataInputStream(InputStream in) {
        this.in = in;
        this.d = new DataInputStream(in);
        w = new byte[8];
    }

    // L I T T L E   E N D I A N   R E A D E R S
    // Little endian methods for multi-byte numeric types.
    // Big-endian do fine for single-byte types and strings.
    /**
    	 * like DataInputStream.readShort except little endian.
    	 */
    public final short readShort() throws IOException {
        if (!littleEndianMode) {
            return d.readShort();
        }
        d.readFully(w, 0, 2);
        return (short) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
    }

    /**
    	 * like DataInputStream.readUnsignedShort except little endian.
    	 * Note, returns int even though it reads a short.
    	 */
    public final int readUnsignedShort() throws IOException {
        if (!littleEndianMode) {
            return d.readUnsignedShort();
        }
        d.readFully(w, 0, 2);
        return ((w[1] & 0xff) << 8 | (w[0] & 0xff));
    }

    /**
    	 * like DataInputStream.readChar except little endian.
    	 */
    public final char readChar() throws IOException {
        if (!littleEndianMode) {
            return d.readChar();
        }
        d.readFully(w, 0, 2);
        return (char) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
    }

    /**
    	 * like DataInputStream.readInt except little endian.
    	 */
    public final int readInt() throws IOException {
        if (!littleEndianMode) {
            return d.readInt();
        }
        d.readFully(w, 0, 4);
        return (w[3])
            << 24 | (w[2] & 0xff)
            << 16 | (w[1] & 0xff)
            << 8 | (w[0] & 0xff);
    }

    /**
    	 * like DataInputStream.readLong except little endian.
    	 */
    public final long readLong() throws IOException {
        if (!littleEndianMode) {
            return d.readLong();
        }
        d.readFully(w, 0, 8);
        return (long) (w[7]) << 56 |
        /* long cast needed or shift done modulo 32 */
        (long) (w[6] & 0xff)
            << 48 | (long) (w[5] & 0xff)
            << 40 | (long) (w[4] & 0xff)
            << 32 | (long) (w[3] & 0xff)
            << 24 | (long) (w[2] & 0xff)
            << 16 | (long) (w[1] & 0xff)
            << 8 | (long) (w[0] & 0xff);
    }

    /**
    	 * like DataInputStream.readFloat except little endian.
    	 */
    public final float readFloat() throws IOException {
        if (!littleEndianMode) {
            return d.readFloat();
        }
        return Float.intBitsToFloat(readInt());
    }

    /**
    	 * like DataInputStream.readDouble except little endian.
    	 */
    public final double readDouble() throws IOException {
        if (!littleEndianMode) {
            return d.readDouble();
        }
        return Double.longBitsToDouble(readLong());
    }

    // p u r e l y   w r a p p e r   m e t h o d s
    // We can't simply inherit since dataInputStream is final.

    /* Watch out, may return fewer bytes than requested. */
    public final int read(byte b[], int off, int len) throws IOException {
        // For efficiency, we avoid one layer of wrapper
        return in.read(b, off, len);
    }

    public final void readFully(byte b[]) throws IOException {
        d.readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off, int len)
        throws IOException {
        d.readFully(b, off, len);
    }

    public final int skipBytes(int n) throws IOException {
        return d.skipBytes(n);
    }

    /* only reads one byte */
    public final boolean readBoolean() throws IOException {
        return d.readBoolean();
    }

    public final byte readByte() throws IOException {
        return d.readByte();
    }

    // note: returns an int, even though says Byte.
    public final int readUnsignedByte() throws IOException {
        return d.readUnsignedByte();
    }

   public final String readLine() throws IOException
   {
	  return d.readLine();
   }      

    public final String readUTF() throws IOException {
        return d.readUTF();
    }

    // Note. This is a STATIC method!
    public final static String readUTF(DataInput in) throws IOException {
        return DataInputStream.readUTF(in);
    }

    public final void close() throws IOException {
        d.close();
    }

    public final void setLittleEndianMode(boolean flag) {
        littleEndianMode = flag;
    }

    public final boolean getLittleEndianMode() {
        return littleEndianMode;
    }

    public final boolean isLittleEndianMode() {
        return littleEndianMode;
    }
    // i n s t a n c e   v a r i a b l e s

    protected DataInputStream d;
    // to get at high level readFully methods of DataInputStream
    protected InputStream in;
    // to get at the low-level read methods of InputStream
    byte w[]; // work array for buffering input
    protected boolean littleEndianMode = true;

} // end class LEDataInputStream