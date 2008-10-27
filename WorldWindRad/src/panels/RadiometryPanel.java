package panels;

import gov.nasa.worldwind.layers.Layer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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

	public RadiometryPanel()
	{
		super(new GridLayout(0, 1));
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
			layer.setEnabled(false);
		}
	}

	protected void fillPanel()
	{
		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateLayers();
			}
		};
		
		radioCheck = new JCheckBox("Radiometrics");
		add(radioCheck);
		radioCheck.addActionListener(al);
		
		ternaryRadio = new JRadioButton("Ternary");
		add(ternaryRadio);
		ternaryRadio.addActionListener(al);
		
		uraniumRadio = new JRadioButton("Uranium");
		add(uraniumRadio);
		uraniumRadio.addActionListener(al);
		
		thoriumRadio = new JRadioButton("Thorium");
		add(thoriumRadio);
		thoriumRadio.addActionListener(al);
		
		potassiumRadio = new JRadioButton("Potassium");
		add(potassiumRadio);
		potassiumRadio.addActionListener(al);
		
		doseRateRadio = new JRadioButton("Dose Rate");
		add(doseRateRadio);
		doseRateRadio.addActionListener(al);
		
		ratioUThRadio = new JRadioButton("Uranium/Thorium Ratio");
		add(ratioUThRadio);
		ratioUThRadio.addActionListener(al);
		
		ratioUKRadio = new JRadioButton("Uranium/Potassium Ratio");
		add(ratioUKRadio);
		ratioUKRadio.addActionListener(al);
		
		ratioThKRadio = new JRadioButton("Thorium/Potassium Ratio");
		add(ratioThKRadio);
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
		
		areasCheck = new JCheckBox("Areas of Interest");
		add(areasCheck);
		areasCheck.addActionListener(al);
		
		ternaryRadio.setSelected(true);
		updateLayers();
	}

	public Layer[] getLayers()
	{
		return layers;
	}

	private void updateLayers()
	{
		boolean radio = radioCheck.isSelected();
		ternary.setEnabled(radio && ternaryRadio.isSelected());
		uranium.setEnabled(radio && uraniumRadio.isSelected());
		thorium.setEnabled(radio && thoriumRadio.isSelected());
		potassium.setEnabled(radio && potassiumRadio.isSelected());
		doseRate.setEnabled(radio && doseRateRadio.isSelected());
		ratioUTh.setEnabled(radio && ratioUThRadio.isSelected());
		ratioUK.setEnabled(radio && ratioUKRadio.isSelected());
		ratioThK.setEnabled(radio && ratioThKRadio.isSelected());
		areas.setEnabled(areasCheck.isSelected());
	}
}
