package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;

public interface ImageTransformerDelegate extends Delegate
{
	BufferedImage transformImage(BufferedImage image);
}
