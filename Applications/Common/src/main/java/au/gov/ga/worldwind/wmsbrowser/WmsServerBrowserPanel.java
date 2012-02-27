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

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import static javax.swing.SwingUtilities.isRightMouseButton;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.FileFilters;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanel;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.common.util.FileUtil;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.wmsbrowser.layer.WmsLayerExporter;
import au.gov.ga.worldwind.wmsbrowser.layer.WmsLayerExporterImpl;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifierImpl;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerImpl;

/**
 * A {@link CollapsiblePanel} that allows the user to browse through known
 * WMS servers and select layers from those servers.
 */
public class WmsServerBrowserPanel extends CollapsiblePanelBase
{
	private static final long serialVersionUID = 20101116L;

	private WmsServerTreeModel treeModel;
	private WmsServerTree serverTree;
	
	private SearchWmsServerDialog searchServerDialog;
	
	private JPopupMenu rightClickMenu;
	
	private JToolBar toolbar;
	private BasicAction addServerAction;
	private BasicAction removeServerAction;
	private BasicAction exportLayerAction;
	private BasicAction useLayerAction;
	private JButton useLayerButton;
	private JMenuItem useLayerItem;
	private BasicAction editServerAction;
	
	private EditWmsServerDialog editServerDialog;
	
	private JFileChooser fileChooser;
	
	private static final WmsLayerExporter exporter = new WmsLayerExporterImpl();
	
	private List<LayerInfoSelectionListener> layerSelectionListeners = new ArrayList<LayerInfoSelectionListener>();
	
	private Set<WmsLayerReceiver> layerReceivers = new LinkedHashSet<WmsLayerReceiver>();
	
	public WmsServerBrowserPanel()
	{
		setName(getMessage(getServerBrowserPanelTitleKey()));
		
		initialiseFileChooser();
		initialiseEditServerDialog();
		initialiseServerTree();
		initialiseToolbar();
		initialiseSearchDialog();
		initialiseRightClickMenu();
		packComponents();
		
		updateActionsEnabledStatus();
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				updateActionsEnabledStatus();
			}
		});
	}

	private void initialiseFileChooser()
	{
		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(FileFilters.getXmlFilter());
	}
	
	private void initialiseEditServerDialog()
	{
		editServerDialog = new EditWmsServerDialog();
	}
	
	private void initialiseServerTree()
	{
		treeModel = new WmsServerTreeModel();
		
		serverTree = new WmsServerTree(treeModel);
		serverTree.addTreeSelectionListener(new LayerSelectionListener());
		serverTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				updateActionsEnabledStatus();
			}
		});
		serverTree.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!serverTree.isPointOnTreeElement(e.getPoint()))
				{
					return;
				}
				
				if (isRightMouseButton(e))
				{
					serverTree.selectTreeElementAtPoint(e.getPoint());
					rightClickMenu.show(serverTree, e.getX(), e.getY());
				}
			}
		});
		
		addKnownServersToTree();
		
		serverTree.validate();
	}

	/**
	 * Add the known WMS servers from the settings file into the browser tree
	 */
	private void addKnownServersToTree()
	{
		try
		{
			List<WmsServer> servers = new ArrayList<WmsServer>();
			for (WmsServerIdentifier serverIdentifier : WmsBrowserSettings.get().getWmsServers())
			{
				WmsServerImpl server = new WmsServerImpl(serverIdentifier);
				servers.add(server);
			}
			treeModel.addServers(servers);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	private void initialiseActions()
	{
		addServerAction = new BasicAction(getMessage(getAddServerMenuLabelKey()), Icons.add.getIcon());
		addServerAction.setToolTipText(getMessage(getAddServerMenuTooltipKey()));
		addServerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchServerDialog.setVisible(true);
				if (searchServerDialog.getResponse() == JOptionPane.OK_OPTION)
				{
					treeModel.addServers(searchServerDialog.getSelectedServers());
					serverTree.validate();
					updateKnownServersInSettings();
				}
			}
		});
		
		removeServerAction = new BasicAction(getMessage(getDeleteServerMenuLabelKey()), Icons.delete.getIcon());
		removeServerAction.setToolTipText(getMessage(getDeleteServerMenuTooltipKey()));
		removeServerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object selectedObject = getSelectedUserObject();
				if (!(selectedObject instanceof WmsServer))
				{
					return;
				}
				
				treeModel.removeServer((WmsServer)selectedObject);
				serverTree.revalidate();
				updateKnownServersInSettings();
			}
		});
		
		exportLayerAction = new BasicAction(getMessage(getWmsExportLayerLabelKey()), Icons.save.getIcon());
		exportLayerAction.setToolTipText(getMessage(getWmsExportLayerTooltipKey()));
		exportLayerAction.setEnabled(false);
		exportLayerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptUserToSaveLayer();
			}
		});
		
		useLayerAction = new BasicAction(getMessage(getUseLayerLabelKey()), Icons.imporrt.getIcon());
		useLayerAction.setToolTipText(getMessage(getUseLayerTooltipKey()));
		useLayerAction.setEnabled(false);
		useLayerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				giveLayerToReceivers(getSelectedLayerInfo());
			}
		});
		
		editServerAction = new BasicAction(getMessage(getEditServerLabelKey()), Icons.edit.getIcon());
		editServerAction.setToolTipText(getMessage(getEditServerTooltipKey()));
		editServerAction.setEnabled(false);
		editServerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptUserToEditServer();
			}
		});
	}
	
	private void initialiseToolbar()
	{
		initialiseActions();
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(addServerAction);
		toolbar.add(removeServerAction);
		toolbar.add(editServerAction);
		toolbar.add(Box.createHorizontalGlue());
		useLayerButton = toolbar.add(useLayerAction);
		toolbar.add(exportLayerAction);
		
		useLayerButton.setVisible(false);
	}
	
	private void initialiseSearchDialog()
	{
		searchServerDialog = new SearchWmsServerDialog();
	}
	
	private void initialiseRightClickMenu()
	{
		rightClickMenu = new JPopupMenu();
		rightClickMenu.add(addServerAction);
		rightClickMenu.add(removeServerAction);
		rightClickMenu.add(editServerAction);
		rightClickMenu.addSeparator();
		useLayerItem = rightClickMenu.add(useLayerAction);
		rightClickMenu.add(exportLayerAction);
		
		useLayerItem.setVisible(false);
	}
	
	private void packComponents()
	{
		JScrollPane scrollPane = new JScrollPane(serverTree);
		add(scrollPane, BorderLayout.CENTER);
		add(toolbar, BorderLayout.NORTH);
	}
	

	private void updateKnownServersInSettings()
	{
		List<WmsServerIdentifier> serverIdentifiers = new ArrayList<WmsServerIdentifier>(treeModel.getNumberOfServers());
		for (WmsServer server : treeModel.getServers())
		{
			serverIdentifiers.add(server.getIdentifier());
		}
		WmsBrowserSettings.get().setWmsServers(serverIdentifiers);
	}
	
	/**
	 * Update the enabled status of the actions. Invoke when something interesting happens in the GUI.
	 */
	private void updateActionsEnabledStatus()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				WmsServer selectedServer = getSelectedWmsServer();
				removeServerAction.setEnabled(selectedServer != null);
				editServerAction.setEnabled(selectedServer != null);
				
				WMSLayerInfo selectedLayer = getSelectedLayerInfo();
				exportLayerAction.setEnabled(selectedLayer != null);
				useLayerAction.setEnabled(selectedLayer != null);
			}
		});
	}
	
	private WMSLayerInfo getSelectedLayerInfo()
	{
		if (serverTree.isSelectionEmpty())
		{
			return null;
		}
		
		Object nodeObject = getSelectedUserObject();
		if (nodeObject == null || !(nodeObject instanceof WMSLayerInfo))
		{
			return null;
		}
		
		return (WMSLayerInfo)nodeObject;
	}
	
	private void notifyLayerSelectionChanged(WMSLayerInfo selectedLayerInfo)
	{
		for (int i = layerSelectionListeners.size() - 1; i >= 0; i--)
		{
			layerSelectionListeners.get(i).layerSelectionChanged(selectedLayerInfo);
		}
	}
	
	public void addLayerInfoSelectionListener(LayerInfoSelectionListener l)
	{
		if (layerSelectionListeners.contains(l))
		{
			return;
		}
		
		layerSelectionListeners.add(l);
	}
	
	public void removeLayerInfoSelectionListener(LayerInfoSelectionListener l)
	{
		layerSelectionListeners.remove(l);
	}
	
	private Object getSelectedTreeObject()
	{
		if (serverTree.getSelectionPath() == null)
		{
			return null;
		}
		return serverTree.getSelectionPath().getLastPathComponent();
	}
	
	private Object getSelectedUserObject()
	{
		Object treeObject = getSelectedTreeObject();
		if (!(treeObject instanceof DefaultMutableTreeNode))
		{
			return null;
		}
		Object userObject = ((DefaultMutableTreeNode)treeObject).getUserObject();
		if (!(userObject instanceof WmsServerTreeObject))
		{
			return userObject;
		}
		return ((WmsServerTreeObject)userObject).getWmsServer();
	}

	private WmsServer getSelectedWmsServer()
	{
		Object userObject = getSelectedUserObject();
		if (userObject instanceof WmsServer)
		{
			return (WmsServer)userObject;
		}
		return null;
	}
	
	private void promptUserToSaveLayer()
	{
		WMSLayerInfo selectedLayerInfo = getSelectedLayerInfo();
		if (selectedLayerInfo == null)
		{
			return;
		}
		
		int response = fileChooser.showSaveDialog(getParent());
		if (response == JFileChooser.CANCEL_OPTION)
		{
			return;
		}
		
		File targetFile = fileChooser.getSelectedFile();
		if (targetFile == null)
		{
			return;
		}
		
		// Add the extension if the user didn't
		if (!FileUtil.hasExtension(targetFile.getAbsolutePath(), FileFilters.XmlFilter.getFileExtension()))
		{
			targetFile = new File(targetFile.getAbsolutePath() + FileFilters.XmlFilter.getFileExtension());
		}
		
		if (targetFile.exists())
		{
			response = JOptionPane.showConfirmDialog(getParent(), getMessage(getLayerDefinitionAlreadyExistsMessageKey(), targetFile.getName()), getMessage(getLayerDefinitionAlreadyExistsTitleKey()), JOptionPane.YES_NO_CANCEL_OPTION);
			if (response == JOptionPane.NO_OPTION)
			{
				return;
			}
			if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
			{
				promptUserToSaveLayer();
			}
		}
		
		exporter.exportLayer(targetFile, selectedLayerInfo);
	}
	
	private void promptUserToEditServer()
	{
		WmsServer selectedServer = getSelectedWmsServer();
		if (selectedServer == null)
		{
			return;
		}
		
		editServerDialog.setCurrentServer(selectedServer);
		editServerDialog.setVisible(true);
		if (editServerDialog.getResponse() == JOptionPane.OK_OPTION)
		{
			// Bind the dialog fields to the selected server
			WmsServerIdentifier identifier = new WmsServerIdentifierImpl(editServerDialog.getServerName(), 
																		 editServerDialog.getServerUrl());
			selectedServer.setIdentifier(identifier);
			treeModel.notifyTreeChanged(selectedServer);
			treeModel.forceRefresh();
			serverTree.validate();
			updateKnownServersInSettings();
		}
	}
	
	public void registerLayerReceiver(WmsLayerReceiver receiver)
	{
		if (receiver == null)
		{
			return;
		}
		layerReceivers.add(receiver);
		useLayerButton.setVisible(true);
		useLayerItem.setVisible(true);
	}
	
	public void removeLayerReceiver(WmsLayerReceiver receiver)
	{
		if (receiver == null)
		{
			return;
		}
		layerReceivers.remove(receiver);
		if (layerReceivers.isEmpty())
		{
			useLayerButton.setVisible(false);
			useLayerItem.setVisible(false);
		}
	}
	
	private void giveLayerToReceivers(WMSLayerInfo layer)
	{
		for (WmsLayerReceiver receiver : layerReceivers)
		{
			receiver.receive(layer);
		}
	}
	
	/**
	 * An interface for classes what wish to be notified of changes to the selected
	 * WMS layer
	 */
	public static interface LayerInfoSelectionListener
	{
		void layerSelectionChanged(WMSLayerInfo selectedLayer);
	}
	
	private class LayerSelectionListener implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			notifyLayerSelectionChanged(getSelectedLayerInfo());
		}
	}
}
