package au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.BufferWrapper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import au.gov.ga.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.worldwind.common.layers.delegate.ITileReaderDelegate;
import au.gov.ga.worldwind.common.util.IOUtil;

public abstract class ElevationImageReaderDelegate implements ITileReaderDelegate
{
	protected final static String doublePattern = "((?:-?\\d*\\.\\d*)|(?:-?\\d+))";

	protected final String pixelType;
	protected final String byteOrder;
	protected final double missingDataSignal;

	public ElevationImageReaderDelegate(String pixelType, String byteOrder, double missingDataSignal)
	{
		this.pixelType = pixelType;
		this.byteOrder = byteOrder;
		this.missingDataSignal = missingDataSignal;
	}

	@Override
	public BufferedImage readImage(IDelegatorTile tile, URL url, Globe globe) throws IOException
	{
		if (!(tile instanceof TextureTile))
		{
			throw new IllegalArgumentException("Tile must be a " + TextureTile.class.getName());
		}
		return readImage((TextureTile) tile, url, globe);
	}

	public BufferedImage readImage(TextureTile tile, URL url, Globe globe) throws IOException
	{
		BufferWrapper byteBuffer = IOUtil.readByteBuffer(url, pixelType, byteOrder);
		
		return generateImage(byteBuffer, tile.getWidth(), tile.getHeight(), globe, tile.getSector());
	}

	protected abstract BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe,
			Sector sector);
}
