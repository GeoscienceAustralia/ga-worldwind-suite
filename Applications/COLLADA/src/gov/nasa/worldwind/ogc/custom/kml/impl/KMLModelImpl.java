package gov.nasa.worldwind.ogc.custom.kml.impl;

import gov.nasa.worldwind.custom.render.Ardor3DModel;
import gov.nasa.worldwind.formats.models.loader.ArdorColladaLoader;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.resource.ResourceLocator;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.URLResourceSource;

public class KMLModelImpl extends Ardor3DModel implements KMLRenderable{
	private static final Log LOG = LogFactory.getLog(KMLModelImpl.class);
	
	KMLRoot kmlroot;
	
	public KMLModelImpl(KMLTraversalContext tc, final KMLModel kmlmodel)
	{
		super((String)kmlmodel.getRoot().resolveReference(kmlmodel.getLink().getHref()),
				new Position(
				Angle.fromDegrees(kmlmodel.getLocation().getLatitude()),
        		Angle.fromDegrees(kmlmodel.getLocation().getLongitude()),
        		kmlmodel.getLocation().getAltitude()));
		this.kmlroot = kmlmodel.getRoot();
		this.setPitch(kmlmodel.getOrientation().getTilt());
		this.setRoll(kmlmodel.getOrientation().getRoll());
		this.setYaw(kmlmodel.getOrientation().getHeading());

		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new ResourceLocator() {
			
			@Override
			public ResourceSource locateResource(String resourceName) {
				try {
					return new URLResourceSource(new URL((String)kmlmodel.getRoot().resolveReference(resourceName)));
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}
			}
		});

		setScalex(kmlmodel.getScale().getX());
		setScaley(kmlmodel.getScale().getY());
		setScalez(kmlmodel.getScale().getZ());
	}
	
	@Override
	protected void loadModel() {
    	try {
			Node node = KMLArdorColladaLoader.loadColladaModel(model.getSource(),kmlroot);
			nodeRef.set(node);
		} catch (Exception e) {
			LOG.error("Failed to load model", e);
		}
	}
	
	@Override
	public void preRender(KMLTraversalContext tc, DrawContext dc) {
		// TODO Auto-generated method stub
		// Nothing earth shattering to do in pre-render
	}

	@Override
	public void render(KMLTraversalContext tc, DrawContext dc) {
		// TODO Auto-generated method stub
		super.render(dc);
	}


}
