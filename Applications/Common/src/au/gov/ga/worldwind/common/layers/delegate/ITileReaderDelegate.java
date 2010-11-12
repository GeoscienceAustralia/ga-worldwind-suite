package au.gov.ga.worldwind.common.layers.delegate;

import gov.nasa.worldwind.globes.Globe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Instances of {@link ITileReaderDelegate} are used when reading images from
 * file URLs (ie during texture load). This is useful if there is a need to read
 * an image from a custom file format (such as a zip file).
 * 
 * @author Michael de Hoog
 */
public interface ITileReaderDelegate extends IDelegate
{
	/**
	 * Read an image from a url.
	 * 
	 * @param tile
	 *            Tile for which to read the image
	 * @param url
	 *            URL to read the image from
	 * @param globe
	 *            Current globe; can be used for vertex calculations if required
	 * @return Loaded image
	 * @throws IOException
	 *             If image reading fails
	 */
	<TILE extends IDelegatorTile> BufferedImage readImage(TILE tile, URL url, Globe globe) throws IOException;
}
