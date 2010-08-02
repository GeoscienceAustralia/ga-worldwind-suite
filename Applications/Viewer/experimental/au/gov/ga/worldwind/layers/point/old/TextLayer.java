package au.gov.ga.worldwind.layers.point.old;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;
import gov.nasa.worldwind.render.Pedestal;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.BasicQuadTree;
import gov.nasa.worldwind.util.Logging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TextLayer extends AbstractLayer
{
	protected final BasicQuadTree<GeographicText> icons =
			new BasicQuadTree<GeographicText>(8, Sector.FULL_SPHERE, null);
	protected Iterable<GeographicText> iconsOverride;
	protected GeographicTextRenderer iconRenderer = new GeographicTextRenderer();
	private Pedestal pedestal;
	private boolean regionCulling = true;

	// These enable the render pass to use the same non-culled icons computed by the pick pass.
	protected Set<GeographicText> lastActiveIcons;
	protected long frameId;

	/** Creates a new <code>IconLayer</code> with an empty collection of Icons. */
	public TextLayer()
	{
	}

	/**
	 * Adds the specified <code>icon</code> to this layer's internal collection.
	 * If this layer's internal collection has been overriden with a call to
	 * {@link #setIcons}, this will throw an exception.
	 * 
	 * @param text
	 *            Icon to add.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>icon</code> is null.
	 * @throws IllegalStateException
	 *             If a custom Iterable has been specified by a call to
	 *             <code>setIcons</code>.
	 */
	public void addIcon(GeographicText text)
	{
		if (text == null)
		{
			String msg = Logging.getMessage("nullValue.Icon");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (this.iconsOverride != null)
		{
			String msg = Logging.getMessage("generic.LayerIsUsingCustomIterable");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		this.icons.add(text, text.getPosition().asDegreesArray());
	}

	/**
	 * Adds the contents of the specified <code>icons</code> to this layer's
	 * internal collection. If this layer's internal collection has been
	 * overriden with a call to {@link #setIcons}, this will throw an exception.
	 * 
	 * @param icons
	 *            Icons to add.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>icons</code> is null.
	 * @throws IllegalStateException
	 *             If a custom Iterable has been specified by a call to
	 *             <code>setIcons</code>.
	 */
	public void addIcons(Iterable<GeographicText> icons)
	{
		if (icons == null)
		{
			String msg = Logging.getMessage("nullValue.IterableIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (this.iconsOverride != null)
		{
			String msg = Logging.getMessage("generic.LayerIsUsingCustomIterable");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		for (GeographicText icon : icons)
		{
			// Internal list of icons does not accept null values.
			if (icon != null)
				this.icons.add(icon, icon.getPosition().asDegreesArray());
		}
	}

	/**
	 * Removes the specified <code>icon</code> from this layer's internal
	 * collection, if it exists. If this layer's internal collection has been
	 * overriden with a call to {@link #setIcons}, this will throw an exception.
	 * 
	 * @param icon
	 *            Icon to remove.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>icon</code> is null.
	 * @throws IllegalStateException
	 *             If a custom Iterable has been specified by a call to
	 *             <code>setIcons</code>.
	 */
	public void removeIcon(GeographicText icon)
	{
		if (icon == null)
		{
			String msg = Logging.getMessage("nullValue.Icon");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (this.iconsOverride != null)
		{
			String msg = Logging.getMessage("generic.LayerIsUsingCustomIterable");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		this.icons.remove(icon);
	}

	/**
	 * Clears the contents of this layer's internal Icon collection. If this
	 * layer's internal collection has been overriden with a call to
	 * {@link #setIcons}, this will throw an exception.
	 * 
	 * @throws IllegalStateException
	 *             If a custom Iterable has been specified by a call to
	 *             <code>setIcons</code>.
	 */
	public void removeAllIcons()
	{
		if (this.iconsOverride != null)
		{
			String msg = Logging.getMessage("generic.LayerIsUsingCustomIterable");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		this.clearIcons();
	}

	protected void clearIcons()
	{
		if (this.icons != null)
			this.icons.clear();
	}

	/**
	 * Returns the Iterable of Icons currently in use by this layer. If the
	 * caller has specified a custom Iterable via {@link #setIcons}, this will
	 * returns a reference to that Iterable. If the caller passed
	 * <code>setIcons</code> a null parameter, or if <code>setIcons</code> has
	 * not been called, this returns a view of this layer's internal collection
	 * of Icons.
	 * 
	 * @return Iterable of currently active Icons.
	 */
	public Iterable<GeographicText> getIcons()
	{
		if (this.icons != null)
			return this.icons;

		return Collections.emptyList();
	}

	/**
	 * Indicates whether the layer culls icons whose latitude-longitude location
	 * is outside the visible terrain area. See
	 * {@link #setRegionCulling(boolean)} for a fuller description of region
	 * culling.
	 * 
	 * @return regionCulling true if region culling is performed, otherwise
	 *         false.
	 */
	public boolean isRegionCulling()
	{
		return this.regionCulling;
	}

	/**
	 * Indicates whether the layer culls icons whose latitude-longitude location
	 * is outside the visible terrain area. This typically provides better
	 * performance when some icons are not in view. However, it might remove
	 * icons at high altitude over the horizon.
	 * 
	 * @param regionCulling
	 *            true if region culling is performed, otherwise false.
	 */
	public void setRegionCulling(boolean regionCulling)
	{
		this.regionCulling = regionCulling;
	}

	/**
	 * Returns the Iterable of currently active Icons. If the caller has
	 * specified a custom Iterable via {@link #setIcons}, this will returns a
	 * reference to that Iterable. If the caller passed <code>setIcons</code> a
	 * null parameter, or if <code>setIcons</code> has not been called, this
	 * returns a view of this layer's internal collection of Icons, culled to
	 * eliminate those outside the current sector geometry.
	 * 
	 * @param dc
	 *            a current DrawContext.
	 * 
	 * @return Iterable of currently active Icons.
	 */
	protected Iterable<GeographicText> getActiveIcons(DrawContext dc)
	{
		if (this.iconsOverride != null)
			return this.iconsOverride;

		if (this.lastActiveIcons != null && this.frameId == dc.getFrameTimeStamp())
			return this.lastActiveIcons;

		this.lastActiveIcons = null;

		if (!this.isRegionCulling())
			return this.icons;

		SectorGeometryList sgList = dc.getSurfaceGeometry();
		if (sgList == null || sgList.size() == 0)
			return Collections.emptyList();

		this.lastActiveIcons = this.icons.getItemsInRegions(sgList, new HashSet<GeographicText>());
		this.frameId = dc.getFrameTimeStamp();

		return this.lastActiveIcons;
	}

	/**
	 * Overrides the collection of currently active Icons with the specified
	 * <code>iconIterable</code>. This layer will maintain a reference to
	 * <code>iconIterable</code> strictly for picking and rendering. This layer
	 * will not modify the Iterable reference. However, this will clear the
	 * internal collection of Icons, and will prevent any modification to its
	 * contents via <code>addIcon, addIcons, or removeIcons</code>.
	 * <p/>
	 * If the specified <code>iconIterable</code> is null, this layer will
	 * revert to maintaining its internal collection.
	 * 
	 * @param iconIterable
	 *            Iterable to use instead of this layer's internal collection,
	 *            or null to use this layer's internal collection.
	 */
	public void setIcons(Iterable<GeographicText> iconIterable)
	{
		this.iconsOverride = iconIterable;
		// Clear the internal collection of Icons.
		clearIcons();
	}

	/**
	 * Returns the <code>Pedestal</code> used by this layers internal
	 * <code>IconRenderer</code>.
	 * 
	 * @return <code>Pedestal</code> used by this layers internal
	 *         <code>IconRenderer</code>.
	 */
	public Pedestal getPedestal()
	{
		return pedestal;
	}

	/**
	 * Sets the <code>Pedestal</code> used by this layers internal
	 * <code>IconRenderer</code>.
	 * 
	 * @param pedestal
	 *            <code>Pedestal</code> to be used by this layers internal
	 *            <code>IconRenderer</code>.
	 */
	public void setPedestal(Pedestal pedestal)
	{
		this.pedestal = pedestal;
	}

	/**
	 * Indicates whether horizon clipping is performed.
	 * 
	 * @return <code>true</code> if horizon clipping is performed, otherwise
	 *         <code>false</code>.
	 * 
	 * @see #setHorizonClippingEnabled(boolean)
	 */
	/*public boolean isHorizonClippingEnabled()
	{
		return this.iconRenderer.isHorizonClippingEnabled();
	}*/

	/**
	 * Indicates whether to render icons beyond the horizon. If view culling is
	 * enabled, the icon is also tested for view volume inclusion. The default
	 * is <code>false</code>, horizon clipping is not performed.
	 * 
	 * @param horizonClippingEnabled
	 *            <code>true</code> if horizon clipping should be performed,
	 *            otherwise <code>false</code>.
	 * 
	 * @see #setViewClippingEnabled(boolean)
	 */
	/*public void setHorizonClippingEnabled(boolean horizonClippingEnabled)
	{
		this.iconRenderer.setHorizonClippingEnabled(horizonClippingEnabled);
	}*/

	/**
	 * Indicates whether view volume clipping is performed.
	 * 
	 * @return <code>true</code> if view volume clipping is performed, otherwise
	 *         <code>false</code>.
	 * 
	 * @see #setViewClippingEnabled(boolean)
	 */
	/*public boolean isViewClippingEnabled()
	{
		return this.iconRenderer.isViewClippingEnabled();
	}*/

	/**
	 * Indicates whether to render icons outside the view volume. This is
	 * primarily to control icon visibility beyond the far view clipping plane.
	 * Some important use cases demand that clipping not be performed. If
	 * horizon clipping is enabled, the icon is also tested for horizon
	 * clipping. The default is <code>false</code>, view volume clipping is not
	 * performed.
	 * 
	 * @param viewClippingEnabled
	 *            <code>true</code> if view clipping should be performed,
	 *            otherwise <code>false</code>.
	 * 
	 * @see #setHorizonClippingEnabled(boolean)
	 */
	/*public void setViewClippingEnabled(boolean viewClippingEnabled)
	{
		this.iconRenderer.setViewClippingEnabled(viewClippingEnabled);
	}*/

	/**
	 * Indicates whether picking volume clipping is performed.
	 * 
	 * @return <code>true</code> if picking volume clipping is performed,
	 *         otherwise <code>false</code>.
	 * 
	 * @see #setViewClippingEnabled(boolean)
	 */
	/*public boolean isPickFrustumClippingEnabled()
	{
		return this.iconRenderer.isPickFrustumClippingEnabled();
	}*/

	/**
	 * Indicates whether to render icons outside the picking volume when in pick
	 * mode. This increases performance by only drawing the icons within the
	 * picking volume when picking is enabled. Some important use cases demand
	 * that clipping not be performed. The default is <code>false</code>,
	 * picking volume clipping is not performed.
	 * 
	 * @param pickFrustumClippingEnabled
	 *            <code>true</code> if picking clipping should be performed,
	 *            otherwise <code>false</code>.
	 */
	/*public void setPickFrustumClippingEnabled(boolean pickFrustumClippingEnabled)
	{
		this.iconRenderer.setPickFrustumClippingEnabled(pickFrustumClippingEnabled);
	}*/

	/**
	 * Indicates whether an icon's elevation is treated as an offset from the
	 * terrain or an absolute elevation above sea level.
	 * 
	 * @return <code>true</code> if icon elevations are treated as absolute,
	 *         <code>false</code> if they're treated as offsets from the
	 *         terrain.
	 */
	/*public boolean isAlwaysUseAbsoluteElevation()
	{
		return this.iconRenderer.isAlwaysUseAbsoluteElevation();
	}*/

	/**
	 * Normally, an icon's elevation is treated as an offset from the terrain
	 * when it is less than the globe's maximum elevation. Setting
	 * #setAlwaysUseAbsoluteElevation to <code>true</code> causes the elevation
	 * to be treated as an absolute elevation above sea level.
	 * 
	 * @param alwaysUseAbsoluteElevation
	 *            <code>true</code> to treat icon elevations as absolute,
	 *            <code>false</code> to treat them as offsets from the terrain.
	 */
	/*public void setAlwaysUseAbsoluteElevation(boolean alwaysUseAbsoluteElevation)
	{
		this.iconRenderer.setAlwaysUseAbsoluteElevation(alwaysUseAbsoluteElevation);
	}*/

	/**
	 * Opacity is not applied to layers of this type. The icon image is assumed
	 * to indicates its opacity.
	 * 
	 * @param opacity
	 *            the current opacity value, which is ignored by this layer.
	 */
	@Override
	public void setOpacity(double opacity)
	{
		super.setOpacity(opacity);
	}

	/**
	 * Returns the layer's opacity value, which is ignored by this layer the
	 * icon's image is assumed to indicate its opacity.
	 * 
	 * @return The layer opacity, a value between 0 and 1.
	 */
	@Override
	public double getOpacity()
	{
		return super.getOpacity();
	}

	/**
	 * Indicates whether icons are picked as a batch and therefore a
	 * {@link gov.nasa.worldwind.event.SelectEvent} will contain only one icon
	 * from the layer. Batch picking is much faster than individual picking, so
	 * this attribute should be used judiciously.
	 * 
	 * @return true if batch picking is allowed, otherwise false.
	 * 
	 * @see #setAllowBatchPicking(boolean)
	 */
	/*public boolean isAllowBatchPicking()
	{
		return this.iconRenderer.isAllowBatchPicking();
	}*/

	/**
	 * Specifies whether batch picking is allowed. If so, a
	 * {@link gov.nasa.worldwind.event.SelectEvent} from the layer will contain
	 * only one icon even if several overlapping icons are at the pick point.
	 * Batch picking is much faster than individual picking so the default value
	 * is true.
	 * 
	 * @param allowBatchPicking
	 *            true if batch picking is allowed, otherwise false.
	 */
	/*public void setAllowBatchPicking(boolean allowBatchPicking)
	{
		this.iconRenderer.setAllowBatchPicking(allowBatchPicking);
	}*/

	/*@Override
	protected void doPick(DrawContext dc, java.awt.Point pickPoint)
	{
		this.iconRenderer.setPedestal(this.pedestal);
		this.iconRenderer.pick(dc, getActiveIcons(dc), pickPoint, this);
	}*/

	@Override
	protected void doRender(DrawContext dc)
	{
		//this.iconRenderer.setPedestal(this.pedestal);
		this.iconRenderer.render(dc, getActiveIcons(dc));
	}

	@Override
	public String toString()
	{
		return Logging.getMessage("layers.IconLayer.Name");
	}
}
