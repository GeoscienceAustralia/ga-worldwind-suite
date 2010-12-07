package au.gov.ga.worldwind.wmsbrowser.search;

import gov.nasa.worldwind.applications.gos.GeodataKey;
import gov.nasa.worldwind.applications.gos.OnlineResource;
import gov.nasa.worldwind.applications.gos.Record;
import gov.nasa.worldwind.applications.gos.RecordList;
import gov.nasa.worldwind.applications.gos.services.CSWGetRecordsRequest;
import gov.nasa.worldwind.applications.gos.services.CSWQueryBuilder;
import gov.nasa.worldwind.applications.gos.services.CSWRecordList;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerImpl;

/**
 * A {@link WmsServerSearchService} that queries a CSW catalogue for WMS services
 * using the provided search string
 */
public class CSWSearchService implements WmsServerSearchService
{
	private static final Logger logger = Logging.logger();
	
	private static final int RECORD_START_INDEX = 0;
	private static final int MAX_RECORDS = 50;
	private static final String RECORD_FORMAT = "wms";
	
	private URL catalogueUrl;
	
	public CSWSearchService(URL catalogueUrl)
	{
		Validate.notNull(catalogueUrl, "A catalogue URL is required");
		this.catalogueUrl = catalogueUrl;
	}
	
	
	@Override
	public List<WmsServerSearchResult> searchForServers(String searchString)
	{
		try
		{
			String requestString = createRequestString(searchString);
			CSWGetRecordsRequest request = new CSWGetRecordsRequest(catalogueUrl.toURI());
			
			RecordList recordList = CSWRecordList.retrieve(request.getUri(), requestString);
			return createWmsServerList(recordList);
		}
		catch (Throwable e)
		{
			logger.log(Level.SEVERE, "Exception occurred during CSW request to " + catalogueUrl.toExternalForm(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * @return A CSW GetRecords request string that searches for WMS servers using the provided search string
	 */
	protected String createRequestString(String searchString)
	{
		AVList params = new AVListImpl();
		params.setValue(GeodataKey.RECORD_START_INDEX, RECORD_START_INDEX);
		params.setValue(GeodataKey.RECORD_PAGE_SIZE, MAX_RECORDS);
		params.setValue(GeodataKey.SEARCH_TEXT, searchString);
		params.setValue(GeodataKey.RECORD_FORMAT, RECORD_FORMAT);
		
		return new CSWQueryBuilder(params).getGetRecordsString();
	}
	
	/**
	 * @return A list of WMS servers extracted from the provided CSW record list
	 */
	private List<WmsServerSearchResult> createWmsServerList(RecordList recordList)
	{
		List<WmsServerSearchResult> result = new ArrayList<WmsServerSearchResult>();
		if (recordList == null || recordList.getRecords() == null)
		{
			return result;
		}
		for (Record cswRecord : recordList.getRecords())
		{
			// If there is no capabilities URL, move on
			OnlineResource capabilitiesResource = cswRecord.getResource(GeodataKey.CAPABILITIES);
			if (capabilitiesResource == null)
			{
				continue;
			}
			
			URL url = Util.stripQuery(capabilitiesResource.getURL());
			String name = cswRecord.getTitle();
			
			result.add(new WmsServerSearchResultImpl(new WmsServerImpl(name, url), cswRecord));
		}
		
		return result;
	}

}
