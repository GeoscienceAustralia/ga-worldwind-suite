package layers.elevation.textured;

import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.SubclassableTexture;
import com.sun.opengl.util.texture.TextureData;

public class ElevationTexture extends SubclassableTexture
{
	private final double minElevation;
	private final double maxElevation;

	public ElevationTexture(TextureData data, double minElevation,
			double maxElevation) throws GLException
	{
		super(data);
		this.minElevation = minElevation;
		this.maxElevation = maxElevation;
	}

	public double getMinElevation()
	{
		return minElevation;
	}

	public double getMaxElevation()
	{
		return maxElevation;
	}
}
