package layers.elevation.textured;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.Capabilities;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ExtendedBasicElevationModelFactory extends BasicFactory
{
	public ElevationModel createFromConfigFile(String fileName)
	{
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String serviceName = null;

		try
		{
			Document doc = WWXML.openDocumentFile(fileName, this.getClass());
			NodeList emNodes = (NodeList) WWXML.makeXPath().evaluate(
					"//ElevationModel", doc, XPathConstants.NODESET);

			if (emNodes == null || emNodes.getLength() == 0)
				throw new IllegalStateException(); // caught and translated in below catch clause

			ElevationModel topModel = null;
			CompoundElevationModel compoundModel = null;

			for (int i = 0; i < emNodes.getLength(); i++)
			{
				Element root = (Element) emNodes.item(i);
				ElevationModel em;

				String modelType = WWXML.getText(root, "@modelType");
				if (modelType != null && modelType.equalsIgnoreCase("Compound"))
				{
					CompoundElevationModel newModel = new CompoundElevationModel();
					if (compoundModel == null)
					{
						compoundModel = newModel;
						topModel = compoundModel;
					}
					else
					{
						// make the new model a child of the current model and make it the new current model
						compoundModel.addElevationModel(newModel);
						compoundModel = newModel;
					}
					continue;
				}

				serviceName = WWXML.getText(root, "Service/@serviceName");

				if (serviceName.equals("WWTileService"))
				{
					em = new ExtendedBasicElevationModel(root, null);
				}
				else if (serviceName.equals(Capabilities.WMS_SERVICE_NAME))
				{
					em = new WMSBasicElevationModel(root, null);
				}
				else
				{
					throw new IllegalStateException(); // caught and translated in below catch clause
				}

				if (compoundModel == null)
					return em; // any compound models must be listed before the first non-compound model
				else
					compoundModel.addElevationModel(em);
			}

			return topModel;
		}
		catch (IllegalStateException e)
		{
			String msg = Logging.getMessage("generic.UnrecognizedServiceName",
					serviceName != null ? serviceName : "null");
			throw new WWUnrecognizedException(msg);
		}
		catch (Exception e)
		{
			String msg = Logging.getMessage(
					"generic.CreationFromConfigurationFileFailed", fileName);
			throw new WWRuntimeException(msg, e);
		}
	}

	protected ElevationModel doCreateFromCapabilities(Capabilities caps,
			AVList params)
	{
		String serviceName = caps.getServiceName();
		if (serviceName == null
				|| !serviceName.equalsIgnoreCase(Capabilities.WMS_SERVICE_NAME))
		{
			String message = Logging.getMessage("WMS.NotWMSService",
					serviceName != null ? serviceName : "null");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		if (params.getStringValue(AVKey.LAYER_NAMES) == null)
		{
			Element[] namedLayers = caps.getNamedLayers();

			if (namedLayers == null || namedLayers.length == 0)
			{
				String message = Logging.getMessage("WMS.NoLayersFound");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}

			// Use the first named layer if no other guidance given
			params.setValue(AVKey.LAYER_NAMES, caps
					.getLayerName(namedLayers[0]));
		}

		return new WMSBasicElevationModel(caps, params);
	}
}
