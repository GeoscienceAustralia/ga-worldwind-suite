package util;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import util.FileFilters.DirectoryFileFilter;
import util.FileFilters.ExtensionFileFilter;

public class Overviewer
{
	public static void createImageOverviews(File directory, String extension,
			int width, int height, boolean saveAlpha, int threadCount)
	{
		createOverviews(directory, extension, width, height, threadCount,
				saveAlpha, 1, null, 0, false);
	}

	public static void createBilOverviews(File directory, int width,
			int height, int threadCount, int bufferTypeSize,
			ByteOrder byteOrder, long nodataValue)
	{
		createOverviews(directory, "bil", width, height, threadCount, false,
				bufferTypeSize, byteOrder, nodataValue, true);
	}

	private static void createOverviews(File directory, String extension,
			int width, int height, int threadCount, boolean saveAlpha,
			int bufferTypeSize, ByteOrder byteOrder, long nodataValue,
			boolean bils)
	{
		if (directory.isDirectory())
		{
			File[] dirs = directory.listFiles(new DirectoryFileFilter());
			int toplevel = Integer.MIN_VALUE;
			for (int i = 0; i < dirs.length; i++)
			{
				try
				{
					int num = Integer.parseInt(dirs[i].getName());
					if (num > toplevel)
					{
						toplevel = num;
					}
				}
				catch (NumberFormatException e)
				{
				}
			}
			while (toplevel > 0)
			{
				File dir = new File(directory.getAbsolutePath() + "/"
						+ toplevel);
				processDirectory(toplevel, dir, extension, threadCount, width,
						height, saveAlpha, bufferTypeSize, byteOrder,
						nodataValue, bils);
				toplevel--;
			}
		}
	}

	public static void main(String[] args)
	{
		short[] minmax = findShortMinMax(new File(
				"D:/SW Margins/sonne/tiledB/8"), (short) -9999,
				Short.MIN_VALUE, (short) -1);
		System.out.println("Min = " + minmax[0]);
		System.out.println("Max = " + minmax[1]);
	}

	public static short[] findShortMinMax(File dir, short nodataValue,
			short ignoreMin, short ignoreMax)
	{
		short min = Short.MAX_VALUE;
		short max = Short.MIN_VALUE;

		String extension = "bil";
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(extension);
		List<File> sourceFiles = new ArrayList<File>();
		FileUtil.recursivelyAddFiles(sourceFiles, dir, fileFilter);

		int count = sourceFiles.size(), done = 0;
		for (File file : sourceFiles)
		{
			System.out.println("Processing " + file + " (" + (++done) + "/"
					+ count + " - " + (done * 100 / count) + "%) " + min + ","
					+ max);
			try
			{
				FileChannel fc = new FileInputStream(file).getChannel();
				ByteBuffer bb = ByteBuffer.allocate((int) file.length());
				bb.order(ByteOrder.LITTLE_ENDIAN);
				fc.read(bb);
				bb.rewind();
				ShortBuffer sb = bb.asShortBuffer();
				for (int i = 0; i < sb.limit(); i++)
				{
					short current = sb.get();
					if (current == nodataValue)
						continue;
					if (current < ignoreMin || current > ignoreMax)
						continue;
					min = current < min ? current : min;
					max = current > max ? current : max;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return new short[] { min, max };
	}

	private static void processDirectory(int level, File dir, String extension,
			int threadCount, final int width, final int height,
			final boolean saveAlpha, final int bufferTypeSize,
			final ByteOrder byteOrder, final long nodataValue,
			final boolean bils)
	{
		if (extension.startsWith("."))
		{
			extension = extension.substring(1);
		}
		final String ext = extension;

		ExtensionFileFilter fileFilter = new ExtensionFileFilter(extension);
		Set<File> sourceFiles = new HashSet<File>();
		FileUtil.recursivelyAddFiles(sourceFiles, dir, fileFilter);

		int count = sourceFiles.size();
		int done = 0;

		final Object semaphore = new Object();
		ThreadPoolExecutor executor = threadCount <= 1 ? null
				: new ThreadPoolExecutor(threadCount, threadCount, 5,
						TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())
				{
					@Override
					protected void afterExecute(Runnable r, Throwable t)
					{
						super.afterExecute(r, t);
						synchronized (semaphore)
						{
							semaphore.notifyAll();
						}
					}
				};

		while (!sourceFiles.isEmpty())
		{
			File file = sourceFiles.iterator().next();
			String path = file.getName();
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(path);
			matcher.find();
			int row = Integer.parseInt(matcher.group());
			matcher.find(matcher.end());
			int col = Integer.parseInt(matcher.group());

			int rowabove = row / 2;
			int colabove = col / 2;

			final File src0 = tileFile(dir, extension, rowabove * 2,
					colabove * 2);
			final File src1 = tileFile(dir, extension, rowabove * 2 + 1,
					colabove * 2);
			final File src2 = tileFile(dir, extension, rowabove * 2,
					colabove * 2 + 1);
			final File src3 = tileFile(dir, extension, rowabove * 2 + 1,
					colabove * 2 + 1);

			if (sourceFiles.remove(src0))
				done++;
			if (sourceFiles.remove(src1))
				done++;
			if (sourceFiles.remove(src2))
				done++;
			if (sourceFiles.remove(src3))
				done++;

			final File dst = tileFile(new File(dir.getParent() + "/"
					+ (level - 1)), extension, rowabove, colabove);
			dst.getParentFile().mkdirs();
			if (dst.exists())
			{
				System.out.println(dst + " already exists");
			}
			else
			{
				if (executor != null
						&& executor.getActiveCount() >= threadCount)
				{
					synchronized (semaphore)
					{
						try
						{
							semaphore.wait();
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}

				System.out.println("Creating " + dst + " (" + done + "/"
						+ count + " - " + (done * 100 / count) + "%)");
				Runnable runnable = new Runnable()
				{
					public void run()
					{
						try
						{
							if (ext.toLowerCase().equals("bil"))
								mixBils(src0, src1, src2, src3, dst, width,
										height, bufferTypeSize, byteOrder,
										nodataValue);
							else
								mixImages(src0, src1, src2, src3, dst, width,
										height, saveAlpha);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				};
				if (executor != null)
					executor.execute(runnable);
				else
					runnable.run();
			}
		}

		if (executor != null)
		{
			executor.shutdown();
			try
			{
				executor.awaitTermination(60, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static File tileFile(File dir, String extension, int row, int col)
	{
		return new File(dir.getAbsolutePath() + "/"
				+ FileUtil.paddedInt(row, 4) + "/" + FileUtil.paddedInt(row, 4)
				+ "_" + FileUtil.paddedInt(col, 4) + "." + extension);
	}

	public static void mixImages(File src0, File src1, File src2, File src3,
			File dst, int width, int height, boolean saveAlpha)
			throws IOException
	{
		if (dst.exists())
			throw new IllegalArgumentException("Destination already exists");

		BufferedImage i0 = src0.exists() ? ImageIO.read(src0) : null;
		BufferedImage i1 = src1.exists() ? ImageIO.read(src1) : null;
		BufferedImage i2 = src2.exists() ? ImageIO.read(src2) : null;
		BufferedImage i3 = src3.exists() ? ImageIO.read(src3) : null;

		int w = width;
		int h = height;
		int w2 = w / 2;
		int h2 = h / 2;

		BufferedImage id = new BufferedImage(w, h,
				saveAlpha ? BufferedImage.TYPE_INT_ARGB
						: BufferedImage.TYPE_INT_RGB);
		Graphics2D g = id.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		if (saveAlpha)
		{
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR,
					0.0f));
			g.fillRect(0, 0, w, h);
			g.setComposite(c);
		}

		//+--+--+ 0
		//|i1|i3|
		//+--+--+ h2
		//|i0|i2|
		//+--+--+ h
		//0  w2 w

		if (i0 != null)
			g.drawImage(i0, 0, h2, w2, h, 0, 0, w, h, null);
		if (i1 != null)
			g.drawImage(i1, 0, 0, w2, h2, 0, 0, w, h, null);
		if (i2 != null)
			g.drawImage(i2, w2, h2, w, h, 0, 0, w, h, null);
		if (i3 != null)
			g.drawImage(i3, w2, 0, w, h2, 0, 0, w, h, null);

		g.dispose();

		dst.getParentFile().mkdirs();
		String imageformat = dst.getName().substring(
				dst.getName().lastIndexOf('.') + 1).toLowerCase();
		ImageIO.write(id, imageformat, dst);
	}

	public static void mixBils(File src0, File src1, File src2, File src3,
			File dst, int width, int height, int bufferTypeSize,
			ByteOrder byteOrder, long nodataValue) throws IOException
	{
		if (!(bufferTypeSize == 1 || bufferTypeSize == 2 || bufferTypeSize == 4))
			throw new IllegalArgumentException("Buffer type size unknown");
		if (dst.exists())
			throw new IllegalArgumentException("Destination already exists");

		FileChannel dstfc = null, src0fc = null, src1fc = null, src2fc = null, src3fc = null;
		try
		{
			dstfc = new RandomAccessFile(dst, "rw").getChannel();
			MappedByteBuffer dstbb = dstfc.map(MapMode.READ_WRITE, 0, width
					* height * bufferTypeSize);
			dstbb.order(byteOrder);

			ByteBuffer src0bb = null, src1bb = null, src2bb = null, src3bb = null;
			if (src0 != null && src0.exists())
			{
				src0fc = new FileInputStream(src0).getChannel();
				src0bb = ByteBuffer.allocate((int) src0.length());
				src0bb.order(byteOrder);
				src0fc.read(src0bb);
				src0bb.rewind();
			}
			if (src1 != null && src1.exists())
			{
				src1fc = new FileInputStream(src1).getChannel();
				src1bb = ByteBuffer.allocate((int) src1.length());
				src1bb.order(byteOrder);
				src1fc.read(src1bb);
				src1bb.rewind();
			}
			if (src2 != null && src2.exists())
			{
				src2fc = new FileInputStream(src2).getChannel();
				src2bb = ByteBuffer.allocate((int) src2.length());
				src2bb.order(byteOrder);
				src2fc.read(src2bb);
				src2bb.rewind();
			}
			if (src3 != null && src3.exists())
			{
				src3fc = new FileInputStream(src3).getChannel();
				src3bb = ByteBuffer.allocate((int) src3.length());
				src3bb.order(byteOrder);
				src3fc.read(src3bb);
				src3bb.rewind();
			}

			switch (bufferTypeSize)
			{
				case 1:
					mixByte(src0bb, src1bb, src2bb, src3bb, dstbb, width,
							height, 1, new byte[] { (byte) nodataValue });
					break;
				case 2:
					ShortBuffer src0sb = (src0bb != null) ? src0bb
							.asShortBuffer() : null;
					ShortBuffer src1sb = (src1bb != null) ? src1bb
							.asShortBuffer() : null;
					ShortBuffer src2sb = (src2bb != null) ? src2bb
							.asShortBuffer() : null;
					ShortBuffer src3sb = (src3bb != null) ? src3bb
							.asShortBuffer() : null;
					mixShort(src0sb, src1sb, src2sb, src3sb, dstbb
							.asShortBuffer(), width, height, 1,
							new short[] { (short) nodataValue });
					break;
				case 4:
					IntBuffer src0ib = (src0bb != null) ? src0bb.asIntBuffer()
							: null;
					IntBuffer src1ib = (src1bb != null) ? src1bb.asIntBuffer()
							: null;
					IntBuffer src2ib = (src2bb != null) ? src2bb.asIntBuffer()
							: null;
					IntBuffer src3ib = (src3bb != null) ? src3bb.asIntBuffer()
							: null;
					mixInt(src0ib, src1ib, src2ib, src3ib, dstbb.asIntBuffer(),
							width, height, 1, new int[] { (int) nodataValue });
					break;
			}
		}
		finally
		{
			if (dstfc != null)
				dstfc.close();
			if (src0fc != null)
				src0fc.close();
			if (src1fc != null)
				src1fc.close();
			if (src2fc != null)
				src2fc.close();
			if (src3fc != null)
				src3fc.close();
		}
	}

	private static void checkParameters(Buffer src0, Buffer src1, Buffer src2,
			Buffer src3, Buffer dst, int width, int height, int bands)
	{
		if (width % 2 != 0 || height % 2 != 0)
			throw new IllegalArgumentException(
					"Width and height must be a factor of 2");
		if ((src0 != null && src0.limit() != width * height * bands)
				|| (src1 != null && src1.limit() != width * height * bands)
				|| (src2 != null && src2.limit() != width * height * bands)
				|| (src3 != null && src3.limit() != width * height * bands))
			throw new IllegalArgumentException(
					"Source buffer size doesn't match parameters");
		if (dst.limit() != width * height * bands)
			throw new IllegalArgumentException(
					"Destination buffer size doesn't match parameters");
	}

	private static <E extends Buffer> E selectBuffer(E src0, E src1, E src2,
			E src3, int x, int y, int w, int h)
	{
		//+--+--+
		//|i1|i3|
		//+--+--+
		//|i0|i2|
		//+--+--+
		return (x < w / 2) ? (y < h / 2 ? src1 : src0) : (y < h / 2 ? src3
				: src2);
	}

	private static void mixByte(ByteBuffer src0, ByteBuffer src1,
			ByteBuffer src2, ByteBuffer src3, ByteBuffer dst, int width,
			int height, int bands, byte[] nodataValues)
	{
		checkParameters(src0, src1, src2, src3, dst, width, height, bands);
		if (nodataValues.length != bands)
			throw new IllegalArgumentException(
					"nodata array does not equal the number of bands");

		for (int b = 0; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				int sy = y % (height / 2);
				for (int x = 0; x < width; x++)
				{
					int sx = x % (width / 2);
					ByteBuffer buffer = selectBuffer(src0, src1, src2, src3, x,
							y, width, height);
					if (buffer == null)
					{
						dst.put(nodataValues[b]);
					}
					else
					{
						byte v0 = buffer.get(b * width * height + (sy * 2)
								* width + (sx * 2));
						byte v1 = buffer.get(b * width * height + (sy * 2)
								* width + (sx * 2 + 1));
						byte v2 = buffer.get(b * width * height + (sy * 2 + 1)
								* width + sx * 2);
						byte v3 = buffer.get(b * width * height + (sy * 2 + 1)
								* width + (sx * 2 + 1));
						if (nodataValues != null
								&& (v0 == nodataValues[b]
										|| v1 == nodataValues[b]
										|| v2 == nodataValues[b] || v3 == nodataValues[b]))
						{
							dst.put(nodataValues[b]);
						}
						else
						{
							dst.put((byte) ((v0 + v1 + v2 + v3) / 4));
						}
					}
				}
			}
		}
	}

	private static void mixShort(ShortBuffer src0, ShortBuffer src1,
			ShortBuffer src2, ShortBuffer src3, ShortBuffer dst, int width,
			int height, int bands, short[] nodataValues)
	{
		checkParameters(src0, src1, src2, src3, dst, width, height, bands);
		if (nodataValues.length != bands)
			throw new IllegalArgumentException(
					"nodata array does not equal the number of bands");

		for (int b = 0; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				int sy = (y % (height / 2)) * 2;
				for (int x = 0; x < width; x++)
				{
					int sx = (x % (width / 2)) * 2;
					ShortBuffer buffer = selectBuffer(src0, src1, src2, src3,
							x, y, width, height);
					if (buffer == null)
					{
						dst.put(nodataValues[b]);
					}
					else
					{
						short v0 = buffer.get(b * width * height + sy * width
								+ sx);
						short v1 = buffer.get(b * width * height + sy * width
								+ (sx + 1));
						short v2 = buffer.get(b * width * height + (sy + 1)
								* width + sx);
						short v3 = buffer.get(b * width * height + (sy + 1)
								* width + (sx + 1));
						if (nodataValues != null
								&& (v0 == nodataValues[b]
										|| v1 == nodataValues[b]
										|| v2 == nodataValues[b] || v3 == nodataValues[b]))
						{
							dst.put(nodataValues[b]);
						}
						else
						{
							dst.put((short) ((v0 + v1 + v2 + v3) / 4));
						}
					}
				}
			}
		}
	}

	private static void mixInt(IntBuffer src0, IntBuffer src1, IntBuffer src2,
			IntBuffer src3, IntBuffer dst, int width, int height, int bands,
			int[] nodataValues)
	{
		checkParameters(src0, src1, src2, src3, dst, width, height, bands);
		if (nodataValues.length != bands)
			throw new IllegalArgumentException(
					"nodata array does not equal the number of bands");

		for (int b = 0; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				int sy = y % (height / 2);
				for (int x = 0; x < width; x++)
				{
					int sx = x % (width / 2);
					IntBuffer buffer = selectBuffer(src0, src1, src2, src3, x,
							y, width, height);
					if (buffer == null)
					{
						dst.put(nodataValues[b]);
					}
					else
					{
						int v0 = buffer.get(b * width * height + (sy * 2)
								* width + (sx * 2));
						int v1 = buffer.get(b * width * height + (sy * 2)
								* width + (sx * 2 + 1));
						int v2 = buffer.get(b * width * height + (sy * 2 + 1)
								* width + sx * 2);
						int v3 = buffer.get(b * width * height + (sy * 2 + 1)
								* width + (sx * 2 + 1));
						if (nodataValues != null
								&& (v0 == nodataValues[b]
										|| v1 == nodataValues[b]
										|| v2 == nodataValues[b] || v3 == nodataValues[b]))
						{
							dst.put(nodataValues[b]);
						}
						else
						{
							dst.put((v0 + v1 + v2 + v3) / 4);
						}
					}
				}
			}
		}
	}
}
