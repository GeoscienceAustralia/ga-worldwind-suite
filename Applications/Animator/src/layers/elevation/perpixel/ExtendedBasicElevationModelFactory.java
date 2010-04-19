package layers.elevation.perpixel;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.Capabilities;

import org.w3c.dom.Element;

public class ExtendedBasicElevationModelFactory extends BasicElevationModelFactory
{
	@Override
	protected ElevationModel createNonCompoundModel(Element domElement,
			AVList params)
	{
		ElevationModel em;

        String serviceName = WWXML.getText(domElement, "Service/@serviceName");

        if (serviceName.equals("Offline"))
        {
            em = new ExtendedBasicElevationModel(domElement, params);
        }
        else if (serviceName.equals("WWTileService"))
        {
            em = new ExtendedBasicElevationModel(domElement, params);
        }
        else if (serviceName.equals(Capabilities.WMS_SERVICE_NAME))
        {
            em = new ExtendedWMSBasicElevationModel(domElement, params);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
            throw new WWUnrecognizedException(msg);
        }

        return em;
	}
}
