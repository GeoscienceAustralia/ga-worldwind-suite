package au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageReaderDelegate;

public abstract class ElevationImageReaderDelegate implements ImageReaderDelegate
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
	public BufferedImage readImage(TextureTile tile, URL url, Globe globe) throws IOException
	{
		ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(url);

		int width = tile.getWidth();
		int height = tile.getHeight();

		// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, pixelType);
		bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
		BufferWrapper elevations = BufferWrapper.wrap(byteBuffer, bufferParams);

		return generateImage(elevations, width, height, globe, tile.getSector());
	}

	protected abstract BufferedImage generateImage(BufferWrapper elevations, int width, int height,
			Globe globe, Sector sector);
}
