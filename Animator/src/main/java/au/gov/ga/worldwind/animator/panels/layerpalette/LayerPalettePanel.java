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
package au.gov.ga.worldwind.animator.panels.layerpalette;

import static au.gov.ga.worldwind.animator.util.ExceptionLogger.logException;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.panels.AnimatorCollapsiblePanel;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.ListBackedModel;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A panel that allows the user to locate and select a layer for inclusion
 * in the animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerPalettePanel extends CollapsiblePanelBase implements AnimatorCollapsiblePanel
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
	
	/** A listener used to update the palette when a layer is added or removed from the animation */
	private AnimationEventListener animationListener;
	
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
		initialiseAnimationListener();
		updateListModel();
		initialiseActions();
		initialiseFileChooser();
		initialiseToolbar();
		packComponents();
		setPreferredSize(new Dimension(0, 50));
	}
	
	private void initialiseLayerList()
	{
		layerList = new JList(knownLayers);
		layerList.setCellRenderer(new LayerListRenderer(animation));
		
		layerList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				LayerIdentifier layerIdentifier = (LayerIdentifier)layerList.getSelectedValue();
				addLayerToAnimationAction.setEnabled(!animation.hasLayer(layerIdentifier));
				removeLayerDefinitionAction.setEnabled(true);
			}
		});
		layerList.setActionMap(null);
		
		layerList.setDragEnabled(true);
		layerList.setDropMode(DropMode.INSERT);
		layerList.setTransferHandler(new LayerPaletteTransferHandler(layerList));
	}
	
	private void initialiseAnimationListener()
	{
		animationListener = new AnimationEventListener()
		{
			@Override
			public void receiveAnimationEvent(AnimationEvent event)
			{
				if (isLayerEvent(event))
				{
					addLayerToAnimationAction.setEnabled(!animation.hasLayer(getSelectedLayerIdentifier()));
					layerList.repaint();
				}
			}

			private boolean isLayerEvent(AnimationEvent event)
			{
				return event.getRootCause().getValue() instanceof AnimatableLayer;
			}

		};
		animation.addChangeListener(animationListener);
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
		removeLayerDefinitionAction.setEnabled(false);
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
	public void updateAnimation(Animation newAnimation)
	{
		if (newAnimation == null)
		{
			return;
		}
		
		this.animation = newAnimation;
		layerList.setCellRenderer(new LayerListRenderer(animation));
		addLayerToAnimationAction.setEnabled(!animation.hasLayer((LayerIdentifier)layerList.getSelectedValue()));
		animation.addChangeListener(animationListener);
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

	private LayerIdentifier getSelectedLayerIdentifier()
	{
		return (LayerIdentifier)layerList.getSelectedValue();
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
		int response = issueConfirmationPrompt(getMessage(getQueryRemoveLayersFromListMessageKey(), selectionList), 
											   getMessage(getQueryRemoveLayersFromListCaptionKey()));
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
	protected File[] promptUserForDefinitionFiles()
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
	 * Issue a confirmation prompt to the user with the provided message and caption.
	 * @return the response from the user, one of {@link JOptionPane#YES_OPTION} or {@link JOptionPane#NO_OPTION}
	 */
	protected int issueConfirmationPrompt(String message, String caption)
	{
		return JOptionPane.showConfirmDialog(getParentWindow(), message, caption, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
}
