package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getServerBrowserPanelTitleKey;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JScrollPane;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
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
	private LazyTree serverTree;
	
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
		serverTree.setRootVisible(false);
		serverTree.setDragEnabled(false);
		serverTree.setEditable(false);
		
		addKnownServersToTree();
		
		serverTree.revalidate();
	}

	private void addKnownServersToTree()
	{
		try
		{
			for (String serverUrl : WmsBrowserSettings.get().getWmsServerUrls())
			{
				WmsServerImpl server = new WmsServerImpl(new URL(serverUrl));
				treeModel.addServer(server);
			}
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
	
	private static class WmsServerTree extends LazyTree
	{
		private static final long serialVersionUID = 20101118L;

		public WmsServerTree(LazyTreeModel newModel)
		{
			super(newModel);
		}

		@Override
		public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (value instanceof WmsServer)
			{
				return ((WmsServer)value).getCapabilitiesUrl().toExternalForm();
			}
			if (value instanceof WMSLayerInfo)
			{
				return ((WMSLayerInfo)value).getTitle();
			}
			return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
		}
	}
}
