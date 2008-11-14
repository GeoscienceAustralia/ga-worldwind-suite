package panels.layers;

import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
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
import util.Util;

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
	private JComboBox areasCombo;

	private WorldWindow wwd;

	private final static Area NSW = new Area("New South Wales (NSW)", -33.1987,
			149.0234, 392604);
	private final static Area VIC = new Area("Victoria (VIC)", -36.9108,
			144.2817, 392604);
	private final static Area QLD = new Area("Mount Isa (QLD)", -20.8108,
			140.1693, 451477);
	private final static Area SA_1 = new Area("NW South Australia (SA)",
			-26.6803, 130.9017, 490283);
	private final static Area SA_2 = new Area("Flinders (SA)", -32.0084,
			138.6874, 392604);
	private final static Area NT = new Area("Central Australia (NT)", -23.8029,
			133.0763, 488105);
	private final static Area WA = new Area("Pilbara (WA)", -21.0474, 119.6494,
			559794);
	private final static Area TAS = new Area("NE Tasmania (TAS)", -41.1247,
			147.8028, 161772);

	private final static Object[] AREAS = new Object[] { "", NSW, VIC, QLD,
			SA_1, SA_2, NT, WA, TAS };

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

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		add(panel, c);

		JLabel label = new JLabel("Fly to area:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);

		areasCombo = new JComboBox(AREAS);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, 5, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(areasCombo, c);

		areasCombo.addActionListener(new ActionListener()
		{
			private Timer timer;

			public void actionPerformed(ActionEvent e)
			{
				Object object = areasCombo.getSelectedItem();
				if (object instanceof Area)
				{
					Area area = (Area) object;
					long lengthMillis = area.applyStateIterator(wwd);
					
					if (timer != null)
					{
						timer.stop();
					}
					timer = new Timer((int) lengthMillis, new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							areasCombo.setSelectedIndex(0);
							timer = null;
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
			}
		});

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

	private static class Area
	{
		private final String name;
		private final LatLon center;
		private final double zoom;

		public Area(String name, double lat, double lon, double zoom)
		{
			this(name, LatLon.fromDegrees(lat, lon), zoom);
		}

		public Area(String name, LatLon center, double zoom)
		{
			this.name = name;
			this.center = center;
			this.zoom = zoom;
		}

		public long applyStateIterator(WorldWindow wwd)
		{
			if (!(wwd.getView() instanceof OrbitView))
				return 0;

			OrbitView view = (OrbitView) wwd.getView();
			Position beginCenter = view.getCenterPosition();
			long lengthMillis = Util.getScaledLengthMillis(beginCenter
					.getLatLon(), center, 2000, 8000);

			ViewStateIterator vsi = FlyToOrbitViewStateIterator
					.createPanToIterator(wwd.getModel().getGlobe(),
							beginCenter, new Position(center, 0), view
									.getHeading(), Angle.ZERO, view.getPitch(),
							Angle.ZERO, view.getZoom(), zoom, lengthMillis,
							true);
			view.applyStateIterator(vsi);
			return lengthMillis;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
