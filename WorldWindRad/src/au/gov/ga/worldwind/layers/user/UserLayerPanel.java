package au.gov.ga.worldwind.layers.user;

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

public class UserLayerPanel extends JPanel
{
	private JPanel layersPanel;
	private WorldWindow wwd;
	private Frame owner;

	public UserLayerPanel(WorldWindow wwd, Frame owner)
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
		for (UserLayer layer : UserLayers.getLayers())
		{
			addUserLayer(layer);
		}
	}

	private void clearUserLayers()
	{
		layersPanel.removeAll();
	}

	private void addUserLayer(final UserLayer layer)
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
				UserLayerDefinition def = layer.getDefinition();
				def = UserLayerEditor.editDefinition(owner, "Edit user layer",
						def);
				if (def != null)
				{
					UserLayers.updateUserLayer(def);
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(edit);

		FlatJButton delete = new FlatJButton(Icons.delete);
		delete.restrictSize();
		delete.setToolTipText("Delete layer");
		delete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int value = JOptionPane.showConfirmDialog(UserLayerPanel.this,
						"Are you sure you want to delete user layer "
								+ layer.getName() + "?", "Delete layer?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (value == JOptionPane.YES_OPTION)
				{
					UserLayers.removeUserLayer(layer.getDefinition());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 3;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(delete, c);
	}
}
