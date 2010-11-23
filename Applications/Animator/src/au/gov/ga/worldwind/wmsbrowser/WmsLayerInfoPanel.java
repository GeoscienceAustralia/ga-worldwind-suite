package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerInfoURL;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import au.gov.ga.worldwind.common.util.Util;

/**
 * A panel used to display WMS Layer information
 */
public class WmsLayerInfoPanel extends JComponent
{
	private static final long serialVersionUID = 20101122L;
	private static int PADDING = 10;
	
	/** The layer this panel is backed by */
	private WMSLayerInfo layerInfo;
	
	private JPanel panel;
	
	private int currentRow = 0;
	
	public WmsLayerInfoPanel()
	{
		setLayout(new BorderLayout());
		
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(panel.getBorder(), new EmptyBorder(PADDING, PADDING, PADDING, PADDING)));
		panel.setBackground(Color.WHITE);
		panel.setOpaque(true);
		
		JScrollPane scrollPane = new JScrollPane(panel);
		add(scrollPane, BorderLayout.CENTER);
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
				return;
			}
			
			currentRow = 0;
			
			addHeading(layerInfo.getTitle());
			
			WMSLayerCapabilities capabilities = ((WMSCapabilities)layerInfo.getCaps()).getLayerByName(layerInfo.getParams().getStringValue(AVKey.LAYER_NAMES));
			if (capabilities == null)
			{
				addSubHeading(getMessage(getLayerInfoNoCapabilitiesMsgKey()));
			}
			
			addNameValuePair(getMessage(getLayerInfoDataUrlKey()), getDataUrlAsString(capabilities));
			addNameValuePair(getMessage(getLayerInfoMetaDataUrlKey()), getMetaDataUrlAsString(capabilities));
			addNameValuePair(getMessage(getLayerInfoLastUpdateKey()), capabilities.getLastUpdate());
			addNameValuePair(getMessage(getLayerInfoAbstractKey()), capabilities.getLayerAbstract());
			addNameValuePair(getMessage(getLayerInfoBoundingBoxKey()), getBoundingBoxAsString(capabilities));
			addNameValuePair(getMessage(getLayerInfoKeywordsKey()), getKeyWordsAsString(capabilities));
			
			addEndPadding();
		}
		finally
		{
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
	
	private void addEndPadding()
	{
		panel.add(Box.createVerticalGlue(), createEndConstraints());
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
		return constraints;
	}
	
	/**
	 * A simple extension of the {@link JTextField} to make it appear like a label,
	 * but allow text selection via the mouse
	 */
	private static class SelectableLabel extends JTextField
	{
		private static final long serialVersionUID = 20101123L;

		public SelectableLabel(String value)
		{
			super(value);
			setEditable(false);
			setBorder(null);
			setForeground(UIManager.getColor("Label.foreground"));
			setFont(UIManager.getFont("Label.font"));
			setOpaque(false);
			setColumns(value.length() + 5);
		}
	}
}
