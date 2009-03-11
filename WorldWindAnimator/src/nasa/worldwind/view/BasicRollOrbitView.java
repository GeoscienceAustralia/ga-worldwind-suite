/*
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.view.AbstractView;
import gov.nasa.worldwind.view.OrbitViewCollisionSupport;
import gov.nasa.worldwind.view.OrbitViewModel;
import gov.nasa.worldwind.view.ViewSupport;

import javax.media.opengl.GL;

/**
 * @author dcollins
 * @version $Id: BasicOrbitView.java 6564 2008-09-11 00:31:51Z dcollins $
 */
public class BasicRollOrbitView extends AbstractView implements RollOrbitView
{
	private Position center = Position.ZERO;
	private Angle heading = Angle.ZERO;
	private Angle pitch = Angle.ZERO;
	private Angle roll = Angle.ZERO;
	private double zoom;
	private Angle fieldOfView = Angle.fromDegrees(45);
	private double nearClipDistance = -1; // Default to auto-configure.
	private double farClipDistance = -1; // Default to auto-configure.
	// Model for defining translations between OrbitView coordinates and 3D coordinates.
	private final RollOrbitViewModel orbitViewModel;
	// Stateless helper classes.
	private final ViewSupport viewSupport = new ViewSupport();
	private final OrbitViewCollisionSupport collisionSupport = new OrbitViewCollisionSupport();
	// Properties updated in doApply().
	private Matrix modelview = Matrix.IDENTITY;
	private Matrix modelviewInv = Matrix.IDENTITY;
	private Matrix projection = Matrix.IDENTITY;
	private java.awt.Rectangle viewport = new java.awt.Rectangle();
	private Frustum frustum = new Frustum();
	// Properties updated during the most recent call to apply().
	private DrawContext dc;
	private Globe globe;
	private Position lastEyePosition = null;
	private Vec4 lastEyePoint = null;
	private Vec4 lastUpVector = null;
	private Vec4 lastForwardVector = null;
	private Frustum lastFrustumInModelCoords = null;

	// TODO: make configurable
	private static final double MINIMUM_NEAR_DISTANCE = 2;
	private static final double MINIMUM_FAR_DISTANCE = 100;
	private static final double COLLISION_THRESHOLD = 10;
	private static final int COLLISION_NUM_ITERATIONS = 4;

	public BasicRollOrbitView()
	{
		this(new BasicRollOrbitViewModel());
	}

	public BasicRollOrbitView(RollOrbitViewModel orbitViewModel)
	{
		if (orbitViewModel == null)
		{
			String message = Logging
					.getMessage("nullValue.OrbitViewModelIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.orbitViewModel = orbitViewModel;
		this.collisionSupport.setCollisionThreshold(COLLISION_THRESHOLD);
		this.collisionSupport.setNumIterations(COLLISION_NUM_ITERATIONS);
		loadConfigurationValues();
	}

	private void loadConfigurationValues()
	{
		Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		double initElev = this.center.getElevation();
		// Set center latitude and longitude. Do not change center elevation.
		if (initLat != null && initLon != null)
			setCenterPosition(Position.fromDegrees(initLat, initLon, initElev));
		// Set only center latitude. Do not change center longitude or center elevation.
		else if (initLat != null)
			setCenterPosition(Position.fromDegrees(initLat, this.center
					.getLongitude().degrees, initElev));
		// Set only center longitude. Do not center latitude or center elevation.
		else if (initLon != null)
			setCenterPosition(Position.fromDegrees(
					this.center.getLatitude().degrees, initLon, initElev));

		Double initHeading = Configuration
				.getDoubleValue(AVKey.INITIAL_HEADING);
		if (initHeading != null)
			setHeading(Angle.fromDegrees(initHeading));

		Double initPitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);
		if (initPitch != null)
			setPitch(Angle.fromDegrees(initPitch));

		Double initAltitude = Configuration
				.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		if (initAltitude != null)
			setZoom(initAltitude);

		Double initFov = Configuration.getDoubleValue(AVKey.FOV);
		if (initFov != null)
			setFieldOfView(Angle.fromDegrees(initFov));
	}

	public Position getCenterPosition()
	{
		return this.center;
	}

	public void setCenterPosition(Position center)
	{
		if (center == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (center.getLatitude().degrees < -90
				|| center.getLatitude().degrees > 90)
		{
			String message = Logging.getMessage("generic.LatitudeOutOfRange",
					center.getLatitude());
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.center = normalizedPosition(center);
		resolveCollisionsWithCenterPosition();
	}

	public Angle getHeading()
	{
		return this.heading;
	}

	public void setHeading(Angle heading)
	{
		if (heading == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.heading = normalizedHeading(heading);
		resolveCollisionsWithPitch();
	}

	public Angle getPitch()
	{
		return this.pitch;
	}

	public void setPitch(Angle pitch)
	{
		if (pitch == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (pitch.degrees > 180 || pitch.degrees < 0)
		{
			String message = Logging.getMessage("generic.AngleOutOfRange",
					pitch);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.pitch = pitch;
		resolveCollisionsWithPitch();
	}

	public double getZoom()
	{
		return this.zoom;
	}

	public void setZoom(double zoom)
	{
		if (zoom < 0)
		{
			String message = Logging.getMessage("generic.ArgumentOutOfRange",
					zoom);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.zoom = zoom;
		resolveCollisionsWithCenterPosition();
	}

	public OrbitViewModel getOrbitViewModel()
	{
		return this.orbitViewModel;
	}

	private static Position normalizedPosition(Position position)
	{
		if (position == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return new Position(Angle.normalizedLatitude(position.getLatitude()),
				Angle.normalizedLongitude(position.getLongitude()), position
						.getElevation());
	}

	private static Angle normalizedHeading(Angle unnormalizedAngle)
	{
		if (unnormalizedAngle == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		double degrees = unnormalizedAngle.degrees;
		double heading = degrees % 360;
		return Angle.fromDegrees(heading > 180 ? heading - 360
				: (heading < -180 ? 360 + heading : heading));
	}

	private void resolveCollisionsWithCenterPosition()
	{
		if (this.dc == null)
			return;

		if (!isDetectCollisions())
			return;

		// Compute the near distance corresponding to the current set of values.
		double nearDistance = this.nearClipDistance > 0 ? this.nearClipDistance
				: getAutoNearClipDistance();
		// If there is no collision, 'newCenterPosition' will be null. Otherwise it will contain a value
		// that will resolve the collision.
		Position newCenter = this.collisionSupport
				.computeCenterPositionToResolveCollision(this, nearDistance,
						this.dc);
		if (newCenter != null && newCenter.getLatitude().degrees >= -90
				&& newCenter.getLongitude().degrees <= 90)
		{
			this.center = newCenter;
			flagHadCollisions();
		}
	}

	private void resolveCollisionsWithPitch()
	{
		if (this.dc == null)
			return;

		if (!isDetectCollisions())
			return;

		// Compute the near distance corresponding to the current set of values.
		double nearDistance = this.nearClipDistance > 0 ? this.nearClipDistance
				: getAutoNearClipDistance();
		// If there is no collision, 'newPitch' will be null. Otherwise it will contain a value
		// that will resolve the collision.
		Angle newPitch = this.collisionSupport.computePitchToResolveCollision(
				this, nearDistance, this.dc);
		if (newPitch != null && newPitch.degrees <= 90 && newPitch.degrees >= 0)
		{
			this.pitch = newPitch;
			flagHadCollisions();
		}
	}

	public boolean canFocusOnViewportCenter()
	{
		return this.dc != null && this.dc.getViewportCenterPosition() != null
				&& this.globe != null;
	}

	public void focusOnViewportCenter()
	{
		if (this.dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (this.globe == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Position viewportCenterPos = this.dc.getViewportCenterPosition();
		if (viewportCenterPos == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextViewportCenterIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// We want the actual "geometric point" here, which must be adjusted for vertical exaggeration.
		Vec4 viewportCenterPoint = this.globe.computePointFromPosition(
				viewportCenterPos.getLatitude(), viewportCenterPos
						.getLongitude(), this.globe.getElevation(
						viewportCenterPos.getLatitude(), viewportCenterPos
								.getLongitude())
						* dc.getVerticalExaggeration());

		if (viewportCenterPoint != null)
		{
			Matrix modelview = this.orbitViewModel.computeTransformMatrix(
					this.globe, this.center, this.heading, this.pitch,
					this.roll, this.zoom);
			if (modelview != null)
			{
				Matrix modelviewInv = modelview.getInverse();
				if (modelviewInv != null)
				{
					// The change in focus must happen seamlessly; we can't move the eye or the forward vector
					// (only the center position and zoom should change). Therefore we pick a point along the
					// forward vector, and *near* the viewportCenterPoint, but not necessarily at the
					// viewportCenterPoint itself.
					Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
					Vec4 forward = Vec4.UNIT_NEGATIVE_Z
							.transformBy4(modelviewInv);
					double distance = eyePoint.distanceTo3(viewportCenterPoint);
					Vec4 newCenterPoint = Vec4.fromLine3(eyePoint, distance,
							forward);

					RollOrbitViewModel.RollModelCoordinates modelCoords = this.orbitViewModel
							.computeModelCoordinates(this.globe, modelview,
									newCenterPoint);
					if (validateModelCoordinates(modelCoords))
					{
						setModelCoordinates(modelCoords);
					}
				}
			}
		}
	}

	public void stopMovementOnCenter()
	{
		firePropertyChange(CENTER_STOPPED, null, null);
	}

	public Position getEyePosition()
	{
		if (this.lastEyePosition == null)
			this.lastEyePosition = computeEyePositionFromModelview();
		return this.lastEyePosition;
	}

	public void setEyePosition(Position eyePosition)
	{
		if (eyePosition == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		LatLon latlon = eyePosition;
		double elevation = eyePosition.getElevation();

		// Set the center lat/lon to the eye lat/lon. Set the center elevation to zero if the eye elevation is >= 0.
		// Set the center elevation to the eye elevation if the eye elevation is < 0.
		this.center = new Position(latlon, elevation >= 0 ? 0 : elevation);
		this.heading = Angle.ZERO;
		this.pitch = Angle.ZERO;
		// If the eye elevation is >= 0, zoom gets the eye elevation. If the eye elevation < 0, zoom gets 0.
		this.zoom = elevation >= 0 ? elevation : 0;

		resolveCollisionsWithCenterPosition();
	}

	public Position getCurrentEyePosition()
	{
		if (this.globe != null)
		{
			Matrix modelview = this.orbitViewModel.computeTransformMatrix(
					this.globe, this.center, this.heading, this.pitch,
					this.roll, this.zoom);
			if (modelview != null)
			{
				Matrix modelviewInv = modelview.getInverse();
				if (modelviewInv != null)
				{
					Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
					return this.globe.computePositionFromPoint(eyePoint);
				}
			}
		}

		return Position.ZERO;
	}

	public void setOrientation(Position eyePosition, Position centerPosition)
	{
		if (eyePosition == null || centerPosition == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (this.globe == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Vec4 newEyePoint = this.globe.computePointFromPosition(eyePosition);
		Vec4 newCenterPoint = this.globe
				.computePointFromPosition(centerPosition);
		if (newEyePoint == null || newCenterPoint == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition, centerPosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// If eye lat/lon != center lat/lon, then the surface normal at the center point will be a good value
		// for the up direction.
		Vec4 up = this.globe.computeSurfaceNormalAtPoint(newCenterPoint);
		// Otherwise, estimate the up direction by using the *current* heading with the new center position.
		Vec4 forward = newCenterPoint.subtract3(newEyePoint).normalize3();
		if (forward.cross3(up).getLength3() < 0.001)
		{
			Matrix modelview = this.orbitViewModel.computeTransformMatrix(
					this.globe, centerPosition, this.heading, Angle.ZERO,
					Angle.ZERO, 1);
			if (modelview != null)
			{
				Matrix modelviewInv = modelview.getInverse();
				if (modelviewInv != null)
				{
					up = Vec4.UNIT_Y.transformBy4(modelviewInv);
				}
			}
		}

		if (up == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition, centerPosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		RollOrbitViewModel.RollModelCoordinates modelCoords = this.orbitViewModel
				.computeModelCoordinates(this.globe, newEyePoint,
						newCenterPoint, up);
		if (!validateModelCoordinates(modelCoords))
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition, centerPosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		setModelCoordinates(modelCoords);
	}

	public Vec4 getEyePoint()
	{
		if (this.lastEyePoint == null)
			this.lastEyePoint = Vec4.UNIT_W.transformBy4(this.modelviewInv);
		return this.lastEyePoint;
	}

	public Vec4 getCurrentEyePoint()
	{
		if (this.globe != null)
		{
			Matrix modelview = this.orbitViewModel.computeTransformMatrix(
					this.globe, this.center, this.heading, this.pitch,
					this.roll, this.zoom);
			if (modelview != null)
			{
				Matrix modelviewInv = modelview.getInverse();
				if (modelviewInv != null)
				{
					return Vec4.UNIT_W.transformBy4(modelviewInv);
				}
			}
		}

		return Vec4.ZERO;
	}

	public Vec4 getUpVector()
	{
		if (this.lastUpVector == null)
			this.lastUpVector = Vec4.UNIT_Y.transformBy4(this.modelviewInv);
		return this.lastUpVector;
	}

	public Vec4 getForwardVector()
	{
		if (this.lastForwardVector == null)
			this.lastForwardVector = Vec4.UNIT_NEGATIVE_Z
					.transformBy4(this.modelviewInv);
		return this.lastForwardVector;
	}

	public Matrix getModelviewMatrix()
	{
		return this.modelview;
	}

	public Angle getFieldOfView()
	{
		return this.fieldOfView;
	}

	public void setFieldOfView(Angle fieldOfView)
	{
		if (fieldOfView == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.fieldOfView = fieldOfView;
	}

	public double getNearClipDistance()
	{
		return this.nearClipDistance;
	}

	public void setNearClipDistance(double distance)
	{
		this.nearClipDistance = distance;
	}

	public double getFarClipDistance()
	{
		return this.farClipDistance;
	}

	public void setFarClipDistance(double distance)
	{
		this.farClipDistance = distance;
	}

	public double getAutoNearClipDistance()
	{
		Position eyePos = getCurrentEyePosition();
		return computeNearDistance(eyePos);
	}

	public double getAutoFarClipDistance()
	{
		Position eyePos = getCurrentEyePosition();
		return computeFarDistance(eyePos);
	}

	private double computeNearDistance(Position eyePosition)
	{
		double near = 0;
		if (eyePosition != null && this.dc != null)
		{
			double elevation = this.viewSupport.computeElevationAboveSurface(
					this.dc, eyePosition);
			double tanHalfFov = this.fieldOfView.tanHalfAngle();
			near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
		}
		return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
	}

	private double computeFarDistance(Position eyePosition)
	{
		double far = 0;
		if (eyePosition != null)
		{
			far = computeHorizonDistance(eyePosition);
		}

		return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
	}

	public java.awt.Rectangle getViewport()
	{
		// java.awt.Rectangle is mutable, so we defensively copy the viewport.
		return new java.awt.Rectangle(this.viewport);
	}

	public Frustum getFrustum()
	{
		return this.frustum;
	}

	public Frustum getFrustumInModelCoordinates()
	{
		if (this.lastFrustumInModelCoords == null)
		{
			Matrix modelviewTranspose = this.modelview.getTranspose();
			if (modelviewTranspose != null)
				this.lastFrustumInModelCoords = this.frustum
						.transformBy(modelviewTranspose);
			else
				this.lastFrustumInModelCoords = this.frustum;
		}
		return this.lastFrustumInModelCoords;
	}

	public Matrix getProjectionMatrix()
	{
		return this.projection;
	}

	protected void doApply(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGlobe() == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = this.orbitViewModel.computeTransformMatrix(this.globe,
				this.center, this.heading, this.pitch, this.roll, this.zoom);
		if (this.modelview == null)
			this.modelview = Matrix.IDENTITY;
		// Compute the current inverse-modelview matrix.
		this.modelviewInv = this.modelview.getInverse();
		if (this.modelviewInv == null)
			this.modelviewInv = Matrix.IDENTITY;

		//========== projection matrix state ==========//
		// Get the current OpenGL viewport state.
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
		this.viewport = new java.awt.Rectangle(viewportArray[0],
				viewportArray[1], viewportArray[2], viewportArray[3]);
		// Compute the current clip plane distances.
		double nearDistance = this.nearClipDistance <= 0.0 ? getAutoNearClipDistance()
				: this.nearClipDistance;
		double farDistance = this.farClipDistance <= 0.0 ? getAutoFarClipDistance()
				: this.farClipDistance;
		// Compute the current viewport dimensions.
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0
				: this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0
				: this.viewport.getHeight();
		// Compute the current projection matrix.
		this.projection = Matrix.fromPerspective(this.fieldOfView,
				viewportWidth, viewportHeight, nearDistance, farDistance);
		// Compute the current frustum.
		this.frustum = Frustum.fromPerspective(this.fieldOfView,
				(int) viewportWidth, (int) viewportHeight, nearDistance,
				farDistance);

		//========== load GL matrix state ==========//
		this.viewSupport.loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Clear cached computations.
		this.lastEyePosition = null;
		this.lastEyePoint = null;
		this.lastUpVector = null;
		this.lastForwardVector = null;
		this.lastFrustumInModelCoords = null;
	}

	public Vec4 project(Vec4 modelPoint)
	{
		if (modelPoint == null)
		{
			String message = Logging.getMessage("nullValue.Vec4IsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return this.viewSupport.project(modelPoint, this.modelview,
				this.projection, this.viewport);
	}

	public Vec4 unProject(Vec4 windowPoint)
	{
		if (windowPoint == null)
		{
			String message = Logging.getMessage("nullValue.Vec4IsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return this.viewSupport.unProject(windowPoint, this.modelview,
				this.projection, this.viewport);
	}

	public Line computeRayFromScreenPoint(double x, double y)
	{
		return this.viewSupport.computeRayFromScreenPoint(x, y, this.modelview,
				this.projection, this.viewport);
	}

	public Position computePositionFromScreenPoint(double x, double y)
	{
		if (this.globe != null)
		{
			Line ray = computeRayFromScreenPoint(x, y);
			if (ray != null)
				return this.globe.getIntersectionPosition(ray);
		}

		return null;
	}

	public double computePixelSizeAtDistance(double distance)
	{
		return this.viewSupport.computePixelSizeAtDistance(distance,
				this.fieldOfView, this.viewport);
	}

	public double computeHorizonDistance()
	{
		double horizon = 0;
		Position eyePos = computeEyePositionFromModelview();
		if (eyePos != null)
		{
			horizon = computeHorizonDistance(eyePos);
		}

		return horizon;
	}

	private double computeHorizonDistance(Position eyePosition)
	{
		if (this.globe != null && eyePosition != null)
		{
			double elevation = eyePosition.getElevation();
			double elevationAboveSurface = this.viewSupport
					.computeElevationAboveSurface(this.dc, eyePosition);
			return this.viewSupport.computeHorizonDistance(this.globe, Math
					.max(elevation, elevationAboveSurface));
		}

		return 0;
	}

	private Position computeEyePositionFromModelview()
	{
		if (this.globe != null)
		{
			Vec4 eyePoint = Vec4.UNIT_W.transformBy4(this.modelviewInv);
			return this.globe.computePositionFromPoint(eyePoint);
		}

		return Position.ZERO;
	}

	private void setModelCoordinates(
			RollOrbitViewModel.RollModelCoordinates modelCoords)
	{
		if (modelCoords != null)
		{
			if (modelCoords.getCenterPosition() != null)
				this.center = normalizedPosition(modelCoords
						.getCenterPosition());
			if (modelCoords.getHeading() != null)
				this.heading = normalizedHeading(modelCoords.getHeading());
			if (modelCoords.getPitch() != null)
				this.pitch = modelCoords.getPitch();
			if (modelCoords.getRoll() != null)
				this.roll = modelCoords.getRoll();
			this.zoom = modelCoords.getZoom();
		}
	}

	private boolean validateModelCoordinates(
			RollOrbitViewModel.RollModelCoordinates modelCoords)
	{
		return (modelCoords != null && modelCoords.getCenterPosition() != null
				&& modelCoords.getCenterPosition().getLatitude().degrees >= -90
				&& modelCoords.getCenterPosition().getLatitude().degrees <= 90
				&& modelCoords.getHeading() != null
				&& modelCoords.getPitch() != null
				&& modelCoords.getPitch().degrees >= 0
				&& modelCoords.getPitch().degrees <= 180
				&& modelCoords.getRoll() != null && modelCoords.getZoom() >= 0);
	}

	public String getRestorableState()
	{
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
		// Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
		if (rs == null)
			return null;

		rs.addStateValueAsBoolean("detectCollisions", isDetectCollisions());

		if (this.fieldOfView != null)
			rs.addStateValueAsDouble("fieldOfView", this.fieldOfView.degrees);

		if (this.nearClipDistance > 0)
			rs.addStateValueAsDouble("nearClipDistance", this.nearClipDistance);

		if (this.farClipDistance > 0)
			rs.addStateValueAsDouble("farClipDistance", this.farClipDistance);

		if (this.center != null)
		{
			RestorableSupport.StateObject so = rs.addStateObject("center");
			if (so != null)
			{
				rs.addStateValueAsDouble(so, "latitude", this.center
						.getLatitude().degrees);
				rs.addStateValueAsDouble(so, "longitude", this.center
						.getLongitude().degrees);
				rs.addStateValueAsDouble(so, "elevation", this.center
						.getElevation());
			}
		}

		if (this.heading != null)
			rs.addStateValueAsDouble("heading", this.heading.degrees);

		if (this.pitch != null)
			rs.addStateValueAsDouble("pitch", this.pitch.degrees);
		
		if (this.roll != null)
			rs.addStateValueAsDouble("roll", this.roll.degrees);

		rs.addStateValueAsDouble("zoom", this.zoom);

		return rs.getStateAsXml();
	}

	public void restoreState(String stateInXml)
	{
		if (stateInXml == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		RestorableSupport rs;
		try
		{
			rs = RestorableSupport.parse(stateInXml);
		}
		catch (Exception e)
		{
			// Parsing the document specified by stateInXml failed.
			String message = Logging.getMessage(
					"generic.ExceptionAttemptingToParseStateXml", stateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}

		Boolean b = rs.getStateValueAsBoolean("detectCollisions");
		if (b != null)
			setDetectCollisions(b);

		// Restore the center property only if all parts are available.
		// We will not restore a partial center (for example, just latitude).
		RestorableSupport.StateObject so = rs.getStateObject("center");
		if (so != null)
		{
			Double lat = rs.getStateValueAsDouble(so, "latitude");
			Double lon = rs.getStateValueAsDouble(so, "longitude");
			Double ele = rs.getStateValueAsDouble(so, "elevation");
			if (lat != null && lon != null)
				setCenterPosition(Position.fromDegrees(lat, lon,
						(ele != null ? ele : 0)));
		}

		Double d = rs.getStateValueAsDouble("heading");
		if (d != null)
			setHeading(Angle.fromDegrees(d));

		d = rs.getStateValueAsDouble("pitch");
		if (d != null)
			setPitch(Angle.fromDegrees(d));
		
		d = rs.getStateValueAsDouble("roll");
		if (d != null)
			setRoll(Angle.fromDegrees(d));

		d = rs.getStateValueAsDouble("zoom");
		if (d != null)
			setZoom(d);

		d = rs.getStateValueAsDouble("fieldOfView");
		if (d != null)
			setFieldOfView(Angle.fromDegrees(d));

		d = rs.getStateValueAsDouble("nearClipDistance");
		if (d != null)
			setNearClipDistance(d);

		d = rs.getStateValueAsDouble("farClipDistance");
		if (d != null)
			setFarClipDistance(d);
	}

	public Angle getRoll()
	{
		return this.roll;
	}

	public void setRoll(Angle roll)
	{
		if (roll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		this.roll = roll;
	}

	public void setEye(LatLon eye, Angle heading, Angle pitch, Angle roll,
			double zoom)
	{
		if (eye == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (heading == null || pitch == null || roll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (this.globe == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Position eyePosition = new Position(eye, zoom);
		Vec4 newEyePoint = this.globe.computePointFromPosition(eyePosition);

		if (newEyePoint == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromRotationX(roll));
		transform = transform
				.multiply(Matrix.fromRotationX(pitch.multiply(-1)));
		transform = transform.multiply(Matrix.fromRotationZ(heading));

		Vec4 direction = newEyePoint.getNegative3().normalize3();
		Vec4 left = direction.cross3(Vec4.UNIT_Y);

		Quaternion qh = Quaternion.fromAxisAngle(heading, direction);
		left = left.transformBy3(qh);

		Quaternion qp = Quaternion.fromAxisAngle(pitch, left);
		direction = direction.transformBy3(qp);

		Quaternion qr = Quaternion.fromAxisAngle(roll, direction);
		left = left.transformBy3(qr);

		Vec4 up = left.cross3(direction);
		Vec4 newCenterPoint = newEyePoint.add3(direction);

		RollOrbitViewModel.RollModelCoordinates modelCoords = this.orbitViewModel
				.computeModelCoordinates(this.globe, newEyePoint,
						newCenterPoint, up);

		if (modelCoords == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.center = normalizedPosition(modelCoords.getCenterPosition());
		this.heading = normalizedHeading(heading);
		this.pitch = pitch;
		this.roll = roll;
		this.zoom = modelCoords.getZoom();
	}

	public void setCenter(LatLon center, Angle heading, Angle pitch,
			Angle roll, double zoom)
	{
		setCenterPosition(new Position(center, 0));
		setZoom(zoom);
		setHeading(heading);
		setPitch(pitch);
		setRoll(roll);
	}
}
