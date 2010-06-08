/*
 * LEDataOutputStream.java
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
 * Copyright (c) 1998 Roedy Green of Canadian Mind Products
 * Version 1.0 1998 January 6
 *         1.1 1998 January 7 - officially implements DataInput
 *         1.2 1998 January 9 - add LERandomAccessFile
 *         1.3 1998 August 28
 *         1.4 1998 November 10 - add new address and phone.
 *         1.5 1999 October 8 - use cmp.LEDataStream package name.
 *         1.5.x 2000 Febuary 15 - Modifed by James Macgill to include LE/BE switch
 * Very similar to DataOutputStream except it writes little-endian instead of
 * big-endian binary data.
 * We can't extend DataOutputStream directly since it has only final methods.
 * This forces us implement LEDataOutputStream with a DataOutputStream object,
 * and use wrapper methods.
 */

package cmp.LEDataStream;
import java.io.*;

public
class LEDataOutputStream implements DataOutput {

   private static final String EmbeddedCopyright =
   "Copyright 1998 Roedy Green, Canadian Mind Products, http://mindprod.com";
   /**
	 * constructor
	 */
   public LEDataOutputStream(OutputStream out) {
	  this.d = new DataOutputStream(out);
	  w = new byte[8]; // work array for composing output
   }      

   // L I T T L E   E N D I A N   W R I T E R S
   // Little endian methods for multi-byte numeric types.
   // Big-endian do fine for single-byte types and strings.

   /**
	 * like DataOutputStream.writeShort.
	 * also acts as a writeUnsignedShort
	 */
   public final void writeShort(int v) throws IOException
   {
	  if(!littleEndianMode){d.writeShort(v);return;}
	  w[0] = (byte) v;
	  w[1] = (byte)(v >> 8);
	  d.write(w, 0, 2);
   }      

   /**
	* like DataOutputStream.writeChar.
	* Note the parm is an int even though this as a writeChar
	*/
   public final void writeChar(int v) throws IOException
   {
	  if(!littleEndianMode){d.writeChar(v);return;}
	  // same code as writeShort
	  w[0] = (byte) v;
	  w[1] = (byte)(v >> 8);
	  d.write(w, 0, 2);
   }      

   /**
	 * like DataOutputStream.writeInt.
	 */
   public final void writeInt(int v) throws IOException
   {
	  if(!littleEndianMode){d.writeInt(v);return;}
	  w[0] = (byte) v;
	  w[1] = (byte)(v >> 8);
	  w[2] = (byte)(v >> 16);
	  w[3] = (byte)(v >> 24);
	  d.write(w, 0, 4);
   }      

   /**
	 * like DataOutputStream.writeLong.
	 */
   public final void writeLong(long v) throws IOException
   {
	  if(!littleEndianMode){d.writeLong(v);return;}
	  w[0] = (byte) v;
	  w[1] = (byte)(v >> 8);
	  w[2] = (byte)(v >> 16);
	  w[3] = (byte)(v >> 24);
	  w[4] = (byte)(v >> 32);
	  w[5] = (byte)(v >> 40);
	  w[6] = (byte)(v >> 48);
	  w[7] = (byte)(v >> 56);
	  d.write(w, 0, 8);
   }      

   /**
	 * like DataOutputStream.writeFloat.
	 */
   public final void writeFloat(float v) throws IOException
   {
	  if(!littleEndianMode){d.writeFloat(v);return;}
	  writeInt(Float.floatToIntBits(v));
   }      

   /**
	 * like DataOutputStream.writeDouble.
	 */
   public final void writeDouble(double v) throws IOException
   {
	  if(!littleEndianMode){d.writeDouble(v);return;}
	  writeLong(Double.doubleToLongBits(v));
   }      

   /**
	 * like DataOutputStream.writeChars, flip each char.
	 */
   public final void writeChars(String s) throws IOException
   {
	  if(!littleEndianMode){d.writeChars(s);return;}
	  int len = s.length();
	  for ( int i = 0 ; i < len ; i++ ) {
		 writeChar(s.charAt(i));
	  }
   } // end writeChars   

   // p u r e l y   w r a p p e r   m e t h o d s
   // We cannot inherit since DataOutputStream is final.

   /* This method writes only one byte, even though it says int */
   public final synchronized void write(int b) throws IOException
   {
	  d.write(b);
   }      

   public final synchronized void write(byte b[], int off, int len)
   throws IOException
   {
	  d.write(b, off, len);
   }      

   public void flush() throws IOException
   {
	  d.flush();
   }      


   /* Only writes one byte */
   public final void writeBoolean(boolean v) throws IOException
   {
	  d.writeBoolean(v);
   }      

   public final void writeByte(int v) throws IOException
   {
	  d.writeByte(v);
   }      

   public final void writeBytes(String s) throws IOException
   {
	  d.writeBytes(s);
   }      

   public final void writeUTF(String str) throws IOException
   {
	  d.writeUTF(str);
   }      

   public final int size() {
	  return d.size();
   }      

   public final void write(byte b[]) throws IOException
   {
	  d.write(b, 0, b.length);
   }      

   public final  void close() throws IOException
   {
	  d.close();
   }      

   public final void setLittleEndianMode(boolean flag){
		littleEndianMode = flag;
	}

	public final boolean getLittleEndianMode(){
		return littleEndianMode;
	}

	public final boolean isLittleEndianMode(){
		return littleEndianMode;
	} 

   // i n s t a n c e   v a r i a b l e s

   protected DataOutputStream d; // to get at high level write methods of DataOutputStream
   byte w[]; // work array for composing output
   protected boolean littleEndianMode = true;
} // end LEDataOutputStream