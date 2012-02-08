package au.gov.ga.worldwind.common.layers.tiled.image.delegate.nearestneighbor;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;

import com.sun.opengl.util.texture.Texture;

/**
 * {@link TextureTile} which performs NearestNeighbor Magnification at when
 * viewing the layer's lowest level (highest resolution).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NearestNeighborTextureTile extends DelegatorTextureTile
{
	private int numLevels = 0;

	public NearestNeighborTextureTile(Sector sector, Level level, int row, int col,
			ITileFactoryDelegate<DelegatorTextureTile, Sector, Level> delegate)
	{
		super(sector, level, row, col, delegate);

		AVList params = level.getParams();
		Object o = params.getValue(AVKey.NUM_LEVELS);
		if (o != null && o instanceof Integer)
			numLevels = (Integer) o;
	}

	@Override
	protected void setTextureParameters(DrawContext dc, Texture t)
	{
		super.setTextureParameters(dc, t);

		//set the magnification filter to nearest neighbor if this tile's level is the layer's last level
		if ((numLevels > 0 && getLevelNumber() >= numLevels - 1) || dc.getCurrentLayer().isAtMaxResolution())
		{
			dc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		}
	}
}
