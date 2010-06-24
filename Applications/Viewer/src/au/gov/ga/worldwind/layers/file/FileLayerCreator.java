package au.gov.ga.worldwind.layers.file;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.LayerConfiguration;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.components.JDoubleField;
import au.gov.ga.worldwind.components.JIntegerField;
import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.panels.dataset.LayerDefinition;
import au.gov.ga.worldwind.panels.layers.LayersPanel.LayerDefinitionFileFilter;
import au.gov.ga.worldwind.util.AVKeyMore;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;
import au.gov.ga.worldwind.util.XMLUtil;

public class FileLayerCreator extends JDialog
{
	private JTextField nameField;
	private JTextField dirField;
	private JTextField outputField;
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
	private boolean outputFieldChanged = false;
	private boolean ignoreOutputFieldChange = false;

	private JButton okButton;
	private boolean okPressed = false;

	private ILayerDefinition layer;

	private final static String[] FILE_EXTENSIONS = { "JPG", "PNG", "DDS", "BMP", "GIF" };

	private JFileChooser chooser;

	private FileLayerCreator(Frame owner, String title, ImageIcon icon)
	{
		super(owner, title, true);
		setIconImage(icon.getImage());

		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		chooser = new JFileChooser();

		DocumentListener filedl = new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				updateOutputField();
			}

			public void insertUpdate(DocumentEvent e)
			{
				updateOutputField();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOutputField();
			}
		};
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
		int i = 0;
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
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		nameField = new JTextField();
		nameField.getDocument().addDocumentListener(dl);
		nameField.getDocument().addDocumentListener(filedl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, s);
		panel.add(nameField, c);

		label = new JLabel("Tile directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		dirField = new JTextField();
		dirField.getDocument().addDocumentListener(dl);
		dirField.getDocument().addDocumentListener(filedl);
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
				chooser.setDialogTitle("Select layer tileset directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(null);
				if (chooser.showOpenDialog(FileLayerCreator.this) == JFileChooser.APPROVE_OPTION)
				{
					dirField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(s, 0, 0, s);
		panel2.add(browse, c);

		label = new JLabel("Output layer file:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		outputField = new JTextField();
		outputField.getDocument().addDocumentListener(dl);
		outputField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				outputFieldChanged();
			}

			public void insertUpdate(DocumentEvent e)
			{
				outputFieldChanged();
			}

			public void changedUpdate(DocumentEvent e)
			{
				outputFieldChanged();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(outputField, c);

		browse = new JButton("...");
		browse.setToolTipText("Browse");
		browse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				chooser.setDialogTitle("Select output layer file");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new LayerDefinitionFileFilter());
				if (chooser.showSaveDialog(FileLayerCreator.this) == JFileChooser.APPROVE_OPTION)
				{
					outputField.setText(chooser.getSelectedFile().getAbsolutePath());
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
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		extensionCombo = new JComboBox(FILE_EXTENSIONS);
		extensionCombo.addActionListener(al);
		extensionCombo.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(extensionCombo, c);


		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Size"));
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		label = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(panel2, c);

		tilesizeField = new JIntegerField(512);
		tilesizeField.getDocument().addDocumentListener(dl);
		size = tilesizeField.getPreferredSize();
		size.width = numberFieldWidth;
		tilesizeField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(tilesizeField, c);

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		lztsdField = new JDoubleField(36d);
		lztsdField.getDocument().addDocumentListener(dl);
		size = lztsdField.getPreferredSize();
		size.width = numberFieldWidth;
		lztsdField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(lztsdField, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);


		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Extents"));
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		final JButton calculateExtents = new JButton("Calculate from directory");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i++;
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
							"Selected directory is invalid", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (lztsd == null || lztsd <= 0)
				{
					JOptionPane.showMessageDialog(calculateExtents,
							"Level zero tile size is invalid", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int levels = levelCount(directory);
				if (levels <= 0)
				{
					JOptionPane.showMessageDialog(calculateExtents, "Found " + levels
							+ " tiles in selected directory", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Sector sector =
						sector(directory, (String) extensionCombo.getSelectedItem(), levels,
								lztsdField.getValue());
				levelcountField.setValue(levels);
				topleftField.setText(formatLatLon(sector.getMinLatitude().degrees, sector
						.getMinLongitude().degrees));
				bottomrightField.setText(formatLatLon(sector.getMaxLatitude().degrees, sector
						.getMaxLongitude().degrees));
			}
		});

		label = new JLabel("Level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		levelcountField = new JIntegerField(null);
		levelcountField.getDocument().addDocumentListener(dl);
		size = levelcountField.getPreferredSize();
		size.width = numberFieldWidth;
		levelcountField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, 0, s);
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		panel.add(levelcountField, c);

		label = new JLabel("Bottom left lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		topleftField = new JTextField();
		topleftField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(topleftField, c);

		label = new JLabel("Top right lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		bottomrightField = new JTextField();
		bottomrightField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(bottomrightField, c);

		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Transparency"));
		c = new GridBagConstraints();
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.weighty = 1;
		mainPanel.add(panel, c);

		transparentCheck = new JCheckBox("Has transparent color");
		transparentCheck.setSelected(false);
		transparentCheck.addActionListener(al);
		transparentCheck.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(transparentCheck, c);

		transparentLabel = new JLabel("Transparent color:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(transparentLabel, c);

		Color color = Color.black;
		transparentColor = new JColorComponent(color);
		transparentColor.setBorder(BorderFactory.createLineBorder(Color.black));
		transparentColor.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (transparentColor.isEnabled())
				{
					Color color =
							JColorChooser.showDialog(transparentColor, "Select transparent color",
									transparentColor.getColor());
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
		c.gridy = i++;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		c.weightx = 1;
		panel.add(transparentColor, c);

		fuzzLabel = new JLabel("Transparent fuzz:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(fuzzLabel, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		fuzzField = new JIntegerField(0);
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
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveFile();
				okPressed = true;
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

		checkFields();
		enableTransparent();
		pack();
		size = getPreferredSize();
		setSize(500, size.height);
		setLocationRelativeTo(owner);
	}

	private void updateOutputField()
	{
		if (!outputFieldChanged)
		{
			if (nameField.getText().length() > 0 && dirField.getText().length() > 0)
			{
				ignoreOutputFieldChange = true;
				String filename =
						dirField.getText() + File.separator + nameField.getText() + ".xml";
				outputField.setText(filename);
				ignoreOutputFieldChange = false;
			}
		}
	}

	private void outputFieldChanged()
	{
		if (!ignoreOutputFieldChange)
		{
			outputFieldChanged = true;
		}
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
		valid = valid && outputField.getText().length() > 0;
		File file = new File(outputField.getText());
		valid = valid && !file.isDirectory();
		valid = valid && lztsdField.getValue() != null && lztsdField.getValue() > 0;
		valid = valid && tilesizeField.getValue() != null && tilesizeField.getValue() > 0;
		valid = valid && levelcountField.getValue() != null && levelcountField.getValue() > 0;
		valid = valid && Util.computeLatLonFromString(topleftField.getText()) != null;
		valid = valid && Util.computeLatLonFromString(bottomrightField.getText()) != null;
		if (transparentCheck.isSelected())
		{
			valid = valid && transparentColor.getColor() != null;
			valid = valid && fuzzField.getValue() != null && fuzzField.getValue() >= 0;
		}
		okButton.setEnabled(valid);
	}

	private boolean allowTransparent()
	{
		return !"DDS".equalsIgnoreCase((String) extensionCombo.getSelectedItem());
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

	public static ILayerDefinition createDefinition(Frame owner, String title, ImageIcon icon)
	{
		FileLayerCreator editor = new FileLayerCreator(owner, title, icon);
		editor.setVisible(true);
		if (editor.okPressed)
		{
			return editor.layer;
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

	public static Sector sector(File directory, String extension, int levels, double lztsd)
	{
		int level = levels - 1;
		File lastLevelDirectory = new File(directory, String.valueOf(level));
		MinMax rowMinMax = getMinMaxRow(lastLevelDirectory);
		MinMax colMinMax = getMinMaxCol(lastLevelDirectory, extension);
		return Sector.fromDegrees(getTileLat(rowMinMax.min, level, lztsd), getTileLat(
				rowMinMax.max + 1, level, lztsd), getTileLon(colMinMax.min, level, lztsd),
				getTileLon(colMinMax.max + 1, level, lztsd));
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
					if (file.toLowerCase().matches(
							"\\d+\\_\\d+\\Q." + extension.toLowerCase() + "\\E"))
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

	private void saveFile()
	{
		layer = null;

		File file = new File(outputField.getText());
		if (file.isDirectory())
			file = null;
		else
		{
			File dir = new File(dirField.getText());
			String directory = dir.getAbsolutePath();
			String parentFilePath = file.getParentFile().getAbsolutePath();
			if (directory.equals(parentFilePath))
				directory = "";
			else if (directory.startsWith(parentFilePath))
				directory = directory.substring(parentFilePath.length() + 1);

			Date date = new Date();
			//DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss z");
			//String lastUpdate = dateFormat.format(date);

			String ext = extensionCombo.getSelectedItem().toString().toLowerCase();
			String imageFormat = "image/" + ext;

			Integer levelsI = levelcountField.getValue();
			int levels = levelsI == null ? 0 : levelsI;

			Double lztsD = lztsdField.getValue();
			double lzts = lztsD == null ? 36d : lztsD;

			Integer tilesizeI = tilesizeField.getValue();
			int tilesize = tilesizeI == null ? 512 : tilesizeI;

			LatLon topleft = Util.computeLatLonFromString(topleftField.getText());
			LatLon bottomright = Util.computeLatLonFromString(bottomrightField.getText());
			Sector sector =
					new Sector(topleft.latitude, bottomright.latitude, topleft.longitude,
							bottomright.longitude);

			Integer fuzzI = fuzzField.getValue();
			Double fuzz = null;
			if (fuzzI != null)
			{
				fuzz = fuzzI / 100d;
			}

			AVList params = new AVListImpl();

			params.setValue(AVKey.DISPLAY_NAME, nameField.getText());
			params.setValue(AVKey.SERVICE_NAME, "FileTileService");
			params.setValue(AVKey.SERVICE, directory);
			params.setValue(AVKey.EXPIRY_TIME, date.getTime());
			params.setValue(AVKey.DATASET_NAME, ".");
			params.setValue(AVKey.DATA_CACHE_NAME, nameField.getText());
			params.setValue(AVKey.IMAGE_FORMAT, imageFormat);
			params.setValue(AVKey.AVAILABLE_IMAGE_FORMATS, new String[] { imageFormat });
			params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
			params.setValue(AVKey.NUM_LEVELS, levels);
			params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
			params.setValue(AVKey.TILE_ORIGIN, LatLon.fromDegrees(-90d, -180d));
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(lzts, lzts));
			params.setValue(AVKey.TILE_WIDTH, tilesize);
			params.setValue(AVKey.TILE_HEIGHT, tilesize);
			params.setValue(AVKey.SECTOR, sector);
			params.setValue(AVKey.USE_TRANSPARENT_TEXTURES, true);
			params.setValue(AVKey.USE_MIP_MAPS, true);
			params.setValue(AVKey.RETAIN_LEVEL_ZERO_TILES, true);
			params.setValue(AVKey.FORCE_LEVEL_ZERO_LOADS, true);
			if (transparentCheck.isSelected())
			{
				params.setValue(AVKeyMore.TRANSPARENT_COLOR, transparentColor.getColor());
				params.setValue(AVKeyMore.TRANSPARENT_FUZZ, fuzz);
			}

			Document document = LayerConfiguration.createTiledImageLayerDocument(params);
			Element element = document.getDocumentElement();
			FileTiledImageLayer.createTiledImageLayerElements(element, params);
			XMLUtil.saveDocumentToFormattedFile(document, file.getAbsolutePath());

			try
			{
				URL url = file.toURI().toURL();
				layer =
						new LayerDefinition(nameField.getText(), url, null, Icons.file.getURL(),
								true, false);
			}
			catch (MalformedURLException e)
			{
				JOptionPane.showMessageDialog(this, "Error adding layer file: " + e, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
