package au.gov.ga.worldwind.wmsbrowser.layer;

import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import org.w3c.dom.Document;

public class MetacartaCountryBoundariesLayer extends WMSTiledImageLayer
{
    public MetacartaCountryBoundariesLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("au/gov/ga/worldwind/wmsbrowser/layer/metacarta_country_boundaries.xml", null);
    }
}
