package au.gov.ga.worldwind.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;

public interface ImageTransformerDelegate
{
	BufferedImage transformImage(BufferedImage image);
}
