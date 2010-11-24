package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerInfoURL;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import nasa.worldwind.awt.WorldWindowGLCanvas;
import au.gov.ga.worldwind.animator.application.AnimatorConfiguration;
import au.gov.ga.worldwind.common.ui.SelectableLabel;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.wmsbrowser.layer.MetacartaCoastlineLayer;
import au.gov.ga.worldwind.wmsbrowser.layer.MetacartaCountryBoundariesLayer;

/**
 * A panel used to display WMS Layer information
 * <p/>
 * Uses the capabilities information to display layer metadata, and a flat globe view to display the layer data itself
 */
public class WmsLayerInfoPanel extends JComponent
{
	private static final Sector DEFAULT_VIEW_EXTENTS = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG180, Angle.POS180);
	private static final long serialVersionUID = 20101122L;
	private static int PADDING = 10;
	
	/** The layer this panel is backed by */
	private WMSLayerInfo layerInfo;
	private WMSLayerCapabilities layerCapabilities;
	
	private JPanel splitPanel;
	
	private JPanel panel;
	
	private int currentRow = 0;
	
	private WorldWindowGLCanvas wwd;
	private WorldMapLayer worldMapLayer;
	private MetacartaCountryBoundariesLayer countryBoundariesLayer;
	private MetacartaCoastlineLayer coastlinesLayer;
	
	public WmsLayerInfoPanel()
	{
		setLayout(new BorderLayout());
		
		initialiseSplitPanel();
		initialisePanel();
		initialiseWorldWindow();
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				wwd.createBufferStrategy(2);
			}
		});
	}

	private void initialiseSplitPanel()
	{
		splitPanel = new JPanel();
		splitPanel.setLayout(new GridLayout(2, 1));
		splitPanel.setBorder(null);
		add(splitPanel, BorderLayout.CENTER);
	}

	private void initialisePanel()
	{
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(panel.getBorder(), new EmptyBorder(PADDING, PADDING, PADDING, PADDING)));
		panel.setBackground(Color.WHITE);
		panel.setOpaque(true);
		
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(panel);
		splitPanel.add(scrollPane);
	}
	
	private void initialiseWorldWindow()
	{
		wwd = new WorldWindowGLCanvas(AnimatorConfiguration.getGLCapabilities());
		wwd.setModel(new BasicModel());
		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		wwd.setMinimumSize(new Dimension(1, 1));
		((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(Angle.ZERO, Angle.ZERO);
		((OrbitView) wwd.getView()).getOrbitViewLimits().setHeadingLimits(Angle.ZERO, Angle.ZERO);
		wwd.getModel().setGlobe(new EarthFlat());
		wwd.setVisible(false);
		
		splitPanel.add(wwd);
		
		worldMapLayer = new WorldMapLayer();
		countryBoundariesLayer = new MetacartaCountryBoundariesLayer();
		coastlinesLayer = new MetacartaCoastlineLayer();
	}
	
	public void setLayerInfo(WMSLayerInfo layerInfo)
	{
		if (this.layerInfo == layerInfo)
		{
			return;
		}
		
		this.layerInfo = layerInfo;
		updateLayerInfoPanel();
	}

	private synchronized void updateLayerInfoPanel()
	{
		try
		{
			panel.removeAll();
			if (layerInfo == null)
			{
				wwd.setVisible(false);
				return;
			}
			
			currentRow = 0;
			
			addHeading(layerInfo.getTitle());
			
			layerCapabilities = ((WMSCapabilities)layerInfo.getCaps()).getLayerByName(layerInfo.getParams().getStringValue(AVKey.LAYER_NAMES));
			if (layerCapabilities == null)
			{
				addSubHeading(getMessage(getLayerInfoNoCapabilitiesMsgKey()));
			}
			
			addNameValuePair(getMessage(getLayerInfoDataUrlKey()), getDataUrlAsString(layerCapabilities));
			addNameValuePair(getMessage(getLayerInfoMetaDataUrlKey()), getMetaDataUrlAsString(layerCapabilities));
			addNameValuePair(getMessage(getLayerInfoLastUpdateKey()), layerCapabilities.getLastUpdate());
			addNameValuePair(getMessage(getLayerInfoAbstractKey()), layerCapabilities.getLayerAbstract());
			addNameValuePair(getMessage(getLayerInfoBoundingBoxKey()), getBoundingBoxAsString(layerCapabilities));
			addNameValuePair(getMessage(getLayerInfoKeywordsKey()), getKeyWordsAsString(layerCapabilities));
			addEndingSpace();
			
			updateFlatGlobeViewer();
		}
		finally
		{
			panel.validate();
			panel.repaint();
			validate();
			repaint();
		}
	}

	private static String getKeyWordsAsString(WMSLayerCapabilities capabilities)
	{
		String result = "";
		Iterator<String> keywords = capabilities.getKeywords().iterator();
		while (keywords.hasNext())
		{
			result += keywords.next();
			if (keywords.hasNext())
			{
				result += ", ";
			}
		}
		return result;
	}

	private static String getBoundingBoxAsString(WMSLayerCapabilities capabilities)
	{
		return capabilities.getGeographicBoundingBox().toString();
	}

	private static String getMetaDataUrlAsString(WMSLayerCapabilities capabilities)
	{
		return asString(capabilities.getMetadataURLs().iterator());
	}
	
	private static String getDataUrlAsString(WMSLayerCapabilities capabilities)
	{
		return asString(capabilities.getDataURLs().iterator());
	}
	
	private static String asString(Iterator<WMSLayerInfoURL> iterator)
	{
		String result = "";
		while (iterator.hasNext())
		{
			result += iterator.next().getOnlineResource().getHref() + "\n";
		}
		return result;
	}
	
	/**
	 * Add an entry to the info panel of the form 'Name: value'
	 */
	private void addNameValuePair(String name, String value)
	{
		Container container = new Container();
		container.setLayout(new FlowLayout(FlowLayout.LEFT, PADDING, 0));
		
		JLabel nameLabel = new JLabel(name + ":");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		container.add(nameLabel, Box.LEFT_ALIGNMENT);
		
		SelectableLabel valueLabel = new SelectableLabel(getStringValue(value));
		container.add(valueLabel);

		panel.add(container, createElementConstraints());
		currentRow++;
	}

	private void addHeading(String heading)
	{
		JLabel headingLabel = new JLabel(heading);
		headingLabel.setFont(headingLabel.getFont().deriveFont(Font.BOLD, headingLabel.getFont().getSize() * 1.5f));
		
		panel.add(headingLabel, createElementConstraints());
		currentRow++;
	}
	
	private void addSubHeading(String heading)
	{
		Container container = new Container();
		container.setLayout(new FlowLayout(FlowLayout.LEFT, PADDING, 0));
		
		JLabel headingLabel = new JLabel(heading);
		headingLabel.setFont(headingLabel.getFont().deriveFont(Font.BOLD, headingLabel.getFont().getSize() * 1.3f));
		container.add(headingLabel);
		
		panel.add(container, createElementConstraints());
		currentRow++;
	}
	
	private void addEndingSpace()
	{
		panel.add(Box.createVerticalGlue(), createEndConstraints());
	}
	
	private void updateFlatGlobeViewer()
	{
		wwd.setVisible(true);
		wwd.getModel().getLayers().clear();		
		wwd.getModel().getLayers().add(new WMSTiledImageLayer((WMSCapabilities)layerInfo.getCaps(), layerInfo.getParams()));
		wwd.getModel().getLayers().add(countryBoundariesLayer);
		wwd.getModel().getLayers().add(coastlinesLayer);
		wwd.getModel().getLayers().add(worldMapLayer);
		zoomViewToSector(layerCapabilities.getGeographicBoundingBox() == null ? DEFAULT_VIEW_EXTENTS : layerCapabilities.getGeographicBoundingBox());
		wwd.redraw();
	}
	
	private void zoomViewToSector(Sector geographicBoundingBox)
	{
		// Estimate the earth as a perfect sphere...
		double earthRadius = wwd.getModel().getGlobe().getRadius();
		
		// Compute the estimated deltas in metres (Add some padding to account for rounding errors and some buffering)
		double deltaX = earthRadius * geographicBoundingBox.getDeltaLonRadians();
		double deltaY = earthRadius * geographicBoundingBox.getDeltaLatRadians();

		// Compute the altitude required to ensure that the whole of each axis is visible in the viewport 
		double altitudeX = (deltaX/2) / Math.tan(wwd.getView().getFieldOfView().radians / 2) * 1.3;
		double altitudeY = (deltaY/2) / Math.tan(wwd.getView().getFieldOfView().radians / 2) * 1.3;
		
		LatLon centroid = geographicBoundingBox.getCentroid();
		Position pos = new Position(centroid.getLatitude(), centroid.getLongitude(), Math.max(altitudeX, altitudeY));
		
		wwd.getView().setEyePosition(pos);
	}

	private static String getStringValue(String value)
	{
		return Util.isBlank(value) ? getMessage(getLayerInfoDefaultStringValueKey()) : value;
	}
	
	private GridBagConstraints createElementConstraints()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = currentRow;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, PADDING, 0);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		return constraints;
	}
	
	private GridBagConstraints createEndConstraints()
	{
		GridBagConstraints constraints = createElementConstraints();
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		return constraints;
	}
}
