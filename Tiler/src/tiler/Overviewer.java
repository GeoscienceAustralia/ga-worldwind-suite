package tiler;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.gdal.gdal.gdal;

import util.FileUtil;
import util.FileFilters.DirectoryFileFilter;
import util.FileFilters.ExtensionFileFilter;

public class Overviewer
{
	public static void createImageOverviews(File directory, String extension,
			int width, int height, int[] outsideValues)
	{
		createOverviews(false, directory, extension, width, height, 1, null,
				outsideValues);
	}

	public static void createElevationOverviews(File directory, int width,
			int height, int bufferType, ByteOrder byteOrder, int[] outsideValues)
	{
		createOverviews(true, directory, "bil", width, height, bufferType,
				byteOrder, outsideValues);
	}

	private static void createOverviews(boolean elevations, File directory,
			String extension, int width, int height, int bufferType,
			ByteOrder byteOrder, int[] outsideValues)
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
				processDirectory(elevations, toplevel, dir, extension, width,
						height, bufferType, byteOrder, outsideValues);
				toplevel--;
			}
		}
	}

	private static void processDirectory(boolean elevations, int level,
			File dir, String extension, int width, int height, int bufferType,
			ByteOrder byteOrder, int[] outsideValues)
	{
		//fix extension
		if (extension.startsWith("."))
		{
			extension = extension.substring(1);
		}

		//create a list of files to process
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(extension);
		Set<File> sourceFiles = new HashSet<File>();
		FileUtil.recursivelyAddFiles(sourceFiles, dir, fileFilter);

		//progress variables
		int count = sourceFiles.size();
		int done = 0;

		//calculate buffer type size
		int bufferTypeSize = 1;
		if (elevations)
		{
			bufferTypeSize = gdal.GetDataTypeSize(bufferType);
			if (bufferTypeSize != 1 && bufferTypeSize != 2
					&& bufferTypeSize != 4)
			{
				throw new IllegalArgumentException("Illegal buffer type");
			}
		}

		//create buffer/image for tiles outside extents (tiles that don't exist)
		ByteBuffer outsideBuffer = null;
		BufferedImage outsideImage = null;
		if (outsideValues != null)
		{
			int bandCount = outsideValues.length;
			int[] offsets = new int[bandCount];

			byte[] bytes = new byte[bandCount * width * height * bufferTypeSize];
			outsideBuffer = ByteBuffer.wrap(bytes);

			for (int b = 0; b < bandCount; b++)
			{
				offsets[b] = b * width * height * bufferTypeSize;
				for (int i = 0; i < width * height; i++)
				{
					int index = offsets[b] + i;
					switch (bufferTypeSize)
					{
						case 1:
							outsideBuffer.put(index, (byte) outsideValues[b]);
							break;
						case 2:
							outsideBuffer.putShort(index,
									(short) outsideValues[b]);
							break;
						case 4:
							outsideBuffer.putInt(index, outsideValues[b]);
							break;
					}
				}
			}

			if (!elevations)
			{
				DataBuffer buffer = new DataBufferByte(bytes, bytes.length);
				SampleModel sampleModel = new ComponentSampleModel(
						DataBuffer.TYPE_BYTE, width, height, 1, width, offsets);
				WritableRaster raster = Raster.createWritableRaster(
						sampleModel, buffer, null);
				int imageType = bandCount == 1 ? BufferedImage.TYPE_BYTE_GRAY
						: bandCount == 3 ? BufferedImage.TYPE_INT_RGB
								: BufferedImage.TYPE_INT_ARGB_PRE; //TODO should imagetype be premultiplied alpha?
				outsideImage = new BufferedImage(width, height, imageType);
				outsideImage.setData(raster);
			}
		}

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
				System.out.println("Creating " + dst + " (" + done + "/"
						+ count + " - " + (done * 100 / count) + "%)");
				try
				{
					if (elevations)
					{
						mixElevations(src0, src1, src2, src3, dst, width,
								height, bufferTypeSize, byteOrder,
								outsideValues);
					}
					else
					{
						mixImages(src0, src1, src2, src3, dst, width, height,
								outsideImage);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static File tileFile(File dir, String extension, int row, int col)
	{
		return new File(dir.getAbsolutePath() + "/"
				+ FileUtil.paddedInt(row, 4) + "/" + FileUtil.paddedInt(row, 4)
				+ "_" + FileUtil.paddedInt(col, 4) + "." + extension);
	}

	private static void mixImages(File src0, File src1, File src2, File src3,
			File dst, int width, int height, BufferedImage outsideImage)
			throws IOException
	{
		if (dst.exists())
			throw new IllegalArgumentException("Destination already exists");

		BufferedImage i0 = src0.exists() ? ImageIO.read(src0) : null;
		BufferedImage i1 = src1.exists() ? ImageIO.read(src1) : null;
		BufferedImage i2 = src2.exists() ? ImageIO.read(src2) : null;
		BufferedImage i3 = src3.exists() ? ImageIO.read(src3) : null;

		BufferedImage image = i0 != null ? i0 : i1 != null ? i1
				: i2 != null ? i2 : i3 != null ? i3 : outsideImage;
		int type = image != null ? image.getType()
				: BufferedImage.TYPE_INT_ARGB;

		//TODO warn if image == outsideImage (no src files exist)

		i0 = i0 != null ? i0 : outsideImage;
		i1 = i1 != null ? i1 : outsideImage;
		i2 = i2 != null ? i2 : outsideImage;
		i3 = i3 != null ? i3 : outsideImage;

		int w = width;
		int h = height;
		int w2 = w / 2;
		int h2 = h / 2;

		BufferedImage id = new BufferedImage(w, h, type);
		Graphics2D g = id.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		if (outsideImage == null
				&& (image == null || image.getColorModel().hasAlpha()))
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

	private static void mixElevations(File src0, File src1, File src2,
			File src3, File dst, int width, int height, int bufferTypeSize,
			ByteOrder byteOrder, int[] outsideValues) throws IOException
	{
		if (dst.exists())
			throw new IllegalArgumentException("Destination already exists");

		boolean useOutsideValue = outsideValues != null
				&& outsideValues.length > 0;
		int outsideValue = useOutsideValue ? outsideValues[0] : 0;

		FileChannel dstfc = null, src0fc = null, src1fc = null, src2fc = null, src3fc = null;
		try
		{
			dstfc = new RandomAccessFile(dst, "rw").getChannel();
			MappedByteBuffer dstbb = dstfc.map(MapMode.READ_WRITE, 0, width
					* height * bufferTypeSize);
			dstbb.order(byteOrder);

			src0 = src0 != null && src0.exists() ? src0 : null;
			src1 = src1 != null && src1.exists() ? src1 : null;
			src2 = src2 != null && src2.exists() ? src2 : null;
			src3 = src3 != null && src3.exists() ? src3 : null;

			src0fc = src0 != null ? new FileInputStream(src0).getChannel()
					: null;
			src1fc = src1 != null ? new FileInputStream(src1).getChannel()
					: null;
			src2fc = src2 != null ? new FileInputStream(src2).getChannel()
					: null;
			src3fc = src3 != null ? new FileInputStream(src3).getChannel()
					: null;

			ByteBuffer src0bb = src0fc != null ? getFileChannelAsByteBuffer(
					src0fc, (int) src0.length(), byteOrder) : null;
			ByteBuffer src1bb = src1fc != null ? getFileChannelAsByteBuffer(
					src1fc, (int) src1.length(), byteOrder) : null;
			ByteBuffer src2bb = src2fc != null ? getFileChannelAsByteBuffer(
					src2fc, (int) src2.length(), byteOrder) : null;
			ByteBuffer src3bb = src3fc != null ? getFileChannelAsByteBuffer(
					src3fc, (int) src3.length(), byteOrder) : null;

			/*src0bb = src0bb != null ? src0bb : outsideBuffer;
			src1bb = src1bb != null ? src1bb : outsideBuffer;
			src2bb = src2bb != null ? src2bb : outsideBuffer;
			src3bb = src3bb != null ? src3bb : outsideBuffer;*/

			checkParameters(src0bb, src1bb, src2bb, src3bb, dstbb, width,
					height);

			for (int y = 0; y < height; y++)
			{
				int sy = y % (height / 2);
				for (int x = 0; x < width; x++)
				{
					int sx = x % (width / 2);
					ByteBuffer buffer = selectBuffer(src0bb, src1bb, src2bb,
							src3bb, x, y, width, height);
					long value = useOutsideValue ? outsideValue : 0;
					if (buffer != null)
					{
						int index0 = (width * height + (sy * 2) * width + (sx * 2))
								* bufferTypeSize;
						int index1 = (width * height + (sy * 2) * width + (sx * 2 + 1))
								* bufferTypeSize;
						int index2 = (width * height + (sy * 2 + 1) * width + sx * 2)
								* bufferTypeSize;
						int index3 = (width * height + (sy * 2 + 1) * width + (sx * 2 + 1))
								* bufferTypeSize;
						long v0 = 0, v1 = 0, v2 = 0, v3 = 0;

						switch (bufferTypeSize)
						{
							case 1:
								v0 = buffer.get(index0);
								v1 = buffer.get(index1);
								v2 = buffer.get(index2);
								v3 = buffer.get(index3);
								break;
							case 2:
								v0 = buffer.getShort(index0);
								v1 = buffer.getShort(index1);
								v2 = buffer.getShort(index2);
								v3 = buffer.getShort(index3);
								break;
							case 4:
								v0 = buffer.getInt(index0);
								v1 = buffer.getInt(index1);
								v2 = buffer.getInt(index2);
								v3 = buffer.getInt(index3);
								break;
						}

						if (useOutsideValue
								&& (v0 == outsideValue || v1 == outsideValue
										|| v2 == outsideValue || v3 == outsideValue))
						{
							value = outsideValue;
						}
						else
						{
							value = (v0 + v1 + v2 + v3) / 4;
						}
					}

					switch (bufferTypeSize)
					{
						case 1:
							dstbb.put((byte) value);
							break;
						case 2:
							dstbb.putShort((byte) value);
							break;
						case 4:
							dstbb.putInt((byte) value);
							break;
					}
				}
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

	private static ByteBuffer getFileChannelAsByteBuffer(
			FileChannel fileChannel, int length, ByteOrder byteOrder)
			throws IOException
	{
		ByteBuffer bb = ByteBuffer.allocate(length);
		bb.order(byteOrder);
		fileChannel.read(bb);
		bb.rewind();
		return bb;
	}

	private static void checkParameters(Buffer src0, Buffer src1, Buffer src2,
			Buffer src3, Buffer dst, int width, int height)
	{
		if (width % 2 != 0 || height % 2 != 0)
			throw new IllegalArgumentException(
					"Width and height must be a factor of 2");
		if ((src0 != null && src0.limit() != width * height)
				|| (src1 != null && src1.limit() != width * height)
				|| (src2 != null && src2.limit() != width * height)
				|| (src3 != null && src3.limit() != width * height))
			throw new IllegalArgumentException(
					"Source buffer size doesn't match parameters");
		if (dst.limit() != width * height)
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

	/*private static void mixByte(ByteBuffer src0, ByteBuffer src1,
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
	}*/
}
