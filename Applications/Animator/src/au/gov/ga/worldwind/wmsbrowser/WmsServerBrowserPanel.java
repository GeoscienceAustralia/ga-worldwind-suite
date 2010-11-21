package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getServerBrowserPanelTitleKey;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanel;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;

/**
 * A {@link CollapsiblePanel} that allows the user to browse through known
 * WMS servers and select layers from those servers.
 */
public class WmsServerBrowserPanel extends CollapsiblePanelBase
{
	private static final long serialVersionUID = 20101116L;

	private WmsServerTreeModel treeModel;
	private WmsServerTree serverTree;
	
	public WmsServerBrowserPanel()
	{
		setName(getMessage(getServerBrowserPanelTitleKey()));
		
		initialiseServerTree();
		packComponents();
	}

	private void initialiseServerTree()
	{
		
		treeModel = new WmsServerTreeModel();
		
		serverTree = new WmsServerTree(treeModel);
		
		addKnownServersToTree();
		
		serverTree.validate();
	}

	private void addKnownServersToTree()
	{
		try
		{
			List<WmsServer> servers = new ArrayList<WmsServer>();
			for (String serverUrl : WmsBrowserSettings.get().getWmsServerUrls())
			{
				WmsServerImpl server = new WmsServerImpl(new URL(serverUrl));
				servers.add(server);
			}
			treeModel.addServers(servers);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void packComponents()
	{
		JScrollPane scrollPane = new JScrollPane(serverTree);
		add(scrollPane, BorderLayout.CENTER);
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
					return ((WmsServerTreeObject)nodeObject).getWmsServer().getCapabilitiesUrl().toExternalForm();
				}
				if (nodeObject instanceof WMSLayerInfo)
				{
					return ((WMSLayerInfo)nodeObject).getTitle();
				}
			}
			
			return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
		}
	}
}
