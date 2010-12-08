package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanel;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;
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
	
	private JToolBar toolbar;
	private BasicAction addServerAction;
	private BasicAction removeServerAction;
	
	private List<LayerInfoSelectionListener> layerSelectionListeners = new ArrayList<LayerInfoSelectionListener>();
	
	public WmsServerBrowserPanel()
	{
		setName(getMessage(getServerBrowserPanelTitleKey()));
		
		initialiseServerTree();
		initialiseToolbar();
		initialiseSearchDialog();
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
	
	private void initialiseToolbar()
	{
		initialiseActions();
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(addServerAction);
		toolbar.add(removeServerAction);
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
				removeServerAction.setEnabled(getSelectedUserObject() instanceof WmsServer);
			}
		});
	}

	private void initialiseSearchDialog()
	{
		searchServerDialog = new SearchWmsServerDialog();
	}
	
	private void packComponents()
	{
		JScrollPane scrollPane = new JScrollPane(serverTree);
		add(scrollPane, BorderLayout.CENTER);
		add(toolbar, BorderLayout.NORTH);
	}
	
	private class LayerSelectionListener implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			notifyLayerSelectionChanged(getSelectedLayerInfo());
		}
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

	/**
	 * An extension of the {@link LazyTree} class customised for
	 * use in the WMS browser panel
	 */
	private static class WmsServerTree extends LazyTree
	{
		private static final long serialVersionUID = 20101118L;

		public WmsServerTree(LazyTreeModel treeModel)
		{
			super(treeModel);
			setRootVisible(false);
			setDragEnabled(false);
			setEditable(false);
			setShowsRootHandles(true);
		}

		@Override
		public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (value instanceof DefaultMutableTreeNode)
			{
				Object nodeObject = ((DefaultMutableTreeNode)value).getUserObject();
				
				if (nodeObject instanceof WmsServerTreeObject)
				{
					return ((WmsServerTreeObject)nodeObject).getWmsServer().getName();
				}
				if (nodeObject instanceof WMSLayerInfo)
				{
					return ((WMSLayerInfo)nodeObject).getTitle();
				}
			}
			
			return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
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
}
