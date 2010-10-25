package au.gov.ga.worldwind.common.layers.geometry.types.airspace;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Curtain;
import gov.nasa.worldwind.render.airspaces.Geometry;

import javax.media.opengl.GL;

/**
 * An extension of the {@link Curtain} airspace that can render the 
 * generating shape as a line at the upper and lower elevations.
 */
public class ShapeOutlineCurtain extends Curtain
{
	boolean renderCurtain = true;
	boolean renderUpperShapeOutline = true;
	boolean renderLowerShapeOutline = false;
	
	public ShapeOutlineCurtain()
	{
		super();
	}

	public ShapeOutlineCurtain(AirspaceAttributes attributes)
	{
		super(attributes);
	}

	public ShapeOutlineCurtain(Iterable<? extends LatLon> locations)
	{
		super(locations);
	}

	@Override
	protected void doRenderGeometry(DrawContext dc, String drawStyle)
	{
		if (renderCurtain)
		{
			super.doRenderGeometry(dc, drawStyle);
		}
		if (renderUpperShapeOutline)
		{
			renderUpperShapeOutline(dc);
		}
		if (renderLowerShapeOutline)
		{
			renderLowerShapeOutline(dc);
		}
	}

	private void renderUpperShapeOutline(DrawContext dc)
	{
		Vec4 refCenter = computeReferenceCenter(dc);
		CurtainGeometry curtain = getCurtainGeometry(dc, locations.size(), locations.toArray(new LatLon[locations.size()]), 
													 pathType, splitThreshold, getAltitudes(), isTerrainConforming(), 
													 refCenter);
		
		int count = curtain.getVertexGeometry().getCount(2) - 2;
		int[] shapeIndices = new int[count];
		int i = 0;
		while (i < count)
		{
			shapeIndices[i] = i+1;
			shapeIndices[i+1] = i+3;
			i+=2;
		}
		
		Geometry shapeOutlineElementGeometry = new Geometry();
		shapeOutlineElementGeometry.setElementData(GL.GL_LINES, shapeIndices.length, shapeIndices);
		
		dc.getView().pushReferenceCenter(dc, refCenter);
		getAttributes().applyOutline(dc, false);
		getRenderer().drawGeometry(dc, shapeOutlineElementGeometry, curtain.getVertexGeometry());
		dc.getView().popReferenceCenter(dc);
	}
	
	private void renderLowerShapeOutline(DrawContext dc)
	{
		// TODO Auto-generated method stub
		
	}
}
