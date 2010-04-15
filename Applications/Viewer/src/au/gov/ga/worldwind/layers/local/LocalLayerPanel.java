package au.gov.ga.worldwind.layers.local;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.util.FlatJButton;
import au.gov.ga.worldwind.util.Icons;

public class LocalLayerPanel extends JPanel
{
	private JPanel layersPanel;
	private WorldWindow wwd;
	private Frame owner;

	public LocalLayerPanel(WorldWindow wwd, Frame owner)
	{
		this.wwd = wwd;
		this.owner = owner;
		setLayout(new BorderLayout());
		layersPanel = new JPanel(new GridLayout(0, 1, 0, 4));
		JScrollPane scrollPane = new JScrollPane(layersPanel);
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.NORTH);
	}

	public void updateLayers()
	{
		clearUserLayers();
		for (LocalLayer layer : LocalLayers.get().getLayers())
		{
			addUserLayer(layer);
		}
	}

	private void clearUserLayers()
	{
		layersPanel.removeAll();
	}

	private void addUserLayer(final LocalLayer layer)
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());
		layersPanel.add(panel);

		final JCheckBox check = new JCheckBox(layer.getName());
		check.setSelected(layer.isEnabled());
		check.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				layer.setEnabled(check.isSelected());
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(check, c);

		final JSlider slider = new JSlider(0, 100,
				(int) (layer.getOpacity() * 100));
		slider.setPaintLabels(false);
		slider.setPaintTicks(false);
		Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				layer.setOpacity(slider.getValue() / 100d);
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(slider, c);

		FlatJButton edit = new FlatJButton(Icons.edit);
		edit.restrictSize();
		edit.setToolTipText("Edit layer");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				LocalLayerDefinition def = layer.getDefinition();
				def = LocalLayerEditor.editDefinition(owner, "Edit local tileset",
						def);
				if (def != null)
				{
					LocalLayers.get().updateLayer(def);
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(edit);

		FlatJButton delete = new FlatJButton(Icons.delete);
		delete.restrictSize();
		delete.setToolTipText("Delete tileset");
		delete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int value = JOptionPane.showConfirmDialog(LocalLayerPanel.this,
						"Are you sure you want to delete local tileset "
								+ layer.getName() + "?", "Delete tileset?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (value == JOptionPane.YES_OPTION)
				{
					LocalLayers.get().removeLayer(layer.getDefinition());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 3;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(delete, c);
	}

	public boolean isEmpty()
	{
		return LocalLayers.get().isEmpty();
	}
}
