package annotations;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
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

import util.FlatJButton;
import util.Icons;
import util.JDoubleField;
import util.Util;

public class AnnotationEditor extends JDialog
{
	public static String IMPERIAL = "Imperial";
	public static String METRIC = "Metric";
	private static String UNITS = METRIC;

	private Annotation annotation;
	private JCheckBox visible;
	private JTextArea text;
	private JTextField latlon;
	private JLabel latlonLabel;
	private JDoubleField minZoom;
	private JDoubleField maxZoom;
	private JComboBox minZoomUnits;
	private JComboBox maxZoomUnits;
	private JCheckBox cameraInformation;
	private JLabel zoomLabel;
	private JLabel headingLabel;
	private JLabel pitchLabel;
	private JDoubleField zoom;
	private JDoubleField heading;
	private JDoubleField pitch;
	private JComboBox zoomUnits;
	private WorldWindow wwd;
	private int returnValue = JOptionPane.CANCEL_OPTION;
	private JButton okButton;

	private enum Units
	{
		Kilometres("km", 1d / 1000d),
		Metres("m", 1d),
		Miles("mi", Util.METER_TO_MILE),
		Feet("ft", Util.METER_TO_FEET);

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

	public AnnotationEditor(WorldWindow wwd, Frame owner, String title,
			Annotation annotation)
	{
		super(owner, title, true);
		this.wwd = wwd;
		this.annotation = annotation;
		setLayout(new BorderLayout());

		Insets insets = new Insets(3, 1, 3, 1);
		GridBagConstraints c;
		JLabel label;
		JPanel panel, panel2;

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(panel, BorderLayout.CENTER);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		DocumentListener dl = new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			public void insertUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			public void removeUpdate(DocumentEvent e)
			{
				checkValidity();
			}
		};
		
		visible = new JCheckBox("Visible", annotation.isVisible());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.insets = (Insets) insets.clone();
		panel.add(visible, c);

		label = new JLabel("Label:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		text = new JTextArea(annotation.getLabel(), 3, 30);
		text.setFont(Font.decode(""));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = (Insets) insets.clone();
		JScrollPane scrollPane = new JScrollPane(text,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(scrollPane, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel.add(label, c);

		latlon = new JTextField(textFormatedLatLon(annotation));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel.add(latlon, c);

		latlonLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.top = 0;
		panel.add(latlonLabel, c);

		label = new JLabel("Fade in zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		Double minz = Double.valueOf(annotation.getMinZoom());
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
		panel2.add(minZoom, c);

		minZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			minZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel2.add(minZoomUnits, c);

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				minZoom.setScale(((Units) minZoomUnits.getSelectedItem())
						.getScale());
			}
		};
		minZoomUnits.addActionListener(al);
		al.actionPerformed(null);

		FlatJButton flat = new FlatJButton(Icons.remove);
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel2.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				minZoom.setValue(null);
			}
		});

		label = new JLabel("Fade out zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		Double maxz = Double.valueOf(annotation.getMaxZoom());
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
		panel2.add(maxZoom, c);

		maxZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			maxZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel2.add(maxZoomUnits, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				maxZoom.setScale(((Units) maxZoomUnits.getSelectedItem())
						.getScale());
			}
		};
		maxZoomUnits.addActionListener(al);
		al.actionPerformed(null);

		flat = new FlatJButton(Icons.remove);
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel2.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				maxZoom.setValue(null);
			}
		});

		cameraInformation = new JCheckBox("Save camera information", annotation
				.isSaveCamera());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel.add(cameraInformation, c);
		cameraInformation.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableCameraInformation();
				checkValidity();
			}
		});

		headingLabel = new JLabel("Heading:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(headingLabel, c);

		heading = new JDoubleField(annotation.getHeading(), 2);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 7;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel.add(heading, c);

		pitchLabel = new JLabel("Pitch:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 8;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(pitchLabel, c);

		pitch = new JDoubleField(annotation.getPitch(), 2);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 8;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel.add(pitch, c);

		zoomLabel = new JLabel("Zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 9;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(zoomLabel, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		zoom = new JDoubleField(annotation.getZoom(), 2);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.weightx = 1;
		panel2.add(zoom, c);

		zoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			zoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel2.add(zoomUnits, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoom.setScale(((Units) zoomUnits.getSelectedItem()).getScale());
			}
		};
		zoomUnits.addActionListener(al);
		al.actionPerformed(null);

		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing,
				spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
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
	}

	private boolean checkValidity()
	{
		boolean valid = true;
		
		annotation.setVisible(visible.isSelected());

		String label = text.getText();
		if (label.length() == 0)
			valid = false;
		else
			annotation.setLabel(label);

		LatLon ll = parseLatLon(latlon.getText());
		if (ll == null)
		{
			valid = false;
			latlonLabel.setText("Invalid coordinates");
		}
		else
		{
			annotation.setLatitude(ll.getLatitude().degrees);
			annotation.setLongitude(ll.getLongitude().degrees);
			latlonLabel.setText(labelFormatedLatLon(annotation));
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

		annotation.setMinZoom(minz);
		annotation.setMaxZoom(maxz);
		annotation.setZoom(z);

		Double p = pitch.getValue();
		Double h = heading.getValue();
		if (p == null)
			p = 0d;
		if (h == null)
			h = 0d;

		annotation.setSaveCamera(cameraInformation.isSelected());
		annotation.setHeading(h);
		annotation.setPitch(p);

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
		headingLabel.setEnabled(cameraInformation.isSelected());
		zoomLabel.setEnabled(cameraInformation.isSelected());
		pitchLabel.setEnabled(cameraInformation.isSelected());
		heading.setEnabled(cameraInformation.isSelected());
		zoom.setEnabled(cameraInformation.isSelected());
		pitch.setEnabled(cameraInformation.isSelected());
		zoomUnits.setEnabled(cameraInformation.isSelected());
	}

	private String textFormatedLatLon(Annotation annotation)
	{
		return String.format("%7.4f %7.4f", annotation.getLatitude(),
				annotation.getLongitude());
	}

	private String labelFormatedLatLon(Annotation annotation)
	{
		return String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0", annotation
				.getLatitude(), annotation.getLongitude());
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
