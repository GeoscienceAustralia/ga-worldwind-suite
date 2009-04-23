package layers.elevation.textured;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

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

import layers.immediate.ImmediateMode;
import nasa.worldwind.layers.TiledImageLayer;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class ElevationLayer extends TiledImageLayer
{
	protected static final String CACHE_NAME_PREFIX = "Elevation Layer/";

	protected ExtendedBasicElevationModel elevationModel;
	protected double exaggeration = 1.0;
	protected Vec4 sunPosition = new Vec4(1, 1, 1);
	protected Vec4 sunPositionNormalized = sunPosition.normalize3();
	protected double minElevationClamp = -Double.MAX_VALUE;
	protected double maxElevationClamp = Double.MAX_VALUE;
	protected double bakedExaggeration = 100.0;

	protected Object fileLock = new Object();

	private int shaderprogram = -1;
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

	private DrawContext dc = null;

	public ElevationLayer(ExtendedBasicElevationModel elevationModel)
	{
		super(makeLevels(elevationModel));
		this.elevationModel = elevationModel;
		setUseTransparentTextures(true);
		setUseMipMaps(true);
	}

	protected static LevelSet makeLevels(
			ExtendedBasicElevationModel elevationModel)
	{
		LevelSet levels = elevationModel.getLevels();

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, levels.getLastLevel().getTileWidth());
		params.setValue(AVKey.TILE_HEIGHT, levels.getLastLevel()
				.getTileHeight());
		params.setValue(AVKey.DATA_CACHE_NAME, CACHE_NAME_PREFIX
				+ levels.getLastLevel().getCacheName());
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, levels.getLastLevel().getDataset());
		params.setValue(AVKey.FORMAT_SUFFIX, ".elev");
		params.setValue(AVKey.NUM_LEVELS, levels.getNumLevels());
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0); //?
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, levels.getFirstLevel()
				.getTileDelta());
		params.setValue(AVKey.SECTOR, levels.getSector());
		params.setValue(AVKey.TILE_URL_BUILDER, null);

		return new LevelSet(params);
	}

	@Override
	public void render(DrawContext dc)
	{
		this.dc = dc;

		if (shaderprogram == -1)
		{
			setupShader(dc);
		}
		GL gl = dc.getGL();
		gl.glUseProgram(shaderprogram);

		/*double minElevation = ((ElevationTesselator) dc.getGlobe()
				.getTessellator()).getMinElevation();
		double maxElevation = ((ElevationTesselator) dc.getGlobe()
				.getTessellator()).getMaxElevation();*/
		double minElevation = elevationModel.getMinElevation();
		double maxElevation = elevationModel.getMaxElevation();
		minElevation = clamp(minElevation, minElevationClamp, maxElevationClamp);
		maxElevation = clamp(maxElevation, minElevationClamp, maxElevationClamp);
		gl.glUniform1f(minElevationUniform, (float) minElevation);
		gl.glUniform1f(maxElevationUniform, (float) maxElevation);
		gl.glUniform1f(exaggerationUniform, (float) exaggeration);
		gl.glUniform1f(bakedExaggerationUniform, (float) bakedExaggeration);
		gl.glUniform1f(opacityUniform, (float) getOpacity());

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

		Vec4 eye = dc.getView().getEyePoint();
		gl.glUniform3f(eyePositionUniform, (float) eye.x, (float) eye.y,
				(float) eye.z);
		gl.glUniform3f(sunPositionUniform, (float) sunPositionNormalized.x,
				(float) sunPositionNormalized.y,
				(float) sunPositionNormalized.z);

		super.render(dc);
		gl.glUseProgram(0);
	}

	private void setupShader(DrawContext dc)
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

		shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, v);
		gl.glAttachShader(shaderprogram, f);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);

		gl.glUseProgram(shaderprogram);
		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex1"), 1);
		minElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"minElevation");
		maxElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"maxElevation");
		minTexElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"minTexElevation");
		maxTexElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"maxTexElevation");
		exaggerationUniform = gl.glGetUniformLocation(shaderprogram,
				"exaggeration");
		bakedExaggerationUniform = gl.glGetUniformLocation(shaderprogram,
				"bakedExaggeration");
		opacityUniform = gl.glGetUniformLocation(shaderprogram, "opacity");
		eyePositionUniform = gl.glGetUniformLocation(shaderprogram,
				"eyePosition");
		sunPositionUniform = gl.glGetUniformLocation(shaderprogram,
				"sunPosition");
		oldModelViewInverseUniform = gl.glGetUniformLocation(shaderprogram,
				"oldModelViewInverse");
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		if (dc == null)
			return;
		attemptLoad(dc.getGlobe(), tile);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		if (ImmediateMode.isImmediate())
		{
			attemptLoad(dc.getGlobe(), tile);
			return;
		}

		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		if (this.getReferencePoint() != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

		RequestTask task = new RequestTask(dc.getGlobe(), tile, this);
		this.getRequestQ().add(task);
	}

	protected void attemptLoad(Globe globe, TextureTile tile)
	{
		URL textureURL = WorldWind.getDataFileStore().findFile(tile.getPath(),
				false);
		if (textureURL != null)
		{
			if (loadTexture(tile, textureURL))
			{
				return;
			}
		}

		if (requestElevations(globe, tile))
		{
			if (!ImmediateMode.isImmediate())
			{
				firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				textureURL = WorldWind.getDataFileStore().findFile(
						tile.getPath(), false);
				if (textureURL != null)
				{
					loadTexture(tile, textureURL);
				}
			}
		}
	}

	protected boolean loadTexture(TextureTile tile, URL textureURL)
	{
		if (!(tile instanceof MinMaxTextureTile))
		{
			Logging.logger().severe(
					"Tile is not instance of " + MinMaxTextureTile.class);
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
				SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE,
						width, height, bands);
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

	protected boolean saveTexture(File file, byte[][] bytes, int width,
			int height, double minElevation, double maxElevation)
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

	protected boolean requestElevations(Globe globe, TextureTile tile)
	{
		TileKey[] keys = new TileKey[9];
		Sector[] sectors = new Sector[9];
		boolean anyTouching = fillTouchingElevationTileKeys(tile, keys, sectors);
		if (!anyTouching)
		{
			getLevels().markResourceAbsent(tile);
			return false;
		}

		int width = tile.getLevel().getTileWidth();
		int height = tile.getLevel().getTileHeight();
		boolean allLoaded = true;
		BufferWrapper[] elevations = new BufferWrapper[9];
		for (int i = 0; i < elevations.length; i++)
		{
			if (keys[i] != null)
			{
				elevations[i] = getElevationsFromMemory(keys[i]);
				if (elevations[i] == null)
				{
					allLoaded = false;
				}
				else if (elevations[i].length() != width * height)
				{
					Logging.logger().severe(
							"Elevations array has incorrect length");
					getLevels().markResourceAbsent(tile);
					return false;
				}
			}
		}

		if (allLoaded)
		{
			double[] minmax = getMinMax(elevations[4], elevationModel
					.getMissingDataSignal());
			byte[][] bytes = elevationsToTexture(width, height, globe,
					sectors[4], elevations, minmax[0], minmax[1]);

			File file = WorldWind.getDataFileStore().newFile(tile.getPath());
			return saveTexture(file, bytes, width, height, minmax[0], minmax[1]);
		}

		return false;
	}

	protected BufferWrapper getElevationsFromMemory(TileKey key)
	{
		BufferWrapper elevations = elevationModel.getElevationsFromMemory(key);
		if (elevations == null)
		{
			elevationModel.requestTile(key);
			if (ImmediateMode.isImmediate())
			{
				elevations = elevationModel.getElevationsFromMemory(key);
			}
		}
		return elevations;
	}

	protected void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected boolean fillTouchingElevationTileKeys(TextureTile tile,
			TileKey[] keys, Sector[] sectors)
	{
		if (keys.length != 9 || sectors.length != 9)
			throw new IllegalArgumentException("Illegal array length");

		LevelSet elevationLevels = elevationModel.getLevels();
		Level elevationLevel = elevationLevels.getLevel(tile.getLevelNumber());
		if (elevationLevel == null)
			return false;

		//calculate column and row count over the globe for this level
		int colCount = (int) Math.ceil(Angle.POS360.divide(elevationLevel
				.getTileDelta().longitude));
		int rowCount = (int) Math.ceil(Angle.POS180.divide(elevationLevel
				.getTileDelta().latitude));

		//fill list of 9 tilekeys, 5'th one is the center tile
		boolean anyValid = false;
		for (int i = 0, r = -1; r <= 1; r++)
		{
			for (int c = -1; c <= 1; c++, i++)
			{
				int col = (c + tile.getColumn() + colCount) % colCount;
				int row = (r + tile.getRow() + rowCount) % rowCount;

				TileKey key = new TileKey(tile.getLevelNumber(), row, col,
						elevationLevel.getCacheName());
				sectors[i] = elevationLevels.computeSectorForKey(key);
				//only add if the sector intersects the levels sector
				if (elevationLevels.getSector().intersects(sectors[i]))
				{
					int maxlevel = elevationLevels.getLastLevel(sectors[i])
							.getLevelNumber();
					if (maxlevel >= elevationLevel.getLevelNumber())
					{
						keys[i] = key;
						anyValid = true;
					}
				}
			}
		}

		if (!anyValid || keys[4] == null)
			return false;

		return true;
	}

	protected double[] getMinMax(BufferWrapper elevations,
			double missingDataSignal)
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
		if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE)
		{
			throw new IllegalStateException(
					"All elevations are missing data, min/max not found");
		}
		return new double[] { min, max };
	}

	protected byte[][] elevationsToTexture(int width, int height, Globe globe,
			Sector sector, BufferWrapper[] elevations, double minElevation,
			double maxElevation)
	{
		//elevation tile index configuration:
		//+-+-+-+
		//|6|7|8|
		//+-+-+-+
		//|3|4|5|
		//+-+-+-+
		//|0|1|2|
		//+-+-+-+

		Vec4[] verts = calculateTileVerts(width, height, globe, sector,
				elevations, elevationModel.getMissingDataSignal());
		Vec4[] normals = calculateNormals(width, height, verts);

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
			alpha[i] = (byte) (255.0 * (elevations[4].getDouble(i) - minElevation) / (maxElevation - minElevation));
		}

		byte[][] bytes = new byte[4][];
		bytes[0] = red;
		bytes[1] = green;
		bytes[2] = blue;
		bytes[3] = alpha;
		return bytes;
	}

	protected Vec4[] calculateTileVerts(int width, int height, Globe globe,
			Sector sector, BufferWrapper[] elevations, double missingDataSignal)
	{
		double[] allElevations = new double[(width + 2) * (height + 2)];
		for (int y = 0; y < height + 2; y++)
		{
			int srcy = clamp(y - 1, 0, height - 1);
			for (int x = 0; x < width + 2; x++)
			{
				int srcx = clamp(x - 1, 0, width - 1);
				allElevations[getArrayIndex(width + 2, height + 2, x, y)] = elevations[4]
						.getDouble(getArrayIndex(width, height, srcx, srcy));
			}
		}
		//left & right rows, not corners
		for (int x = 0; x < width + 2; x += width + 1)
		{
			boolean left = x == 0;
			int srcx = left ? x - 2 : x;
			BufferWrapper elevs = left ? elevations[3] : elevations[5];
			if (elevs != null)
			{
				for (int y = 1; y < height + 1; y++)
				{
					int srcy = y - 1;
					allElevations[getArrayIndex(width + 2, height + 2, x, y)] = elevs
							.getDouble(getArrayIndex(width, height, srcx, srcy));
				}
			}
		}
		//top & bottom rows, and corners
		for (int y = 0; y < height + 2; y += height + 1)
		{
			boolean top = y == 0;
			int srcy = top ? y - 2 : y;
			for (int x = 0; x < width + 2; x++)
			{
				int srcx = x - 1;
				BufferWrapper elevs = top ? elevations[7] : elevations[1];
				if (x == 0)
				{
					//left corners
					elevs = top ? elevations[6] : elevations[0];
					srcx--;
				}
				else if (x == width + 1)
				{
					//right corners
					elevs = top ? elevations[8] : elevations[2];
					srcx++;
				}
				if (elevs != null)
				{
					allElevations[getArrayIndex(width + 2, height + 2, x, y)] = elevs
							.getDouble(getArrayIndex(width, height, srcx, srcy));
				}
				/*else if(y == 0 || y == height + 1)
				{
					//copy from next or prev row
					allElevations[getArrayIndex(width + 2, height + 2, x, y)] = allElevations[getArrayIndex(
							width + 2, height + 2, x, top ? y + 1 : y - 1)];
				}*/
			}
		}

		Vec4[] verts = new Vec4[(width + 2) * (height + 2)];
		double dlon = sector.getDeltaLonDegrees() / width;
		double dlat = sector.getDeltaLatDegrees() / height;

		for (int y = 0; y < height + 2; y++)
		{
			Angle lat = sector.getMaxLatitude().subtractDegrees(dlat * (y - 1));
			for (int x = 0; x < width + 2; x++)
			{
				Angle lon = sector.getMinLongitude().addDegrees(dlon * (x - 1));
				int index = getArrayIndex(width + 2, height + 2, x, y);
				double elevation = allElevations[index];
				if (elevation != missingDataSignal
						&& elevation >= minElevationClamp
						&& elevation <= maxElevationClamp)
				{
					verts[index] = globe.computePointFromPosition(lat, lon,
							elevation * bakedExaggeration);
				}
			}
		}

		return verts;
	}

	protected Vec4[] calculateNormals(int width, int height, Vec4[] verts)
	{
		if (verts.length != (width + 2) * (height + 2))
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

				Vec4 v0 = verts[getArrayIndex(width + 2, height + 2, x + 1,
						y + 1)];
				if (v0 != null)
				{
					Vec4 v1 = verts[getArrayIndex(width + 2, height + 2, x,
							y + 1)];
					Vec4 v2 = verts[getArrayIndex(width + 2, height + 2, x + 1,
							y)];
					Vec4 v3 = verts[getArrayIndex(width + 2, height + 2, x + 2,
							y + 1)];
					Vec4 v4 = verts[getArrayIndex(width + 2, height + 2, x + 1,
							y + 2)];

					Vec4[] normals = new Vec4[4];
					normals[0] = v1 != null && v2 != null ? v1.subtract3(v0)
							.cross3(v0.subtract3(v2)).normalize3() : null;
					normals[1] = v2 != null && v3 != null ? v2.subtract3(v0)
							.cross3(v0.subtract3(v3)).normalize3() : null;
					normals[2] = v3 != null && v4 != null ? v3.subtract3(v0)
							.cross3(v0.subtract3(v4)).normalize3() : null;
					normals[3] = v4 != null && v1 != null ? v4.subtract3(v0)
							.cross3(v0.subtract3(v1)).normalize3() : null;
					Vec4 normal = Vec4.ZERO;
					for (Vec4 n : normals)
					{
						if (n != null)
							normal = normal.add3(n);
					}
					if (normal != Vec4.ZERO)
					{
						norms[getArrayIndex(width, height, x, y)] = normal
								.normalize3();
					}
				}
			}
		}
		return norms;
	}

	protected static int getArrayIndex(int width, int height, int x, int y)
	{
		while (x < 0)
			x += width;
		while (y < 0)
			y += height;
		while (x >= width)
			x -= width;
		while (y >= height)
			y -= height;
		return width * y + x;
	}

	protected static int clamp(int value, int min, int max)
	{
		return (value < min) ? min : (value > max) ? max : value;
	}

	protected static double clamp(double value, double min, double max)
	{
		return (value < min) ? min : (value > max) ? max : value;
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

	public double getMinElevationClamp()
	{
		return minElevationClamp;
	}

	public void setMinElevationClamp(double minElevationClamp)
	{
		this.minElevationClamp = minElevationClamp;
	}

	public double getMaxElevationClamp()
	{
		return maxElevationClamp;
	}

	public void setMaxElevationClamp(double maxElevationClamp)
	{
		this.maxElevationClamp = maxElevationClamp;
	}

	@Override
	public void setSplitScale(double splitScale)
	{
		super.setSplitScale(splitScale);
	}

	@Override
	protected TextureTile createTile(Sector sector, Level level, int row,
			int col)
	{
		return new MinMaxTextureTile(this, sector, level, row, col);
	}

	@Override
	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected static class RequestTask implements Runnable,
			Comparable<RequestTask>
	{
		private final Globe globe;
		private final ElevationLayer layer;
		private final TextureTile tile;

		public RequestTask(Globe globe, TextureTile tile, ElevationLayer layer)
		{
			this.globe = globe;
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			layer.attemptLoad(globe, tile);
		}

		public int compareTo(RequestTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0
					: this.tile.getPriority() < that.tile.getPriority() ? -1
							: 1;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		public String toString()
		{
			return this.tile.toString();
		}
	}

	protected static class MinMaxTextureTile extends TextureTile
	{
		private double minElevation = Double.MAX_VALUE;
		private double maxElevation = -Double.MAX_VALUE;
		private final ElevationLayer layer;

		public MinMaxTextureTile(ElevationLayer layer, Sector sector,
				Level level, int row, int col)
		{
			super(sector, level, row, col);
			this.layer = layer;
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

		@Override
		protected Texture initializeTexture(DrawContext dc)
		{
			if (dc == null)
			{
				String message = Logging
						.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}

			Texture t = this.getTexture(dc.getTextureCache());
			if (t != null)
				return t;

			if (this.getTextureData() == null)
			{
				String msg = Logging.getMessage("nullValue.TextureDataIsNull");
				Logging.logger().severe(msg);
				throw new IllegalStateException(msg);
			}

			try
			{
				t = new ElevationTexture(this.getTextureData(),
						getMinElevation(), getMaxElevation());
			}
			catch (Exception e)
			{
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
								e);
				return null;
			}

			this.setTexture(dc.getTextureCache(), t);
			t.bind();

			this.setTextureParameters(dc, t);

			return t;
		}

		@Override
		public boolean bind(DrawContext dc)
		{
			if (dc == null)
			{
				String message = Logging
						.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}

			Texture t = this.getTexture(dc.getTextureCache());
			if (t == null && this.getTextureData() != null)
			{
				t = this.initializeTexture(dc);
				if (t != null)
					return true; // texture was bound during initialization.
			}

			if (t == null && this.getFallbackTile() != null)
			{
				MinMaxTextureTile resourceTile = this.getFallbackTile();
				t = resourceTile.getTexture(dc.getTextureCache());
				if (t == null)
				{
					t = resourceTile.initializeTexture(dc);
					if (t != null)
						return true; // texture was bound during initialization.
				}
			}

			if (t != null)
			{
				t.bind();

				if (!(t instanceof ElevationTexture))
				{
					String message = "Texture is not instance of ElevationTexture";
					Logging.logger().severe(message);
					throw new IllegalStateException(message);
				}

				GL gl = dc.getGL();
				gl.glUniform1f(layer.minTexElevationUniform,
						(float) ((ElevationTexture) t).getMinElevation());
				gl.glUniform1f(layer.maxTexElevationUniform,
						(float) ((ElevationTexture) t).getMaxElevation());
			}

			return t != null;
		}

		protected TextureTile getTileFromMemoryCache(TileKey tileKey)
		{
			return (TextureTile) WorldWind.getMemoryCache(
					TextureTile.class.getName()).getObject(tileKey);
		}

		@Override
		public TextureTile[] createSubTiles(Level nextLevel)
		{
			if (nextLevel == null)
			{
				String msg = Logging.getMessage("nullValue.LevelIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			Angle p0 = this.getSector().getMinLatitude();
			Angle p2 = this.getSector().getMaxLatitude();
			Angle p1 = Angle.midAngle(p0, p2);

			Angle t0 = this.getSector().getMinLongitude();
			Angle t2 = this.getSector().getMaxLongitude();
			Angle t1 = Angle.midAngle(t0, t2);

			String nextLevelCacheName = nextLevel.getCacheName();
			int nextLevelNum = nextLevel.getLevelNumber();
			int row = this.getRow();
			int col = this.getColumn();

			TextureTile[] subTiles = new TextureTile[4];

			TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col,
					nextLevelCacheName);
			TextureTile subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[0] = subTile;
			else
				subTiles[0] = new MinMaxTextureTile(layer, new Sector(p0, p1,
						t0, t1), nextLevel, 2 * row, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[1] = subTile;
			else
				subTiles[1] = new MinMaxTextureTile(layer, new Sector(p0, p1,
						t1, t2), nextLevel, 2 * row, 2 * col + 1);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[2] = subTile;
			else
				subTiles[2] = new MinMaxTextureTile(layer, new Sector(p1, p2,
						t0, t1), nextLevel, 2 * row + 1, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[3] = subTile;
			else
				subTiles[3] = new MinMaxTextureTile(layer, new Sector(p1, p2,
						t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);

			return subTiles;
		}

		@Override
		public MinMaxTextureTile getFallbackTile()
		{
			return (MinMaxTextureTile) super.getFallbackTile();
		}
	}
}
