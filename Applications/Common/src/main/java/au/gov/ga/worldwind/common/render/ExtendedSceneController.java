package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;

/**
 * {@link SceneController} that uses a separate {@link Tessellator} to generate
 * a separate set of flat geometry, used by layers that are rendered onto a flat
 * surface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ExtendedSceneController extends AbstractSceneController
{
	private FlatRectangularTessellator flatTessellator = new FlatRectangularTessellator();

	@Override
	protected void createTerrain(DrawContext dc)
	{
		super.createTerrain(dc);

		if (dc instanceof ExtendedDrawContext)
		{
			ExtendedDrawContext edc = (ExtendedDrawContext) dc;
			if (edc.getFlatSurfaceGeometry() == null)
			{
				if (dc.getModel() != null && dc.getModel().getGlobe() != null)
				{
					SectorGeometryList sgl = flatTessellator.tessellate(dc);
					edc.setFlatSurfaceGeometry(sgl);
				}
			}
		}
	}
}
