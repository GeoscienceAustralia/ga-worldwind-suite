package au.gov.ga.worldwind.panels.oldlayers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.components.FlatJButton;
import au.gov.ga.worldwind.components.HtmlViewer;
import au.gov.ga.worldwind.layers.ga.GALayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.DoseRateLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.PotassiumLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.RatioThKLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.RatioUKLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.RatioUThLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.TernaryLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.ThoriumLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.USquaredLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.UraniumLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.DoseRateAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.PotassiumAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.RatioThKAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.RatioUKAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.RatioUThAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.TernaryAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.ThoriumAreasLayer;
import au.gov.ga.worldwind.layers.ga.radiometrics.areas.UraniumAreasLayer;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;

public class RadiometricsPanel extends JPanel {
	private Layer[] layers;

	private Layer ternary;
	private Layer uranium;
	private Layer thorium;
	private Layer potassium;
	private Layer doseRate;
	private Layer ratioUTh;
	private Layer ratioUK;
	private Layer ratioThK;
	private Layer ternaryAreas;
	private Layer uraniumAreas;
	private Layer thoriumAreas;
	private Layer potassiumAreas;
	private Layer doseRateAreas;
	private Layer ratioUThAreas;
	private Layer ratioUKAreas;
	private Layer ratioThKAreas;
	private Layer USquaredOverTh;

	private JCheckBox radioCheck;
	private JRadioButton ternaryRadio;
	private JRadioButton uraniumRadio;
	private JRadioButton thoriumRadio;
	private JRadioButton potassiumRadio;
	private JRadioButton doseRateRadio;
	private JRadioButton ratioUThRadio;
	private JRadioButton ratioUKRadio;
	private JRadioButton ratioThKRadio;
	private JRadioButton USquaredOverThRadio;
	private JCheckBox areasCheck;
	private JComboBox areasCombo;
	private JSlider radioSlider;
	private JSlider areasSlider;
	private JComboBox flytoCombo;

	private WorldWindow wwd;
	private Frame frame;

	private final static Area NSW = new Area("Bathurst/Orange Region - NSW",
			-33.1987, 149.0234, 392604);
	private final static Area VIC = new Area("Central Uplands - VIC", -36.9108,
			144.2817, 392604);
	private final static Area QLD = new Area("Mount Isa Region - QLD",
			-20.8108, 140.1693, 451477);
	private final static Area SA_1 = new Area("Musgrave Range - SA", -26.6803,
			130.9017, 490283);
	private final static Area SA_2 = new Area("Flinders Ranges - SA", -32.0084,
			138.6874, 392604);
	private final static Area NT = new Area("Macdonnell Ranges - NT", -23.8029,
			133.0763, 488105);
	private final static Area WA = new Area("Pilbara Region - WA", -21.0474,
			119.6494, 559794);
	private final static Area TAS = new Area("North East Region - TAS",
			-41.1247, 147.8028, 161772);

	private final static Object[] FLYTO = new Object[] { "", NSW, VIC, QLD,
			SA_1, SA_2, NT, WA, TAS };

	public RadiometricsPanel(WorldWindow wwd, Frame frame) {
		this.wwd = wwd;
		this.frame = frame;
		createLayers();
		fillPanel();
	}

	private void createLayers() {
		ternary = new TernaryLayer();
		uranium = new UraniumLayer();
		thorium = new ThoriumLayer();
		potassium = new PotassiumLayer();
		doseRate = new DoseRateLayer();
		ratioUTh = new RatioUThLayer();
		ratioUK = new RatioUKLayer();
		ratioThK = new RatioThKLayer();
		USquaredOverTh = new USquaredLayer();

		ternaryAreas = new TernaryAreasLayer();
		uraniumAreas = new UraniumAreasLayer();
		thoriumAreas = new ThoriumAreasLayer();
		potassiumAreas = new PotassiumAreasLayer();
		doseRateAreas = new DoseRateAreasLayer();
		ratioUThAreas = new RatioUThAreasLayer();
		ratioUKAreas = new RatioUKAreasLayer();
		ratioThKAreas = new RatioThKAreasLayer();

		layers = new Layer[] { ternary, uranium, thorium, potassium, doseRate,
				ratioUTh, ratioUK, ratioThK, ternaryAreas, uraniumAreas,
				thoriumAreas, potassiumAreas, doseRateAreas, ratioUThAreas,
				ratioUKAreas, ratioThKAreas, USquaredOverTh };
		for (Layer layer : layers) {
			wwd.getModel().getLayers().add(layer);
			layer.setEnabled(false);
		}
	}

	private void fillPanel() {
		int INDENT = 20;
		int SPACING = 5;

		setLayout(new GridBagLayout());
		GridBagConstraints c;
		JPanel panel;
		Dimension size;
		JSeparator js;

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLayers();
			}
		};
		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateLayers();
			}
		};

		JPanel mainPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		add(mainPanel, c);

		panel = new TitlePanel(
				new String[] { "RADIOMETRIC MAP", "OF AUSTRALIA" },
				new String[] { "1st Edition, 2009" }, 0, 0);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		mainPanel.add(panel, c);

		js = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, 0, SPACING, 0);
		mainPanel.add(js, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		mainPanel.add(panel, c);

		int gridy = 0;

		radioCheck = new JCheckBox("Radioelements");
		radioCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy;
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
		c.gridy = gridy;
		c.anchor = GridBagConstraints.WEST;
		panel.add(radioSlider, c);

		FlatJButton metadata = new FlatJButton(Icons.info);
		metadata.restrictSize();
		metadata.addActionListener(createMetadataListener("Radiometrics",
				"info_radio.html", 700, 500));
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(metadata, c);

		ActionListener ternaryLegend = createMetadataListener("Ternary legend",
				"ternary_legend.html", 500, 380);
		ActionListener KLegend = createMetadataListener("Potassium legend",
				"k_legend.html", 300, 380);
		ActionListener ThLegend = createMetadataListener("Thorium legend",
				"th_legend.html", 300, 380);
		ActionListener ULegend = createMetadataListener("Uranium legend",
				"u_legend.html", 300, 380);
		ActionListener ratioLegend = createMetadataListener("Ratio legend",
				"ratio_legend.html", 300, 380);
		ActionListener doseLegend = createMetadataListener("Dose rate legend",
				"dose_legend.html", 300, 380);

		ternaryRadio = new JRadioButton(ternary.getName());
		ternaryRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ternaryRadio, ternaryLegend);

		potassiumRadio = new JRadioButton(potassium.getName());
		potassiumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, potassiumRadio, KLegend);

		thoriumRadio = new JRadioButton(thorium.getName());
		thoriumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, thoriumRadio, ThLegend);

		uraniumRadio = new JRadioButton(uranium.getName());
		uraniumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, uraniumRadio, ULegend);

		doseRateRadio = new JRadioButton(doseRate.getName());
		doseRateRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, doseRateRadio, doseLegend);

		ratioThKRadio = new JRadioButton(ratioThK.getName());
		ratioThKRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioThKRadio, ratioLegend);

		ratioUKRadio = new JRadioButton(ratioUK.getName());
		ratioUKRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUKRadio, ratioLegend);

		ratioUThRadio = new JRadioButton(ratioUTh.getName());
		ratioUThRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUThRadio, ratioLegend);

		USquaredOverThRadio = new JRadioButton(USquaredOverTh.getName());
		USquaredOverThRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, USquaredOverThRadio, ratioLegend);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ternaryRadio);
		buttonGroup.add(uraniumRadio);
		buttonGroup.add(thoriumRadio);
		buttonGroup.add(potassiumRadio);
		buttonGroup.add(doseRateRadio);
		buttonGroup.add(ratioUThRadio);
		buttonGroup.add(ratioUKRadio);
		buttonGroup.add(ratioThKRadio);
		buttonGroup.add(USquaredOverThRadio);

		js = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, 0, SPACING, 0);
		mainPanel.add(js, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.insets = new Insets(0, INDENT, 0, 0);
		c.anchor = GridBagConstraints.WEST;
		mainPanel.add(panel, c);

		areasCheck = new JCheckBox("Colour-enhanced areas");
		areasCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
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
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(areasSlider, c);

		metadata = new FlatJButton(Icons.info);
		metadata.restrictSize();
		metadata.addActionListener(createMetadataListener(
				"Colour-enhanced areas", "info_areas.html", 700, 500));
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(metadata, c);

		JLabel label = new JLabel("Layer:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(5, 0, 5, 0);
		panel.add(label, c);

		Layer[] areas = new Layer[] { ternaryAreas, potassiumAreas,
				thoriumAreas, uraniumAreas, doseRateAreas, ratioThKAreas,
				ratioUKAreas, ratioUThAreas };
		areasCombo = new JComboBox(areas);
		areasCombo.setMaximumRowCount(areas.length);
		areasCombo.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 0);
		c.anchor = GridBagConstraints.WEST;
		panel.add(areasCombo, c);

		FlatJButton legend = new FlatJButton(Icons.legend);
		legend.restrictSize();
		legend
				.addActionListener(createMetadataListener(
						"Color-enhanced areas legends", "areas_legends.html",
						700, 500));
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 5, 0);
		panel.add(legend, c);

		label = new JLabel("Fly to:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);

		flytoCombo = new JComboBox(FLYTO);
		flytoCombo.setMaximumRowCount(FLYTO.length);
		size = flytoCombo.getPreferredSize();
		size.width -= 10;
		flytoCombo.setPreferredSize(size);
		flytoCombo.setMaximumSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 3;
		c.insets = new Insets(0, 5, 0, 0);
		c.anchor = GridBagConstraints.WEST;
		panel.add(flytoCombo, c);

		flytoCombo.addActionListener(new ActionListener() {
			private Timer timer;

			public void actionPerformed(ActionEvent e) {
				Object object = flytoCombo.getSelectedItem();
				if (object instanceof Area) {
					Area area = (Area) object;
					long lengthMillis = area.applyStateIterator(wwd);

					if (timer != null && timer.isRunning()) {
						timer.stop();
					}
					timer = new Timer((int) lengthMillis, new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							flytoCombo.setSelectedIndex(0);
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
			}
		});

		js = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING * 2, 0, SPACING * 2, 0);
		mainPanel.add(js, c);

		ternaryRadio.setSelected(true);
		updateLayers();
		this.revalidate();
	}

	private void addRadioToPanel(JPanel panel, int gridy, JRadioButton radio,
			ActionListener legendAL) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(radio, c);

		if (legendAL != null) {
			FlatJButton legend = new FlatJButton(Icons.legend);
			legend.restrictSize();
			legend.addActionListener(legendAL);
			legend.setToolTipText("Show legend");
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = gridy;
			c.anchor = GridBagConstraints.WEST;
			panel.add(legend, c);
		}
	}

	private ActionListener createMetadataListener(final String title,
			final String htmlpage, final int width, final int height) {
		return new ActionListener() {
			private HtmlViewer dialog = null;

			public void actionPerformed(ActionEvent ae) {
				if (dialog == null) {
					URL page = null, base = null;
					try {
						base = new URL(GALayer.getMetadataBaseUrl(),
								"radiometrics/");
						page = new URL(base, htmlpage);
					} catch (MalformedURLException e) {
					}
					dialog = new HtmlViewer(frame, title, page, base);
					dialog.setSize(width, height);
					dialog.setLocationRelativeTo(frame);
				}
				if (dialog.isVisible())
					dialog.requestFocus();
				else
					dialog.setVisible(true);
			}
		};
	}

	public Layer[] getLayers() {
		return layers;
	}

	private void updateLayers() {
		boolean radio = radioCheck.isSelected();
		boolean areas = areasCheck.isSelected();

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
		USquaredOverTh.setEnabled(radio && USquaredOverThRadio.isSelected());
		USquaredOverTh.setOpacity(radioSlider.getValue() / 100d);

		Layer area = (Layer) areasCombo.getSelectedItem();
		ternaryAreas.setEnabled(areas && area == ternaryAreas);
		ternaryAreas.setOpacity(areasSlider.getValue() / 100d);
		uraniumAreas.setEnabled(areas && area == uraniumAreas);
		uraniumAreas.setOpacity(areasSlider.getValue() / 100d);
		thoriumAreas.setEnabled(areas && area == thoriumAreas);
		thoriumAreas.setOpacity(areasSlider.getValue() / 100d);
		potassiumAreas.setEnabled(areas && area == potassiumAreas);
		potassiumAreas.setOpacity(areasSlider.getValue() / 100d);
		doseRateAreas.setEnabled(areas && area == doseRateAreas);
		doseRateAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioUThAreas.setEnabled(areas && area == ratioUThAreas);
		ratioUThAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioUKAreas.setEnabled(areas && area == ratioUKAreas);
		ratioUKAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioThKAreas.setEnabled(areas && area == ratioThKAreas);
		ratioThKAreas.setOpacity(areasSlider.getValue() / 100d);

		wwd.redraw();
	}

	private static class Area {
		private final String name;
		private final LatLon center;
		private final double zoom;

		public Area(String name, double lat, double lon, double zoom) {
			this(name, LatLon.fromDegrees(lat, lon), zoom);
		}

		public Area(String name, LatLon center, double zoom) {
			this.name = name;
			this.center = center;
			this.zoom = zoom;
		}

		public long applyStateIterator(WorldWindow wwd) {
			if (!(wwd.getView() instanceof OrbitView))
				return 0;

			OrbitView view = (OrbitView) wwd.getView();
			Position beginCenter = view.getCenterPosition();
			long lengthMillis = Util.getScaledLengthMillis(beginCenter, center);

			view.addAnimator(FlyToOrbitViewAnimator
					.createFlyToOrbitViewAnimator(view, beginCenter,
							new Position(center, 0), view.getHeading(),
							Angle.ZERO, view.getPitch(), Angle.ZERO, view
									.getZoom(), zoom, lengthMillis, true));
			wwd.redraw();

			return lengthMillis;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
