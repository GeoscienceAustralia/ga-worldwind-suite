package au.gov.ga.worldwind.animator.layers.accessible;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.nio.ByteBuffer;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class AccessibleBasicTiledImageLayer extends BasicTiledImageLayer
{
	private final FileLockAccessor fileLockAccessor = new FileLockAccessor(this);
	
	public AccessibleBasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public AccessibleBasicTiledImageLayer(AVList params)
	{
		super(params);
	}

	public AccessibleBasicTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	protected Object getFileLock()
	{
		return fileLockAccessor.getFileLock();
	}
	
	protected boolean loadTexture(TextureTile tile, java.net.URL textureURL)
	{
		TextureData textureData;

		synchronized (getFileLock())
		{
			textureData = readTexture(textureURL, this.getTextureFormat(), this.isUseMipMaps());
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}
	
	protected void addTileToCache(TextureTile tile)
    {
        TextureTile.getMemoryCache().add(tile.getTileKey(), tile);
    }
	
	protected static TextureData readTexture(java.net.URL url, String textureFormat, boolean useMipMaps)
    {
        try
        {
            // If the caller has enabled texture compression, and the texture data is not a DDS file, then use read the
            // texture data and convert it to DDS.
            if ("image/dds".equalsIgnoreCase(textureFormat) && !url.toString().toLowerCase().endsWith("dds"))
            {
                // Configure a DDS compressor to generate mipmaps based according to the 'useMipMaps' parameter, and
                // convert the image URL to a compressed DDS format.
                DXTCompressionAttributes attributes = DDSCompressor.getDefaultCompressionAttributes();
                attributes.setBuildMipmaps(useMipMaps);
                ByteBuffer buffer = DDSCompressor.compressImageURL(url, attributes);

                return TextureIO.newTextureData(WWIO.getInputStreamFromByteBuffer(buffer), useMipMaps, null);
            }
            // If the caller has disabled texture compression, or if the texture data is already a DDS file, then read
            // the texture data without converting it.
            else
            {
                return TextureIO.newTextureData(url, useMipMaps, null);
            }
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", url);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            return null;
        }
    }

	protected static class FileLockAccessor extends DownloadPostProcessor
	{
		public FileLockAccessor(BasicTiledImageLayer layer)
		{
			super(null, layer);
		}

		@Override
		public Object getFileLock()
		{
			return super.getFileLock();
		}
	}
}
