package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Instances of {@link ImageReaderDelegate} are used when reading images from
 * file URLs (ie during texture load). This is useful if there is a need to read
 * an image from a custom file format (such as a zip file).
 * 
 * @author Michael de Hoog
 */
public interface ImageReaderDelegate extends Delegate
{
	/**
	 * Read an image from a url.
	 * 
	 * @param url
	 *            URL to read the image from
	 * @return Loaded image
	 * @throws IOException
	 *             If image reading fails
	 */
	BufferedImage readImage(URL url) throws IOException;
}
