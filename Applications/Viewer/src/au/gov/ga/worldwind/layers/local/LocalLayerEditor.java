package au.gov.ga.worldwind.layers.local;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.components.JDoubleField;
import au.gov.ga.worldwind.components.JIntegerField;
import au.gov.ga.worldwind.util.Util;

public class LocalLayerEditor extends JDialog
{
	private JTextField nameField;
	private JTextField dirField;
	private JComboBox extensionCombo;
	private JIntegerField tilesizeField;
	private JDoubleField lztsdField;
	private JIntegerField levelcountField;
	private JTextField topleftField;
	private JTextField bottomrightField;
	private JCheckBox transparentCheck;
	private JColorComponent transparentColor;
	private JIntegerField fuzzField;
	private JLabel transparentLabel;
	private JLabel fuzzLabel;
	private JLabel fuzzUnit;

	private JButton okButton;
	private boolean okPressed = false;

	private final static String[] FILE_EXTENSIONS = { "JPG", "PNG", "BMP",
			"JPEG", "DDS" };

	private LocalLayerEditor(Frame owner, String title,
			final LocalLayerDefinition definition)
	{
		super(owner, title, true);

		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		DocumentListener dl = new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				checkFields();
			}

			public void insertUpdate(DocumentEvent e)
			{
				checkFields();
			}

			public void removeUpdate(DocumentEvent e)
			{
				checkFields();
			}
		};
		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				checkFields();
			}
		};
		ActionListener transAl = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableTransparent();
			}
		};

		int s = 5;
		int numberFieldWidth = 100;
		JLabel label;
		JPanel panel, panel2;
		GridBagConstraints c;
		Dimension size;

		JPanel mainPanel = new JPanel(new GridBagLayout());
		add(mainPanel, BorderLayout.CENTER);


		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Tileset"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		label = new JLabel("Name:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		nameField = new JTextField(definition.getName());
		nameField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, s);
		panel.add(nameField, c);

		label = new JLabel("Tile directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		dirField = new JTextField(definition.getDirectory());
		dirField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(dirField, c);

		JButton browse = new JButton("...");
		browse.setToolTipText("Browse");
		browse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select layer tileset directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(LocalLayerEditor.this) == JFileChooser.APPROVE_OPTION)
				{
					dirField.setText(chooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(s, 0, 0, s);
		panel2.add(browse, c);

		label = new JLabel("File extension:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		extensionCombo = new JComboBox(FILE_EXTENSIONS);
		for (int i = 0; i < extensionCombo.getItemCount(); i++)
		{
			if (definition.getExtension().equalsIgnoreCase(
					(String) extensionCombo.getItemAt(i)))
			{
				extensionCombo.setSelectedIndex(i);
				break;
			}
		}
		extensionCombo.addActionListener(al);
		extensionCombo.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(extensionCombo, c);


		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Size"));
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		label = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(panel2, c);

		tilesizeField = new JIntegerField(definition.getTilesize());
		tilesizeField.getDocument().addDocumentListener(dl);
		size = tilesizeField.getPreferredSize();
		size.width = numberFieldWidth;
		tilesizeField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(tilesizeField, c);

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		lztsdField = new JDoubleField(definition.getLztsd());
		lztsdField.getDocument().addDocumentListener(dl);
		size = lztsdField.getPreferredSize();
		size.width = numberFieldWidth;
		lztsdField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(lztsdField, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);


		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Extents"));
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		final JButton calculateExtents = new JButton("Calculate from directory");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(calculateExtents, c);
		calculateExtents.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File directory = new File(dirField.getText());
				Double lztsd = lztsdField.getValue();
				if (!directory.exists() || !directory.isDirectory())
				{
					JOptionPane.showMessageDialog(calculateExtents,
							"Selected directory is invalid", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (lztsd == null || lztsd <= 0)
				{
					JOptionPane.showMessageDialog(calculateExtents,
							"Level zero tile size is invalid", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int levels = levelCount(directory);
				if (levels <= 0)
				{
					JOptionPane.showMessageDialog(calculateExtents, "Found "
							+ levels + " in selected directory", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				Sector sector = sector(directory, (String) extensionCombo
						.getSelectedItem(), levels, lztsdField.getValue());
				levelcountField.setValue(levels);
				topleftField.setText(formatLatLon(
						sector.getMinLatitude().degrees, sector
								.getMinLongitude().degrees));
				bottomrightField.setText(formatLatLon(
						sector.getMaxLatitude().degrees, sector
								.getMaxLongitude().degrees));
			}
		});

		label = new JLabel("Level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		levelcountField = new JIntegerField(definition.getLevelcount());
		levelcountField.getDocument().addDocumentListener(dl);
		size = levelcountField.getPreferredSize();
		size.width = numberFieldWidth;
		levelcountField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(s, s, 0, s);
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		panel.add(levelcountField, c);

		label = new JLabel("Bottom left lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		topleftField = new JTextField(formatLatLon(definition.getMinLat(),
				definition.getMinLon()));
		topleftField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(topleftField, c);

		label = new JLabel("Top right lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		bottomrightField = new JTextField(formatLatLon(definition.getMaxLat(),
				definition.getMaxLon()));
		bottomrightField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(bottomrightField, c);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Transparency"));
		c = new GridBagConstraints();
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.weighty = 1;
		mainPanel.add(panel, c);

		transparentCheck = new JCheckBox("Has transparent color");
		transparentCheck.setSelected(definition.isHasTransparentColor());
		transparentCheck.addActionListener(al);
		transparentCheck.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(transparentCheck, c);

		transparentLabel = new JLabel("Transparent color:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(transparentLabel, c);

		Color color = definition.getTransparentColor();
		if (color == null)
			color = Color.black;
		transparentColor = new JColorComponent(color);
		transparentColor.setBorder(BorderFactory.createLineBorder(Color.black));
		transparentColor.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (transparentColor.isEnabled())
				{
					Color color = JColorChooser.showDialog(transparentColor,
							"Select transparent color", transparentColor
									.getColor());
					if (color != null)
						transparentColor.setColor(color);
				}
			}
		});
		size = transparentColor.getPreferredSize();
		size.width = 32;
		size.height = 16;
		transparentColor.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		c.weightx = 1;
		panel.add(transparentColor, c);

		fuzzLabel = new JLabel("Transparent fuzz:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(fuzzLabel, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		fuzzField = new JIntegerField(definition.getTransparentFuzz());
		fuzzField.getDocument().addDocumentListener(dl);
		size = fuzzField.getPreferredSize();
		size.width = numberFieldWidth;
		fuzzField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(fuzzField, c);

		fuzzUnit = new JLabel("%");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(fuzzUnit, c);


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
				if (checkAndUpdateDefinition(definition))
				{
					okPressed = true;
					dispose();
				}
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

		checkFields();
		enableTransparent();
		pack();
		size = getPreferredSize();
		setSize(500, size.height);
		setLocationRelativeTo(owner);
	}

	private String formatLatLon(double lat, double lon)
	{
		return lat + " " + lon;
	}

	private void checkFields()
	{
		boolean valid = true;
		valid = valid && nameField.getText().length() > 0;
		File dir = new File(dirField.getText());
		valid = valid && dir.exists() && dir.isDirectory();
		valid = valid && lztsdField.getValue() != null
				&& lztsdField.getValue() > 0;
		valid = valid && tilesizeField.getValue() != null
				&& tilesizeField.getValue() > 0;
		valid = valid && levelcountField.getValue() != null
				&& levelcountField.getValue() > 0;
		valid = valid
				&& Util.computeLatLonFromString(topleftField.getText()) != null;
		valid = valid
				&& Util.computeLatLonFromString(bottomrightField.getText()) != null;
		if (transparentCheck.isSelected())
		{
			valid = valid && transparentColor.getColor() != null;
			valid = valid && fuzzField.getValue() != null
					&& fuzzField.getValue() >= 0;
		}
		okButton.setEnabled(valid);
	}

	private boolean checkAndUpdateDefinition(LocalLayerDefinition definition)
	{
		LatLon topleft = Util.computeLatLonFromString(topleftField.getText());
		LatLon bottomright = Util.computeLatLonFromString(bottomrightField
				.getText());
		definition.setName(nameField.getText());
		definition.setDirectory(dirField.getText());
		definition.setExtension((String) extensionCombo.getSelectedItem());
		definition.setTilesize(tilesizeField.getValue());
		definition.setLztsd(lztsdField.getValue());
		definition.setLevelcount(levelcountField.getValue());
		definition.setMinLat(topleft.getLatitude().degrees);
		definition.setMinLon(topleft.getLongitude().degrees);
		definition.setMaxLat(bottomright.getLatitude().degrees);
		definition.setMaxLon(bottomright.getLongitude().degrees);
		definition.setHasTransparentColor(transparentCheck.isSelected()
				&& allowTransparent());
		definition.setTransparentColor(transparentColor.getColor());
		definition.setTransparentFuzz(fuzzField.getValue());
		return true;
	}

	private boolean allowTransparent()
	{
		return !"DDS".equalsIgnoreCase((String) extensionCombo
				.getSelectedItem());
	}

	private void enableTransparent()
	{
		boolean allow = allowTransparent();
		transparentCheck.setEnabled(allow);
		boolean enabled = allow && transparentCheck.isSelected();
		transparentLabel.setEnabled(enabled);
		transparentColor.setEnabled(enabled);
		fuzzLabel.setEnabled(enabled);
		fuzzField.setEnabled(enabled);
		fuzzUnit.setEnabled(enabled);
	}

	public static LocalLayerDefinition editDefinition(Frame owner,
			String title, LocalLayerDefinition definition)
	{
		LocalLayerEditor editor = new LocalLayerEditor(owner, title, definition);
		editor.setVisible(true);
		if (editor.okPressed)
		{
			return definition;
		}
		return null;
	}

	private class JColorComponent extends JComponent
	{
		private Color color;

		public JColorComponent(Color color)
		{
			this.color = color;
		}

		public Color getColor()
		{
			return color;
		}

		public void setColor(Color color)
		{
			this.color = color;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (color != null && isEnabled())
			{
				g.setColor(color);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}

	public static int levelCount(File directory)
	{
		int levels = 0;
		for (int i = 0; true; i++)
		{
			File leveldir = new File(directory, String.valueOf(i));
			if (!leveldir.exists())
				break;
			levels++;
		}
		return levels;
	}

	public static Sector sector(File directory, String extension, int levels,
			double lztsd)
	{
		int level = levels - 1;
		File lastLevelDirectory = new File(directory, String.valueOf(level));
		MinMax rowMinMax = getMinMaxRow(lastLevelDirectory);
		MinMax colMinMax = getMinMaxCol(lastLevelDirectory, extension);
		return Sector.fromDegrees(getTileLat(rowMinMax.min, level, lztsd),
				getTileLat(rowMinMax.max + 1, level, lztsd), getTileLon(
						colMinMax.min, level, lztsd), getTileLon(
						colMinMax.max + 1, level, lztsd));
	}

	private static class MinMax
	{
		private int min = Integer.MAX_VALUE;
		private int max = Integer.MIN_VALUE;
	}

	private static MinMax getMinMaxRow(File directory)
	{
		MinMax minmax = new MinMax();
		String[] list = directory.list();
		boolean match = false;
		for (String file : list)
		{
			if (file.matches("\\d+"))
			{
				match = true;
				int row = Integer.parseInt(file);
				minmax.min = Math.min(minmax.min, row);
				minmax.max = Math.max(minmax.max, row);
			}
		}
		return match ? minmax : null;
	}

	private static MinMax getMinMaxCol(File directory, String extension)
	{
		MinMax minmax = new MinMax();
		boolean match = false;
		File[] levels = directory.listFiles();
		for (File level : levels)
		{
			if (level.isDirectory() && level.getName().matches("\\d+"))
			{
				String[] list = level.list();
				for (String file : list)
				{
					if (file.toLowerCase()
							.matches(
									"\\d+\\_\\d+\\Q." + extension.toLowerCase()
											+ "\\E"))
					{
						Pattern pattern = Pattern.compile("\\d+");
						Matcher matcher = pattern.matcher(file);
						if (matcher.find() && matcher.find(matcher.end()))
						{
							match = true;
							int col = Integer.parseInt(matcher.group());
							minmax.min = Math.min(minmax.min, col);
							minmax.max = Math.max(minmax.max, col);
						}
					}
				}
			}
		}
		return match ? minmax : null;
	}

	private static double getTileLon(int col, int level, double lztsd)
	{
		double levelpow = Math.pow(0.5, level);
		return col * lztsd * levelpow - 180;
	}

	private static double getTileLat(int row, int level, double lztsd)
	{
		double levelpow = Math.pow(0.5, level);
		return row * lztsd * levelpow - 90;
	}
}
