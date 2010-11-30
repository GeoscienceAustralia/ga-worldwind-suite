package au.gov.ga.worldwind.wmsbrowser.layer;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Default implementation of the {@link WmsLayerExporter} interface
 */
public class WmsLayerExporterImpl implements WmsLayerExporter
{

	private static Logger logger = Logging.logger();
	
	@Override
	public void exportLayer(String fileName, WMSLayerInfo layerInfo)
	{
		Validate.notBlank(fileName, "A file name is required");
		Validate.notNull(layerInfo, "Layer information is required");
		
		try
		{
			File outputFile = new File(fileName);
			FileOutputStream targetStream = new FileOutputStream(outputFile);
			exportLayer(targetStream, layerInfo);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to export layer to file " + fileName, e);
			return;
		}
		
	}
	
	@Override
	public void exportLayer(File outputFile, WMSLayerInfo layerInfo)
	{
		Validate.notNull(outputFile, "A file is required");
		
		try
		{
			FileOutputStream targetStream = new FileOutputStream(outputFile);
			exportLayer(targetStream, layerInfo);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to export layer to file " + outputFile.getAbsolutePath(), e);
			return;
		}
	}
	
	@Override
	public void exportLayer(OutputStream targetStream, WMSLayerInfo layerInfo)
	{
		Validate.notNull(targetStream, "A target output stream is required");
		Validate.notNull(layerInfo, "Layer information is required");
		
		AVList layerParameters = WMSTiledImageLayer.wmsGetParamsFromCapsDoc((WMSCapabilities)layerInfo.getCaps(), layerInfo.getParams());
		
		Document document = WWXML.createDocumentBuilder(false).newDocument();
		
		Element rootElement = addRootElement(document);
		document.appendChild(rootElement);
		
		addGeneratedComment(rootElement);
		addDisplayName(layerParameters, rootElement);
		addService(layerParameters, rootElement);
		addCacheName(layerParameters, rootElement);
		addImageFormat(layerParameters, rootElement);
		
		XMLUtil.saveDocumentToFormattedStream(document, targetStream);
	}

	private Element addRootElement(Document document)
	{
		Element rootElement = document.createElement("Layer");
		WWXML.setIntegerAttribute(rootElement, "version", 1);
		WWXML.setTextAttribute(rootElement, "layerType", "TiledImageLayer");
		return rootElement;
	}

	private void addGeneratedComment(Element rootElement)
	{
		String comment = getMessage(getWmsExportedLayerCreationCommentKey(), new Date());
		rootElement.appendChild(rootElement.getOwnerDocument().createComment(comment));
	}
	
	private void addDisplayName(AVList layerParameters, Element rootElement)
	{
		Element displayNameElement = WWXML.appendElement(rootElement, "DisplayName");
		WWXML.appendText(displayNameElement, null, layerParameters.getStringValue(AVKey.DISPLAY_NAME));
	}
	
	private void addService(AVList layerParameters, Element rootElement)
	{
		Element serviceElement = WWXML.appendElement(rootElement, "Service");
		WWXML.setTextAttribute(serviceElement, "serviceName", "OGC:WMS");
		WWXML.setTextAttribute(serviceElement, "version", layerParameters.getStringValue(AVKey.WMS_VERSION));
		
		Element capabilitiesUrlElement = WWXML.appendElement(serviceElement, "GetCapabilitiesURL");
		WWXML.appendText(capabilitiesUrlElement, null, layerParameters.getStringValue(AVKey.GET_CAPABILITIES_URL));
		
		Element mapUrlElement = WWXML.appendElement(serviceElement, "GetMapURL");
		WWXML.appendText(mapUrlElement, null, layerParameters.getStringValue(AVKey.GET_MAP_URL));
	}
	
	private void addCacheName(AVList layerParameters, Element rootElement)
	{
		Element cacheElement = WWXML.appendElement(rootElement, "DataCacheName");
		WWXML.appendText(cacheElement, null, layerParameters.getStringValue(AVKey.DATA_CACHE_NAME));
	}
	
	private void addImageFormat(AVList layerParameters, Element rootElement)
	{
		Element imageFormatElement = WWXML.appendElement(rootElement, "ImageFormat");
		WWXML.appendText(imageFormatElement, null, layerParameters.getStringValue(AVKey.IMAGE_FORMAT));
	}
}
