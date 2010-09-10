package au.gov.ga.worldwind.animator.panels.layerpalette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

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
	
	// Actions
	private BasicAction addLayerToAnimationAction;
	
	public LayerPalettePanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getLayerPalettePanelNameKey()));
		
		initialiseLayerList();
		updateListModel();
		initialiseActions();
		initialiseToolbar();
		packComponents();
	}
	
	private void initialiseActions()
	{
		addLayerToAnimationAction = new BasicAction(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getAddLayerToAnimationLabelKey()), Icons.add.getIcon());
		addLayerToAnimationAction.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				animation.addLayer((LayerIdentifier)layerList.getSelectedValue());
				
			}
		});
	}
	
	private void initialiseToolbar()
	{
		toolbar = new JToolBar();
		
		toolbar.add(addLayerToAnimationAction);
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

	private void initialiseLayerList()
	{
		layerList = new JList(knownLayers);
		layerList.setCellRenderer(new LayerListRenderer(animation));
	}

	private void packComponents()
	{
		scrollPane = new JScrollPane(layerList);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		if (e != null && e.getSource() instanceof Animation)
		{
			this.animation = (Animation)e.getSource();
			layerList.setCellRenderer(new LayerListRenderer(animation));
		}
		layerList.validate();
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
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			LayerIdentifier layerIdentifier = (LayerIdentifier)value;
			
			label.setText(((LayerIdentifier)value).getName());
			
			// Stripe the layer palette
			if (index % 2 == 0 && !isSelected)
			{
				label.setBackground(new Color(230, 247, 252));
			}
			
			if (animation.hasLayer(layerIdentifier))
			{
				label.setIcon(Icons.flag.getIcon());
			}
			
			return label;
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
