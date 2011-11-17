package gov.nasa.worldwind.ogc.custom.kml.model;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.kml.KMLLatLonAltBox;
import gov.nasa.worldwind.ogc.kml.KMLLod;
import gov.nasa.worldwind.ogc.kml.KMLRegion;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomKMLRegion extends KMLRegion
{
	private double cachedVerticalExaggeration = -1;
	private static Map<Double, Double> pow10Cache = new HashMap<Double, Double>();

	public CustomKMLRegion(String namespaceURI)
	{
		super(namespaceURI);
	}

	/**
	 * Math.pow is an expensive operation to call every frame for multiple
	 * models, so instead cache the result (the exponent will probably never
	 * change).
	 * 
	 * @param exponent
	 * @return 10^exponent
	 */
	private static double cachedPow10(double exponent)
	{
		synchronized (pow10Cache)
		{
			if (!pow10Cache.containsKey(exponent))
			{
				pow10Cache.put(exponent, Math.pow(10, exponent));
			}
			return pow10Cache.get(exponent);
		}
	}

	/*
	 * This function is overridden to cache the expensive Math.pow(10, exponent) result.
	 */
	@Override
	protected boolean meetsClampToGroundLodCriteria(KMLTraversalContext tc, DrawContext dc, KMLLod lod)
	{
		// Neither the OGC KML specification nor the Google KML reference specify how to compute a clampToGround
		// Region's projected screen area. However, the Google Earth outreach tutorials, and an official post from a
		// Google engineer on the Google forums both indicate that clampToGround Regions are represented by a flat
		// rectangle on the terrain surface:
		// KML Specification version 2.2, section 6.3.4.
		// http://groups.google.com/group/kml-support-getting-started/browse_thread/thread/bbba32541bace3cc/df4e1dc64a3018d4?lnk=gst#df4e1dc64a3018d4
		// http://earth.google.com/outreach/tutorial_region.html

		Sector sector = this.getCurrentData().getSector();
		List<Vec4> points = this.getCurrentData().getPoints();
		if (sector == null || points == null || points.size() != 5)
			return true; // Assume the criteria is met if we don't know this Region's sector or its surface points.

		// Get the eye distance for each of the sector's corners and its center.
		View view = dc.getView();
		double d1 = view.getEyePoint().distanceTo3(points.get(0));
		double d2 = view.getEyePoint().distanceTo3(points.get(1));
		double d3 = view.getEyePoint().distanceTo3(points.get(2));
		double d4 = view.getEyePoint().distanceTo3(points.get(3));
		double d5 = view.getEyePoint().distanceTo3(points.get(4));

		// Find the minimum eye distance. Compute the sector's size in meters by taking the square root of the sector's
		// area in radians, and multiplying that by the globe's radius at the nearest corner. We take the square root
		// of the area in radians to match the units of this Region's minLodPixels and maxLodPixels, which are the
		// square root of a screen area.
		double minDistance = d1;
		double numRadians = Math.sqrt(sector.getDeltaLatRadians() * sector.getDeltaLonRadians());
		double numMeters = points.get(0).getLength3() * numRadians;

		if (d2 < minDistance)
		{
			minDistance = d2;
			numMeters = points.get(1).getLength3() * numRadians;
		}
		if (d3 < minDistance)
		{
			minDistance = d3;
			numMeters = points.get(2).getLength3() * numRadians;
		}
		if (d4 < minDistance)
		{
			minDistance = d4;
			numMeters = points.get(3).getLength3() * numRadians;
		}
		if (d5 < minDistance)
		{
			minDistance = d5;
			numMeters = points.get(4).getLength3() * numRadians;
		}

		// Compare the scaled distance to the minimum and maximum pixel size in meters, according to the sector's size
		// and this Region's minLodPixels and maxLodPixels. This Region's level of detail criteria are met when the
		// scaled distance is less than or equal to the minimum pixel size, and greater than the maximum pixel size.
		// Said another way, this Region is used when a pixel in the Region's sector is close enough to meet the minimum
		// pixel size criteria, yet far enough away not to exceed the maximum pixel size criteria.

		// NOTE: It's tempting to instead compare a screen pixel count to the minLodPixels and maxLodPixels, but that
		// calculation is window-size dependent and results in activating an excessive number of Regions when a KML
		// super overlay is displayed, especially if the window size is large.

		Double lodMinPixels = lod.getMinLodPixels();
		Double lodMaxPixels = lod.getMaxLodPixels();
		//double distanceFactor = minDistance * Math.pow(10, -this.getDetailFactor(tc));
		double distanceFactor = minDistance * cachedPow10(-this.getDetailFactor(tc));

		// We ignore minLodPixels if it's unspecified, zero, or less than zero. We ignore maxLodPixels if it's
		// unspecified or less than 0 (infinity). In these cases any distance passes the test against minLodPixels or
		// maxLodPixels.
		return (lodMinPixels == null || lodMinPixels <= 0d || (numMeters / lodMinPixels) >= distanceFactor)
				&& (lodMaxPixels == null || lodMaxPixels < 0d || (numMeters / lodMaxPixels) < distanceFactor);
	}

	@Override
	protected void doMakeClampToGroundRegionData(DrawContext dc, KMLLatLonAltBox box)
	{
		//the results calculated by this function only change when the vertical
		//exaggeration changes, so we don't need to recalculate them every frame
		/*if (cachedVerticalExaggeration == dc.getVerticalExaggeration())
			return;
		cachedVerticalExaggeration = dc.getVerticalExaggeration();*/

		super.doMakeClampToGroundRegionData(dc, box);
	}

	@Override
	protected void doMakeAbsoluteRegionData(DrawContext dc, KMLLatLonAltBox box)
	{
		//the results calculated by this function only change when the vertical
		//exaggeration changes, so we don't need to recalculate them every frame
		/*if (cachedVerticalExaggeration == dc.getVerticalExaggeration())
			return;
		cachedVerticalExaggeration = dc.getVerticalExaggeration();*/

		super.doMakeAbsoluteRegionData(dc, box);
	}

	@Override
	protected void doMakeRelativeToGroundRegionData(DrawContext dc, KMLLatLonAltBox box)
	{
		//the results calculated by this function only change when the vertical
		//exaggeration changes, so we don't need to recalculate them every frame
		/*if (cachedVerticalExaggeration == dc.getVerticalExaggeration())
			return;
		cachedVerticalExaggeration = dc.getVerticalExaggeration();*/

		super.doMakeRelativeToGroundRegionData(dc, box);
	}
}
