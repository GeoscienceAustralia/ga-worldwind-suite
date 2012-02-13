package au.gov.ga.worldwind.common.view.kml;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwindx.examples.kml.KMLOrbitViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension to the {@link KMLOrbitViewController} that uses the
 * {@link CustomKMLUtil} to calculate a {@link KMLPlacemark} positions, to add
 * support for flying to {@link KMLModel}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLOrbitViewController extends KMLOrbitViewController
{
	protected CustomKMLOrbitViewController(WorldWindow wwd)
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
