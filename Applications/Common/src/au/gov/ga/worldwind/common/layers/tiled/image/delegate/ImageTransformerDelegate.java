package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;

/**
 * Instances of {@link ImageTransformerDelegate} are used to transform images
 * during texture load. This can be used for post processing of a downloaded
 * texture.
 * 
 * @author Michael de Hoog
 */
public interface ImageTransformerDelegate extends Delegate
{
	/**
	 * Transform an image.
	 * 
	 * @param image
	 *            Image to transform
	 * @return Transformed image
	 */
	BufferedImage transformImage(BufferedImage image);
}
