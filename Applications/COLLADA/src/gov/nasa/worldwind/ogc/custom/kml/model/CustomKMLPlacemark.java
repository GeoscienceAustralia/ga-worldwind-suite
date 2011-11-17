package gov.nasa.worldwind.ogc.custom.kml.model;

import gov.nasa.worldwind.ogc.custom.kml.impl.KMLModelImpl;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class CustomKMLPlacemark extends KMLPlacemark {

	protected KMLModel model;

	public CustomKMLPlacemark(String namespaceURI) {
		super(namespaceURI);
	}
	
	@Override
	protected void doAddEventContent(Object o, XMLEventParserContext ctx,
			XMLEvent event, Object... args) throws XMLStreamException {
		if(o instanceof KMLModel)
			this.setModel((KMLModel)o);
		else
			super.doAddEventContent(o, ctx, event, args);
	}

	private void setModel(KMLModel o) {
		this.model = o;
	}

	public KMLModel getModel() {
		return model;
	}

	@Override
	protected void doPreRender(KMLTraversalContext tc, DrawContext dc) {
		try {
			if (this.getRenderables() == null)
				this.initializeModel(tc, this.getModel());
			super.doPreRender(tc, dc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void initializeModel(final KMLTraversalContext tc, final KMLModel model) {
        if (model == null)
            return;

        if (this.getRenderables() == null)
            this.renderables = new ArrayList<KMLRenderable>(1); // most common case is one renderable

        Runnable runnable = new Runnable()
        {
			@Override
			public void run()
			{
				addRenderable(selectModelRenderable(tc, model));
			}
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
	}

	protected KMLRenderable selectModelRenderable(KMLTraversalContext tc,
			KMLModel model) {
		return new KMLModelImpl(tc, model);
	}
}
