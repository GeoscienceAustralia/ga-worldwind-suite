package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;

import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

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
	
	public WmsLayerInfoPanel()
	{
		setLayout(new BorderLayout());
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
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

	private void updateLayerInfoPanel()
	{
		try
		{
			panel.removeAll();
			if (layerInfo == null)
			{
				return;
			}
			
			addNameValuePair(getMessage(getLayerInfoNameKey()), layerInfo.getTitle());
		}
		finally
		{
			validate();
			repaint();
		}
	}
	
	/**
	 * Add an entry to the info panel of the form 'Name: value'
	 * @param name
	 * @param value
	 */
	private void addNameValuePair(String name, String value)
	{
		JComponent container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
		container.setOpaque(false);
		
		JLabel nameLabel = new JLabel(name + ":");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		container.add(nameLabel, Box.LEFT_ALIGNMENT);
		
		container.add(Box.createHorizontalStrut(PADDING));
		
		JLabel valueLabel = new JLabel(value);
		container.add(valueLabel, Box.LEFT_ALIGNMENT);
		container.add(Box.createHorizontalGlue());
		
		panel.add(container);
	}
}
