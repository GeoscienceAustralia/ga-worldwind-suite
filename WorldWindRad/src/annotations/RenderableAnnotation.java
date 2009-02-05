package annotations;

import java.awt.Point;

import javax.media.opengl.GL;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AbstractAnnotation;
import gov.nasa.worldwind.render.DrawContext;

public class RenderableAnnotation extends AbstractAnnotation implements
		Locatable, Movable
{
	private Annotation annotation;
	private Position position;

	public RenderableAnnotation(Annotation annotation)
	{
		this.annotation = annotation;
		setText(annotation.getLabel());
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
}
