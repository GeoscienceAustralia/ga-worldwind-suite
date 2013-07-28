/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.Screenshot;

/**
 * Helper class that saves the current OpenGL context as a TGA screenshot.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TGAScreenshot
{
	public static void writeToTargaFile(File file, int width, int height) throws GLException, IOException
	{
		writeToTargaFile(file, width, height, false);
	}

	public static void writeToTargaFile(File file, int width, int height, boolean alpha) throws GLException,
			IOException
	{
		writeToTargaFile(file, 0, 0, width, height, alpha);
	}

	public static void writeToTargaFile(File file, int x, int y, int width, int height, boolean alpha)
			throws GLException, IOException
	{
		if (!alpha)
		{
			Screenshot.writeToTargaFile(file, x, y, width, height, alpha);
			return;
		}

		TGAWriter writer = new TGAWriter();
		writer.open(file, width, height, alpha);
		ByteBuffer buf = writer.getImageData();

		GL2 gl = GLContext.getCurrentGL().getGL2();

		// Set up pixel storage modes
		PixelStorageModes psm = new PixelStorageModes();
		psm.save(gl);

		int readbackType = (alpha ? GL2.GL_BGRA : GL2.GL_BGR);

		// read the BGR values into the image buffer
		gl.glReadPixels(x, y, width, height, readbackType, GL2.GL_UNSIGNED_BYTE, buf);

		// Restore pixel storage modes
		psm.restore(gl);

		// close the file
		writer.close();
	}

	public static class TGAWriter
	{
		private static final int TARGA_HEADER_SIZE = 18;

		private OutputStream os;
		private ByteBuffer buf;

		public TGAWriter()
		{
		}

		public void open(File file, int width, int height, boolean alpha) throws IOException
		{
			os = new FileOutputStream(file);
			int pixelSize = (alpha ? 32 : 24);
			int numChannels = (alpha ? 4 : 3);

			// write the TARGA header
			byte[] header = new byte[TARGA_HEADER_SIZE];
			header[2] = 2;
			header[12] = (byte) (width & 0xFF);
			header[13] = (byte) (width >> 8);
			header[14] = (byte) (height & 0xFF);
			header[15] = (byte) (height >> 8);
			header[16] = (byte) pixelSize;
			os.write(header);

			//create buffer for jogl to save pixels into
			buf = Buffers.newDirectByteBuffer(width * height * numChannels);
		}

		public ByteBuffer getImageData()
		{
			return buf;
		}

		public void close() throws IOException
		{
			WritableByteChannel channel = Channels.newChannel(os);
			buf.rewind();
			channel.write(buf);
			channel.close();
			os.close();
			buf = null;
		}
	}

	private static int glGetInteger(GL2 gl, int pname, int[] tmp)
	{
		gl.glGetIntegerv(pname, tmp, 0);
		return tmp[0];
	}

	private static class PixelStorageModes
	{
		int packAlignment;
		int packRowLength;
		int packSkipRows;
		int packSkipPixels;
		int packSwapBytes;
		int[] tmp = new int[1];

		void save(GL2 gl)
		{
			packAlignment = glGetInteger(gl, GL2.GL_PACK_ALIGNMENT, tmp);
			packRowLength = glGetInteger(gl, GL2.GL_PACK_ROW_LENGTH, tmp);
			packSkipRows = glGetInteger(gl, GL2.GL_PACK_SKIP_ROWS, tmp);
			packSkipPixels = glGetInteger(gl, GL2.GL_PACK_SKIP_PIXELS, tmp);
			packSwapBytes = glGetInteger(gl, GL2.GL_PACK_SWAP_BYTES, tmp);

			gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
			gl.glPixelStorei(GL2.GL_PACK_ROW_LENGTH, 0);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_ROWS, 0);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_PIXELS, 0);
			gl.glPixelStorei(GL2.GL_PACK_SWAP_BYTES, 0);
		}

		void restore(GL2 gl)
		{
			gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, packAlignment);
			gl.glPixelStorei(GL2.GL_PACK_ROW_LENGTH, packRowLength);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_ROWS, packSkipRows);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_PIXELS, packSkipPixels);
			gl.glPixelStorei(GL2.GL_PACK_SWAP_BYTES, packSwapBytes);
		}
	}
}
