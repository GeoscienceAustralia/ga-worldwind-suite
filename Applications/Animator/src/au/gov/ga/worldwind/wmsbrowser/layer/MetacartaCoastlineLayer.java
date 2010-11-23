package au.gov.ga.worldwind.wmsbrowser.layer;

import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateBasicTiledImageLayer;

public class MetacartaCoastlineLayer extends ImmediateBasicTiledImageLayer
{
	public MetacartaCoastlineLayer()
	{
		super(MetacartaLayerUtil.makeLevels("Earth/Metacarta Coastline",
				"coastline_02"));
		setUseTransparentTextures(true);
		setUseMipMaps(true);
	}

	@Override
	public String toString()
	{
		return "Metacarta coastline";
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
