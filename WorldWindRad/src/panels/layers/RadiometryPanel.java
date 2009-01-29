package panels.layers;

import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicHTML;

import layers.radioareas.DoseRateAreasLayer;
import layers.radioareas.PotassiumAreasLayer;
import layers.radioareas.RatioThKAreasLayer;
import layers.radioareas.RatioUKAreasLayer;
import layers.radioareas.RatioUThAreasLayer;
import layers.radioareas.TernaryAreasLayer;
import layers.radioareas.ThoriumAreasLayer;
import layers.radioareas.UraniumAreasLayer;
import layers.radiometry.DoseRateLayer;
import layers.radiometry.PotassiumLayer;
import layers.radiometry.RatioThKLayer;
import layers.radiometry.RatioUKLayer;
import layers.radiometry.RatioUThLayer;
import layers.radiometry.TernaryLayer;
import layers.radiometry.ThoriumLayer;
import layers.radiometry.UraniumLayer;
import util.FlatJButton;
import util.Icons;
import util.ImageDialog;
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
	private Layer ternaryAreas;
	private Layer uraniumAreas;
	private Layer thoriumAreas;
	private Layer potassiumAreas;
	private Layer doseRateAreas;
	private Layer ratioUThAreas;
	private Layer ratioUKAreas;
	private Layer ratioThKAreas;

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
	private JRadioButton ternaryAreasRadio;
	private JRadioButton uraniumAreasRadio;
	private JRadioButton thoriumAreasRadio;
	private JRadioButton potassiumAreasRadio;
	private JRadioButton doseRateAreasRadio;
	private JRadioButton ratioUThAreasRadio;
	private JRadioButton ratioUKAreasRadio;
	private JRadioButton ratioThKAreasRadio;
	private JSlider radioSlider;
	private JSlider areasSlider;
	private JComboBox areasCombo;

	private WorldWindow wwd;
	private Frame frame;

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

	public RadiometryPanel(WorldWindow wwd, Frame frame)
	{
		this.wwd = wwd;
		this.frame = frame;
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
				ratioUKAreas, ratioThKAreas };
		for (Layer layer : layers)
		{
			wwd.getModel().getLayers().add(layer);
			layer.setEnabled(false);
		}
	}

	private void fillPanel()
	{
		int INDENT = 20;

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
		ActionListener metadataAL = createMetadataListener();

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		add(panel, c);

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
		metadata.addActionListener(metadataAL);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(metadata, c);

		ActionListener ternaryLegend = createLegendListener("ternary_cube.jpg",
				"Ternary legend");
		ActionListener KLegend = createLegendListener("k_legend.jpg",
				"Potassium legend");
		ActionListener ThLegend = createLegendListener("th_legend.jpg",
				"Thorium legend");
		ActionListener ULegend = createLegendListener("u_legend.jpg",
				"Uranium legend");
		ActionListener ratioLegend = createLegendListener("ratio_legend.jpg",
				"Ratio legend");
		ActionListener doseLegend = createLegendListener("dose_legend.jpg",
				"Dose rate legend");

		ternaryRadio = new JRadioButton("Ternary (K-Th-U)");
		ternaryRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ternaryRadio, ternaryLegend);

		potassiumRadio = new JRadioButton("Potassium (K)");
		potassiumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, potassiumRadio, KLegend);

		thoriumRadio = new JRadioButton("Thorium (Th)");
		thoriumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, thoriumRadio, ThLegend);

		uraniumRadio = new JRadioButton("Uranium (U)");
		uraniumRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, uraniumRadio, ULegend);

		doseRateRadio = new JRadioButton("Dose Rate");
		doseRateRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, doseRateRadio, doseLegend);

		ratioThKRadio = new JRadioButton("Thorium/Potassium Ratio");
		ratioThKRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioThKRadio, ratioLegend);

		ratioUKRadio = new JRadioButton("Uranium/Potassium Ratio");
		ratioUKRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUKRadio, ratioLegend);

		ratioUThRadio = new JRadioButton("Uranium/Thorium Ratio");
		ratioUThRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUThRadio, ratioLegend);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ternaryRadio);
		buttonGroup.add(uraniumRadio);
		buttonGroup.add(thoriumRadio);
		buttonGroup.add(potassiumRadio);
		buttonGroup.add(doseRateRadio);
		buttonGroup.add(ratioUThRadio);
		buttonGroup.add(ratioUKRadio);
		buttonGroup.add(ratioThKRadio);

		areasCheck = new JCheckBox("Detailed areas");
		areasCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy;
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
		c.gridy = gridy;
		c.anchor = GridBagConstraints.WEST;
		panel.add(areasSlider, c);

		metadata = new FlatJButton(Icons.info);
		metadata.restrictSize();
		metadata.addActionListener(metadataAL);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(metadata, c);

		ternaryAreasRadio = new JRadioButton("Ternary (K-Th-U)");
		ternaryAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ternaryAreasRadio);

		potassiumAreasRadio = new JRadioButton("Potassium (K)");
		potassiumAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, potassiumAreasRadio);

		thoriumAreasRadio = new JRadioButton("Thorium (Th)");
		thoriumAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, thoriumAreasRadio);

		uraniumAreasRadio = new JRadioButton("Uranium (U)");
		uraniumAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, uraniumAreasRadio);

		doseRateAreasRadio = new JRadioButton("Dose Rate");
		doseRateAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, doseRateAreasRadio);

		ratioThKAreasRadio = new JRadioButton("Thorium/Potassium Ratio");
		ratioThKAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioThKAreasRadio);

		ratioUKAreasRadio = new JRadioButton("Uranium/Potassium Ratio");
		ratioUKAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUKAreasRadio);

		ratioUThAreasRadio = new JRadioButton("Uranium/Thorium Ratio");
		ratioUThAreasRadio.addActionListener(al);
		addRadioToPanel(panel, gridy++, ratioUThAreasRadio);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(ternaryAreasRadio);
		buttonGroup.add(uraniumAreasRadio);
		buttonGroup.add(thoriumAreasRadio);
		buttonGroup.add(potassiumAreasRadio);
		buttonGroup.add(doseRateAreasRadio);
		buttonGroup.add(ratioUThAreasRadio);
		buttonGroup.add(ratioUKAreasRadio);
		buttonGroup.add(ratioThKAreasRadio);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
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

					if (timer != null && timer.isRunning())
					{
						timer.stop();
					}
					timer = new Timer((int) lengthMillis, new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							areasCombo.setSelectedIndex(0);
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
			}
		});

		ternaryRadio.setSelected(true);
		ternaryAreasRadio.setSelected(true);
		updateLayers();
		this.revalidate();
	}

	private void addRadioToPanel(JPanel panel, int gridy, JRadioButton radio)
	{
		addRadioToPanel(panel, gridy, radio, null);
	}

	private void addRadioToPanel(JPanel panel, int gridy, JRadioButton radio,
			ActionListener legendAL)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(radio, c);

		if (legendAL != null)
		{
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

	private ActionListener createMetadataListener()
	{
		return new ActionListener()
		{
			JDialog dialog = init();

			public JDialog init()
			{
				final JDialog dialog = new JDialog(frame, "Metadata", false);

				JEditorPane editorPane = new JEditorPane();
				editorPane.putClientProperty(BasicHTML.documentBaseKey, this
						.getClass().getResource("/data/help/"));
				editorPane.setEditable(false);
				java.net.URL url = this.getClass().getResource(
						"/data/help/metadata");
				if (url != null)
				{
					try
					{
						editorPane.setPage(url);
					}
					catch (IOException e)
					{
						editorPane.setText(e.toString());
					}
				}
				else
				{
					editorPane.setText("Could not find page");
				}

				JScrollPane scrollPane = new JScrollPane(editorPane);
				dialog.add(scrollPane, BorderLayout.CENTER);

				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						dialog.setVisible(false);
					}
				});

				return dialog;
			}

			public void actionPerformed(ActionEvent ae)
			{
				if (!dialog.isVisible())
				{
					dialog.setSize(640, 480);
					dialog.setLocationRelativeTo(frame);
				}
				dialog.setVisible(!dialog.isVisible());
				dialog.validate();
			}
		};
	}

	private ActionListener createLegendListener(final String image,
			final String title)
	{
		return new ActionListener()
		{
			private JDialog dialog = init();
			private int width;
			private int height;

			private JDialog init()
			{
				java.net.URL url = this.getClass().getResource(
						"/data/legends/" + image);
				BufferedImage bi = null;
				if (url != null)
				{
					try
					{
						bi = ImageIO.read(url);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				width = bi.getWidth() / 2;
				height = bi.getHeight() / 2;

				final JDialog dialog = new ImageDialog(frame, title, false, bi);

				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						dialog.setVisible(false);
					}
				});

				return dialog;
			}

			public void actionPerformed(ActionEvent e)
			{
				if (!dialog.isVisible())
				{
					dialog.setSize(width, height);
					dialog.setLocationRelativeTo(frame);
				}
				dialog.setVisible(!dialog.isVisible());
				dialog.validate();
			}
		};
	}

	public Layer[] getLayers()
	{
		return layers;
	}

	private void updateLayers()
	{
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

		ternaryAreas.setEnabled(areas && ternaryAreasRadio.isSelected());
		ternaryAreas.setOpacity(areasSlider.getValue() / 100d);
		uraniumAreas.setEnabled(areas && uraniumAreasRadio.isSelected());
		uraniumAreas.setOpacity(areasSlider.getValue() / 100d);
		thoriumAreas.setEnabled(areas && thoriumAreasRadio.isSelected());
		thoriumAreas.setOpacity(areasSlider.getValue() / 100d);
		potassiumAreas.setEnabled(areas && potassiumAreasRadio.isSelected());
		potassiumAreas.setOpacity(areasSlider.getValue() / 100d);
		doseRateAreas.setEnabled(areas && doseRateAreasRadio.isSelected());
		doseRateAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioUThAreas.setEnabled(areas && ratioUThAreasRadio.isSelected());
		ratioUThAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioUKAreas.setEnabled(areas && ratioUKAreasRadio.isSelected());
		ratioUKAreas.setOpacity(areasSlider.getValue() / 100d);
		ratioThKAreas.setEnabled(areas && ratioThKAreasRadio.isSelected());
		ratioThKAreas.setOpacity(areasSlider.getValue() / 100d);

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
