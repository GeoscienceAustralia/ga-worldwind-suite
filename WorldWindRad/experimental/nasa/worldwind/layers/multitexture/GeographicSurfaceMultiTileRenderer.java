/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers.multitexture;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.SectorGeometry;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: GeographicSurfaceTileRenderer.java 4905 2008-04-03 12:19:04Z tgaskins $
 */
public class GeographicSurfaceMultiTileRenderer extends SurfaceMultiTileRenderer
{
    private double sgWidth;
    private double sgHeight;
    private double sgMinWE;
    private double sgMinSN;

    protected void preComputeTransform(DrawContext dc, SectorGeometry sg)
    {
        Sector st = sg.getSector();
        this.sgWidth = st.getDeltaLonRadians();
        this.sgHeight = st.getDeltaLatRadians();
        this.sgMinWE = st.getMinLongitude().radians;
        this.sgMinSN = st.getMinLatitude().radians;
    }

    protected void computeTransform(DrawContext dc, SurfaceTile tile, Transform t)
    {
//        Angle latShift = Angle.fromDegrees(0.0028);
//        Angle lonShift = Angle.fromDegrees(-0.0030);
        Sector st = tile.getSector();
        double tileWidth = st.getDeltaLonRadians();
        double tileHeight = st.getDeltaLatRadians();
        double minLon = st.getMinLongitude().radians;// + lonShift.radians;
        double minLat = st.getMinLatitude().radians;// + latShift.radians;

        t.VScale = tileHeight > 0 ? this.sgHeight / tileHeight : 1;
        t.HScale = tileWidth > 0 ? this.sgWidth / tileWidth : 1;
        t.VShift = -(minLat - this.sgMinSN) / this.sgHeight;
        t.HShift = -(minLon - this.sgMinWE) / this.sgWidth;
    }

    protected Iterable<SurfaceTile> getIntersectingTiles(DrawContext dc, SectorGeometry sg,
        Iterable<? extends SurfaceTile> tiles)
    {
        ArrayList<SurfaceTile> intersectingTiles = null;

        for (SurfaceTile tile : tiles)
        {
            if (!tile.getSector().intersects(sg.getSector()))
                continue;

            if (intersectingTiles == null)
                intersectingTiles = new ArrayList<SurfaceTile>();

            intersectingTiles.add(tile);
        }

        if (intersectingTiles == null)
            return null;

        return intersectingTiles;
    }

	@Override
	protected void computeTransform(DrawContext dc, SurfaceTile tile,
			gov.nasa.worldwind.render.SurfaceTileRenderer.Transform t)
	{
		throw new IllegalStateException("THIS FUNCTION IS INVALID");
	}
}
