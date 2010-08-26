package au.gov.ga.worldwind.viewer.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoubleClickZoomListener extends MouseAdapter
{
	private WorldWindow wwd;
	private LatLon latlon;
	private double minElevation;

	public DoubleClickZoomListener(WorldWindow wwd, double minElevation)
	{
		this.wwd = wwd;
		this.minElevation = minElevation;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
			return;

		if (!(wwd.getView() instanceof OrbitView))
			return;
		OrbitView view = (OrbitView) wwd.getView();

		if (e.getClickCount() % 2 == 1)
		{
			// single click
			latlon = null;

			PickedObjectList pickedObjects = wwd.getObjectsAtCurrentPosition();
			if (pickedObjects == null)
				return;

			PickedObject top = pickedObjects.getTopPickedObject();
			if (top == null || !top.isTerrain())
				return;

			latlon = top.getPosition();
		}
		else
		{
			// double click
			if (latlon != null)
			{
				double zoom = view.getZoom();
				if (zoom > minElevation)
				{
					zoom =
							Math.max(minElevation, e.getButton() == MouseEvent.BUTTON1 ? zoom / 3
									: zoom * 3);
				}
				Position beginCenter = view.getCenterPosition();
				Position endCenter = new Position(latlon, beginCenter.getElevation());

				view.stopMovement();
				view.stopAnimations();

				view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view,
						beginCenter, endCenter, view.getHeading(), view.getHeading(),
						view.getPitch(), view.getPitch(), view.getZoom(), zoom, 1000, true));
				e.consume();
				wwd.redraw();
				latlon = null;
			}
		}
	}
}
