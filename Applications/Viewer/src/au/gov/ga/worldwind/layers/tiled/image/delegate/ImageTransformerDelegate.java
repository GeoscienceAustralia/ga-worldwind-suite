package au.gov.ga.worldwind.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;

public interface ImageTransformerDelegate extends Delegate
{
	BufferedImage transformImage(BufferedImage image);
}
