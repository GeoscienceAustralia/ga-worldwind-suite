package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.io.Serializable;

import au.gov.ga.worldwind.viewer.panels.layers.INode;

/**
 * Represents a bookmarked 'place', which stores a latlon and label for a saved
 * position. Also supports storing camera state, enabled layers, and vertical
 * exaggeration.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Place implements Serializable
{
	private String label = "";
	private LatLon latlon = LatLon.ZERO;
	private boolean visible = true;
	private double minZoom = -1;
	private double maxZoom = -1;
	private boolean saveCamera = false;
	private Position eyePosition = null;
	private Vec4 upVector = null;
	private boolean excludeFromPlaylist = false;
	private INode layers = null;
	private Double verticalExaggeration = null;

	public Place()
	{
	}

	public Place(String label, LatLon latlon)
	{
		this(label, latlon, -1);
	}

	public Place(String label, LatLon latlon, double minZoom)
	{
		this.label = label;
		this.latlon = latlon;
		this.minZoom = minZoom;
	}

	public Place(Place place)
	{
		setValuesFrom(place);
	}

	/**
	 * Copy the values from the given place into this place.
	 * 
	 * @param place
	 */
	public void setValuesFrom(Place place)
	{
		this.label = place.label;
		this.latlon = place.latlon;
		this.visible = place.visible;
		this.minZoom = place.minZoom;
		this.maxZoom = place.maxZoom;
		this.saveCamera = place.saveCamera;
		this.eyePosition = place.eyePosition;
		this.upVector = place.upVector;
		this.excludeFromPlaylist = place.excludeFromPlaylist;
		this.layers = place.layers;
		this.verticalExaggeration = place.verticalExaggeration;
	}

	/**
	 * @return Label of this place
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Set the label of this place
	 * 
	 * @param label
	 */
	public void setLabel(String label)
	{
		if (label == null)
			label = "";
		this.label = label;
	}

	/**
	 * @return The latitude/longitude of this place
	 */
	public LatLon getLatLon()
	{
		return latlon;
	}

	/**
	 * Set the latitude/longitude of this place
	 * 
	 * @param latlon
	 */
	public void setLatLon(LatLon latlon)
	{
		if (latlon == null)
			latlon = LatLon.ZERO;
		this.latlon = latlon;
	}

	/**
	 * @return Is this place visible (or, should an annotation be rendered at
	 *         this place's position)?
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Set the visibility of this place
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * @return The minimum zoom at which to fade-on this place (ignored if
	 *         negative)
	 */
	public double getMinZoom()
	{
		return minZoom;
	}

	/**
	 * Set the minimum zoom at which to fade-on this place
	 * 
	 * @param minZoom
	 */
	public void setMinZoom(double minZoom)
	{
		this.minZoom = minZoom;
	}

	/**
	 * @return The maximum zoom at which to fade-off this place (ignored if
	 *         negative)
	 */
	public double getMaxZoom()
	{
		return maxZoom;
	}

	/**
	 * Set the maximum zoom at which to fade-off this place
	 * 
	 * @param maxZoom
	 */
	public void setMaxZoom(double maxZoom)
	{
		this.maxZoom = maxZoom;
	}

	/**
	 * @return Does this place have camera information stored?
	 */
	public boolean isSaveCamera()
	{
		return saveCamera;
	}

	/**
	 * Set whether this place has camera information stored
	 * 
	 * @param saveCamera
	 */
	public void setSaveCamera(boolean saveCamera)
	{
		this.saveCamera = saveCamera;
	}

	/**
	 * @return Camera/eye position stored with this place
	 */
	public Position getEyePosition()
	{
		return eyePosition;
	}

	/**
	 * Set the camera/eye position for this place
	 * 
	 * @param eyePosition
	 */
	public void setEyePosition(Position eyePosition)
	{
		this.eyePosition = eyePosition;
	}

	/**
	 * @return Camera's up vector stored with this place
	 */
	public Vec4 getUpVector()
	{
		return upVector;
	}

	/**
	 * Set the camera's up vector for this place
	 * 
	 * @param upVector
	 */
	public void setUpVector(Vec4 upVector)
	{
		this.upVector = upVector;
	}

	/**
	 * @return Should this place be excluded when playing through the places
	 *         list?
	 */
	public boolean isExcludeFromPlaylist()
	{
		return excludeFromPlaylist;
	}

	/**
	 * Set whether this place should be excluded when playing through the places
	 * list
	 * 
	 * @param excludeFromPlaylist
	 */
	public void setExcludeFromPlaylist(boolean excludeFromPlaylist)
	{
		this.excludeFromPlaylist = excludeFromPlaylist;
	}

	/**
	 * @return Hierarchy of layers associated with this place if any. When
	 *         layers are associated with a place, and the place is selected,
	 *         all referenced layers are faded in, and other layers are faded
	 *         out.
	 */
	public INode getLayers()
	{
		return layers;
	}

	/**
	 * Set the layers associated with this place
	 * 
	 * @param layers
	 */
	public void setLayers(INode layers)
	{
		this.layers = layers;
	}

	/**
	 * @return Vertical exaggeration associated with this place. Null if none.
	 */
	public Double getVerticalExaggeration()
	{
		return verticalExaggeration;
	}

	/**
	 * Set the vertical exaggeration for this place
	 * 
	 * @param verticalExaggeration
	 */
	public void setVerticalExaggeration(Double verticalExaggeration)
	{
		this.verticalExaggeration = verticalExaggeration;
	}
}
