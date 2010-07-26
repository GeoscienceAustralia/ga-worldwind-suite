package au.gov.ga.worldwind.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

public class ElevationModelFactory extends BasicElevationModelFactory
{
	@Override
	protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
	{
		ElevationModel em;

		String serviceName = WWXML.getText(domElement, "Service/@serviceName");

		if (serviceName.equals("Offline"))
		{
			em = new ExtendedElevationModel(domElement, params);
		}
		else if (serviceName.equals("WWTileService"))
		{
			em = new ExtendedElevationModel(domElement, params);
		}
		else if (serviceName.equals(OGCConstants.WMS_SERVICE_NAME))
		{
			em = new WMSBasicElevationModel(domElement, params);
		}
		else
		{
			String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
			throw new WWUnrecognizedException(msg);
		}

		return em;
	}
}
