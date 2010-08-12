package au.gov.ga.worldwind.animator.layers.elevation.perpixel.shaded;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ElevationLayer;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedElevationModel;

import com.sun.opengl.util.texture.SubclassableTexture;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class ShadedElevationLayer extends ElevationLayer
{
	protected final static String CACHE_NAME_PREFIX = "Elevation Layer/";
	protected final static String FORMAT_SUFFIX = ".elev";

	protected double exaggeration = 1.0;
	protected Vec4 sunPosition = new Vec4(1, 1, 1);
	protected Vec4 sunPositionNormalized = sunPosition.normalize3();
	protected double bakedExaggeration = 100.0;
	protected double shaderMinElevation;
	protected double shaderMaxElevation;

	private int shaderProgram = -1;
	private int minElevationUniform;
	private int maxElevationUniform;
	private int minTexElevationUniform;
	private int maxTexElevationUniform;
	private int exaggerationUniform;
	private int bakedExaggerationUniform;
	private int opacityUniform;
	private int eyePositionUniform;
	private int sunPositionUniform;
	private int oldModelViewInverseUniform;

	public ShadedElevationLayer(ExtendedElevationModel elevationModel)
	{
		this(elevationModel, null);
	}

	public ShadedElevationLayer(ExtendedElevationModel elevationModel, Sector sector)
	{
		super(elevationModel, CACHE_NAME_PREFIX, FORMAT_SUFFIX, sector);
		shaderMinElevation = elevationModel.getMinElevation();
		shaderMaxElevation = elevationModel.getMaxElevation();
	}

	@Override
	protected void setupShader(DrawContext dc)
	{
		if (shaderProgram == -1)
		{
			initShader(dc);
		}

		GL gl = dc.getGL();
		gl.glUseProgram(shaderProgram);

		//double minElevation = ((ElevationTesselator) dc.getGlobe().getTessellator()).getMinElevation();
		//double maxElevation = ((ElevationTesselator) dc.getGlobe().getTessellator()).getMaxElevation();
		double minElevation = clamp(shaderMinElevation, minElevationClamp, maxElevationClamp);
		double maxElevation = clamp(shaderMaxElevation, minElevationClamp, maxElevationClamp);
		gl.glUniform1f(minElevationUniform, (float) minElevation);
		gl.glUniform1f(maxElevationUniform, (float) maxElevation);
		gl.glUniform1f(exaggerationUniform, (float) exaggeration);
		gl.glUniform1f(bakedExaggerationUniform, (float) bakedExaggeration);
		gl.glUniform1f(opacityUniform, (float) getOpacity());

		Matrix modelViewInv = dc.getView().getModelviewMatrix().getInverse();
		float[] modelViewInvArray =
				new float[] { (float) modelViewInv.m11, (float) modelViewInv.m21,
						(float) modelViewInv.m31, (float) modelViewInv.m41,
						(float) modelViewInv.m12, (float) modelViewInv.m22,
						(float) modelViewInv.m32, (float) modelViewInv.m42,
						(float) modelViewInv.m13, (float) modelViewInv.m23,
						(float) modelViewInv.m33, (float) modelViewInv.m43,
						(float) modelViewInv.m14, (float) modelViewInv.m24,
						(float) modelViewInv.m34, (float) modelViewInv.m44 };
		gl.glUniformMatrix4fv(oldModelViewInverseUniform, 1, false, modelViewInvArray, 0);

		Vec4 eye = dc.getView().getEyePoint();
		gl.glUniform3f(eyePositionUniform, (float) eye.x, (float) eye.y, (float) eye.z);
		gl.glUniform3f(sunPositionUniform, (float) sunPositionNormalized.x,
				(float) sunPositionNormalized.y, (float) sunPositionNormalized.z);
	}

	protected void initShader(DrawContext dc)
	{
		GL gl = dc.getGL();
		int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		String vsrc = "", fsrc = "", line;

		try
		{
			BufferedReader brv =
					new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
							"vertexshader.glsl")));
			while ((line = brv.readLine()) != null)
			{
				vsrc += line + "\n";
			}

			BufferedReader brf =
					new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
							"fragmentshader.glsl")));
			while ((line = brf.readLine()) != null)
			{
				fsrc += line + "\n";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		gl.glShaderSource(v, 1, new String[] { vsrc }, new int[] { vsrc.length() }, 0);
		gl.glCompileShader(v);
		gl.glShaderSource(f, 1, new String[] { fsrc }, new int[] { fsrc.length() }, 0);
		gl.glCompileShader(f);

		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, v);
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		gl.glUseProgram(shaderProgram);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex1"), 1);
		minElevationUniform = gl.glGetUniformLocation(shaderProgram, "minElevation");
		maxElevationUniform = gl.glGetUniformLocation(shaderProgram, "maxElevation");
		minTexElevationUniform = gl.glGetUniformLocation(shaderProgram, "minTexElevation");
		maxTexElevationUniform = gl.glGetUniformLocation(shaderProgram, "maxTexElevation");
		exaggerationUniform = gl.glGetUniformLocation(shaderProgram, "exaggeration");
		bakedExaggerationUniform = gl.glGetUniformLocation(shaderProgram, "bakedExaggeration");
		opacityUniform = gl.glGetUniformLocation(shaderProgram, "opacity");
		eyePositionUniform = gl.glGetUniformLocation(shaderProgram, "eyePosition");
		sunPositionUniform = gl.glGetUniformLocation(shaderProgram, "sunPosition");
		oldModelViewInverseUniform = gl.glGetUniformLocation(shaderProgram, "oldModelViewInverse");
	}

	@Override
	protected void packupShader(DrawContext dc)
	{
		dc.getGL().glUseProgram(0);
	}

	public double getExaggeration()
	{
		return exaggeration;
	}

	public void setExaggeration(double exaggeration)
	{
		this.exaggeration = exaggeration;
	}

	public Vec4 getSunPosition()
	{
		return sunPosition;
	}

	public void setSunPosition(Vec4 sunPosition)
	{
		this.sunPosition = sunPosition;
		this.sunPositionNormalized = sunPosition.normalize3();
	}

	public double getShaderMinElevation()
	{
		return shaderMinElevation;
	}

	public void setShaderMinElevation(double shaderMinElevation)
	{
		this.shaderMinElevation = shaderMinElevation;
	}

	public double getShaderMaxElevation()
	{
		return shaderMaxElevation;
	}

	public void setShaderMaxElevation(double shaderMaxElevation)
	{
		this.shaderMaxElevation = shaderMaxElevation;
	}

	@Override
	protected boolean handleElevations(Globe globe, TextureTile tile, Sector sector,
			BufferWrapper[] elevations)
	{
		int width = tile.getLevel().getTileWidth();
		int height = tile.getLevel().getTileHeight();

		double[] minmax = getMinMax(elevations[4], elevationModel.getMissingDataSignal());
		byte[][] bytes =
				elevationsToTexture(width, height, globe, sector, elevations, minmax[0], minmax[1]);

		File file = WorldWind.getDataFileStore().newFile(tile.getPath());
		return saveTexture(file, bytes, width, height, minmax[0], minmax[1]);
	}

	protected boolean saveTexture(File file, byte[][] bytes, int width, int height,
			double minElevation, double maxElevation)
	{
		synchronized (fileLock)
		{
			OutputStream os = null;
			try
			{
				os = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(os);
				int bands = bytes.length;
				dos.writeInt(width);
				dos.writeInt(height);
				dos.writeInt(bands);
				dos.writeDouble(minElevation);
				dos.writeDouble(maxElevation);
				for (int i = 0; i < bands; i++)
				{
					os.write(bytes[i]);
				}
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			finally
			{
				if (os != null)
				{
					try
					{
						os.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
	}

	@Override
	protected boolean loadTexture(TextureTile tile, URL textureURL)
	{
		if (!(tile instanceof MinMaxTextureTile))
		{
			Logging.logger().severe("Tile is not instance of " + MinMaxTextureTile.class);
			getLevels().markResourceAbsent(tile);
			return false;
		}

		synchronized (fileLock)
		{
			InputStream is = null;
			try
			{
				is = textureURL.openStream();
				DataInputStream dis = new DataInputStream(is);

				int width = dis.readInt();
				int height = dis.readInt();
				int bands = dis.readInt();
				double minElevation = dis.readDouble();
				double maxElevation = dis.readDouble();
				byte[][] bytes = new byte[bands][];
				for (int i = 0; i < bands; i++)
				{
					bytes[i] = new byte[width * height];
					is.read(bytes[i]);
				}

				DataBuffer db = new DataBufferByte(bytes, width * height);
				SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, bands);
				Raster raster = Raster.createRaster(sm, db, null);
				BufferedImage image =
						new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
				image.setData(raster);

				TextureData textureData = TextureIO.newTextureData(image, isUseMipMaps());
				if (textureData == null)
				{
					throw new Exception("Could not create texture data for " + textureURL);
				}

				((MinMaxTextureTile) tile).setMinElevation(minElevation);
				((MinMaxTextureTile) tile).setMaxElevation(maxElevation);
				tile.setTextureData(textureData);

				//if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
				addTileToCache(tile);

				getLevels().unmarkResourceAbsent(tile);
				firePropertyChange(AVKey.LAYER, null, this);
				return true;
			}
			catch (Exception e)
			{
				// Assume that something's wrong with the file and delete it.
				gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(textureURL);
				getLevels().markResourceAbsent(tile);
				String message = Logging.getMessage("generic.DeletedCorruptDataFile", textureURL);
				Logging.logger().info(message + ":" + e.getLocalizedMessage());
				return false;
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
	}

	protected double[] getMinMax(BufferWrapper elevations, double missingDataSignal)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < elevations.length(); i++)
		{
			double value = elevations.getDouble(i);
			if (value != missingDataSignal)
			{
				min = Math.min(min, elevations.getDouble(i));
				max = Math.max(max, elevations.getDouble(i));
			}
		}
		if (min < minElevationClamp)
			min = minElevationClamp;
		if (max > maxElevationClamp)
			max = maxElevationClamp;
		/*if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE)
		{
			Logging.logger().warning("No elevations found in tile");
		}*/
		return new double[] { min, max };
	}

	protected byte[][] elevationsToTexture(int width, int height, Globe globe, Sector sector,
			BufferWrapper[] elevations, double minElevation, double maxElevation)
	{
		//elevation tile index configuration:
		//+-+-+-+
		//|6|7|8|
		//+-+-+-+
		//|3|4|5|
		//+-+-+-+
		//|0|1|2|
		//+-+-+-+

		int padding = 1;
		double missingDataSignal = elevationModel.getMissingDataSignal();
		double[] paddedElevations =
				calculatePaddedElevations(width, height, padding, elevations, missingDataSignal);
		Vec4[] verts =
				calculateTileVerts(width, height, globe, sector, paddedElevations,
						missingDataSignal, padding, bakedExaggeration);
		Vec4[] normals = calculateNormals(width, height, verts, padding);
		//Vec4[] bentNormals = calculateBentNormals(width, height, verts, padding);
		//normals = mixNormals(normals, bentNormals, 0.5);

		byte[] red = new byte[width * height];
		byte[] blue = new byte[width * height];
		byte[] green = new byte[width * height];
		byte[] alpha = new byte[width * height];

		for (int i = 0; i < width * height; i++)
		{
			Vec4 normal = normals[i];
			if (normal == null)
				normal = Vec4.ZERO;

			red[i] = (byte) (255.0 * (normal.x + 1) / 2);
			green[i] = (byte) (255.0 * (normal.y + 1) / 2);
			blue[i] = (byte) (255.0 * (normal.z + 1) / 2);
			double elevation = elevations[4].getDouble(i);
			elevation = clamp(elevation, minElevation, maxElevation);
			alpha[i] = (byte) (255.0 * (elevation - minElevation) / (maxElevation - minElevation));
		}

		byte[][] bytes = new byte[4][];
		bytes[0] = red;
		bytes[1] = green;
		bytes[2] = blue;
		bytes[3] = alpha;
		return bytes;
	}

	protected Vec4[] calculateNormals(int width, int height, Vec4[] verts, int padding)
	{
		int padding2 = padding * 2;

		if (verts.length != (width + padding2) * (height + padding2))
			throw new IllegalStateException("Illegal vertices length");

		Vec4[] norms = new Vec4[width * height];
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				//   v2
				//   |
				//v1-v0-v3
				//   |
				//   v4

				Vec4 v0 =
						verts[getArrayIndex(width + padding2, height + padding2, x + padding, y
								+ padding)];
				if (v0 != null)
				{
					Vec4 v1 =
							verts[getArrayIndex(width + padding2, height + padding2, x + padding
									- 1, y + padding)];
					Vec4 v2 =
							verts[getArrayIndex(width + padding2, height + padding2, x + padding, y
									+ padding - 1)];
					Vec4 v3 =
							verts[getArrayIndex(width + padding2, height + padding2, x + padding
									+ 1, y + padding)];
					Vec4 v4 =
							verts[getArrayIndex(width + padding2, height + padding2, x + padding, y
									+ padding + 1)];

					Vec4[] normals = new Vec4[4];
					normals[0] =
							v1 != null && v2 != null ? v1.subtract3(v0).cross3(v0.subtract3(v2))
									.normalize3() : null;
					normals[1] =
							v2 != null && v3 != null ? v2.subtract3(v0).cross3(v0.subtract3(v3))
									.normalize3() : null;
					normals[2] =
							v3 != null && v4 != null ? v3.subtract3(v0).cross3(v0.subtract3(v4))
									.normalize3() : null;
					normals[3] =
							v4 != null && v1 != null ? v4.subtract3(v0).cross3(v0.subtract3(v1))
									.normalize3() : null;
					Vec4 normal = Vec4.ZERO;
					for (Vec4 n : normals)
					{
						if (n != null)
							normal = normal.add3(n);
					}
					if (normal != Vec4.ZERO)
					{
						norms[getArrayIndex(width, height, x, y)] = normal.normalize3();
					}
				}
			}
		}
		return norms;
	}

	protected Vec4[] calculateBentNormals(int width, int height, Vec4[] verts, int padding)
	{
		int padding2 = padding * 2;

		if (verts.length != (width + padding2) * (height + padding2))
			throw new IllegalStateException("Illegal vertices length");

		Vec4[] norms = new Vec4[width * height];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Vec4 vec =
						verts[getArrayIndex(width + padding2, height + padding2, x + padding, y
								+ padding)];
				if (vec != null)
				{
					Vec4 vecnorm = vec.normalize3();
					Vec4[] maxes = new Vec4[16];
					double[] angles = new double[16];
					for (int i = 0; i < angles.length; i++)
					{
						angles[i] = Double.MAX_VALUE;
					}

					//  2  3  4  5  6
					//  1           7
					//  0           8
					// 15           9
					// 14 13 12 11 10

					//for (int i = 1; i <= padding; i++)
					for (int i = 2; i <= padding; i += 2)
					{
						Vec4[] vecs = new Vec4[16];
						vecs[0] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding - i, y + padding)];
						vecs[2] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding - i, y + padding - i)];
						vecs[4] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding, y + padding - i)];
						vecs[6] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding + i, y + padding - i)];
						vecs[8] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding + i, y + padding)];
						vecs[10] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding + i, y + padding + i)];
						vecs[12] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding, y + padding + i)];
						vecs[14] =
								verts[getArrayIndex(width + padding2, height + padding2, x
										+ padding - i, y + padding + i)];
						if (i % 2 == 0)
						{
							int i2 = i / 2;
							vecs[1] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding - i, y + padding - i2)];
							vecs[3] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding - i2, y + padding - i)];
							vecs[5] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding + i2, y + padding - i)];
							vecs[7] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding + i, y + padding - i2)];
							vecs[9] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding + i, y + padding + i2)];
							vecs[11] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding + i2, y + padding + i)];
							vecs[13] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding - i2, y + padding + i)];
							vecs[15] =
									verts[getArrayIndex(width + padding2, height + padding2, x
											+ padding - i, y + padding + i2)];
						}

						for (int j = 0; j < maxes.length; j++)
						{
							if (vecs[j] != null)
							{
								Vec4 v = vecs[j].subtract3(vec).normalize3();
								double angle = Math.acos(v.dot3(vecnorm));
								if (angle < angles[j])
								{
									angles[j] = angle;
									maxes[j] = v;
								}
							}
						}
					}

					Vec4 normal = Vec4.ZERO;
					for (int i = 0; i < maxes.length; i++)
					{
						if (maxes[i] != null)
						{
							Vec4 n = maxes[i].cross3(vecnorm).cross3(maxes[i]);
							normal = normal.add3(n);
						}
					}
					if (normal != Vec4.ZERO)
					{
						norms[getArrayIndex(width, height, x, y)] = normal.normalize3();
					}
				}
			}
		}

		return norms;
	}

	@Override
	protected TextureTile createTile(Sector sector, Level level, int row, int col)
	{
		return new MinMaxTextureTile(this, sector, level, row, col);
	}

	@Override
	protected Texture createTexture(TextureTile tile)
	{
		if (!(tile instanceof MinMaxTextureTile))
		{
			String message = "Tile is not instance of " + MinMaxTextureTile.class;
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		MinMaxTextureTile mmtt = (MinMaxTextureTile) tile;
		return new ElevationTexture(tile.getTextureData(), mmtt.getMinElevation(), mmtt
				.getMaxElevation());
	}

	@Override
	protected void setupTexture(DrawContext dc, Texture texture)
	{
		super.setupTexture(dc, texture);

		if (!(texture instanceof ElevationTexture))
		{
			String message = "Texture is not instance of " + ElevationTexture.class;
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		GL gl = dc.getGL();
		gl.glUniform1f(minTexElevationUniform, (float) ((ElevationTexture) texture)
				.getMinElevation());
		gl.glUniform1f(maxTexElevationUniform, (float) ((ElevationTexture) texture)
				.getMaxElevation());
	}

	protected static class MinMaxTextureTile extends ElevationTextureTile
	{
		private double minElevation = Double.MAX_VALUE;
		private double maxElevation = -Double.MAX_VALUE;

		public MinMaxTextureTile(ElevationLayer layer, Sector sector, Level level, int row, int col)
		{
			super(layer, sector, level, row, col);
		}

		public double getMinElevation()
		{
			return minElevation;
		}

		public void setMinElevation(double minElevation)
		{
			this.minElevation = minElevation;
		}

		public double getMaxElevation()
		{
			return maxElevation;
		}

		public void setMaxElevation(double maxElevation)
		{
			this.maxElevation = maxElevation;
		}
	}

	public static class ElevationTexture extends SubclassableTexture
	{
		private final double minElevation;
		private final double maxElevation;

		public ElevationTexture(TextureData data, double minElevation, double maxElevation)
				throws GLException
		{
			super(data);
			this.minElevation = minElevation;
			this.maxElevation = maxElevation;
		}

		public double getMinElevation()
		{
			return minElevation;
		}

		public double getMaxElevation()
		{
			return maxElevation;
		}
	}
}
