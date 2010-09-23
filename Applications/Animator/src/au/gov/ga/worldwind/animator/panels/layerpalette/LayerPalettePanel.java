package au.gov.ga.worldwind.animator.panels.layerpalette;

import static au.gov.ga.worldwind.animator.util.ExceptionLogger.logException;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.ui.BasicAction;

/**
 * A panel that allows the user to locate and select a layer for inclusion
 * in the animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerPalettePanel extends CollapsiblePanelBase 
{
	private static final long serialVersionUID = 20100910L;

	/** The current animation */
	private Animation animation;
	
	/** A toolbar holding operations to perform on the layers */
	private JToolBar toolbar;
	
	/** A scrollable container for holding the tree */
	private JScrollPane scrollPane;
	
	/** The list that allows the user to browse through known layers */
	private JList layerList;
	
	/** The identities of known layers */
	private ListBackedModel<LayerIdentifier> knownLayers = new ListBackedModel<LayerIdentifier>();
	
	/** Used for selecting layer definitions */
	private JFileChooser fileChooser;
	
	// Actions
	private BasicAction addLayerToAnimationAction;
	private BasicAction loadLayerDefinitionAction;
	private BasicAction removeLayerDefinitionAction;
	
	public LayerPalettePanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(getMessage(getLayerPalettePanelNameKey()));
		
		initialiseLayerList();
		updateListModel();
		initialiseActions();
		initialiseFileChooser();
		initialiseToolbar();
		packComponents();
	}
	
	private void initialiseLayerList()
	{
		layerList = new JList(knownLayers);
		layerList.setCellRenderer(new LayerListRenderer(animation));
		
		layerList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				LayerIdentifier layerIdentifier = (LayerIdentifier)((JList)e.getSource()).getSelectedValue();
				addLayerToAnimationAction.setEnabled(!animation.hasLayer(layerIdentifier));
			}
		});
		layerList.setActionMap(null);
		
		layerList.setDragEnabled(true);
		layerList.setTransferHandler(new LayerPaletteTransferHandler(animation, layerList));
	}
	
	private void updateListModel()
	{
		List<LayerIdentifier> knownLayerLocations = Settings.get().getKnownLayers();
		for (LayerIdentifier layerIdentifier : knownLayerLocations)
		{
			if (knownLayers.contains(layerIdentifier))
			{
				continue;
			}
			knownLayers.add(layerIdentifier);
		}
	}
	
	private void initialiseActions()
	{
		addLayerToAnimationAction = new BasicAction(getMessage(getAddLayerToAnimationLabelKey()), Icons.add.getIcon());
		addLayerToAnimationAction.setEnabled(false);
		addLayerToAnimationAction.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				addSelectedLayerToAnimation();
			}
		});
		
		loadLayerDefinitionAction = new BasicAction(getMessage(getAddLayerToListLabelKey()), Icons.imporrt.getIcon());
		loadLayerDefinitionAction.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptToAddLayersFromDefinitions();
			}
		});
		
		removeLayerDefinitionAction = new BasicAction(getMessage(getRemoveLayerFromListLabelKey()), Icons.delete.getIcon());
		removeLayerDefinitionAction.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptToRemoveLayersFromList();
			}
		});
	}
	
	private void initialiseFileChooser()
	{
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f)
			{
				if (f.isDirectory())
				{
					return true;
				}
				return f.getName().toLowerCase().endsWith(".xml");
			}

			@Override
			public String getDescription()
			{
				return "Layer definition files (*.xml)";
			}
		});
		
	}

	private void initialiseToolbar()
	{
		toolbar = new JToolBar();
		
		toolbar.add(loadLayerDefinitionAction);
		toolbar.add(removeLayerDefinitionAction);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(addLayerToAnimationAction);
	}

	private void packComponents()
	{
		scrollPane = new JScrollPane(layerList);
		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		if (e != null && e.getSource() instanceof Animation)
		{
			this.animation = (Animation)e.getSource();
			layerList.setCellRenderer(new LayerListRenderer(animation));
			addLayerToAnimationAction.setEnabled(!animation.hasLayer((LayerIdentifier)layerList.getSelectedValue()));
		}
		layerList.validate();
	}
	
	private void addSelectedLayerToAnimation()
	{
		animation.addLayer((LayerIdentifier)layerList.getSelectedValue());
		addLayerToAnimationAction.setEnabled(false);
		layerList.repaint();
	}
	
	private void promptToRemoveLayersFromList()
	{
		LayerIdentifier[] layerIdentifiers = getSelectedLayerIdentifiers();
		if (layerIdentifiers == null || layerIdentifiers.length == 0)
		{
			return;
		}
		
		boolean removalConfirmed = promptUserForConfirmationOfRemoval(layerIdentifiers);
		if (removalConfirmed)
		{
			removeLayersFromList(layerIdentifiers);
		}
	}
	
	private LayerIdentifier[] getSelectedLayerIdentifiers()
	{
		List<LayerIdentifier> result = new ArrayList<LayerIdentifier>();
		for (Object o : layerList.getSelectedValues())
		{
			result.add((LayerIdentifier)o);
		}
		return result.toArray(new LayerIdentifier[result.size()]);
	}

	/**
	 * Prompt the user to remove the provided layers from the list of known layers
	 */
	private boolean promptUserForConfirmationOfRemoval(LayerIdentifier[] layerIdentifiers)
	{
		String selectionList = "";
		for (LayerIdentifier identifier : layerIdentifiers)
		{
			if (identifier == null)
			{
				continue;
			}
			selectionList += "- " + identifier.getName() + "\n";
			
		}
		int response = JOptionPane.showConfirmDialog(getParentWindow(),
									  				 getMessage(getQueryRemoveLayersFromListMessageKey(), selectionList), 
									  				 getMessage(getQueryRemoveLayersFromListCaptionKey()),
									  				 JOptionPane.YES_NO_OPTION,
									  				 JOptionPane.QUESTION_MESSAGE);
		
		return response == JOptionPane.YES_OPTION;
	}
	
	/**
	 * Remove the provided layers from the list of known layers
	 */
	private void removeLayersFromList(LayerIdentifier[] layerIdentifiers)
	{
		if (layerIdentifiers == null || layerIdentifiers.length == 0)
		{
			return;
		}
		for (LayerIdentifier identifier : layerIdentifiers)
		{
			knownLayers.remove(identifier);
			Settings.get().removeKnownLayer(identifier);
		}
		layerList.setSelectedIndex(0);
		layerList.validate();
	}

	private void promptToAddLayersFromDefinitions()
	{
		File[] definitionFiles = promptUserForDefinitionFiles();
		addLayersFromDefinitionFiles(definitionFiles);
	}
	
	/**
	 * Prompt the user to select one or more layer definition files to add to the known layers
	 */
	private File[] promptUserForDefinitionFiles()
	{
		int userAction = fileChooser.showOpenDialog(getParentWindow());
		if (userAction == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFiles();
		}
		return null;
	}
	
	/**
	 * Load layer identifiers from the provided definition files and add them to the list of known layers
	 */
	private void addLayersFromDefinitionFiles(File[] definitionFiles)
	{
		if (definitionFiles == null || definitionFiles.length == 0)
		{
			return;
		}
		for (File definitionFile : definitionFiles)
		{
			if (definitionFile == null || definitionFile.isDirectory())
			{
				continue;
			}
			try
			{
				addIdentifierToKnownLayers(LayerIdentifierFactory.createFromDefinition(definitionFile.toURI().toURL()));
			}
			catch (MalformedURLException e)
			{
				logException(e);
			}
		}
		layerList.validate();
	}
	
	private void addIdentifierToKnownLayers(LayerIdentifier identifier)
	{
		if (identifier == null)
		{
			return;
		}
		knownLayers.add(identifier);
		Settings.get().addKnownLayer(identifier);
	}
	
	/**
	 * The renderer to use for the layer list. 
	 */
	private static class LayerListRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 20100910L;

		private Animation animation;
		
		public LayerListRenderer(Animation animation)
		{
			this.animation = animation;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			LayerIdentifier layerIdentifier = (LayerIdentifier)value;
			
			JLabel label = (JLabel)super.getListCellRendererComponent(list, layerIdentifier, index, isSelected, cellHasFocus);
			label.setText(((LayerIdentifier)value).getName());
			
			JComponent result = new JPanel(new BorderLayout());
			result.add(label, BorderLayout.WEST);
			
			// Stripe the layer palette
			if (index % 2 == 0 && !isSelected)
			{
				label.setBackground(LAFConstants.getHighlightColor());
			} 
			result.setBackground(label.getBackground());
			
			// Add the 'included in animation' indicator
			if (animation.hasLayer(layerIdentifier))
			{
				result.add(new JLabel(Icons.flag.getIcon()), BorderLayout.EAST);
			}
			
			return result;
		}
	}
	
	/**
	 * An implementation of the {@link ListModel} interface that is backed by a {@link List}.
	 * <p/>
	 * Add and remove events are fired when elements are added or removed from the backing list.
	 */
	private static class ListBackedModel<T> extends AbstractListModel
	{
		private static final long serialVersionUID = 20100910L;
		
		private List<T> backingList = new ArrayList<T>();

		public boolean contains(T object)
		{
			return backingList.contains(object);
		}
		
		public void add(T object)
		{
			backingList.add(object);
			fireIntervalAdded(this, backingList.size() - 1, backingList.size());
		}
		
		public void remove(T object)
		{
			backingList.remove(object);
			fireIntervalRemoved(this,  backingList.size(),  backingList.size() - 1);
		}
		
		@Override
		public int getSize()
		{
			return backingList.size();
		}

		@Override
		public Object getElementAt(int index)
		{
			return backingList.get(index);
		}
		
	}
}
