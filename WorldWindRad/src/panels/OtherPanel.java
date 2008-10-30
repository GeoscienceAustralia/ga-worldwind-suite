package panels;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import layers.geonames.GeoNamesLayer;
import layers.other.GravityLayer;
import layers.other.MagneticsLayer;

public class OtherPanel extends JPanel
{
	private Layer gravity;
	private Layer magnetics;

	private JCheckBox gravityCheck;
	private JSlider gravitySlider;
	private JCheckBox magneticsCheck;
	private JSlider magneticsSlider;

	private WorldWindow wwd;

	public OtherPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		createLayers();
		fillPanel();
	}

	private void createLayers()
	{
		gravity = new GravityLayer();
		gravity.setEnabled(false);
		wwd.getModel().getLayers().add(gravity);

		magnetics = new MagneticsLayer();
		magnetics.setEnabled(false);
		wwd.getModel().getLayers().add(magnetics);
		
		wwd.getModel().getLayers().add(new GeoNamesLayer());
	}

	private void fillPanel()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints c;
		JPanel panel;
		Dimension size;

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateLayers();
			}
		};
		ChangeListener cl = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				updateLayers();
			}
		};

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		add(panel, c);

		gravityCheck = new JCheckBox("Gravity");
		gravityCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(gravityCheck, c);

		gravitySlider = new JSlider(1, 100, 100);
		gravitySlider.setPaintLabels(false);
		gravitySlider.setPaintTicks(false);
		gravitySlider.addChangeListener(cl);
		size = gravitySlider.getPreferredSize();
		size.width = 50;
		gravitySlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(gravitySlider, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		add(panel, c);

		magneticsCheck = new JCheckBox("Magnetics");
		magneticsCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(magneticsCheck, c);

		magneticsSlider = new JSlider(1, 100, 100);
		magneticsSlider.setPaintLabels(false);
		magneticsSlider.setPaintTicks(false);
		magneticsSlider.addChangeListener(cl);
		size = magneticsSlider.getPreferredSize();
		size.width = 50;
		magneticsSlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(magneticsSlider, c);
	}

	private void updateLayers()
	{
		gravity.setEnabled(gravityCheck.isSelected());
		gravity.setOpacity(gravitySlider.getValue() / 100d);
		magnetics.setEnabled(magneticsCheck.isSelected());
		magnetics.setOpacity(magneticsSlider.getValue() / 100d);
		wwd.redraw();
	}
}
