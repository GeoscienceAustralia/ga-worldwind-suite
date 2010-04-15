package tiler;

import gdal.GDALTile;
import gdal.GDALUtil;

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
import util.NumberArray;
import util.ProgressReporter;
import util.Sector;
import util.FileFilters.DirectoryFileFilter;
import util.FileFilters.ExtensionFileFilter;

public class Overviewer
{
	public static void createImageOverviews(File directory, String extension,
			int width, int height, NumberArray outsideValues, Sector sector,
			double lzts, ProgressReporter reporter)
	{
		OverviewCreator overviewCreator = new ImageOverviewCreator(width,
				height, outsideValues);
		createOverviews(overviewCreator, directory, extension, sector, lzts,
				reporter);
	}

	public static void createElevationOverviews(File directory, int width,
			int height, int bufferType, ByteOrder byteOrder,
			NumberArray outsideValues, Sector sector, double lzts,
			ProgressReporter reporter)
	{
		int bands = 1;
		OverviewCreator overviewCreator = new ElevationOverviewCreator(width,
				height, bands, bufferType, byteOrder, outsideValues);
		createOverviews(overviewCreator, directory, "bil", sector, lzts,
				reporter);
	}

	private static void createOverviews(OverviewCreator overviewCreator,
			File directory, String extension, Sector sector, double lzts,
			ProgressReporter progress)
	{
		progress.getLogger().info("Generating overviews...");

		if (directory.isDirectory())
		{
			// fix extension
			if (extension.startsWith("."))
			{
				extension = extension.substring(1);
			}

			File[] dirs = directory.listFiles(new DirectoryFileFilter());
			int maxlevel = Integer.MIN_VALUE;
			for (int i = 0; i < dirs.length; i++)
			{
				try
				{
					int num = Integer.parseInt(dirs[i].getName());
					if (num > maxlevel)
					{
						maxlevel = num;
					}
				}
				catch (NumberFormatException e)
				{
				}
			}

			int count = 0;
			int size = 0;
			for (int i = 0; i < maxlevel; i++)
			{
				size += GDALUtil.tileCount(sector, i, lzts);
			}

			for (int level = maxlevel; level > 0; level--)
			{
				if (progress.isCancelled())
					break;

				// level directory
				File dir = new File(directory.getAbsolutePath() + "/" + level);

				// create a list of files to process
				ExtensionFileFilter fileFilter = new ExtensionFileFilter(
						extension);
				Set<File> sourceFiles = new HashSet<File>();
				FileUtil.recursivelyAddFiles(sourceFiles, dir, fileFilter);

				while (!sourceFiles.isEmpty())
				{
					if (progress.isCancelled())
						break;

					count++;
					progress.getLogger().fine(
							"Overview " + count + "/" + size + " ("
									+ (count * 100 / size) + "%)");
					progress.progress(count / (double) size);

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
					final File src1 = tileFile(dir, extension,
							rowabove * 2 + 1, colabove * 2);
					final File src2 = tileFile(dir, extension, rowabove * 2,
							colabove * 2 + 1);
					final File src3 = tileFile(dir, extension,
							rowabove * 2 + 1, colabove * 2 + 1);

					sourceFiles.remove(src0);
					sourceFiles.remove(src1);
					sourceFiles.remove(src2);
					sourceFiles.remove(src3);

					final File dst = tileFile(new File(dir.getParent() + "/"
							+ (level - 1)), extension, rowabove, colabove);
					dst.getParentFile().mkdirs();
					if (dst.exists())
					{
						progress.getLogger().warning(
								dst.getAbsolutePath() + " already exists");
					}
					else
					{
						try
						{
							overviewCreator.mix(src0, src1, src2, src3, dst);
						}
						catch (IOException e)
						{
							progress.getLogger().severe(e.getMessage());
						}
					}
				}
			}
		}

		progress.getLogger().info(
				"Overview generation "
						+ (progress.isCancelled() ? "cancelled" : "complete"));
	}

	private static File tileFile(File dir, String extension, int row, int col)
	{
		return new File(dir.getAbsolutePath() + "/"
				+ FileUtil.paddedInt(row, 4) + "/" + FileUtil.paddedInt(row, 4)
				+ "_" + FileUtil.paddedInt(col, 4) + "." + extension);
	}

	private interface OverviewCreator
	{
		void mix(File src0, File src1, File src2, File src3, File dst)
				throws IOException;
	}

	private static class ImageOverviewCreator implements OverviewCreator
	{
		private BufferedImage outsideImage;
		private int width;
		private int height;

		public ImageOverviewCreator(int width, int height,
				NumberArray outsideValues)
		{
			this.width = width;
			this.height = height;

			// create image for tiles outside extents (tiles that don't
			// exist)

			if (outsideValues != null)
			{
				int bandCount = outsideValues.length();
				int[] offsets = new int[bandCount];

				byte[] bytes = new byte[bandCount * width * height];
				ByteBuffer buffer = ByteBuffer.wrap(bytes);

				for (int b = 0; b < bandCount; b++)
				{
					offsets[b] = b * width * height; // * bufferTypeSize;
					for (int i = 0; i < width * height; i++)
					{
						int index = offsets[b] + i;
						buffer.put(index, outsideValues.getByte(b));
					}
				}

				DataBuffer db = new DataBufferByte(bytes, bytes.length);
				SampleModel sampleModel = new ComponentSampleModel(
						DataBuffer.TYPE_BYTE, width, height, 1, width, offsets);
				WritableRaster raster = Raster.createWritableRaster(
						sampleModel, db, null);
				int imageType = bandCount == 1 ? BufferedImage.TYPE_BYTE_GRAY
						: bandCount == 3 ? BufferedImage.TYPE_INT_RGB
								: BufferedImage.TYPE_INT_ARGB_PRE;
				outsideImage = new BufferedImage(width, height, imageType);
				outsideImage.setData(raster);
			}
		}

		@Override
		public void mix(File src0, File src1, File src2, File src3, File dst)
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
			int type = image != null && image.getType() != 0 ? image.getType()
					: BufferedImage.TYPE_INT_ARGB;

			// TODO warn if image == outsideImage (no src files exist)

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

			// +--+--+ 0
			// |i1|i3|
			// +--+--+ h2
			// |i0|i2|
			// +--+--+ h
			// 0 w2 w

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
	}

	private static class ElevationOverviewCreator implements OverviewCreator
	{
		private boolean floatingPoint;
		private int bufferTypeSize;
		private NumberArray outsideValues;
		private ByteOrder byteOrder;
		private int width;
		private int height;
		private int bands;
		private TypeHandler typeHandler;

		public ElevationOverviewCreator(int width, int height, int bands,
				int bufferType, ByteOrder byteOrder, NumberArray outsideValues)
		{
			floatingPoint = GDALTile.isTypeFloatingPoint(bufferType);

			bufferTypeSize = gdal.GetDataTypeSize(bufferType);
			if (!((!floatingPoint && (bufferTypeSize == 8 || bufferTypeSize == 16))
					|| (floatingPoint && bufferTypeSize == 64) || (bufferTypeSize == 32)))
				throw new IllegalArgumentException("Illegal buffer type");
			bufferTypeSize /= 8;

			if (width % 2 != 0 || height % 2 != 0)
				throw new IllegalArgumentException(
						"Width/Height must be multiples of 2");

			if (outsideValues != null && outsideValues.length() != bands)
				throw new IllegalArgumentException(
						"Outside values array length doesn't equal the number of bands");

			typeHandler = createTypeHandler();

			this.outsideValues = outsideValues;
			this.byteOrder = byteOrder;
			this.width = width;
			this.height = height;
			this.bands = bands;
		}

		private TypeHandler createTypeHandler()
		{
			switch (bufferTypeSize)
			{
			case 1:
				return new ByteTypeHandler();
			case 2:
				return new ShortTypeHandler();
			case 4:
				return floatingPoint ? new FloatTypeHandler()
						: new IntTypeHandler();
			case 8:
				return new DoubleTypeHandler();
			}
			return null;
		}

		@Override
		public void mix(File src0, File src1, File src2, File src3, File dst)
				throws IOException
		{
			if (dst.exists())
				throw new IllegalArgumentException("Destination already exists");

			RandomAccessFile dstraf = null;
			FileInputStream src0fis = null, src1fis = null, src2fis = null, src3fis = null;
			try
			{
				src0 = src0 != null && src0.exists() ? src0 : null;
				src1 = src1 != null && src1.exists() ? src1 : null;
				src2 = src2 != null && src2.exists() ? src2 : null;
				src3 = src3 != null && src3.exists() ? src3 : null;

				long length = src0 != null ? src0.length()
						: src1 != null ? src1.length() : src2 != null ? src2
								.length() : src3 != null ? src3.length() : -1;

				if ((src1 != null && src1.length() != length)
						|| (src2 != null && src2.length() != length)
						|| (src3 != null && src3.length() != length))
					throw new IllegalArgumentException(
							"Source file(s) don't have equal sizes");

				if (length > 0
						&& length != width * height * bands * bufferTypeSize)
					throw new IllegalArgumentException(
							"Source file(s) have an invalid size");

				dstraf = new RandomAccessFile(dst, "rw");
				FileChannel dstfc = dstraf.getChannel();
				MappedByteBuffer dstbb = dstfc.map(MapMode.READ_WRITE, 0, width
						* height * bands * bufferTypeSize);
				dstbb.order(byteOrder);

				src0fis = src0 != null ? new FileInputStream(src0) : null;
				src1fis = src1 != null ? new FileInputStream(src1) : null;
				src2fis = src2 != null ? new FileInputStream(src2) : null;
				src3fis = src3 != null ? new FileInputStream(src3) : null;

				ByteBuffer src0bb = src0fis != null ? getFileChannelAsByteBuffer(
						src0fis.getChannel(), (int) src0.length(), byteOrder)
						: null;
				ByteBuffer src1bb = src1fis != null ? getFileChannelAsByteBuffer(
						src1fis.getChannel(), (int) src1.length(), byteOrder)
						: null;
				ByteBuffer src2bb = src2fis != null ? getFileChannelAsByteBuffer(
						src2fis.getChannel(), (int) src2.length(), byteOrder)
						: null;
				ByteBuffer src3bb = src3fis != null ? getFileChannelAsByteBuffer(
						src3fis.getChannel(), (int) src3.length(), byteOrder)
						: null;

				for (int b = 0; b < bands; b++)
				{
					int offset = b * width * height * bufferTypeSize;
					Object outsideValue = outsideValues == null ? null
							: typeHandler.getNumberArrayValue(b, outsideValues);

					for (int y = 0; y < height; y++)
					{
						int sy = y % (height / 2);
						for (int x = 0; x < width; x++)
						{
							int sx = x % (width / 2);
							ByteBuffer buffer = selectBuffer(src0bb, src1bb,
									src2bb, src3bb, x, y, width, height);
							Object value = outsideValue;
							if (buffer != null)
							{
								int index0 = ((sy * 2) * width + (sx * 2))
										* bufferTypeSize + offset;
								int index1 = ((sy * 2) * width + (sx * 2 + 1))
										* bufferTypeSize + offset;
								int index2 = ((sy * 2 + 1) * width + sx * 2)
										* bufferTypeSize + offset;
								int index3 = ((sy * 2 + 1) * width + (sx * 2 + 1))
										* bufferTypeSize + offset;

								Object v0 = typeHandler.getBufferValue(index0,
										buffer);
								Object v1 = typeHandler.getBufferValue(index1,
										buffer);
								Object v2 = typeHandler.getBufferValue(index2,
										buffer);
								Object v3 = typeHandler.getBufferValue(index3,
										buffer);

								if (value == null
										|| !(v0 == value || v1 == value
												|| v2 == value || v3 == value))
								{
									value = typeHandler.average(v0, v1, v2, v3);
								}
							}
							if (value == null)
								dstbb.position(dstbb.position()
										+ bufferTypeSize);
							else
								typeHandler.putBufferValue(dstbb, value);
						}
					}
				}
			}
			finally
			{
				if (dstraf != null)
					dstraf.close();
				if (src0fis != null)
					src0fis.close();
				if (src1fis != null)
					src1fis.close();
				if (src2fis != null)
					src2fis.close();
				if (src3fis != null)
					src3fis.close();
			}
		}

		private ByteBuffer getFileChannelAsByteBuffer(FileChannel fileChannel,
				int length, ByteOrder byteOrder) throws IOException
		{
			ByteBuffer bb = ByteBuffer.allocate(length);
			bb.order(byteOrder);
			fileChannel.read(bb);
			bb.rewind();
			return bb;
		}

		private <E extends Buffer> E selectBuffer(E src0, E src1, E src2,
				E src3, int x, int y, int w, int h)
		{
			// +--+--+
			// |i1|i3|
			// +--+--+
			// |i0|i2|
			// +--+--+
			return (x < w / 2) ? (y < h / 2 ? src1 : src0) : (y < h / 2 ? src3
					: src2);
		}
	}

	private interface TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na);

		public Object getBufferValue(int index, ByteBuffer buffer);

		public void putBufferValue(ByteBuffer buffer, Object value);

		public Object average(Object v0, Object v1, Object v2, Object v3);
	}

	private static class ByteTypeHandler implements TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na)
		{
			return na.getByte(index);
		}

		public Object getBufferValue(int index, ByteBuffer buffer)
		{
			return buffer.get(index);
		}

		public void putBufferValue(ByteBuffer buffer, Object value)
		{
			buffer.put((Byte) value);
		}

		public Object average(Object v0, Object v1, Object v2, Object v3)
		{
			return (byte) ((((Byte) v0).shortValue() + ((Byte) v1).shortValue()
					+ ((Byte) v2).shortValue() + ((Byte) v3).shortValue()) / (short) 4);
		}
	}

	private static class ShortTypeHandler implements TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na)
		{
			return na.getShort(index);
		}

		public Object getBufferValue(int index, ByteBuffer buffer)
		{
			return buffer.getShort(index);
		}

		public void putBufferValue(ByteBuffer buffer, Object value)
		{
			buffer.putShort((Short) value);
		}

		public Object average(Object v0, Object v1, Object v2, Object v3)
		{
			return (short) ((((Short) v0).intValue() + ((Short) v1).intValue()
					+ ((Short) v2).intValue() + ((Short) v3).intValue()) / 4);
		}
	}

	private static class IntTypeHandler implements TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na)
		{
			return na.getInt(index);
		}

		public Object getBufferValue(int index, ByteBuffer buffer)
		{
			return buffer.getInt(index);
		}

		public void putBufferValue(ByteBuffer buffer, Object value)
		{
			buffer.putInt((Integer) value);
		}

		public Object average(Object v0, Object v1, Object v2, Object v3)
		{
			return (int) ((((Integer) v0).longValue()
					+ ((Integer) v1).longValue() + ((Integer) v2).longValue() + ((Integer) v3)
					.longValue()) / 4l);
		}
	}

	private static class FloatTypeHandler implements TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na)
		{
			return na.getFloat(index);
		}

		public Object getBufferValue(int index, ByteBuffer buffer)
		{
			return buffer.getFloat(index);
		}

		public void putBufferValue(ByteBuffer buffer, Object value)
		{
			buffer.putFloat((Float) value);
		}

		public Object average(Object v0, Object v1, Object v2, Object v3)
		{
			return (float) ((((Float) v0).doubleValue()
					+ ((Float) v1).doubleValue() + ((Float) v2).doubleValue() + ((Float) v3)
					.doubleValue()) / 4d);
		}
	}

	private static class DoubleTypeHandler implements TypeHandler
	{
		public Object getNumberArrayValue(int index, NumberArray na)
		{
			return na.getDouble(index);
		}

		public Object getBufferValue(int index, ByteBuffer buffer)
		{
			return buffer.getDouble(index);
		}

		public void putBufferValue(ByteBuffer buffer, Object value)
		{
			buffer.putDouble((Double) value);
		}

		public Object average(Object v0, Object v1, Object v2, Object v3)
		{
			return ((Double) v0 + (Double) v1 + (Double) v2 + (Double) v3) / 4d;
		}
	}
}
