package au.gov.ga.worldwind.panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.components.FlatJButton;
import au.gov.ga.worldwind.components.JDoubleField;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;


public class PlaceEditor extends JDialog
{
	public static String IMPERIAL = "Imperial";
	public static String METRIC = "Metric";
	private static String UNITS = METRIC;

	private Place place;
	private JCheckBox visible;
	private JTextArea text;
	private JTextField latlon;
	private JLabel latlonLabel;
	private JDoubleField minZoom;
	private JDoubleField maxZoom;
	private JComboBox minZoomUnits;
	private JComboBox maxZoomUnits;
	private JCheckBox cameraInformation;
	private JButton copyCamera;
	private JCheckBox excludeFromPlaylist;
	private JLabel zoomLabel;
	private JLabel headingLabel;
	private JLabel pitchLabel;
	private JLabel elevationLabel;
	private JDoubleField zoom;
	private JDoubleField heading;
	private JDoubleField pitch;
	private JDoubleField elevation;
	private JComboBox elevationUnits;
	private JComboBox zoomUnits;
	private WorldWindow wwd;
	private int returnValue = JOptionPane.CANCEL_OPTION;
	private JButton okButton;

	private enum Units
	{
		Kilometres("km", 1d / 1000d), Metres("m", 1d), Miles("mi", Util.METER_TO_MILE), Feet(
				"ft",
				Util.METER_TO_FEET);

		private final String label;
		private final double scale;

		private Units(String label, double scale)
		{
			this.label = label;
			this.scale = scale;
		}

		public double getScale()
		{
			return scale;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	public PlaceEditor(final WorldWindow wwd, Window owner, String title, final Place place,
			ImageIcon icon)
	{
		super(owner, title, ModalityType.APPLICATION_MODAL);
		this.wwd = wwd;
		this.place = place;
		setLayout(new BorderLayout());
		setIconImage(icon.getImage());

		Insets insets = new Insets(3, 1, 3, 1);
		GridBagConstraints c;
		JLabel label;
		JPanel panel, panel2, panel3;

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(panel, BorderLayout.CENTER);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		DocumentListener dl = new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				checkValidity();
			}
		};
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				checkValidity();
			}
		};

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Place"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(panel2, c);

		label = new JLabel("Label:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		text = new JTextArea(place.getLabel(), 3, 30);
		text.setFont(Font.decode(""));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = (Insets) insets.clone();
		JScrollPane scrollPane =
				new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel2.add(scrollPane, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(label, c);

		latlon = new JTextField(textFormatedLatLon(place.getLatitude(), place.getLongitude()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(latlon, c);

		latlonLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.top = 0;
		panel2.add(latlonLabel, c);

		panel3 = new JPanel(new GridLayout(0, 2));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(panel3, c);

		visible = new JCheckBox("Show on globe", place.isVisible());
		panel3.add(visible);
		visible.addActionListener(al);

		excludeFromPlaylist = new JCheckBox("Exclude from playlist", place.isExcludeFromPlaylist());
		panel3.add(excludeFromPlaylist);
		excludeFromPlaylist.addActionListener(al);

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Fade"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Fade in zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		Double minz = Double.valueOf(place.getMinZoom());
		if (minz < 0)
			minz = null;
		minZoom = new JDoubleField(minz, 2);
		minZoom.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel3.add(minZoom, c);

		minZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			minZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(minZoomUnits, c);

		ActionListener mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				minZoom.setScale(((Units) minZoomUnits.getSelectedItem()).getScale());
			}
		};
		minZoomUnits.addActionListener(mzal);
		mzal.actionPerformed(null);

		FlatJButton flat = new FlatJButton(Icons.remove.getIcon());
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				minZoom.setValue(null);
			}
		});

		label = new JLabel("Fade out zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		Double maxz = Double.valueOf(place.getMaxZoom());
		if (maxz < 0)
			maxz = null;
		maxZoom = new JDoubleField(maxz, 2);
		maxZoom.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel3.add(maxZoom, c);

		maxZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			maxZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(maxZoomUnits, c);

		mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maxZoom.setScale(((Units) maxZoomUnits.getSelectedItem()).getScale());
			}
		};
		maxZoomUnits.addActionListener(mzal);
		mzal.actionPerformed(null);

		flat = new FlatJButton(Icons.remove.getIcon());
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maxZoom.setValue(null);
			}
		});

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Camera"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		panel3 = new JPanel(new GridLayout(0, 2));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.gridwidth = 2;
		panel2.add(panel3, c);

		cameraInformation = new JCheckBox("Save camera information", place.isSaveCamera());
		panel3.add(cameraInformation);
		cameraInformation.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableCameraInformation();
				checkValidity();
			}
		});

		copyCamera = new JButton("Fill from current view");
		panel3.add(copyCamera);
		copyCamera.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				View view = wwd.getView();
				if (view instanceof OrbitView)
				{
					OrbitView orbitView = (OrbitView) view;
					heading.setValue(orbitView.getHeading().degrees);
					pitch.setValue(orbitView.getPitch().degrees);
					zoom.setValue(orbitView.getZoom());
					Position pos = orbitView.getCenterPosition();
					elevation.setValue(pos.getElevation());
					double lat = pos.getLatitude().degrees;
					double lon = pos.getLongitude().degrees;
					if (Math.abs(lat - place.getLatitude()) > 0.1
							|| Math.abs(lon - place.getLongitude()) > 0.1)
					{
						int value =
								JOptionPane
										.showConfirmDialog(
												PlaceEditor.this,
												"Do you want to change the Lat/Lon to match the current camera center?",
												"Move place", JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE);
						if (value == JOptionPane.YES_OPTION)
						{
							latlon.setText(textFormatedLatLon(lat, lon));
						}
					}
					checkValidity();
				}
			}
		});

		headingLabel = new JLabel("Heading:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(headingLabel, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(panel3, c);

		heading = new JDoubleField(place.getHeading(), 2);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel3.add(heading, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, insets.left + insets.right, 0, 0);
		panel3.add(label, c);

		pitchLabel = new JLabel("Pitch:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(pitchLabel, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(panel3, c);

		pitch = new JDoubleField(place.getPitch(), 2);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel3.add(pitch, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, insets.left + insets.right, 0, 0);
		panel3.add(label, c);

		zoomLabel = new JLabel("Zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(zoomLabel, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		zoom = new JDoubleField(place.getZoom(), 2);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.weightx = 1;
		panel3.add(zoom, c);

		zoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			zoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(zoomUnits, c);

		mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				zoom.setScale(((Units) zoomUnits.getSelectedItem()).getScale());
			}
		};
		zoomUnits.addActionListener(mzal);
		mzal.actionPerformed(null);

		elevationLabel = new JLabel("Elevation:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(elevationLabel, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		elevation = new JDoubleField(place.getElevation(), 2);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.weightx = 1;
		panel3.add(elevation, c);

		elevationUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			elevationUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(elevationUnits, c);

		mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				elevation.setScale(((Units) elevationUnits.getSelectedItem()).getScale());
			}
		};
		elevationUnits.addActionListener(mzal);
		mzal.actionPerformed(null);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = JOptionPane.OK_OPTION;
				dispose();
			}
		});
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		pack();
		setLocationRelativeTo(owner);
		checkValidity();
		enableCameraInformation();

		text.getDocument().addDocumentListener(dl);
		latlon.getDocument().addDocumentListener(dl);
		minZoom.getDocument().addDocumentListener(dl);
		maxZoom.getDocument().addDocumentListener(dl);
		zoom.getDocument().addDocumentListener(dl);
		heading.getDocument().addDocumentListener(dl);
		pitch.getDocument().addDocumentListener(dl);
		elevation.getDocument().addDocumentListener(dl);
	}

	private boolean checkValidity()
	{
		boolean valid = true;

		place.setVisible(visible.isSelected());

		String label = text.getText();
		if (label.length() == 0)
			valid = false;
		else
			place.setLabel(label);

		LatLon ll = parseLatLon(latlon.getText());
		if (ll == null)
		{
			valid = false;
			latlonLabel.setText("Invalid coordinates");
		}
		else
		{
			place.setLatitude(ll.getLatitude().degrees);
			place.setLongitude(ll.getLongitude().degrees);
			latlonLabel.setText(labelFormatedLatLon(place));
		}

		Double minz = minZoom.getValue();
		Double maxz = maxZoom.getValue();
		Double z = zoom.getValue();
		if (minz != null && maxz != null && maxz > minz)
		{
			Double temp = maxz;
			maxz = minz;
			minz = temp;
		}
		if (minz == null)
			minz = -1d;
		if (maxz == null)
			maxz = -1d;
		if (z == null || z <= 0)
			z = wwd.getView().getEyePosition().getElevation();

		place.setMinZoom(minz);
		place.setMaxZoom(maxz);
		place.setZoom(z);

		Double p = pitch.getValue();
		Double h = heading.getValue();
		Double e = elevation.getValue();
		if (p == null)
			p = 0d;
		if (h == null)
			h = 0d;
		if (e == null)
			e = 0d;

		place.setSaveCamera(cameraInformation.isSelected());
		place.setHeading(h);
		place.setPitch(p);
		place.setElevation(e);
		place.setExcludeFromPlaylist(excludeFromPlaylist.isSelected());

		okButton.setEnabled(valid);
		return valid;
	}

	public int getOkCancel()
	{
		text.requestFocusInWindow();
		setVisible(true);
		checkValidity();
		dispose();
		return returnValue;
	}

	private void enableCameraInformation()
	{
		boolean enabled = cameraInformation.isSelected();
		copyCamera.setEnabled(enabled);
		headingLabel.setEnabled(enabled);
		zoomLabel.setEnabled(enabled);
		pitchLabel.setEnabled(enabled);
		elevationLabel.setEnabled(enabled);
		heading.setEnabled(enabled);
		zoom.setEnabled(enabled);
		pitch.setEnabled(enabled);
		elevation.setEnabled(enabled);
		zoomUnits.setEnabled(enabled);
		elevationUnits.setEnabled(enabled);
	}

	private String textFormatedLatLon(double latitude, double longitude)
	{
		return String.format("%7.4f %7.4f", latitude, longitude);
	}

	private String labelFormatedLatLon(Place place)
	{
		return String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0", place.getLatitude(), place
				.getLongitude());
	}

	private LatLon parseLatLon(String text)
	{
		return Util.computeLatLonFromString(text, wwd.getModel().getGlobe());
	}

	public static void setUnits(String units)
	{
		UNITS = units;
	}
}
