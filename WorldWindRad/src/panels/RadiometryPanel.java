package panels;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import layers.radiometry.AreasLayer;
import layers.radiometry.DoseRateLayer;
import layers.radiometry.PotassiumLayer;
import layers.radiometry.RatioThKLayer;
import layers.radiometry.RatioUKLayer;
import layers.radiometry.RatioUThLayer;
import layers.radiometry.TernaryLayer;
import layers.radiometry.ThoriumLayer;
import layers.radiometry.UraniumLayer;

public class RadiometryPanel extends JPanel
{
	private Layer[] layers;

	private Layer ternary;
	private Layer uranium;
	private Layer thorium;
	private Layer potassium;
	private Layer doseRate;
	private Layer ratioUTh;
	private Layer ratioUK;
	private Layer ratioThK;
	private Layer areas;

	private JCheckBox radioCheck;
	private JRadioButton ternaryRadio;
	private JRadioButton uraniumRadio;
	private JRadioButton thoriumRadio;
	private JRadioButton potassiumRadio;
	private JRadioButton doseRateRadio;
	private JRadioButton ratioUThRadio;
	private JRadioButton ratioUKRadio;
	private JRadioButton ratioThKRadio;
	private JCheckBox areasCheck;
	private JSlider radioSlider;
	private JSlider areasSlider;

	private WorldWindow wwd;

	public RadiometryPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		createLayers();
		fillPanel();
	}

	private void createLayers()
	{
		ternary = new TernaryLayer();
		uranium = new UraniumLayer();
		thorium = new ThoriumLayer();
		potassium = new PotassiumLayer();
		doseRate = new DoseRateLayer();
		ratioUTh = new RatioUThLayer();
		ratioUK = new RatioUKLayer();
		ratioThK = new RatioThKLayer();
		areas = new AreasLayer();

		layers = new Layer[] { ternary, uranium, thorium, potassium, doseRate,
				ratioUTh, ratioUK, ratioThK, areas };
		for (Layer layer : layers)
		{
			wwd.getModel().getLayers().add(layer);
			layer.setEnabled(false);
		}
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

		radioCheck = new JCheckBox("Radiometrics");
		radioCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(radioCheck, c);

		radioSlider = new JSlider(1, 100, 100);
		radioSlider.setPaintLabels(false);
		radioSlider.setPaintTicks(false);
		radioSlider.addChangeListener(cl);
		size = radioSlider.getPreferredSize();
		size.width = 50;
		radioSlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(radioSlider, c);

		panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		add(panel, c);

		ternaryRadio = new JRadioButton("Ternary");
		panel.add(ternaryRadio);
		ternaryRadio.addActionListener(al);

		uraniumRadio = new JRadioButton("Uranium");
		panel.add(uraniumRadio);
		uraniumRadio.addActionListener(al);

		thoriumRadio = new JRadioButton("Thorium");
		panel.add(thoriumRadio);
		thoriumRadio.addActionListener(al);

		potassiumRadio = new JRadioButton("Potassium");
		panel.add(potassiumRadio);
		potassiumRadio.addActionListener(al);

		doseRateRadio = new JRadioButton("Dose Rate");
		panel.add(doseRateRadio);
		doseRateRadio.addActionListener(al);

		ratioUThRadio = new JRadioButton("Uranium/Thorium Ratio");
		panel.add(ratioUThRadio);
		ratioUThRadio.addActionListener(al);

		ratioUKRadio = new JRadioButton("Uranium/Potassium Ratio");
		panel.add(ratioUKRadio);
		ratioUKRadio.addActionListener(al);

		ratioThKRadio = new JRadioButton("Thorium/Potassium Ratio");
		panel.add(ratioThKRadio);
		ratioThKRadio.addActionListener(al);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ternaryRadio);
		buttonGroup.add(uraniumRadio);
		buttonGroup.add(thoriumRadio);
		buttonGroup.add(potassiumRadio);
		buttonGroup.add(doseRateRadio);
		buttonGroup.add(ratioUThRadio);
		buttonGroup.add(ratioUKRadio);
		buttonGroup.add(ratioThKRadio);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		add(panel, c);
		
		areasCheck = new JCheckBox("Areas of Interest");
		areasCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(areasCheck, c);
		
		areasSlider = new JSlider(1, 100, 100);
		areasSlider.setPaintLabels(false);
		areasSlider.setPaintTicks(false);
		areasSlider.addChangeListener(cl);
		size = areasSlider.getPreferredSize();
		size.width = 50;
		areasSlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(areasSlider, c);

		ternaryRadio.setSelected(true);
		updateLayers();
		this.revalidate();
	}

	public Layer[] getLayers()
	{
		return layers;
	}

	private void updateLayers()
	{
		boolean radio = radioCheck.isSelected();
		ternary.setEnabled(radio && ternaryRadio.isSelected());
		ternary.setOpacity(radioSlider.getValue() / 100d);
		uranium.setEnabled(radio && uraniumRadio.isSelected());
		uranium.setOpacity(radioSlider.getValue() / 100d);
		thorium.setEnabled(radio && thoriumRadio.isSelected());
		thorium.setOpacity(radioSlider.getValue() / 100d);
		potassium.setEnabled(radio && potassiumRadio.isSelected());
		potassium.setOpacity(radioSlider.getValue() / 100d);
		doseRate.setEnabled(radio && doseRateRadio.isSelected());
		doseRate.setOpacity(radioSlider.getValue() / 100d);
		ratioUTh.setEnabled(radio && ratioUThRadio.isSelected());
		ratioUTh.setOpacity(radioSlider.getValue() / 100d);
		ratioUK.setEnabled(radio && ratioUKRadio.isSelected());
		ratioUK.setOpacity(radioSlider.getValue() / 100d);
		ratioThK.setEnabled(radio && ratioThKRadio.isSelected());
		ratioThK.setOpacity(radioSlider.getValue() / 100d);
		areas.setEnabled(areasCheck.isSelected());
		areas.setOpacity(areasSlider.getValue() / 100d);
		wwd.redraw();
	}
}
