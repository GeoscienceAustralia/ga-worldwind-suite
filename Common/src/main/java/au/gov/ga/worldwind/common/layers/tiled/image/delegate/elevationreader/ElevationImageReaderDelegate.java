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

/**
 * Abstract class that acts as a super class of all {@link ITileReaderDelegate}s
 * that generate an image from elevation tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	/**
	 * @see ITileReaderDelegate#readImage(IDelegatorTile, URL, Globe)
	 */
	public BufferedImage readImage(TextureTile tile, URL url, Globe globe) throws IOException
	{
		BufferWrapper byteBuffer = IOUtil.readByteBuffer(url, pixelType, byteOrder);
		return generateImage(byteBuffer, tile.getWidth(), tile.getHeight(), globe, tile.getSector());
	}

	/**
	 * Generate an image from elevation data.
	 * 
	 * @param elevations
	 *            Wrapped elevation data
	 * @param width
	 *            Width of the data tile
	 * @param height
	 *            Height of the data tile
	 * @param globe
	 *            Current globe
	 * @param sector
	 *            Sector of the data tile
	 * @return Image generated from the elevation data
	 */
	protected abstract BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe,
			Sector sector);
}
