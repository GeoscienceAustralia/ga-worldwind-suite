package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.view.orbit.OrbitView;

/**
 * {@link GlobeAnnotation} subclass that is used to render {@link Place}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RenderablePlace extends GlobeAnnotation
{
	private Place place;
	private Position position;
	private boolean dragging;

	public RenderablePlace(Place place)
	{
		super(place.getLabel(), new Position(place.getLatLon(), 0));
		this.place = place;
		setAttributes(new MyAnnotationAttributes(place));
		getAttributes().setTextAlign(AVKey.CENTER);
	}

	//taken from superclass, apart from additions (see below)
	@Override
	protected void doRenderNow(DrawContext dc)
	{
		if (dc.isPickingMode() && this.getPickSupport() == null)
			return;

		Vec4 point = this.getAnnotationDrawPoint(dc);
		if (point == null)
			return;

		if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0)
			return;

		Vec4 screenPoint = dc.getView().project(point);
		if (screenPoint == null)
			return;

		java.awt.Dimension size = this.getPreferredSize(dc);
		Position pos = dc.getGlobe().computePositionFromPoint(point);

		// Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
		Double lookAtDistanceD = this.computeLookAtDistance(dc);
		double lookAtDistance = lookAtDistanceD != null ? lookAtDistanceD : 0;
		double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
		double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
		double scale =
				WWMath.clamp(distanceFactor, this.attributes.getDistanceMinScale(),
						this.attributes.getDistanceMaxScale());
		double opacity = WWMath.clamp(distanceFactor, this.attributes.getDistanceMinOpacity(), 1);


		//added lines below
		double minZoom = place.getMinZoom();
		double maxZoom = place.getMaxZoom();
		double zoom;
		View view = dc.getView();
		if (view instanceof OrbitView)
			zoom = ((OrbitView) view).getZoom();
		else
			zoom = view.getEyePosition().getElevation();
		if (minZoom >= 0 && zoom > minZoom)
			opacity = WWMath.clamp(1 - 5 * (zoom - minZoom) / minZoom, 0, opacity);
		else if (maxZoom >= 0 && zoom < maxZoom)
			opacity = WWMath.clamp(1 - 5 * (maxZoom - zoom) / maxZoom, 0, opacity);
		if (opacity < 0.1)
			getAttributes().setHighlighted(false);
		//added lines above


		this.setDepthFunc(dc, screenPoint);
		this.drawTopLevelAnnotation(dc, (int) screenPoint.x, (int) screenPoint.y, size.width, size.height, scale,
				opacity, pos);
	}

	@Override
	public String getText()
	{
		String text = place.getLabel();
		if (dragging)
		{
			String latlon =
					String.format("Lat %7.4f\u00B0\nLon %7.4f\u00B0", getPosition().getLatitude().degrees,
							getPosition().getLongitude().degrees);
			text += "\n" + latlon;
		}
		return text;
	}

	@Override
	public void setText(String text)
	{
		if (place != null)
			place.setLabel(text);
	}

	@Override
	public Position getPosition()
	{
		if (position == null)
			position = new Position(place.getLatLon(), 0);
		return position;
	}

	@Override
	public Position getReferencePosition()
	{
		return getPosition();
	}

	@Override
	public void move(Position position)
	{
		Position newPosition = getPosition().add(position);
		place.setLatLon(newPosition);
		this.position = null;
	}

	@Override
	public void moveTo(Position position)
	{
		place.setLatLon(position);
		this.position = null;
	}

	public boolean isDragging()
	{
		return dragging;
	}

	public void setDragging(boolean dragging)
	{
		this.dragging = dragging;
	}

	public Place getPlace()
	{
		return place;
	}

	public static class MyAnnotationAttributes extends AnnotationAttributes
	{
		private Place place;

		public MyAnnotationAttributes(Place place)
		{
			super();
			this.place = place;
		}

		@Override
		public boolean isVisible()
		{
			return place.isVisible();
		}

		@Override
		public void setVisible(boolean visible)
		{
			place.setVisible(visible);
		}
	}
}
