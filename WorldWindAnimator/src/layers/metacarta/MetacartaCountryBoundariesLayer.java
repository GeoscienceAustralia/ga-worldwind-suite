package layers.metacarta;

import javax.media.opengl.GL;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;

public class MetacartaCountryBoundariesLayer extends BasicTiledImageLayer
{
	public MetacartaCountryBoundariesLayer()
	{
		super(MetacartaLayerUtil.makeLevels("Earth/Metacarta Country Boundaries",
				"country_02"));
		setUseTransparentTextures(true);
		setUseMipMaps(true);
	}

	@Override
	public String toString()
	{
		return "Metacarta country boundaries";
	}
	
	@Override
	public void setSplitScale(double splitScale)
	{
		super.setSplitScale(splitScale);
	}
	
	@Override
	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}
}
