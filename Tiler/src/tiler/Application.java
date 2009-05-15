package tiler;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import util.DocumentAdapter;
import util.DocumentLogger;
import util.GDALTile;
import util.GDALUtil;
import util.JDoubleField;
import util.JIntegerField;
import util.ProgressReporterImpl;
import util.Sector;
import util.TilerException;

public class Application
{
	/*
	BUTTONS
	-------
	Open File
	Preview?
	
	INFORMATION
	-----------
	Dimensions
	Extents
	Coordinate system
	Band count
	Buffer format
	
	TILING OPTIONS
	--------------
	Tiling type (image or bil (or mapnik?))
	Level Zero Tile Size (degrees)
	Tile size (pixels)
	No data value (show value for each band)
	Create overviews (true/false)
	
	FOR IMAGES:
	Image format (JPEG or PNG)
	Save alpha channel (PNG only)
	
	FOR BILS:
	Output type (byte, int16, int32)
	Band to use (if more than one band)
	 */

	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		GDALUtil.init();
	}

	public final static String LOGGER = "TilerLogger";

	private Logger logger;
	private JTextPane textLog;
	private JPopupMenu loggerPopup;
	private JFrame frame;
	private JProgressBar progress;

	private JTextField fileField;
	private JButton browseButton;
	private JTextArea infoText;
	private JLabel previewCanvas;
	private JRadioButton imageRadio;
	private JRadioButton elevationRadio;
	private JDoubleField lztsField;
	private JIntegerField tilesizeField;
	private JCheckBox outsideCheck;
	private JPanel outsidePanel;
	private JIntegerField[] outsideFields = new JIntegerField[0];
	private JCheckBox overviewsCheck;
	private JCheckBox replaceCheck;
	private JLabel replace1Label;
	private JLabel replace2Label;
	private JPanel minPanel;
	private JPanel maxPanel;
	private JPanel replacePanel;
	private JIntegerField[] minFields = new JIntegerField[0];
	private JIntegerField[] maxFields = new JIntegerField[0];
	private JIntegerField[] replaceFields = new JIntegerField[0];

	private JPanel cards;

	private JRadioButton jpegRadio;
	private JRadioButton pngRadio;
	private JCheckBox alphaCheck;

	private JRadioButton byteRadio;
	private JRadioButton int16Radio;
	private JRadioButton int32Radio;
	private JComboBox bandCombo;
	private JCheckBox overrideLevelsCheck;
	private JSpinner levelsSpinner;

	private JTextField outputDirectory;
	private JButton outputButton;

	private JTextArea tileText;
	private JButton tileButton;
	private JButton cancelButton;

	private boolean tilesizeChanged = false;
	private boolean tilesizeBeingSet = false;
	private boolean lztsChanged = false;
	private boolean lztsBeingSet = false;
	private boolean alphaSet = false;

	private boolean fileOpen = false;
	private boolean validOptions = false;
	private boolean running = false;
	private int bandCount = 0;
	private Dataset dataset;
	private Sector sector;
	private int levels;

	private TilerProgressReporter reporter;

	private List<JComponent> labels = new ArrayList<JComponent>();

	public static void main(String[] args)
	{
		new Application();
	}

	public Application()
	{
		JLabel label;
		GridBagConstraints c;
		JScrollPane scrollPane;
		ButtonGroup bg;
		JPanel panel;
		Dimension size;

		frame = new JFrame("Tiler");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JPanel tlPanel = new JPanel(new GridBagLayout());
		tlPanel.setBorder(BorderFactory.createTitledBorder("Dataset"));
		JPanel trPanel = new JPanel(new GridBagLayout());
		trPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel blPanel = new JPanel(new GridBagLayout());
		blPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		JPanel brPanel = new JPanel(new GridBagLayout());
		brPanel.setBorder(BorderFactory.createTitledBorder("Tiler"));
		JPanel bPanel = new JPanel(new GridBagLayout());

		JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				tlPanel, blPanel);
		JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				trPanel, brPanel);
		JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				leftSplit, rightSplit);
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				topSplit, bPanel);

		frame.add(split, BorderLayout.CENTER);

		textLog = new JTextPane();
		textLog.setEditable(false);
		scrollPane = new JScrollPane(textLog,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		bPanel.add(scrollPane, c);
		textLog.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			public void anyChange(DocumentEvent e)
			{
				int length = textLog.getDocument().getLength();
				textLog.select(length, length);
			}
		});

		progress = new JProgressBar(0, 100);
		progress.setString("");
		progress.setStringPainted(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		bPanel.add(progress, c);

		logger = new DocumentLogger(LOGGER, textLog.getStyledDocument());
		LogManager.getLogManager().addLogger(logger);
		logger.setLevel(Level.FINE);
		createLoggerPopupMenu();

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		tlPanel.add(panel, c);

		label = new JLabel("Dataset:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);

		fileField = new JTextField();
		fileField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(fileField, c);

		browseButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(browseButton, c);
		browseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new GDALFileFilter());
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					openDataset(file);
				}
			}
		});

		infoText = new JTextArea();
		infoText.setBackground(label.getBackground());
		infoText.setEditable(false);
		Font font = Font.decode(null);
		infoText.setFont(font);
		scrollPane = new JScrollPane(infoText,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		size = new Dimension(20, 20);
		scrollPane.setMinimumSize(size);
		scrollPane.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTH;
		tlPanel.add(scrollPane, c);

		previewCanvas = new JLabel();
		previewCanvas.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane = new JScrollPane(previewCanvas,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		blPanel.add(scrollPane, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		label = new JLabel("Tile type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tileTypeChanged();
			}
		};

		imageRadio = new JRadioButton("Images");
		imageRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(imageRadio, c);
		imageRadio.addActionListener(al);

		elevationRadio = new JRadioButton("Elevations");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(elevationRadio, c);
		elevationRadio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(imageRadio);
		bg.add(elevationRadio);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		label = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		tilesizeField = new JIntegerField(512);
		tilesizeField.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		size = tilesizeField.getPreferredSize();
		size.width = 50;
		tilesizeField.setMinimumSize(size);
		tilesizeField.setMaximumSize(size);
		tilesizeField.setPreferredSize(size);
		panel.add(tilesizeField, c);
		tilesizeField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			public void anyChange(DocumentEvent e)
			{
				if (!tilesizeBeingSet)
				{
					tilesizeChanged = true;
				}
			}
		});

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(label, c);
		labels.add(label);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		lztsField = new JDoubleField(36d);
		c = new GridBagConstraints();
		c.gridx = 1;
		size = lztsField.getPreferredSize();
		size.width = 50;
		lztsField.setMinimumSize(size);
		lztsField.setMaximumSize(size);
		lztsField.setPreferredSize(size);
		panel.add(lztsField, c);
		lztsField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			public void anyChange(DocumentEvent e)
			{
				if (!lztsBeingSet)
				{
					lztsChanged = true;
				}
			}
		});

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(label, c);
		labels.add(label);

		cards = new JPanel(new CardLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(cards, c);

		JPanel imageCard = new JPanel(new GridBagLayout());
		cards.add(imageCard, imageRadio.getText());

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		imageCard.add(panel, c);

		label = new JLabel("Image format:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				imageFormatChanged();
			}
		};

		jpegRadio = new JRadioButton("JPEG");
		jpegRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(jpegRadio, c);
		jpegRadio.addActionListener(al);

		pngRadio = new JRadioButton("PNG");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(pngRadio, c);
		pngRadio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(jpegRadio);
		bg.add(pngRadio);

		alphaCheck = new JCheckBox("Add alpha channel");
		c = new GridBagConstraints();
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		imageCard.add(alphaCheck, c);
		alphaCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				alphaSet = alphaCheck.isSelected();
				bandCountChanged();
			}
		});

		JPanel elevationCard = new JPanel(new GridBagLayout());
		cards.add(elevationCard, elevationRadio.getText());

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		elevationCard.add(panel, c);

		label = new JLabel("Output cell type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		byteRadio = new JRadioButton("Byte");
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(byteRadio, c);

		int16Radio = new JRadioButton("16-bit integer");
		int16Radio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(int16Radio, c);

		int32Radio = new JRadioButton("32-bit integer");
		c = new GridBagConstraints();
		c.gridx = 3;
		panel.add(int32Radio, c);

		bg = new ButtonGroup();
		bg.add(byteRadio);
		bg.add(int16Radio);
		bg.add(int32Radio);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		elevationCard.add(panel, c);

		label = new JLabel("Band:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);
		labels.add(label);

		bandCombo = new JComboBox(new Integer[] { 1 });
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(bandCombo, c);

		overviewsCheck = new JCheckBox("Generate overviews");
		overviewsCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(overviewsCheck, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		overrideLevelsCheck = new JCheckBox("Override level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(overrideLevelsCheck, c);
		overrideLevelsCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});

		levelsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(levelsSpinner, c);

		outsideCheck = new JCheckBox("Set pixels outsize dataset extents to:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(outsideCheck, c);
		outsideCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});

		outsidePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(outsidePanel, c);

		replaceCheck = new JCheckBox("Set pixels with values between:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 8;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(replaceCheck, c);
		replaceCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});

		minPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 9;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(minPanel, c);

		replace1Label = new JLabel("and:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 10;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(replace1Label, c);

		maxPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 11;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(maxPanel, c);

		replace2Label = new JLabel("to:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 12;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(replace2Label, c);

		replacePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 13;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1;
		c.insets = new Insets(0, 32, 0, 0);
		trPanel.add(replacePanel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		brPanel.add(panel, c);

		label = new JLabel("Output directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(label, c);

		outputDirectory = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(outputDirectory, c);

		outputButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(outputButton, c);
		outputButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					File dir = chooser.getSelectedFile();
					outputDirectory.setText(dir.getAbsolutePath());
				}
			}
		});

		tileText = new JTextArea();
		tileText.setBackground(label.getBackground());
		tileText.setEditable(false);
		tileText.setFont(font);
		scrollPane = new JScrollPane(tileText,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		size = new Dimension(20, 20);
		scrollPane.setMinimumSize(size);
		scrollPane.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTH;
		brPanel.add(scrollPane, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		brPanel.add(panel, c);

		tileButton = new JButton("Generate tiles");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(tileButton, c);
		tileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				generateTiles();
			}
		});

		cancelButton = new JButton("Cancel");
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(cancelButton, c);
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});

		addRecalculateListeners();
		openDataset(null);
		logger.info("Started");

		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		//TODO make better
		split.setDividerLocation(0.7);
		leftSplit.setDividerLocation(0.5);
		rightSplit.setDividerLocation(0.5);
		topSplit.setDividerLocation(0.5);
		split.setResizeWeight(0.7);
		leftSplit.setResizeWeight(0.5);
		rightSplit.setResizeWeight(0.5);
		topSplit.setResizeWeight(0.5);
	}

	private void createLoggerPopupMenu()
	{
		loggerPopup = new JPopupMenu();
		Level[] levels = new Level[] { Level.FINEST, Level.FINER, Level.FINE,
				Level.INFO, Level.WARNING, Level.SEVERE };

		ButtonGroup bg = new ButtonGroup();
		for (final Level level : levels)
		{
			JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
					new AbstractAction(level.getName())
					{
						public void actionPerformed(ActionEvent e)
						{
							logger.setLevel(level);
						}
					});
			loggerPopup.add(menuItem);
			bg.add(menuItem);
			menuItem.setSelected(logger.getLevel().equals(level));
		}

		MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					loggerPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		textLog.addMouseListener(ml);
	}

	private void addRecalculateListeners()
	{
		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				recalculateTiles();
			}
		};
		DocumentListener dl = new DocumentAdapter()
		{
			@Override
			public void anyChange(DocumentEvent e)
			{
				recalculateTiles();
			}
		};
		ChangeListener cl = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				recalculateTiles();
			}
		};

		lztsField.getDocument().addDocumentListener(dl);
		tilesizeField.getDocument().addDocumentListener(dl);
		overviewsCheck.addActionListener(al);
		outputDirectory.getDocument().addDocumentListener(dl);
		levelsSpinner.addChangeListener(cl);
		overrideLevelsCheck.addActionListener(al);

		/*alphaCheck.addActionListener(tal);
		bandCombo.addActionListener(tal);
		byteRadio.addActionListener(tal);
		elevationRadio.addActionListener(tal);
		imageRadio.addActionListener(tal);
		int16Radio.addActionListener(tal);
		int32Radio.addActionListener(tal);
		jpegRadio.addActionListener(tal);
		pngRadio.addActionListener(tal);
		for (JIntegerField field : nodataFields)
		{
			field.getDocument().addDocumentListener(tdl);
		}*/
	}

	private void openDataset(File file)
	{
		fileOpen = file != null;

		if (!fileOpen)
		{
			//no file open
			infoText.setText("");
			fileField.setText("");
			previewCanvas.setIcon(null);
			bandCount = 0;
		}
		else
		{
			try
			{
				fileField.setText(file.getAbsolutePath());

				final Dataset dataset = GDALUtil.open(file);
				final Sector sector = GDALUtil.getSector(dataset);
				this.sector = sector;
				this.dataset = dataset;
				final int width = dataset.getRasterXSize();
				final int height = dataset.getRasterYSize();
				if (sector.getMinLongitude() == 0
						&& sector.getMinLatitude() == 0
						&& sector.getMaxLongitude() == width
						&& sector.getMaxLatitude() == height)
				{
					throw new TilerException("Dataset " + file
							+ " is not geo-referenced");
				}
				bandCount = dataset.getRasterCount();
				if (bandCount == 0)
				{
					throw new TilerException("Dataset contains 0 raster bands");
				}
				String projection = dataset.GetProjection();
				SpatialReference spatialReference = (projection == null || projection
						.length() == 0) ? null : new SpatialReference(
						projection);
				String[] dataTypes = new String[bandCount];
				int[] dataTypeSizes = new int[bandCount];
				for (int i = 0; i < bandCount; i++)
				{
					Band band = dataset.GetRasterBand(i + 1);
					int dataType = band.getDataType();
					dataTypes[i] = gdal.GetDataTypeName(dataType);
					dataTypeSizes[i] = gdal.GetDataTypeSize(dataType);
				}
				StringBuilder info = new StringBuilder();
				info.append("Dataset information:\n");
				info.append("Size = " + width + ", " + height + "\n");
				info.append("Cell size = "
						+ (sector.getDeltaLongitude() / width) + ", "
						+ (sector.getDeltaLatitude() / height) + "\n");
				info.append("Top left corner = (" + sector.getMinLongitude()
						+ ", " + sector.getMinLatitude() + ")\n");
				info.append("Bottom right corner = ("
						+ sector.getMaxLongitude() + ", "
						+ sector.getMaxLatitude() + ")\n");
				info.append("Raster band count = " + bandCount + "\n");
				for (int i = 0; i < bandCount; i++)
				{
					info.append("Band " + (i + 1) + " data type = "
							+ dataTypes[i] + " (" + dataTypeSizes[i]
							+ " bit)\n");
				}
				if (spatialReference != null)
				{
					info.append("Coordinate system = \n");
					info.append(spatialReference.ExportToPrettyWkt() + "\n");
				}

				String text = info.substring(0, info.length() - 1);
				infoText.setText(text);
				infoText.select(0, 0);

				Thread thread = new Thread(new Runnable()
				{
					public void run()
					{
						logger.fine("Generating preview...");
						try
						{
							int w = 400;
							int h = w;
							if (width > height)
							{
								h = w * height / width;
							}
							else
							{
								w = h * width / height;
							}
							GDALTile tile = new GDALTile(dataset, w, h, sector
									.getMinLatitude(),
									sector.getMinLongitude(), sector
											.getMaxLatitude(), sector
											.getMaxLongitude());
							BufferedImage image = tile.getAsImage();
							ImageIcon icon = new ImageIcon(image);
							previewCanvas.setIcon(icon);
							logger.fine("Preview generation complete");
						}
						catch (Exception e)
						{
							logger.severe(e.getMessage());
						}
					}
				});
				thread.setDaemon(true);
				thread.start();

				logger.info("Dataset " + file + " opened");
			}
			catch (Exception e)
			{
				logger.severe(e.getMessage());
				return;
			}
		}

		bandCountChanged();
		tileTypeChanged();
		imageFormatChanged();
		recalculateTiles();

		frame.doLayout();
	}

	private void tileTypeChanged()
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (elevationRadio.isSelected())
		{
			cl.show(cards, elevationRadio.getText());
		}
		else
		{
			cl.show(cards, imageRadio.getText());
		}

		if (!tilesizeChanged)
		{
			tilesizeBeingSet = true;
			if (elevationRadio.isSelected())
			{
				tilesizeField.setValue(150);
			}
			else
			{
				tilesizeField.setValue(512);
			}
			tilesizeBeingSet = false;
		}
		if (!lztsChanged)
		{
			lztsBeingSet = true;
			if (elevationRadio.isSelected())
			{
				lztsField.setValue(20d);
			}
			else
			{
				lztsField.setValue(36d);
			}
			lztsBeingSet = false;
		}
	}

	private void imageFormatChanged()
	{
		alphaCheck.setSelected(pngRadio.isSelected() && alphaSet
				&& bandCount != 4);
		bandCountChanged();
		enableFields();
	}

	private void bandCountChanged()
	{
		GridBagConstraints c;

		//save old values
		Integer[] outsideBackup = readIntegerFields(outsideFields);
		Integer[] minBackup = readIntegerFields(minFields);
		Integer[] maxBackup = readIntegerFields(maxFields);
		Integer[] replaceBackup = readIntegerFields(replaceFields);

		outsidePanel.removeAll();
		minPanel.removeAll();
		maxPanel.removeAll();
		replacePanel.removeAll();
		while (bandCombo.getItemCount() > 0)
		{
			bandCombo.removeItemAt(0);
		}
		int bandCount = this.bandCount <= 0 ? 1 : this.bandCount;
		bandCount = bandCount == 3 && alphaCheck.isSelected() ? 4 : bandCount;
		outsideFields = new JIntegerField[bandCount];
		minFields = new JIntegerField[bandCount];
		maxFields = new JIntegerField[bandCount];
		replaceFields = new JIntegerField[bandCount];
		for (int i = 0; i < bandCount; i++)
		{
			outsideFields[i] = new JIntegerField(0);
			Dimension size = outsideFields[i].getPreferredSize();
			size.width = 50;
			outsideFields[i].setMinimumSize(size);
			outsideFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			outsidePanel.add(outsideFields[i], c);

			minFields[i] = new JIntegerField(0);
			minFields[i].setMinimumSize(size);
			minFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			minPanel.add(minFields[i], c);

			maxFields[i] = new JIntegerField(0);
			maxFields[i].setMinimumSize(size);
			maxFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			maxPanel.add(maxFields[i], c);

			replaceFields[i] = new JIntegerField(null);
			replaceFields[i].setMinimumSize(size);
			replaceFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			replacePanel.add(replaceFields[i], c);

			bandCombo.addItem(new Integer(i + 1));
		}
		
		//restore values
		writeIntegerFields(outsideFields, outsideBackup);
		writeIntegerFields(minFields, minBackup);
		writeIntegerFields(maxFields, maxBackup);
		writeIntegerFields(replaceFields, replaceBackup);

		enableFields();
	}

	private Integer[] readIntegerFields(JIntegerField[] fields)
	{
		Integer[] ints = new Integer[fields.length];
		for (int i = 0; i < fields.length; i++)
		{
			ints[i] = fields[i].getValue();
		}
		return ints;
	}

	private void writeIntegerFields(JIntegerField[] fields, Integer[] values)
	{
		for (int i = 0; i < fields.length && i < values.length; i++)
		{
			fields[i].setValue(values[i]);
		}
	}

	private void enableFields()
	{
		boolean standard = fileOpen && !running;

		byteRadio.setEnabled(standard);
		elevationRadio.setEnabled(standard);
		imageRadio.setEnabled(standard);
		//infoText.setEnabled(standard);
		//tileText.setEnabled(standard);
		int16Radio.setEnabled(standard);
		int32Radio.setEnabled(standard);
		jpegRadio.setEnabled(standard);
		lztsField.setEnabled(standard);
		outsideCheck.setEnabled(standard);
		overviewsCheck.setEnabled(standard);
		overrideLevelsCheck.setEnabled(standard);
		pngRadio.setEnabled(standard);
		tilesizeField.setEnabled(standard);
		for (JComponent label : labels)
		{
			label.setEnabled(standard);
		}

		alphaCheck.setEnabled(pngRadio.isSelected() && standard
				&& bandCount != 4);
		bandCombo.setEnabled(bandCount > 1 && standard);
		levelsSpinner.setEnabled(overrideLevelsCheck.isSelected() && standard);

		for (JIntegerField field : outsideFields)
		{
			field.setEnabled(outsideCheck.isSelected() && standard);
		}

		replaceCheck.setEnabled(standard);
		replace1Label.setEnabled(replaceCheck.isSelected() && standard);
		replace2Label.setEnabled(replaceCheck.isSelected() && standard);
		for (JIntegerField field : minFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JIntegerField field : maxFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JIntegerField field : replaceFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}

		browseButton.setEnabled(!running);
		outputButton.setEnabled(!running);
		outputDirectory.setEnabled(!running);
		tileButton.setEnabled(validOptions
				&& outputDirectory.getText().length() != 0 && !running);
		cancelButton.setEnabled(running);
		progress.setEnabled(running);
	}

	private void recalculateTiles()
	{
		validOptions = fileOpen;

		StringBuilder info = new StringBuilder();

		if (validOptions)
		{
			Double lzts = lztsField.getValue();
			Integer tilesize = tilesizeField.getValue();
			if (tilesize != null && tilesize <= 0)
				tilesize = null;
			if (lzts != null && lzts <= 0)
				lzts = null;

			validOptions = lzts != null && tilesize != null;
			if (validOptions)
			{
				if (overrideLevelsCheck.isSelected())
				{
					levels = (Integer) levelsSpinner.getModel().getValue();
				}
				else
				{
					levels = GDALUtil.levelCount(dataset, lzts, sector,
							tilesize);
					levelsSpinner.getModel().setValue(levels);
				}
				int[] tileCount = new int[levels];
				int totalCount = 0;
				boolean overviews = overviewsCheck.isSelected();

				for (int i = overviews ? 0 : levels - 1; i < levels; i++)
				{
					int minX = GDALUtil.getTileX(sector.getMinLongitude(), i,
							lzts);
					int maxX = GDALUtil.getTileX(sector.getMaxLongitude(), i,
							lzts);
					int minY = GDALUtil.getTileY(sector.getMinLatitude(), i,
							lzts);
					int maxY = GDALUtil.getTileY(sector.getMaxLatitude(), i,
							lzts);
					tileCount[i] = (maxX - minX + 1) * (maxY - minY + 1);
					totalCount += tileCount[i];
				}

				info.append("Tiling information:\n");
				info.append("Level count = " + levels + "\n");
				if (overviews)
				{
					info.append("Tile count at highest level = "
							+ tileCount[levels - 1] + "\n");
					info.append("Overview tile count = "
							+ (totalCount - tileCount[levels - 1]) + "\n");
				}
				info.append("Total tile count = " + totalCount + "\n");
			}
		}

		String outDir = outputDirectory.getText();
		if (outDir.length() == 0)
		{
			info.append("Please select an output directory\n");
		}
		else
		{
			info.append("Tiles will be saved to "
					+ new File(outDir).getAbsolutePath() + "\n");
		}

		if (!validOptions)
		{
			tileText.setText("Invalid dataset or options");
		}
		else
		{
			tileText.setText(info.substring(0, info.length() - 1));
		}
		tileText.select(0, 0);

		enableFields();
	}

	private void generateTiles()
	{
		final TilerProgressReporter reporter = new TilerProgressReporter(this,
				logger);
		this.reporter = reporter;
		started();

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				int level = levels - 1;
				int tilesize = tilesizeField.getValue();
				double lzts = lztsField.getValue();
				File outDir = new File(outputDirectory.getText());
				int[] outsideValues = null;
				int[] minReplace = null;
				int[] maxReplace = null;
				Integer[] replace = null;
				if (outsideCheck.isSelected())
				{
					outsideValues = new int[bandCount];
					for (int b = 0; b < bandCount; b++)
					{
						Integer value = outsideFields[b].getValue();
						outsideValues[b] = value != null ? value : 0;
					}
				}
				if (replaceCheck.isSelected())
				{
					minReplace = new int[bandCount];
					maxReplace = new int[bandCount];
					replace = new Integer[bandCount];
					for (int b = 0; b < bandCount; b++)
					{
						Integer value = minFields[b].getValue();
						minReplace[b] = value != null ? value : 0;
						value = maxFields[b].getValue();
						maxReplace[b] = value != null ? value : 0;
						replace[b] = replaceFields[b].getValue();
					}
				}

				if (imageRadio.isSelected())
				{
					String imageFormat = pngRadio.isSelected() ? "png" : "jpg";
					boolean addAlpha = pngRadio.isSelected()
							&& alphaCheck.isSelected();

					Tiler.tileImages(dataset, sector, level, tilesize, lzts,
							imageFormat, addAlpha, outsideValues, minReplace,
							maxReplace, replace, outDir, reporter);
					Overviewer.createImageOverviews(outDir, imageFormat,
							tilesize, tilesize, outsideValues);
				}
				else if (elevationRadio.isSelected())
				{
					int bufferType = byteRadio.isSelected() ? gdalconstConstants.GDT_Byte
							: int16Radio.isSelected() ? gdalconstConstants.GDT_Int16
									: gdalconstConstants.GDT_Int32;
					int band = Integer.parseInt((String) bandCombo
							.getSelectedItem()) - 1;
					Tiler.tileElevations(dataset, sector, level, tilesize,
							lzts, bufferType, band, outsideValues, minReplace,
							maxReplace, replace, outDir, reporter);
					Overviewer.createElevationOverviews(outDir, tilesize,
							tilesize, bufferType, ByteOrder.LITTLE_ENDIAN, /*TODO remove hardcoded byteorder*/
							outsideValues);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void cancel()
	{
		if (reporter != null)
		{
			reporter.cancel();
		}
		completed();
	}

	private void started()
	{
		running = true;
		enableFields();
	}

	private void completed()
	{
		running = false;
		enableFields();
		reporter = null;
	}

	private static class GDALFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f)
		{
			return true;
		}

		@Override
		public String getDescription()
		{
			return "Any GDAL supported dataset (*.*)";
		}
	}

	private static class TilerProgressReporter extends ProgressReporterImpl
	{
		private Application tiler;

		public TilerProgressReporter(Application tiler, Logger logger)
		{
			super(logger);
			this.tiler = tiler;
		}

		public void done()
		{
			tiler.completed();
			resetProgress();
		}

		@Override
		public void cancel()
		{
			super.cancel();
			tiler.completed();
			resetProgress();
		}

		private void resetProgress()
		{
			tiler.progress.setString("");
			tiler.progress.setValue(0);
		}

		public void progress(double percent)
		{
			int ip = (int) (percent * 100);
			tiler.progress.setValue(ip);
			tiler.progress.setString(ip + "% complete");
		}
	}
}
