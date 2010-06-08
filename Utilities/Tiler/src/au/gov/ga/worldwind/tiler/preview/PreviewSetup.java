package au.gov.ga.worldwind.tiler.preview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import au.gov.ga.worldwind.tiler.application.Application;
import au.gov.ga.worldwind.tiler.util.JDoubleField;
import au.gov.ga.worldwind.tiler.util.JIntegerField;
import au.gov.ga.worldwind.tiler.util.Prefs;


public class PreviewSetup
{
	public PreviewSetup(JFrame parent, String directory, String extension,
			boolean elevations, Integer type, Integer tilesize, Double nodata,
			Double lzts)
	{
		initilize(parent);

		this.directory.setText(directory);
		this.imageRadio.setSelected(!elevations);
		this.elevationRadio.setSelected(elevations);
		this.pngRadio.setSelected(extension.equals("png"));
		this.jpegRadio.setSelected(extension.equals("jpg"));
		this.tilesizeField.setValue(tilesize);
		this.nodataCheck.setSelected(nodata != null);
		this.nodataField.setValue(nodata);
		this.lztsField.setValue(lzts);

		this.byteRadio.setSelected(type == gdalconst.GDT_Byte);
		this.int16Radio.setSelected(type == gdalconst.GDT_Int16);
		this.int32Radio.setSelected(type == gdalconst.GDT_Int32);
		this.float32Radio.setSelected(type == gdalconst.GDT_Float32);

		enableFields();
		frame.setSize(600, 300);
		frame.setLocationRelativeTo(parent);
		frame.setVisible(true);
	}

	private JFrame frame;
	private Preferences preferences = Prefs.getPreferences();

	private JTextField directory;

	private JLabel tileTypeLabel;
	private JRadioButton imageRadio;
	private JRadioButton elevationRadio;
	private JDoubleField lztsField;
	private JLabel tilesizeLabel;
	private JPanel tilesizePanel;
	private JIntegerField tilesizeField;

	private JLabel imageFormatLabel;
	private JRadioButton jpegRadio;
	private JRadioButton pngRadio;

	private JLabel cellTypeLabel;
	private JRadioButton byteRadio;
	private JRadioButton int16Radio;
	private JRadioButton int32Radio;
	private JRadioButton float32Radio;
	private JCheckBox nodataCheck;
	private JDoubleField nodataField;

	private void initilize(JFrame parent)
	{
		GridBagConstraints c;
		JPanel panel;
		int SPACING = 4;
		ActionListener al;
		JLabel label;
		Dimension size;
		ButtonGroup bg;

		frame = new JFrame("World Wind Tile Previewer");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JPanel trPanel = new JPanel(new GridBagLayout());
		trPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		frame.add(trPanel, BorderLayout.CENTER);


		label = new JLabel("Tile directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		c.anchor = GridBagConstraints.EAST;
		trPanel.add(label, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		trPanel.add(panel, c);

		directory = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(directory, c);

		JButton browse = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(browse, c);
		browse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(preferences.get(
						Application.OUTPUT_DIR_KEY, null));
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					File dir = chooser.getSelectedFile();
					directory.setText(dir.getAbsolutePath());
					preferences.put(Application.OUTPUT_DIR_KEY, dir
							.getAbsolutePath());
				}
			}
		});

		tileTypeLabel = new JLabel("Tile type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(tileTypeLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		};

		imageRadio = new JRadioButton("Images");
		imageRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(imageRadio, c);
		imageRadio.addActionListener(al);

		elevationRadio = new JRadioButton("Elevations");
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(elevationRadio, c);
		elevationRadio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(imageRadio);
		bg.add(elevationRadio);

		tilesizeLabel = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(tilesizeLabel, c);

		tilesizePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(tilesizePanel, c);

		tilesizeField = new JIntegerField(512);
		tilesizeField.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		size = tilesizeField.getPreferredSize();
		size.width = 50;
		tilesizeField.setMinimumSize(size);
		tilesizeField.setMaximumSize(size);
		tilesizeField.setPreferredSize(size);
		tilesizePanel.add(tilesizeField, c);

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, SPACING / 2, 0, 0);
		tilesizePanel.add(label, c);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(label, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		lztsField = new JDoubleField(36d);
		c = new GridBagConstraints();
		c.gridx = 0;
		size = lztsField.getPreferredSize();
		size.width = 50;
		lztsField.setMinimumSize(size);
		lztsField.setMaximumSize(size);
		lztsField.setPreferredSize(size);
		panel.add(lztsField, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, SPACING / 2, 0, 0);
		panel.add(label, c);

		imageFormatLabel = new JLabel("Image format:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(imageFormatLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		jpegRadio = new JRadioButton("JPEG");
		jpegRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(jpegRadio, c);

		pngRadio = new JRadioButton("PNG");
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(pngRadio, c);

		bg = new ButtonGroup();
		bg.add(jpegRadio);
		bg.add(pngRadio);


		cellTypeLabel = new JLabel("Output cell type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(cellTypeLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		byteRadio = new JRadioButton("Byte");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(byteRadio, c);

		int16Radio = new JRadioButton("16-bit integer");
		int16Radio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(int16Radio, c);

		int32Radio = new JRadioButton("32-bit integer");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(int32Radio, c);

		float32Radio = new JRadioButton("32-bit float");
		c = new GridBagConstraints();
		c.gridx = 3;
		panel.add(float32Radio, c);

		bg = new ButtonGroup();
		bg.add(byteRadio);
		bg.add(int16Radio);
		bg.add(int32Radio);
		bg.add(float32Radio);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		nodataCheck = new JCheckBox("Use NODATA value:");
		c = new GridBagConstraints();
		panel.add(nodataCheck, c);
		nodataCheck.addActionListener(al);

		nodataField = new JDoubleField(0d);
		size = nodataField.getPreferredSize();
		size.width = 50;
		nodataField.setMinimumSize(size);
		nodataField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(nodataField, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		trPanel.add(panel, c);

		JButton previewButton = new JButton("Preview");
		c = new GridBagConstraints();
		panel.add(previewButton, c);
		previewButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (directory.getText().length() <= 0)
						throw new Exception("Directory field is blank");
					File dir = new File(directory.getText());
					if (!dir.exists())
						throw new Exception("Directory does not exist: "
								+ dir.getAbsolutePath());
					if (lztsField.getValue() == null)
						throw new Exception(
								"Level Zero Tile Size field is blank");

					if (elevationRadio.isSelected())
					{
						if (tilesizeField.getValue() == null)
							throw new Exception("Tile Size field is blank");

						int type = getElevationBufferType();
						int tilesize = tilesizeField.getValue();
						Double nodata = nodataCheck.isSelected() ? nodataField
								.getValue() : null;
						new Previewer(dir, "bil", type, tilesize, tilesize,
								nodata, lztsField.getValue());
					}
					else
					{
						new Previewer(dir, getImageFormat(), lztsField
								.getValue());
					}

					frame.setVisible(false);
					frame.dispose();
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(frame, ex.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private void enableFields()
	{
		boolean elevations = elevationRadio.isSelected();

		tilesizeLabel.setVisible(elevations);
		tilesizePanel.setVisible(elevations);

		imageFormatLabel.setVisible(!elevations);
		jpegRadio.setVisible(!elevations);
		pngRadio.setVisible(!elevations);

		cellTypeLabel.setVisible(elevations);
		byteRadio.setVisible(elevations);
		int16Radio.setVisible(elevations);
		int32Radio.setVisible(elevations);
		float32Radio.setVisible(elevations);
		nodataCheck.setVisible(elevations);
		nodataField.setVisible(elevations);

		nodataField.setEnabled(nodataCheck.isSelected());
	}

	private String getImageFormat()
	{
		return pngRadio.isSelected() ? "png" : "jpg";
	}

	private int getElevationBufferType()
	{
		return byteRadio.isSelected() ? gdalconstConstants.GDT_Byte
				: int16Radio.isSelected() ? gdalconstConstants.GDT_Int16
						: int32Radio.isSelected() ? gdalconstConstants.GDT_Int32
								: gdalconstConstants.GDT_Float32;
	}
}
