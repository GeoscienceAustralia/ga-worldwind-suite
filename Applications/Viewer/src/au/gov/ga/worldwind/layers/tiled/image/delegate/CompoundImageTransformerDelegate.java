package au.gov.ga.worldwind.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class CompoundImageTransformerDelegate extends ArrayList<ImageTransformerDelegate> implements
		ImageTransformerDelegate
{
	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		for (ImageTransformerDelegate delegate : this)
		{
			image = delegate.transformImage(image);
		}
		return image;
	}
}
