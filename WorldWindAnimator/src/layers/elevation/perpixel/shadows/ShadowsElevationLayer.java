package layers.elevation.perpixel.shadows;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.BufferWrapper;
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

import layers.elevation.perpixel.ElevationLayer;
import layers.elevation.perpixel.ExtendedBasicElevationModel;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class ShadowsElevationLayer extends ElevationLayer
{
	protected final static String CACHE_NAME_PREFIX = "Shadows Layer/";
	protected final static String FORMAT_SUFFIX = ".shad";

	private int shaderProgram = -1;
	private int opacityUniform;
	private int sunPositionUniform;
	private int oldModelViewInverseUniform;
	private int scaleUniform;
	private int biasUniform;

	protected Vec4 sunPosition = new Vec4(1, 1, 1);
	protected double scale = 1.0;
	protected double bias = 0.0;

	protected double bakedExaggeration = 100.0;

	public ShadowsElevationLayer(ExtendedBasicElevationModel elevationModel)
	{
		this(elevationModel, null);
	}

	public ShadowsElevationLayer(ExtendedBasicElevationModel elevationModel,
			Sector sector)
	{
		super(elevationModel, CACHE_NAME_PREFIX, FORMAT_SUFFIX, sector);
	}

	public Vec4 getSunPosition()
	{
		return sunPosition;
	}

	public void setSunPosition(Vec4 sunPosition)
	{
		this.sunPosition = sunPosition;
	}

	public double getScale()
	{
		return scale;
	}

	public void setScale(double scale)
	{
		this.scale = scale;
	}

	public double getBias()
	{
		return bias;
	}

	public void setBias(double bias)
	{
		this.bias = bias;
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

		Matrix modelViewInv = dc.getView().getModelviewMatrix().getInverse();
		float[] modelViewInvArray = new float[] { (float) modelViewInv.m11,
				(float) modelViewInv.m21, (float) modelViewInv.m31,
				(float) modelViewInv.m41, (float) modelViewInv.m12,
				(float) modelViewInv.m22, (float) modelViewInv.m32,
				(float) modelViewInv.m42, (float) modelViewInv.m13,
				(float) modelViewInv.m23, (float) modelViewInv.m33,
				(float) modelViewInv.m43, (float) modelViewInv.m14,
				(float) modelViewInv.m24, (float) modelViewInv.m34,
				(float) modelViewInv.m44 };
		gl.glUniformMatrix4fv(oldModelViewInverseUniform, 1, false,
				modelViewInvArray, 0);

		gl.glUniform3f(sunPositionUniform, (float) sunPosition.x,
				(float) sunPosition.y, (float) sunPosition.z);
		gl.glUniform1f(opacityUniform, (float) getOpacity());
		gl.glUniform1f(biasUniform, (float) getBias());
		gl.glUniform1f(scaleUniform, (float) getScale());
	}

	protected void initShader(DrawContext dc)
	{
		GL gl = dc.getGL();
		int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		String vsrc = "", fsrc = "", line;

		try
		{
			BufferedReader brv = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("vertexshader.glsl")));
			while ((line = brv.readLine()) != null)
			{
				vsrc += line + "\n";
			}

			BufferedReader brf = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("fragmentshader.glsl")));
			while ((line = brf.readLine()) != null)
			{
				fsrc += line + "\n";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		gl.glShaderSource(v, 1, new String[] { vsrc }, new int[] { vsrc
				.length() }, 0);
		gl.glCompileShader(v);
		gl.glShaderSource(f, 1, new String[] { fsrc }, new int[] { fsrc
				.length() }, 0);
		gl.glCompileShader(f);

		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, v);
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		gl.glUseProgram(shaderProgram);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex1"), 1);

		opacityUniform = gl.glGetUniformLocation(shaderProgram, "opacity");
		biasUniform = gl.glGetUniformLocation(shaderProgram, "bias");
		scaleUniform = gl.glGetUniformLocation(shaderProgram, "scale");
		sunPositionUniform = gl.glGetUniformLocation(shaderProgram,
				"sunPosition");
		oldModelViewInverseUniform = gl.glGetUniformLocation(shaderProgram,
				"oldModelViewInverse");
	}

	@Override
	protected void packupShader(DrawContext dc)
	{
		dc.getGL().glUseProgram(0);
	}

	@Override
	protected boolean handleElevations(Globe globe, TextureTile tile,
			Sector sector, BufferWrapper[] elevations)
	{
		int width = tile.getLevel().getTileWidth();
		int height = tile.getLevel().getTileHeight();

		byte[][] bytes = elevationsToHorizonAngles(width, height, globe,
				sector, elevations);

		width *= 2;
		height *= 2;

		/*DataBuffer db = new DataBufferByte(bytes, width * height);
		SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, width,
				height, bytes.length);
		Raster raster = Raster.createRaster(sm, db, null);
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB_PRE);
		image.setData(raster);*/

		File file = WorldWind.getDataFileStore().newFile(tile.getPath());
		//return saveTexture(file, image);
		return saveTexture(file, bytes, width, height);
	}

	protected boolean saveTexture(File file, byte[][] bytes, int width,
			int height)
	{
		synchronized (fileLock)
		{
			OutputStream os = null;
			try
			{
				os = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeInt(width);
				dos.writeInt(height);
				for (int i = 0; i < bytes.length; i++)
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
		synchronized (fileLock)
		{
			InputStream is = null;
			try
			{
				is = textureURL.openStream();
				DataInputStream dis = new DataInputStream(is);

				int width = dis.readInt();
				int height = dis.readInt();
				byte[][] bytes = new byte[4][];
				for (int i = 0; i < bytes.length; i++)
				{
					bytes[i] = new byte[width * height];
					is.read(bytes[i]);
				}

				DataBuffer db = new DataBufferByte(bytes, width * height);
				SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE,
						width, height, bytes.length);
				Raster raster = Raster.createRaster(sm, db, null);
				BufferedImage image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB_PRE);
				image.setData(raster);

				TextureData textureData = TextureIO.newTextureData(image,
						isUseMipMaps());
				if (textureData == null)
				{
					throw new Exception("Could not create texture data for "
							+ textureURL);
				}

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
				gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(
						textureURL);
				getLevels().markResourceAbsent(tile);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", textureURL);
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

	/*protected boolean saveTexture(File file, BufferedImage image)
	{
		synchronized (fileLock)
		{
			try
			{
				ImageIO.write(image, FORMAT_SUFFIX.substring(1), file);
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}*/

	/*@Override
	protected boolean loadTexture(TextureTile tile, URL textureURL)
	{
		try
		{
			TextureData textureData = null;
			synchronized (fileLock)
			{
				BufferedImage image = ImageIO.read(textureURL);
				image.coerceData(true);
				textureData = TextureIO.newTextureData(image, isUseMipMaps());
				//textureData = TextureIO.newTextureData(textureURL, isUseMipMaps(), null);
			}

			if (textureData == null)
			{
				throw new IOException("Could not create texture data for "
						+ textureURL);
			}

			tile.setTextureData(textureData);

			//if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			addTileToCache(tile);

			getLevels().unmarkResourceAbsent(tile);
			firePropertyChange(AVKey.LAYER, null, this);
			return true;
		}
		catch (IOException e)
		{
			// Assume that something's wrong with the file and delete it.
			gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(
					textureURL);
			getLevels().markResourceAbsent(tile);
			String message = Logging.getMessage(
					"generic.DeletedCorruptDataFile", textureURL);
			Logging.logger().info(message + ":" + e.getLocalizedMessage());
			return false;
		}
	}*/

	protected byte[][] elevationsToHorizonAngles(int width, int height,
			Globe globe, Sector sector, BufferWrapper[] elevations)
	{
		int padding = width;
		Vec4[] verts = calculateTileVerts(width, height, globe, sector,
				elevations, elevationModel.getMissingDataSignal(), padding,
				bakedExaggeration);
		System.out.println("Calculating horizon angles for sector " + sector
				+ "...");
		double[][] angles = calculateHorizonAngles(width, height, verts,
				padding);
		System.out.println(sector + " complete");
		byte[][] texture = convertHorizonAnglesToTexture(width, height, angles);
		return texture;
	}

	protected double[][] calculateHorizonAngles(int width, int height,
			Vec4[] verts, int padding)
	{
		int padding2 = padding * 2;

		if (verts.length != (width + padding2) * (height + padding2))
			throw new IllegalStateException("Illegal vertices length");

		double[][] angles = new double[16][];
		for (int i = 0; i < angles.length; i++)
		{
			angles[i] = new double[width * height];
			for (int j = 0; j < width * height; j++)
			{
				angles[i][j] = Double.MAX_VALUE;
			}
		}

		for (int y = 0; y < height; y++)
		{
			System.out.println((y * 100 / height) + "% done");

			for (int x = 0; x < width; x++)
			{
				Vec4 vec = verts[getArrayIndex(width + padding2, height
						+ padding2, x + padding, y + padding)];
				if (vec != null)
				{
					Vec4 vecnorm = vec.normalize3();

					//  2  3  4  5  6
					//  1           7
					//  0           8
					// 15           9
					// 14 13 12 11 10

					//for (int i = 1; i <= padding; i++)
					for (int i = 2; i <= padding; i += 2)
					{
						Vec4[] vecs = new Vec4[16];
						vecs[0] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding - i, y + padding)];
						vecs[2] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding - i, y + padding - i)];
						vecs[4] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding, y + padding - i)];
						vecs[6] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding + i, y + padding - i)];
						vecs[8] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding + i, y + padding)];
						vecs[10] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding + i, y + padding + i)];
						vecs[12] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding, y + padding + i)];
						vecs[14] = verts[getArrayIndex(width + padding2, height
								+ padding2, x + padding - i, y + padding + i)];
						if (i % 2 == 0)
						{
							int i2 = i / 2;
							vecs[1] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding - i, y
											+ padding - i2)];
							vecs[3] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding - i2, y
											+ padding - i)];
							vecs[5] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding + i2, y
											+ padding - i)];
							vecs[7] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding + i, y
											+ padding - i2)];
							vecs[9] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding + i, y
											+ padding + i2)];
							vecs[11] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding + i2, y
											+ padding + i)];
							vecs[13] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding - i2, y
											+ padding + i)];
							vecs[15] = verts[getArrayIndex(width + padding2,
									height + padding2, x + padding - i, y
											+ padding + i2)];
						}

						for (int j = 0; j < angles.length; j++)
						{
							if (vecs[j] != null)
							{
								Vec4 v = vecs[j].subtract3(vec).normalize3();
								double angle = Math.acos(v.dot3(vecnorm));
								int index = getArrayIndex(width, height, x, y);
								if (angle < angles[j][index])
								{
									angles[j][index] = angle;
								}
							}
						}
					}
				}
			}
		}

		return angles;
	}

	protected byte[][] convertHorizonAnglesToTexture(int width, int height,
			double[][] angles)
	{
		if (angles.length != 16 || angles[0].length != width * height)
			throw new IllegalStateException("Illegal angles array length");

		byte[][] bytes = new byte[4][];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = new byte[width * height * 4];
		}

		for (int d = 0; d < angles.length; d++)
		{
			int band = d / 4;
			int xoff = (d % 4) == 1 || (d % 4) == 3 ? width : 0;
			int yoff = (d % 4) == 2 || (d % 4) == 3 ? height : 0;
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					double angle = angles[d][getArrayIndex(width, height, x, y)];
					byte b = (byte) (255.0 * angle / Math.PI); //TODO scale logarithmically?
					bytes[band][getArrayIndex(width * 2, height * 2, x + xoff,
							y + yoff)] = b;
				}
			}
		}

		return bytes;
	}
}
