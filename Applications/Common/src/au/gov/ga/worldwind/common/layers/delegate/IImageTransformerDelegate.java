package au.gov.ga.worldwind.common.layers.delegate;

import java.awt.image.BufferedImage;

/**
 * Instances of {@link IImageTransformerDelegate} are used to transform images
 * during texture load. This can be used for post processing of a downloaded
 * texture.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IImageTransformerDelegate extends IDelegate
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
