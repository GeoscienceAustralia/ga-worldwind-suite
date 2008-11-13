package util;

import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.view.OrbitView;

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
		if (!(wwd.getView() instanceof OrbitView))
			return;
		OrbitView view = (OrbitView) wwd.getView();

		if (e.getClickCount() % 2 == 1)
		{
			//single click
			latlon = null;

			PickedObjectList pickedObjects = wwd
					.getObjectsAtCurrentPosition();
			if (pickedObjects == null)
				return;

			PickedObject top = pickedObjects.getTopPickedObject();
			if (top == null || !top.isTerrain())
				return;

			latlon = top.getPosition().getLatLon();
		}
		else
		{
			//double click
			if (latlon != null)
			{
				Position eyePosition = view.getEyePosition();
				double newElevation = eyePosition.getElevation();
				if (newElevation > minElevation)
				{
					newElevation = Math.max(minElevation,
							newElevation / 2);
				}
				ViewStateIterator vsi = EyePositionViewStateIterator
						.createIterator(eyePosition, new Position(
								latlon, newElevation), 1000, true);
				view.applyStateIterator(vsi);
				latlon = null;
			}
		}
	}
}
