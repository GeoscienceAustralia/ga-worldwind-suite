package au.gov.ga.worldwind.common.view.kml;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwindx.examples.kml.KMLFlyViewController;

import java.util.ArrayList;
import java.util.List;

public class CustomKMLFlyViewController extends KMLFlyViewController
{
	protected CustomKMLFlyViewController(WorldWindow wwd)
	{
		super(wwd);
	}
	
	@Override
	protected void goToDefaultPlacemarkView(KMLPlacemark placemark)
	{
		View view = this.wwd.getView();
        List<Position> positions = new ArrayList<Position>();

        // Find all the points in the placemark. We want to bring the entire placemark into view.
        KMLAbstractGeometry geometry = placemark.getGeometry();
        CustomKMLUtil.getPositions(view.getGlobe(), geometry, positions);

        this.goToDefaultView(positions);
	}
}
