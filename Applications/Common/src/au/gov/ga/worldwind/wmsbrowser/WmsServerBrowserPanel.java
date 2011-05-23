package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.Util.isEmpty;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.FileFilters;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
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
	
	private JToolBar toolbar;
	private JButton useLayerButton;
	private BasicAction addServerAction;
	private BasicAction removeServerAction;
	private BasicAction exportLayerAction;
	private BasicAction useLayerAction;
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

	private WmsServer getSelectedWmsServer()
	{
		Object userObject = getSelectedUserObject();
		if (userObject instanceof WmsServer)
		{
			return (WmsServer)userObject;
		}
		return null;
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
	 * A simple dialog box used to edit WMS server details
	 */
	private static class EditWmsServerDialog extends JDialog
	{
		private static final Dimension SIZE = new Dimension(600, 150);
		
		private JPanel contentFrame; 
	
		private BasicAction okAction;
		private BasicAction cancelAction;
		
		private JTextField serverNameField;
		private JTextField serverUrlField;
		
		private int response = JOptionPane.CANCEL_OPTION;
		
		public EditWmsServerDialog()
		{
			contentFrame = new JPanel();
			contentFrame.setLayout(new BoxLayout(contentFrame, BoxLayout.Y_AXIS));
			
			setLocationRelativeTo(null);
			setTitle(getMessage(getEditServerDialogTitleKey()));
			setContentPane(contentFrame);
			setModal(true);
			setSize(SIZE);
			setPreferredSize(SIZE);
			
			initialiseActions();
			addFields();
			addButtonBar();
			
			addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentShown(ComponentEvent e)
				{
					response = JOptionPane.CANCEL_OPTION;
				}
			});
		}
		
		private void initialiseActions()
		{
			okAction = new BasicAction(getMessage(getTermOkKey()), null);
			okAction.setToolTipText(getMessage(getEditServerDialogOkButtonTooltipKey()));
			okAction.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					closeDialogWithResponse(JOptionPane.OK_OPTION);
				}
			});
			
			cancelAction = new BasicAction(getMessage(getTermCancelKey()), null);
			cancelAction.setToolTipText(getMessage(getEditServerDialogCancelButtonTooltipKey()));
			cancelAction.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					closeDialogWithResponse(JOptionPane.CANCEL_OPTION);
				}
			});
		}
		
		private void addFields()
		{
			JLabel serverNameLabel = new JLabel(getMessage(getEditServerDialogEditNameLabelKey()));
			serverNameField = new JTextField();
			serverNameField.setToolTipText(getMessage(getEditServerDialogEditNameTooltipKey()));
			
			JLabel serverUrlLabel = new JLabel(getMessage(getEditServerDialogEditUrlLabelKey()));
			serverUrlField = new JTextField();
			serverUrlField.setToolTipText(getMessage(getEditServerDialogEditUrlTooltipKey()));
			
			JPanel container = new JPanel();
			GroupLayout layout = new GroupLayout(container);
			container.setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			Component vglue = Box.createVerticalGlue();

			layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addGroup(
						layout.createParallelGroup(Alignment.TRAILING)
							.addComponent(serverNameLabel)
							.addComponent(serverUrlLabel)
					).addGroup(
						layout.createParallelGroup(Alignment.LEADING)
							.addComponent(serverNameField)
							.addComponent(serverUrlField)
					)
					.addComponent(vglue)
			);
			layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(
						layout.createParallelGroup()
							.addComponent(serverNameLabel)
							.addComponent(serverNameField)
							.addComponent(vglue)
					).addGroup(
						layout.createParallelGroup()
							.addComponent(serverUrlLabel)
							.addComponent(serverUrlField)
					)
			);
			
			contentFrame.add(container);
		}
		
		private void addButtonBar()
		{
			JPanel container = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			container.add(new JButton(cancelAction));
			container.add(new JButton(okAction));
			contentFrame.add(container);
		}
		
		private void closeDialogWithResponse(int response)
		{
			if (response == JOptionPane.OK_OPTION)
			{
				String[] validationMessages = validateData();
				if (!isEmpty(validationMessages))
				{
					String userMessage = getMessage(getEditServerDialogInvalidDetailsMessageKey());
					for (String validationMessage : validationMessages)
					{
						userMessage += "    - " + validationMessage + "\n";
					}
					JOptionPane.showMessageDialog(this, 
												  userMessage, 
												  getMessage(getEditServerDialogInvalidDetailsTitleKey()), 
												  JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			this.response = response;
			setVisible(false);
		}
		
		/** @return any validation messages to be shown to the user, or empty array if validation succeeds. */
		private String[] validateData()
		{
			ArrayList<String> result = new ArrayList<String>();
			
			// Validate that the URL is actually valid
			try
			{
				new URL(getServerUrlString());
			}
			catch (MalformedURLException e)
			{
				result.add(getMessage(getEditServerDialogInvalidUrlMessageKey()));
			}
			
			return result.toArray(new String[0]);
		}
		
		public int getResponse()
		{
			return response;
		}
		
		public void setCurrentServer(WmsServer server)
		{
			this.serverNameField.setText(server.getName());
			this.serverUrlField.setText(server.getCapabilitiesUrl().toExternalForm());
		}
		
		public String getServerName()
		{
			return serverNameField.getText();
		}
		
		public String getServerUrlString()
		{
			return serverUrlField.getText();
		}
		
		/**
		 * @return The server URL, or <code>null</code> if the user entered and invalid URL
		 */
		public URL getServerUrl()
		{
			try
			{
				return new URL(getServerUrlString());
			}
			catch (MalformedURLException e)
			{
				return null;
			}
		}
	}
}
