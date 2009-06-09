package au.gov.ga.worldwind.annotations;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.view.OrbitView;

public class RenderableAnnotation extends GlobeAnnotation
{
	private Annotation annotation;
	private Position position;
	private boolean dragging;

	public RenderableAnnotation(Annotation annotation)
	{
		super(annotation.getLabel(), Position.fromDegrees(annotation
				.getLatitude(), annotation.getLongitude(), 0));
		this.annotation = annotation;
		setAttributes(new MyAnnotationAttributes(annotation));
		getAttributes().setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
	}

	//taken from superclass, apart from additions (see below)
	protected void doRenderNow(DrawContext dc)
	{
		if (dc.isPickingMode() && this.getPickSupport() == null)
			return;

		Vec4 point = dc.getAnnotationRenderer()
				.getAnnotationDrawPoint(dc, this);
		if (point == null)
			return;

		if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(
				point) < 0)
			return;

		Vec4 screenPoint = dc.getView().project(point);
		if (screenPoint == null)
			return;

		java.awt.Dimension size = this.getPreferredSize(dc);
		Position pos = dc.getGlobe().computePositionFromPoint(point);

		// Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
		double lookAtDistance = this.computeLookAtDistance(dc);
		double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
		double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
		double scale = WWMath.clamp(distanceFactor, this.attributes
				.getDistanceMinScale(), this.attributes.getDistanceMaxScale());
		double opacity = WWMath.clamp(distanceFactor, this.attributes
				.getDistanceMinOpacity(), 1);

		
		//added lines below
		double minZoom = annotation.getMinZoom();
		double maxZoom = annotation.getMaxZoom();
		double zoom;
		View view = dc.getView();
		if (view instanceof OrbitView)
			zoom = ((OrbitView) view).getZoom();
		else
			zoom = view.getEyePosition().getElevation();
		if (minZoom >= 0 && zoom > minZoom)
			opacity = WWMath.clamp(1 - 5 * (zoom - minZoom) / minZoom, 0,
					opacity);
		else if (maxZoom >= 0 && zoom < maxZoom)
			opacity = WWMath.clamp(1 - 5 * (maxZoom - zoom) / maxZoom, 0,
					opacity);
		if (opacity < 0.1)
			getAttributes().setHighlighted(false);
		//added lines above


		this.setDepthFunc(dc, screenPoint);
		this.drawTopLevelAnnotation(dc, (int) screenPoint.x,
				(int) screenPoint.y, size.width, size.height, scale, opacity,
				pos);
	}

	@Override
	public String getText()
	{
		String text = annotation.getLabel();
		if (dragging)
		{
			String latlon = String.format("Lat %7.4f\u00B0\nLon %7.4f\u00B0",
					getPosition().getLatitude().degrees, getPosition()
							.getLongitude().degrees);
			text += "\n" + latlon;
		}
		return text;
	}

	@Override
	public void setText(String text)
	{
		if (annotation != null)
			annotation.setLabel(text);
	}

	public Position getPosition()
	{
		if (position == null)
			position = Position.fromDegrees(annotation.getLatitude(),
					annotation.getLongitude(), 0);
		return position;
	}

	public Position getReferencePosition()
	{
		return getPosition();
	}

	public void move(Position position)
	{
		Position newPosition = getPosition().add(position);
		annotation.setLatitude(newPosition.getLatitude().degrees);
		annotation.setLongitude(newPosition.getLongitude().degrees);
		this.position = null;
	}

	public void moveTo(Position position)
	{
		annotation.setLatitude(position.getLatitude().degrees);
		annotation.setLongitude(position.getLongitude().degrees);
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

	public Annotation getAnnotation()
	{
		return annotation;
	}

	public static class MyAnnotationAttributes extends AnnotationAttributes
	{
		private Annotation annotation;

		public MyAnnotationAttributes(Annotation annotation)
		{
			super();
			this.annotation = annotation;
		}

		@Override
		public boolean isVisible()
		{
			return annotation.isVisible();
		}

		@Override
		public void setVisible(boolean visible)
		{
			annotation.setVisible(visible);
		}
	}
}
