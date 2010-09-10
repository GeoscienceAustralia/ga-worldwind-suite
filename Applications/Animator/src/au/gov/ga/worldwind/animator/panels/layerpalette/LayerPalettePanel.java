package au.gov.ga.worldwind.animator.panels.layerpalette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;

import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
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

	/** A scrollable container for holding the tree */
	private JScrollPane scrollPane;
	
	/** The list that allows the user to browse through known layers */
	private JList layerList;
	
	/** The identities of known layers */
	private ListBackedModel<LayerIdentifier> knownLayers = new ListBackedModel<LayerIdentifier>();
	
	public LayerPalettePanel()
	{
		setName(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getLayerPalettePanelNameKey()));
		
		initialiseLayerList();
		updateListModel();
		packComponents();
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
		layerList.setCellRenderer(new LayerListRenderer());
	}

	private void packComponents()
	{
		scrollPane = new JScrollPane(layerList);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		layerList.validate();
	}
	
	private static class LayerListRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setText(((LayerIdentifier)value).getName());
			
			if (index % 2 == 0 && !isSelected)
			{
				label.setBackground(new Color(230, 247, 252));
			}
			
			return label;
		}
	}
	
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
