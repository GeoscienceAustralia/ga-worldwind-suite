package au.gov.ga.worldwind.animator.layers.accessible;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.layers.accessible.AccessibleBasicTiledImageLayer.FileLockAccessor;

/**
 * {@link WMSTiledImageLayer} that provides access to its internal filelock
 * object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AccessibleWMSTiledImageLayer extends WMSTiledImageLayer
{
	private final FileLockAccessor fileLockAccessor = new FileLockAccessor(this);

	public AccessibleWMSTiledImageLayer(AVList params)
	{
		super(params);
	}

	public AccessibleWMSTiledImageLayer(Document dom, AVList params)
	{
		super(dom, params);
	}

	public AccessibleWMSTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	public AccessibleWMSTiledImageLayer(WMSCapabilities caps, AVList params)
	{
		super(caps, params);
	}

	public AccessibleWMSTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	protected Object getFileLock()
	{
		return fileLockAccessor.getFileLock();
	}
}
