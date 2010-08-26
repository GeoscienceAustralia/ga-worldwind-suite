package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public interface ImageReaderDelegate extends Delegate
{
	BufferedImage readImage(URL url) throws IOException;
}
