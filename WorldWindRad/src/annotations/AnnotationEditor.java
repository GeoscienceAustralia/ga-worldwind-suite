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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
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
	private JTextArea text;
	private JTextField latlon;
	private JLabel latlonLabel;
	private JDoubleField minZoom;
	private JDoubleField maxZoom;
	private JComboBox minZoomUnits;
	private JComboBox maxZoomUnits;
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

		Insets insets = new Insets(5, 1, 5, 1);
		GridBagConstraints c;
		JLabel label;

		JPanel panel = new JPanel(new GridBagLayout());
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

		label = new JLabel("Label:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		Border border = UIManager.getBorder("TextField.border");
		text = new JTextArea(annotation.getLabel());
		text.setBorder(border);
		text.setFont(Font.decode(""));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = (Insets) insets.clone();
		panel.add(text, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel.add(label, c);

		latlon = new JTextField(textFormatedLatLon(annotation));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel.add(latlon, c);

		latlonLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.top = 0;
		panel.add(latlonLabel, c);

		label = new JLabel("Fade in zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		Double minz = Double.valueOf(annotation.getMinZoom());
		if (minz < 0)
			minz = null;
		minZoom = new JDoubleField(minz, 4);
		minZoom.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel.add(minZoom, c);

		minZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			minZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel.add(minZoomUnits, c);

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
		c.gridx = 3;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel.add(flat, c);
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
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel.add(label, c);

		Double maxz = Double.valueOf(annotation.getMaxZoom());
		if (maxz < 0)
			maxz = null;
		maxZoom = new JDoubleField(maxz, 4);
		maxZoom.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel.add(maxZoom, c);

		maxZoomUnits = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			maxZoomUnits.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel.add(maxZoomUnits, c);

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
		c.gridx = 3;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				maxZoom.setValue(null);
			}
		});


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing,
				spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

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

		setSize(400, 300);
		checkValidity();

		text.getDocument().addDocumentListener(dl);
		latlon.getDocument().addDocumentListener(dl);
		minZoom.getDocument().addDocumentListener(dl);
		maxZoom.getDocument().addDocumentListener(dl);
	}

	private boolean checkValidity()
	{
		boolean valid = true;

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
		if (minz == null)
			minz = -1d;
		if (maxz == null)
			maxz = -1d;

		annotation.setMinZoom(minz);
		annotation.setMaxZoom(maxz);

		okButton.setEnabled(valid);
		return valid;
	}

	public int getOkCancel()
	{
		setVisible(true);
		dispose();
		return returnValue;
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
