package au.gov.ga.worldwind.annotations;

import java.awt.Point;

import javax.media.opengl.GL;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AbstractAnnotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.MultiLineTextRenderer;

public class RenderableAnnotation extends AbstractAnnotation implements
		Locatable, Movable
{
	private Annotation annotation;
	private Position position;
	private boolean dragging;

	public RenderableAnnotation(Annotation annotation)
	{
		this.annotation = annotation;
		setText(annotation.getLabel());
		setAttributes(new MyAnnotationAttributes(annotation));
		getAttributes().setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
	}

	@Override
	protected void doDraw(DrawContext dc)
	{
		if (dc.isPickingMode() && this.getPickSupport() == null)
			return;

		Vec4 point = dc.getAnnotationRenderer()
				.getAnnotationDrawPoint(dc, this);
		if (point == null)
			return;

		double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
		final Vec4 screenPoint = dc.getView().project(point);
		if (screenPoint == null)
			return;

		Position pos = dc.getGlobe().computePositionFromPoint(point);

		// Determine scaling and transparency factors based on distance from eye vs the distance to the look at point
		double drawScale = computeLookAtDistance(dc) / eyeDistance; // TODO: cache lookAtDistance for one frame cycle
		double drawAlpha = Math.min(1d, Math.max(attributes
				.getDistanceMinOpacity(), Math.sqrt(drawScale)));
		drawScale = Math.min(attributes.getDistanceMaxScale(), Math.max(
				attributes.getDistanceMinScale(), drawScale)); // Clamp to factor range

		double minZoom = annotation.getMinZoom();
		double maxZoom = annotation.getMaxZoom();
		Position eyePos = dc.getView().getEyePosition();
		double zoom = eyePos.getElevation();
		if (minZoom >= 0 && zoom > minZoom)
			drawAlpha = Math.max(0, Math.min(drawAlpha, 1 - 5
					* (zoom - minZoom) / minZoom));
		else if (maxZoom >= 0 && zoom < maxZoom)
			drawAlpha = Math.max(0, Math.min(drawAlpha, 1 - 5
					* (maxZoom - zoom) / maxZoom));

		if (drawAlpha < 0.1)
			getAttributes().setHighlighted(false);

		// Prepare to draw
		this.setDepthFunc(dc, screenPoint);
		GL gl = dc.getGL();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		// Translate to screenpoint
		gl.glTranslated(screenPoint.x, screenPoint.y, 0d);

		// Draw
		drawAnnotation(dc, new Point((int) screenPoint.x, (int) screenPoint.y),
				drawScale, drawAlpha, pos);
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

	public class MyAnnotationAttributes extends AnnotationAttributes
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

	public Annotation getAnnotation()
	{
		return annotation;
	}
}
