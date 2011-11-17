package gov.nasa.worldwind.ogc.custom.kml.impl;

import gov.nasa.worldwind.custom.render.Ardor3DModel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLLocation;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLOrientation;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.KMLScale;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;
import gov.nasa.worldwind.render.DrawContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ardor3d.scenegraph.Node;

public class KMLModelImpl extends Ardor3DModel implements KMLRenderable
{
	private static final Log LOG = LogFactory.getLog(KMLModelImpl.class);
	
	protected KMLRoot kmlroot;
	protected String href;

	public KMLModelImpl(KMLTraversalContext tc, final KMLModel kmlmodel)
	{
		super((String) kmlmodel.getRoot().resolveReference(kmlmodel.getLink().getHref()), locationToPosition(kmlmodel
				.getLocation()));
		
		this.href = kmlmodel.getLink().getHref();
		this.kmlroot = kmlmodel.getRoot();
		
		String altitudeMode = kmlmodel.getAltitudeMode();
		if(altitudeMode != null)
		{
			setAltitudeMode(KMLUtil.convertAltitudeMode(altitudeMode));
		}

		KMLOrientation orientation = kmlmodel.getOrientation();
		if (orientation != null)
		{
			Double pitch = orientation.getTilt(), roll = orientation.getRoll(), yaw = orientation.getHeading();
			setPitch(pitch != null ? -pitch : 0);
			setRoll(roll != null ? roll : 0);
			setYaw(yaw != null ? yaw : 0);
		}

		KMLScale scale = kmlmodel.getScale();
		if (scale != null)
		{
			Double sx = scale.getX(), sy = scale.getY(), sz = scale.getZ();
			setScalex(sx != null ? sx : 1);
			setScaley(sy != null ? sy : 1);
			setScalez(sz != null ? sz : 1);
		}
	}

	protected static Position locationToPosition(KMLLocation location)
	{
		if (location == null)
			return null;
		
		Double lat = location.getLatitude(), lon = location.getLongitude(), alt = location.getAltitude();
		return new Position(Angle.fromDegrees(lat != null ? lat : 0), Angle.fromDegrees(lon != null ? lon : 0),
				alt != null ? alt : 0);
	}

	@Override
	protected void loadModel()
	{
		try
		{
			Node node = KMLArdorColladaLoader.loadColladaModel(model.getSource(), href, kmlroot);
			nodeRef.set(node);
		}
		catch (Exception e)
		{
			LOG.error("Failed to load model", e);
		}
	}

	@Override
	public void preRender(KMLTraversalContext tc, DrawContext dc)
	{
		// Nothing earth shattering to do in pre-render
	}

	@Override
	public void render(KMLTraversalContext tc, DrawContext dc)
	{
		super.render(dc);
	}
}
