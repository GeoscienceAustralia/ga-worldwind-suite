package au.gov.ga.worldwind.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

public class ElevationModelFactory extends BasicElevationModelFactory
{
	//functions copied from superclass, replacing the model objects with our extensions 

	@Override
	protected CompoundElevationModel createCompoundModel(Element[] elements, AVList params)
	{
		BoundedCompoundElevationModel compoundModel = new BoundedCompoundElevationModel();

		if (elements == null || elements.length == 0)
			return compoundModel;

		for (Element element : elements)
		{
			try
			{
				ElevationModel em = this.doCreateFromElement(element, params);
				if (em != null)
					compoundModel.addElevationModel(em);
			}
			catch (Exception e)
			{
				String msg = Logging.getMessage("ElevationModel.ExceptionCreatingElevationModel");
				Logging.logger().log(java.util.logging.Level.WARNING, msg, e);
			}
		}

		return compoundModel;
	}

	@Override
	protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
	{
		ElevationModel em;

		String serviceName = WWXML.getText(domElement, "Service/@serviceName");

		if ("Offline".equals(serviceName))
		{
			em = new ExtendedElevationModel(domElement, params);
		}
		else if ("WWTileService".equals(serviceName))
		{
			em = new ExtendedElevationModel(domElement, params);
		}
		else if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			em = new BoundedWMSBasicElevationModel(domElement, params);
		}
		else
		{
			String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
			throw new WWUnrecognizedException(msg);
		}

		return em;
	}
}
