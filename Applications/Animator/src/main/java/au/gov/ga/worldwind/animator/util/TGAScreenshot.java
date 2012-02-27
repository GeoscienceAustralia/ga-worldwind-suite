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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.Screenshot;

/**
 * Helper class that saves the current OpenGL context as a TGA screenshot.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TGAScreenshot
{
	private static void checkExtABGR()
	{
		GL gl = GLU.getCurrentGL();
		if (!gl.isExtensionAvailable("GL_EXT_abgr"))
		{
			throw new IllegalArgumentException("Saving alpha channel requires GL_EXT_abgr");
		}
	}

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

		checkExtABGR();

		TGAWriter writer = new TGAWriter();
		writer.open(file, width, height, alpha);
		ByteBuffer bgr = writer.getImageData();

		GL gl = GLU.getCurrentGL();

		// Set up pixel storage modes
		PixelStorageModes psm = new PixelStorageModes();
		psm.save(gl);

		int readbackType = (alpha ? GL.GL_ABGR_EXT : GL.GL_BGR);

		// read the BGR values into the image buffer
		gl.glReadPixels(x, y, width, height, readbackType, GL.GL_UNSIGNED_BYTE, bgr);

		// Restore pixel storage modes
		psm.restore(gl);

		// close the file
		writer.close();
	}

	public static class TGAWriter
	{
		private static final int TARGA_HEADER_SIZE = 18;

		private FileChannel ch;
		private MappedByteBuffer image;
		private ByteBuffer buf;
		private boolean alpha;

		public TGAWriter()
		{
		}

		public void open(File file, int width, int height, boolean alpha) throws IOException
		{
			this.alpha = alpha;
			RandomAccessFile out = new RandomAccessFile(file, "rw");
			ch = out.getChannel();
			int pixelSize = (alpha ? 32 : 24);
			int numChannels = (alpha ? 4 : 3);

			int fileLength = TARGA_HEADER_SIZE + width * height * numChannels;
			out.setLength(fileLength);
			image = ch.map(FileChannel.MapMode.READ_WRITE, 0, fileLength);

			// write the TARGA header
			image.put(0, (byte) 0).put(1, (byte) 0);
			image.put(2, (byte) 2); // uncompressed type
			image.put(12, (byte) (width & 0xFF)); // width
			image.put(13, (byte) (width >> 8)); // width
			image.put(14, (byte) (height & 0xFF)); // height
			image.put(15, (byte) (height >> 8)); // height
			image.put(16, (byte) pixelSize); // pixel size

			// go to image data position
			image.position(TARGA_HEADER_SIZE);
			if (alpha)
				//create buffer for jogl to save pixels into
				buf = BufferUtil.newByteBuffer(width * height * numChannels);
			else
				// jogl needs a sliced buffer
				buf = image.slice();
		}

		public ByteBuffer getImageData()
		{
			return buf;
		}

		public void close() throws IOException
		{
			if (alpha)
			{
				buf.rewind();
				while (buf.hasRemaining())
				{
					image.put(image.position() + 3, buf.get());
					image.put(buf.get());
					image.put(buf.get());
					image.put(buf.get());
					image.get();
				}
			}

			// close the file channel
			ch.close();
			buf = null;
		}
	}

	private static int glGetInteger(GL gl, int pname, int[] tmp)
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

		void save(GL gl)
		{
			packAlignment = glGetInteger(gl, GL.GL_PACK_ALIGNMENT, tmp);
			packRowLength = glGetInteger(gl, GL.GL_PACK_ROW_LENGTH, tmp);
			packSkipRows = glGetInteger(gl, GL.GL_PACK_SKIP_ROWS, tmp);
			packSkipPixels = glGetInteger(gl, GL.GL_PACK_SKIP_PIXELS, tmp);
			packSwapBytes = glGetInteger(gl, GL.GL_PACK_SWAP_BYTES, tmp);

			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
			gl.glPixelStorei(GL.GL_PACK_ROW_LENGTH, 0);
			gl.glPixelStorei(GL.GL_PACK_SKIP_ROWS, 0);
			gl.glPixelStorei(GL.GL_PACK_SKIP_PIXELS, 0);
			gl.glPixelStorei(GL.GL_PACK_SWAP_BYTES, 0);
		}

		void restore(GL gl)
		{
			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, packAlignment);
			gl.glPixelStorei(GL.GL_PACK_ROW_LENGTH, packRowLength);
			gl.glPixelStorei(GL.GL_PACK_SKIP_ROWS, packSkipRows);
			gl.glPixelStorei(GL.GL_PACK_SKIP_PIXELS, packSkipPixels);
			gl.glPixelStorei(GL.GL_PACK_SWAP_BYTES, packSwapBytes);
		}
	}
}
