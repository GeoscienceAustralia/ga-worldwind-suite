/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.ui.SwingUtil.invokeTaskOnEDT;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getWindowTitleKey;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import au.gov.ga.worldwind.common.ui.collapsiblesplit.CollapsibleSplitConstraints;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.l2fprod.CollapsibleGroup;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanel;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.wmsbrowser.WmsServerBrowserPanel.LayerInfoSelectionListener;

/**
 * A browser tool used to locate layers residing in WMS browsers
 * <p/>
 * Allows users to:
 * <ul>
 * 	<li> Add WMS servers to a known server list
 * 	<li> Browse WMS layers available on known servers
 *  <li> View information about a selected layer
 *  <li> Export a layer as a WW compatible layer definition for use in the Viewer and Animator
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsBrowser
{
	private JFrame frame;
	private JSplitPane splitPane;
	
	private CollapsibleSplitPane sidebar;
	
	private WmsLayerInfoPanel layerInfoPanel;
	private WmsServerBrowserPanel serverBrowserPanel;
	
	public WmsBrowser(String parentApplicationTitle)
	{
		MessageSourceAccessor.addBundle("messages.wmsBrowserMessages");
		
		initialiseWindow(parentApplicationTitle);
		initialiseWindowContents();
		initialiseBrowserPanel();
	}

	private void initialiseWindow(String parentApplicationTitle)
	{
		frame = new JFrame(getMessage(getWindowTitleKey()) + " - " + parentApplicationTitle);
		frame.setIconImage(Icons.wmsbrowser.getIcon().getImage());
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				WmsBrowserSettings.get().setSplitLocation(splitPane.getDividerLocation());
				WmsBrowserSettings.get().setWindowDimension(frame.getSize());
				WmsBrowserSettings.save();
			}
		});
	}
	
	private void initialiseWindowContents()
	{
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		frame.setSize(WmsBrowserSettings.get().getWindowDimension());
		frame.setPreferredSize(WmsBrowserSettings.get().getWindowDimension());
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(WmsBrowserSettings.get().getSplitLocation());
		panel.add(splitPane, BorderLayout.CENTER);
		
		sidebar = new CollapsibleSplitPane();
		splitPane.setLeftComponent(sidebar);
		
		layerInfoPanel = new WmsLayerInfoPanel();
		splitPane.setRightComponent(layerInfoPanel);
	}
	
	private void initialiseBrowserPanel()
	{
		serverBrowserPanel = new WmsServerBrowserPanel();
		serverBrowserPanel.addLayerInfoSelectionListener(new LayerInfoSelectionListener()
		{
			@Override
			public void layerSelectionChanged(WMSLayerInfo selectedLayer)
			{
				if (layerInfoPanel != null)
				{
					layerInfoPanel.setLayerInfo(selectedLayer);
				}
			}
		});
		
		addPanelToSidebar(serverBrowserPanel);
	}
	
	/** Show the WMS Browser tool */
	public void show()
	{
		invokeTaskOnEDT(new Runnable() {
			@Override
			public void run()
			{
				frame.pack();
				frame.setSize(WmsBrowserSettings.get().getWindowDimension());
				frame.setPreferredSize(WmsBrowserSettings.get().getWindowDimension());
				frame.setVisible(true);
			}
		});
	}

	/** Hide the WMS Browser tool */
	public void hide()
	{
		invokeTaskOnEDT(new Runnable() {
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(false);
			}
		});
	}
	
	/**
	 * Add the provided panel to the sidebar
	 */
	private void addPanelToSidebar(CollapsiblePanel panel)
	{
		CollapsibleGroup group = new CollapsibleGroup();
		group.setIcon(panel.getIcon());
		group.setVisible(panel.isOn());
		group.setCollapsed(!panel.isExpanded());
		group.setScrollOnExpand(true);
		group.setLayout(new BorderLayout());
		group.setTitle(panel.getName());
		group.add(panel.getPanel(), BorderLayout.CENTER);

		CollapsibleSplitConstraints c = new CollapsibleSplitConstraints();
		c.collapsed = !panel.isExpanded();
		c.resizable = panel.isResizable();
		c.weight = panel.getWeight();

		sidebar.add(group, c);
	}
	
	/**
	 * Register a class to receive WMS layers selected by the user
	 */
	public void registerLayerReceiver(WmsLayerReceiver receiver)
	{
		serverBrowserPanel.registerLayerReceiver(receiver);
	}
	
	public void addWindowListener(WindowListener windowListener)
	{
		this.frame.addWindowListener(windowListener);
	}
}
