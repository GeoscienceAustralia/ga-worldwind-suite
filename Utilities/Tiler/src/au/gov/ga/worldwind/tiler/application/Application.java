package au.gov.ga.worldwind.tiler.application;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import au.gov.ga.worldwind.tiler.application.Tiler.TilingType;
import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.gdal.GDALUtil;
import au.gov.ga.worldwind.tiler.mapnik.MapnikUtil;
import au.gov.ga.worldwind.tiler.preview.PreviewSetup;
import au.gov.ga.worldwind.tiler.util.DocumentAdapter;
import au.gov.ga.worldwind.tiler.util.DocumentLogger;
import au.gov.ga.worldwind.tiler.util.JDoubleField;
import au.gov.ga.worldwind.tiler.util.JIntegerField;
import au.gov.ga.worldwind.tiler.util.JLongField;
import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.Prefs;
import au.gov.ga.worldwind.tiler.util.ProgressReporterImpl;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.TilerException;
import au.gov.ga.worldwind.tiler.util.Util;


public class Application implements UncaughtExceptionHandler
{
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
	}

	private final static String LOGGER = "TilerLogger";

	public final static String OUTPUT_DIR_KEY = "Last Output Directory";

	private Logger logger;
	private JTextPane textLog;
	private JPopupMenu loggerPopup;
	private JFrame frame;
	private JProgressBar progress;
	private JSplitPane leftSplit, rightSplit, topSplit, logSplit;
	private Runnable saveSplitLocations;

	private JRadioButton gdalRadio;
	private JRadioButton mapnikRadio;
	private JRadioButton datasetSelected;

	private JDoubleField minLatitudeField;
	private JDoubleField minLongitudeField;
	private JDoubleField maxLatitudeField;
	private JDoubleField maxLongitudeField;

	private JTextField gdalFileField;
	private JTextField mapnikFileField;
	private JTextField pythonBinaryField;
	private JTextField mapnikScriptField;
	private JButton browseGdalButton;
	private JButton browseMapnikButton;
	private JButton browsePythonBinaryButton;
	private JButton browseMapnikScriptButton;
	private JTextArea infoText;
	private JLabel previewCanvas;
	private boolean previewGenerated;

	private JLabel tileTypeLabel;
	private JRadioButton imageRadio;
	private JRadioButton elevationRadio;
	private JCheckBox reprojectCheck;
	private JCheckBox bilinearCheck;
	private JCheckBox bilinearOverviewsCheck;
	private JDoubleField lztsField;
	private JIntegerField tilesizeField;
	private JCheckBox outsideCheck;
	private JPanel outsidePanel;
	private JTextField[] outsideFields = new JTextField[0];
	private JCheckBox ignoreBlankCheck;
	private JLabel levelsLabel;
	private JSpinner levelsSpinner;
	private JCheckBox overviewsCheck;
	private JCheckBox replaceCheck;
	private JLabel replace1Label;
	private JLabel replace2Label;
	private JLabel replace3Label;
	private JLabel replace4Label;
	private JLabel replace5Label;
	private JLabel replace6Label;
	private JPanel minPanel;
	private JPanel maxPanel;
	private JPanel min2Panel;
	private JPanel max2Panel;
	private JPanel withPanel;
	private JPanel otherwisePanel;
	private JTextField[] minFields = new JTextField[0];
	private JTextField[] maxFields = new JTextField[0];
	private JTextField[] min2Fields = new JTextField[0];
	private JTextField[] max2Fields = new JTextField[0];
	private JTextField[] replaceFields = new JTextField[0];
	private JTextField[] otherwiseFields = new JTextField[0];

	private JPanel dataModeCards;

	private JLabel imageFormatLabel;
	private JRadioButton jpegRadio;
	private JRadioButton pngRadio;
	private JCheckBox alphaCheck;

	private JLabel cellTypeLabel;
	private JRadioButton byteRadio;
	private JRadioButton int16Radio;
	private JRadioButton int32Radio;
	private JRadioButton float32Radio;
	private JLabel bandLabel;
	private JComboBox bandCombo;
	private JCheckBox overrideLevelsCheck;
	private JSpinner overrideLevelsSpinner;

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
	private int outputBandCount = 0;
	private Dataset dataset;
	private File mapFile;
	private Sector sector;
	private int levels;

	private TilerProgressReporter reporter;
	private List<JComponent> labels = new ArrayList<JComponent>();

	private Preferences preferences = Prefs.getPreferences();

	public static void start()
	{
		new Application();
	}

	public Application()
	{
		GDALUtil.init();

		if (!GDALUtil.isProjectionsSupported())
		{
			String message =
					"Before running this program, the GDAL_DATA environment variable should point to the directory containing\n"
							+ "'"
							+ GDALUtil.GCS_FILE
							+ "' (usually GDAL_DIR/data). Without this environment variable, map projections are unsupported.\n\n"
							+ "Do you wish to continue without projection support?";
			int value =
					JOptionPane.showConfirmDialog(null, message, "Warning",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (value != JOptionPane.YES_OPTION)
			{
				return;
			}
		}

		Thread.setDefaultUncaughtExceptionHandler(this);

		JLabel label;
		GridBagConstraints c;
		JScrollPane scrollPane;
		ButtonGroup bg;
		JPanel panel;
		Dimension size;
		ActionListener al;
		JSeparator sep;
		int SPACING = 4;

		frame = new JFrame("World Wind Tiler");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent we)
			{
				onExit();
			}
		});

		JPanel tlPanel = new JPanel(new GridBagLayout());
		tlPanel.setBorder(BorderFactory.createTitledBorder("Input"));
		JPanel trPanel = new JPanel(new GridBagLayout());
		trPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel blPanel = new JPanel(new GridBagLayout());
		blPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		JPanel brPanel = new JPanel(new GridBagLayout());
		brPanel.setBorder(BorderFactory.createTitledBorder("Tiler"));
		JPanel bPanel = new JPanel(new GridBagLayout());

		leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tlPanel, blPanel);
		rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, trPanel, brPanel);
		topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSplit, rightSplit);
		logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topSplit, bPanel);

		// add scroll pane to options
		panel = new JPanel(new GridBagLayout());
		scrollPane =
				new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		trPanel.add(scrollPane, c);
		trPanel = panel;

		frame.add(logSplit, BorderLayout.CENTER);

		textLog = new JTextPane();
		textLog.setEditable(false);
		scrollPane =
				new JScrollPane(textLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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
		createLoggerPopupMenu();

		// TOP LEFT

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tlPanel.add(panel, c);

		label = new JLabel("Mode:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, 0, SPACING);
		panel.add(label, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				datasetModeChanged();
			}
		};

		gdalRadio = new JRadioButton("GDAL dataset (raster)");
		gdalRadio.setSelected(true);
		datasetSelected = gdalRadio;
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(gdalRadio, c);
		gdalRadio.addActionListener(al);

		mapnikRadio = new JRadioButton("Mapnik map (vector)");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(mapnikRadio, c);
		mapnikRadio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(gdalRadio);
		bg.add(mapnikRadio);

		sep = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING * 2, 0, SPACING * 2, 0);
		tlPanel.add(sep, c);

		dataModeCards = new JPanel(new CardLayout());
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTH;
		tlPanel.add(dataModeCards, c);

		JPanel gdalCard = new JPanel(new GridBagLayout());
		dataModeCards.add(gdalCard, gdalRadio.getText());

		JPanel mapnikCard = new JPanel(new GridBagLayout());
		dataModeCards.add(mapnikCard, mapnikRadio.getText());

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gdalCard.add(panel, c);

		label = new JLabel("Dataset:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);

		gdalFileField = new JTextField();
		gdalFileField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(gdalFileField, c);

		browseGdalButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(browseGdalButton, c);
		browseGdalButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupInputFile();
			}
		});

		infoText = new JTextArea();
		infoText.setBackground(label.getBackground());
		infoText.setEditable(false);
		Font font = Font.decode(null);
		infoText.setFont(font);
		scrollPane =
				new JScrollPane(infoText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		size = new Dimension(20, 20);
		scrollPane.setMinimumSize(size);
		scrollPane.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		gdalCard.add(scrollPane, c);


		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mapnikCard.add(panel, c);

		label = new JLabel("Python binary:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);

		pythonBinaryField = new JTextField();
		pythonBinaryField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(pythonBinaryField, c);

		browsePythonBinaryButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(browsePythonBinaryButton, c);
		browsePythonBinaryButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupPythonBinary();
			}
		});

		label = new JLabel("nik2img.py script:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);

		mapnikScriptField = new JTextField();
		mapnikScriptField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(mapnikScriptField, c);

		browseMapnikScriptButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(browseMapnikScriptButton, c);
		browseMapnikScriptButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupMapnikScript();
			}
		});

		label = new JLabel("Map file:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, 0, SPACING);
		panel.add(label, c);

		mapnikFileField = new JTextField();
		mapnikFileField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(mapnikFileField, c);

		browseMapnikButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 2;
		panel.add(browseMapnikButton, c);
		browseMapnikButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupInputFile();
			}
		});

		sep = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING * 2, 0, SPACING * 2, 0);
		mapnikCard.add(sep, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		mapnikCard.add(panel, c);

		label = new JLabel("Min latitude:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);
		labels.add(label);

		minLatitudeField = new JDoubleField(-90d);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(minLatitudeField, c);

		label = new JLabel("Min longitude:");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, SPACING, SPACING, SPACING);
		panel.add(label, c);
		labels.add(label);

		minLongitudeField = new JDoubleField(-180d);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(minLongitudeField, c);

		label = new JLabel("Max latitude:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);
		labels.add(label);

		maxLatitudeField = new JDoubleField(90d);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(maxLatitudeField, c);

		label = new JLabel("Max longitude:");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, SPACING, SPACING, SPACING);
		panel.add(label, c);
		labels.add(label);

		maxLongitudeField = new JDoubleField(180d);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(maxLongitudeField, c);

		// BOTTOM LEFT

		previewCanvas = new JLabel();
		previewCanvas.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane =
				new JScrollPane(previewCanvas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		previewCanvas.setBackground(Color.white);
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		blPanel.add(scrollPane, c);

		// TOP RIGHT

		int row = 0;

		tileTypeLabel = new JLabel("Tile type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(tileTypeLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tileTypeChanged();
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

		label = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(label, c);
		labels.add(label);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		tilesizeField = new JIntegerField(512);
		tilesizeField.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
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
		tilesizeField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				tilesizeChanged();
			}
		});

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, SPACING / 2, 0, 0);
		panel.add(label, c);
		labels.add(label);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(label, c);
		labels.add(label);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
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
		c.gridx = 1;
		c.insets = new Insets(0, SPACING / 2, 0, 0);
		panel.add(label, c);
		labels.add(label);

		levelsLabel = new JLabel("Level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(levelsLabel, c);

		levelsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(levelsSpinner, c);

		imageFormatLabel = new JLabel("Image format:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(imageFormatLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

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
		c.gridx = 0;
		panel.add(jpegRadio, c);
		jpegRadio.addActionListener(al);

		pngRadio = new JRadioButton("PNG");
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(pngRadio, c);
		pngRadio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(jpegRadio);
		bg.add(pngRadio);

		alphaCheck = new JCheckBox("Add alpha channel");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(alphaCheck, c);
		alphaCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				alphaSet = alphaCheck.isSelected();
				bandCountChanged();
			}
		});

		cellTypeLabel = new JLabel("Output cell type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(cellTypeLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				elevationFormatChanged();
			}
		};

		byteRadio = new JRadioButton("Byte");
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(byteRadio, c);
		byteRadio.addActionListener(al);

		int16Radio = new JRadioButton("16-bit integer");
		int16Radio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(int16Radio, c);
		int16Radio.addActionListener(al);

		int32Radio = new JRadioButton("32-bit integer");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(int32Radio, c);
		int32Radio.addActionListener(al);

		float32Radio = new JRadioButton("32-bit float");
		c = new GridBagConstraints();
		c.gridx = 3;
		panel.add(float32Radio, c);
		float32Radio.addActionListener(al);

		bg = new ButtonGroup();
		bg.add(byteRadio);
		bg.add(int16Radio);
		bg.add(int32Radio);
		bg.add(float32Radio);

		bandLabel = new JLabel("Band:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(bandLabel, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(panel, c);

		bandCombo = new JComboBox(new Integer[] { 1 });
		bandCombo.setSelectedIndex(0);
		c = new GridBagConstraints();
		c.gridx = 0;
		panel.add(bandCombo, c);

		overviewsCheck = new JCheckBox("Generate overviews");
		overviewsCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(overviewsCheck, c);

		reprojectCheck = new JCheckBox("Reproject if required");
		reprojectCheck.setSelected(false);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(reprojectCheck, c);

		bilinearCheck = new JCheckBox("Use bilinear magnification if required");
		bilinearCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(bilinearCheck, c);

		bilinearOverviewsCheck = new JCheckBox("Use bilinear minification in overviews");
		bilinearOverviewsCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(bilinearOverviewsCheck, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(panel, c);

		overrideLevelsCheck = new JCheckBox("Override level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(overrideLevelsCheck, c);
		overrideLevelsCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (mapnikRadio.isSelected() && !overrideLevelsCheck.isSelected())
				{
					overrideLevelsCheck.setSelected(true);
				}
				enableFields();
			}
		});

		overrideLevelsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(overrideLevelsSpinner, c);

		outsideCheck = new JCheckBox("");
		outsideCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
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
		c.gridx = 1;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(outsidePanel, c);

		ignoreBlankCheck = new JCheckBox("Ignore blank tiles");
		ignoreBlankCheck.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(ignoreBlankCheck, c);

		replaceCheck = new JCheckBox("Set pixels with values:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(replaceCheck, c);
		replaceCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});

		replace1Label = new JLabel("between:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace1Label, c);

		minPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(minPanel, c);

		replace2Label = new JLabel("and:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace2Label, c);

		maxPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(maxPanel, c);

		replace3Label = new JLabel("or between:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace3Label, c);

		min2Panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(min2Panel, c);

		replace4Label = new JLabel("and:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace4Label, c);

		max2Panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(max2Panel, c);

		replace5Label = new JLabel("set to:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace5Label, c);

		withPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(withPanel, c);

		replace6Label = new JLabel("otherwise:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		trPanel.add(replace6Label, c);

		otherwisePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, SPACING, 0);
		trPanel.add(otherwisePanel, c);

		panel = new JPanel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.weighty = 1;
		trPanel.add(panel, c);

		// BOTTOM RIGHT

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		brPanel.add(panel, c);

		label = new JLabel("Output directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, SPACING, SPACING);
		panel.add(label, c);

		outputDirectory = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(outputDirectory, c);

		outputDirectory.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			public void anyChange(DocumentEvent e)
			{
				String text = outputDirectory.getText();
				File dir = new File(text);
				if (dir.isDirectory())
				{
					preferences.put(OUTPUT_DIR_KEY, text);
				}
			}
		});

		outputButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.insets = new Insets(0, 0, SPACING, 0);
		panel.add(outputButton, c);
		outputButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(preferences.get(OUTPUT_DIR_KEY, null));
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					File dir = chooser.getSelectedFile();
					outputDirectory.setText(dir.getAbsolutePath());
					preferences.put(OUTPUT_DIR_KEY, dir.getAbsolutePath());
				}
			}
		});

		tileText = new JTextArea();
		tileText.setBackground(label.getBackground());
		tileText.setEditable(false);
		tileText.setFont(font);
		scrollPane =
				new JScrollPane(tileText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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
		c.insets = new Insets(0, 0, SPACING, 0);
		brPanel.add(scrollPane, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		brPanel.add(panel, c);

		tileButton = new JButton("Generate tiles");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, 0, SPACING);
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

		JButton previewButton = new JButton("Preview generated tileset");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(previewButton, c);
		previewButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				preview();
			}
		});

		updatePythonFields();
		addRecalculateListeners();
		openDataset(null);
		datasetModeChanged();

		logger.info("Started");
		if (!GDALUtil.isProjectionsSupported())
		{
			logger.warning("Running without projection support");
		}

		loadFrameBounds();
		loadSplitLocations();
		frame.setVisible(true);
	}

	private void preview()
	{
		boolean elevations = elevationRadio.isSelected();
		String extension = elevations ? "bil" : getImageFormat();
		int type = getElevationBufferType();
		Double nodata = null;
		if (outsideCheck.isSelected() && outsideFields != null && outsideFields.length > 0)
		{
			if (isFloat())
				nodata = ((JDoubleField) outsideFields[0]).getValue();
			else
			{
				Long l = ((JLongField) outsideFields[0]).getValue();
				if (l != null)
					nodata = l.doubleValue();
			}
		}
		new PreviewSetup(frame, outputDirectory.getText(), extension, elevations, type,
				tilesizeField.getValue(), nodata, lztsField.getValue());
	}

	private void loadSplitLocations()
	{
		logSplit.setResizeWeight(0.7);
		leftSplit.setResizeWeight(0.8);
		rightSplit.setResizeWeight(0.0);
		topSplit.setResizeWeight(0.5);

		final String preferenceKey = "Split Locations";
		String splitLocations = preferences.get(preferenceKey, null);
		int width = frame.getWidth();
		int height = frame.getHeight();
		int logSplitL = height * 3 / 4;
		int topSplitL = width / 2;
		int leftSplitL = logSplitL / 2;
		int rightSplitL = rightSplit.getTopComponent().getPreferredSize().height + 10;
		try
		{
			String[] split = splitLocations.split(",");
			if (split.length == 4)
			{
				logSplitL = Integer.parseInt(split[0]);
				topSplitL = Integer.parseInt(split[1]);
				leftSplitL = Integer.parseInt(split[2]);
				rightSplitL = Integer.parseInt(split[3]);
			}
		}
		catch (Exception e)
		{
		}

		logSplit.setDividerLocation(logSplitL);
		topSplit.setDividerLocation(topSplitL);
		leftSplit.setDividerLocation(leftSplitL);
		rightSplit.setDividerLocation(rightSplitL);

		saveSplitLocations = new Runnable()
		{
			public void run()
			{
				String value =
						logSplit.getDividerLocation() + "," + topSplit.getDividerLocation() + ","
								+ leftSplit.getDividerLocation() + ","
								+ rightSplit.getDividerLocation();
				preferences.put(preferenceKey, value);
			}
		};
	}

	private void loadFrameBounds()
	{
		final String preferenceKey = "Frame State";
		String frameState = preferences.get(preferenceKey, null);
		int width = 800;
		int height = 600;
		boolean maximized = false;
		Integer x = null;
		Integer y = null;
		try
		{
			String[] split = frameState.split(",");
			if (split.length == 5)
			{
				width = Integer.parseInt(split[0]);
				height = Integer.parseInt(split[1]);
				x = Integer.parseInt(split[2]);
				y = Integer.parseInt(split[3]);
				maximized = Boolean.parseBoolean(split[4]);
			}
		}
		catch (Exception e)
		{
		}

		frame.setSize(width, height);
		if (x == null || y == null)
		{
			frame.setLocationRelativeTo(null);
		}
		else
		{
			frame.setLocation(x, y);
		}
		if (maximized)
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		final Runnable windowChanger = new Runnable()
		{
			public void run()
			{
				int width = frame.getWidth();
				int height = frame.getHeight();
				Point location = frame.getLocation();
				int x = location.x;
				int y = location.y;
				boolean maximized =
						(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
				String value = width + "," + height + "," + x + "," + y + "," + maximized;
				preferences.put(preferenceKey, value);
			}
		};

		frame.addWindowStateListener(new WindowStateListener()
		{
			public void windowStateChanged(WindowEvent e)
			{
				windowChanger.run();
			}
		});

		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				windowChanger.run();
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				windowChanger.run();
			}
		});
	}

	private void onExit()
	{
		if (saveSplitLocations != null)
		{
			saveSplitLocations.run();
		}

		try
		{
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
	}

	private void createLoggerPopupMenu()
	{
		final String preferenceKey = "Logger Level";
		Level prefLevel = Level.INFO;
		try
		{
			String prefLevelStr = preferences.get(preferenceKey, prefLevel.getName());
			prefLevel = Level.parse(prefLevelStr);
		}
		catch (Exception e)
		{
		}
		logger.setLevel(prefLevel);

		loggerPopup = new JPopupMenu();
		Level[] levels =
				new Level[] { Level.FINEST, Level.FINER, Level.FINE, Level.INFO, Level.WARNING,
						Level.SEVERE };

		ButtonGroup bg = new ButtonGroup();
		for (final Level level : levels)
		{
			JRadioButtonMenuItem menuItem =
					new JRadioButtonMenuItem(new AbstractAction(level.getName())
					{
						public void actionPerformed(ActionEvent e)
						{
							logger.setLevel(level);
							preferences.put(preferenceKey, level.getName());
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
		overrideLevelsSpinner.addChangeListener(cl);
		overrideLevelsCheck.addActionListener(al);
		levelsSpinner.addChangeListener(cl);
		minLatitudeField.getDocument().addDocumentListener(dl);
		minLongitudeField.getDocument().addDocumentListener(dl);
		maxLatitudeField.getDocument().addDocumentListener(dl);
		maxLongitudeField.getDocument().addDocumentListener(dl);

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
		previewGenerated = false;

		if (!fileOpen)
		{
			// no file open
			infoText.setText("");
			gdalFileField.setText("");
			mapnikFileField.setText("");
			previewCanvas.setIcon(null);

			bandCount = 0;
			dataset = null;
			sector = null;
			mapFile = null;
		}
		else
		{
			try
			{
				gdalFileField.setText(file.getAbsolutePath());
				mapnikFileField.setText(file.getAbsolutePath());
				this.mapFile = file;

				if (gdalRadio.isSelected())
				{
					final Dataset dataset = GDALUtil.open(file);
					final Sector sector = GDALUtil.getSector(dataset);
					this.sector = sector;
					this.dataset = dataset;
					final int width = dataset.getRasterXSize();
					final int height = dataset.getRasterYSize();
					if (sector.getMinLongitude() == 0 && sector.getMinLatitude() == 0
							&& sector.getMaxLongitude() == width
							&& sector.getMaxLatitude() == height)
					{
						throw new TilerException("Dataset " + file + " is not geo-referenced");
					}
					if (sector.getMinLatitude() < -180 || sector.getMaxLatitude() > 180
							|| sector.getMinLongitude() < -360 || sector.getMaxLongitude() > 360
							|| Math.abs(sector.getDeltaLatitude()) > 180
							|| Math.abs(sector.getDeltaLongitude()) > 360)
					{
						throw new TilerException("Dataset sector " + sector
								+ " is invalid; only lat/lon coordinate systems are supported");
					}
					bandCount = dataset.getRasterCount();
					if (bandCount == 0)
					{
						throw new TilerException("Dataset contains 0 raster bands");
					}

					String text = GDALUtil.getInfoText(dataset, sector);
					infoText.setText(text);
					infoText.select(0, 0);

					Thread thread = new Thread(new Runnable()
					{
						public void run()
						{
							logger.fine("Generating preview...");
							try
							{
								BufferedImage image =
										new BufferedImage(150, 50, BufferedImage.TYPE_INT_ARGB);
								Graphics2D g = null;
								try
								{
									g = image.createGraphics();
									g.setColor(Color.black);
									String s = "LOADING...";
									Font font = new Font("Dialog", Font.BOLD, 18);
									g.setFont(font);
									int width = g.getFontMetrics().stringWidth(s);
									int height = g.getFontMetrics().getAscent();
									g.drawString(s, (image.getWidth() - width) / 2,
											(image.getHeight() + height) / 2);
								}
								finally
								{
									if (g != null)
										g.dispose();
								}
								ImageIcon icon = new ImageIcon(image);
								previewCanvas.setIcon(icon);

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
								GDALTileParameters parameters =
										new GDALTileParameters(dataset, new Dimension(w, h), sector);
								GDALTile tile = new GDALTile(parameters);
								tile = tile.convertToType(gdalconst.GDT_Byte);
								image = tile.getAsImage();
								icon = new ImageIcon(image);
								previewCanvas.setIcon(icon);
								logger.fine("Preview generation complete");
							}
							catch (Exception e)
							{
								logger.warning(e.getMessage());
								previewCanvas.setIcon(null);
							}
							previewGenerated = true;
							enableFields();
						}
					});
					thread.setDaemon(true);
					thread.start();
				}
				else
				{
					infoText.setText("");
					this.bandCount = 0;

					Sector sect = MapnikUtil.getSector(mapFile);
					if (sect == null)
					{
						sect = Sector.FULL_SPHERE;
						logger.warning("Could not read map sector, setting sector to full globe");
					}

					this.sector = sect;
					minLatitudeField.setValue(sect.getMinLatitude());
					minLongitudeField.setValue(sect.getMinLongitude());
					maxLatitudeField.setValue(sect.getMaxLatitude());
					maxLongitudeField.setValue(sect.getMaxLongitude());

					Thread thread = new Thread(new Runnable()
					{
						public void run()
						{
							logger.fine("Generating preview...");
							try
							{
								File dst = File.createTempFile("preview", ".png");
								dst.deleteOnExit();
								MapnikUtil.tile(sector, previewCanvas.getWidth(),
										previewCanvas.getHeight(), false, mapFile, dst, logger);
								BufferedImage image = ImageIO.read(dst);
								ImageIcon icon = new ImageIcon(image);
								previewCanvas.setIcon(icon);
								logger.fine("Preview generation complete");
							}
							catch (Exception e)
							{
								logger.warning(e.toString());
							}
							previewGenerated = true;
							enableFields();
						}
					});
					thread.setDaemon(true);
					thread.start();
				}
				logger.info("File " + file + " opened");
			}
			catch (Exception e)
			{
				logger.severe(e.getMessage());
				openDataset(null);
				return;
			}
		}

		bandCountChanged();
		tileTypeChanged();
		imageFormatChanged();
		recalculateTiles();

		frame.doLayout();
	}

	private void datasetModeChanged()
	{
		JRadioButton newDatasetSelected = gdalRadio.isSelected() ? gdalRadio : mapnikRadio;
		if (datasetSelected != newDatasetSelected)
		{
			int value = JOptionPane.YES_OPTION;
			if (fileOpen)
			{
				value =
						JOptionPane
								.showConfirmDialog(
										frame,
										"The current file will be closed.\nAre you sure you want to change the mode?",
										"Mode change", JOptionPane.YES_NO_OPTION,
										JOptionPane.INFORMATION_MESSAGE);
			}
			if (value == JOptionPane.YES_OPTION)
			{
				datasetSelected = newDatasetSelected;
				openDataset(null);
			}
		}

		boolean mapnik = mapnikRadio.isSelected();
		if (mapnik)
		{
			imageRadio.setSelected(true);
			replaceCheck.setSelected(false);
			outsideCheck.setSelected(false);
			overrideLevelsCheck.setSelected(false);
		}

		CardLayout cl = (CardLayout) dataModeCards.getLayout();
		datasetSelected.setSelected(true);
		cl.show(dataModeCards, datasetSelected.getText());

		enableFields();
	}

	private void tileTypeChanged()
	{
		/*CardLayout cl = (CardLayout) imageCards.getLayout();
		if (elevationRadio.isSelected())
		{
			cl.show(imageCards, elevationRadio.getText());
		}
		else
		{
			cl.show(imageCards, imageRadio.getText());
		}*/

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

		bandCountChanged();
		tilesizeChanged();
	}

	private void imageFormatChanged()
	{
		alphaCheck.setSelected(pngRadio.isSelected() && alphaSet && bandCount != 4);
		bandCountChanged();
		enableFields();
	}

	private void elevationFormatChanged()
	{
		bandCountChanged();
		enableFields();
	}

	private void tilesizeChanged()
	{
		if (elevationRadio.isSelected() && tilesizeField.getValue() % 2 != 0)
		{
			JOptionPane.showMessageDialog(frame, "Tile size must be a multiple of 2",
					"Invalid tile size", JOptionPane.INFORMATION_MESSAGE);
			tilesizeField.setValue(tilesizeField.getValue() + 1);
		}
	}

	private boolean isFloat()
	{
		return elevationRadio.isSelected() && float32Radio.isSelected();
	}

	private void bandCountChanged()
	{
		GridBagConstraints c;

		// save old values
		Object[] outsideBackup = readNumberFields(outsideFields);
		Object[] minBackup = readNumberFields(minFields);
		Object[] maxBackup = readNumberFields(maxFields);
		Object[] min2Backup = readNumberFields(min2Fields);
		Object[] max2Backup = readNumberFields(max2Fields);
		Object[] replaceBackup = readNumberFields(replaceFields);
		Object[] otherwiseBackup = readNumberFields(otherwiseFields);

		FocusListener fl = new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (e.getSource() instanceof JLongField)
				{
					JLongField jif = (JLongField) e.getSource();
					if (jif.getValue() == null)
						jif.setValue(0l);
				}
				else if (e.getSource() instanceof JDoubleField)
				{
					JDoubleField jdf = (JDoubleField) e.getSource();
					if (jdf.getValue() == null)
						jdf.setValue(0d);
				}
			}
		};

		outsidePanel.removeAll();
		minPanel.removeAll();
		maxPanel.removeAll();
		min2Panel.removeAll();
		max2Panel.removeAll();
		withPanel.removeAll();
		otherwisePanel.removeAll();
		while (bandCombo.getItemCount() > 0)
		{
			bandCombo.removeItemAt(0);
		}
		outputBandCount =
				bandCount <= 0 ? 1 : elevationRadio.isSelected() ? 1 : bandCount == 3
						&& alphaCheck.isSelected() ? 4 : bandCount;
		boolean isFloat = isFloat();
		outsideFields = new JTextField[outputBandCount];
		minFields = new JTextField[outputBandCount];
		maxFields = new JTextField[outputBandCount];
		min2Fields = new JTextField[outputBandCount];
		max2Fields = new JTextField[outputBandCount];
		replaceFields = new JTextField[outputBandCount];
		otherwiseFields = new JTextField[outputBandCount];
		for (int i = 0; i < outputBandCount; i++)
		{
			if (isFloat)
				outsideFields[i] = new JDoubleField(0d);
			else
				outsideFields[i] = new JLongField(0l);
			Dimension size = outsideFields[i].getPreferredSize();
			size.width = 50;
			outsideFields[i].setMinimumSize(size);
			outsideFields[i].setPreferredSize(size);
			outsideFields[i].addFocusListener(fl);
			c = new GridBagConstraints();
			c.gridx = i;
			outsidePanel.add(outsideFields[i], c);

			if (isFloat)
				minFields[i] = new JDoubleField(null);
			else
				minFields[i] = new JLongField(null);
			minFields[i].setMinimumSize(size);
			minFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			minPanel.add(minFields[i], c);

			if (isFloat)
				maxFields[i] = new JDoubleField(null);
			else
				maxFields[i] = new JLongField(null);
			maxFields[i].setMinimumSize(size);
			maxFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			maxPanel.add(maxFields[i], c);

			if (isFloat)
				min2Fields[i] = new JDoubleField(null);
			else
				min2Fields[i] = new JLongField(null);
			min2Fields[i].setMinimumSize(size);
			min2Fields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			min2Panel.add(min2Fields[i], c);

			if (isFloat)
				max2Fields[i] = new JDoubleField(null);
			else
				max2Fields[i] = new JLongField(null);
			max2Fields[i].setMinimumSize(size);
			max2Fields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			max2Panel.add(max2Fields[i], c);

			if (isFloat)
				replaceFields[i] = new JDoubleField(null);
			else
				replaceFields[i] = new JLongField(null);
			replaceFields[i].setMinimumSize(size);
			replaceFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			withPanel.add(replaceFields[i], c);

			if (isFloat)
				otherwiseFields[i] = new JDoubleField(null);
			else
				otherwiseFields[i] = new JLongField(null);
			otherwiseFields[i].setMinimumSize(size);
			otherwiseFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			otherwisePanel.add(otherwiseFields[i], c);

			bandCombo.addItem(new Integer(i + 1));
		}

		// restore values
		writeNumberFields(outsideFields, outsideBackup);
		writeNumberFields(minFields, minBackup);
		writeNumberFields(maxFields, maxBackup);
		writeNumberFields(min2Fields, min2Backup);
		writeNumberFields(max2Fields, max2Backup);
		writeNumberFields(replaceFields, replaceBackup);
		writeNumberFields(otherwiseFields, otherwiseBackup);

		enableFields();
	}

	private Object[] readNumberFields(JTextField[] fields)
	{
		Object[] values = new Object[fields.length];
		for (int i = 0; i < fields.length; i++)
		{
			if (fields[i] instanceof JLongField)
				values[i] = ((JLongField) fields[i]).getValue();
			else if (fields[i] instanceof JDoubleField)
				values[i] = ((JDoubleField) fields[i]).getValue();
		}
		return values;
	}

	private void writeNumberFields(JTextField[] fields, Object[] values)
	{
		for (int i = 0; i < fields.length && i < values.length; i++)
		{
			if (fields[i] instanceof JLongField)
			{
				Long value =
						values[i] == null ? null : values[i] instanceof Long ? (Long) values[i]
								: values[i] instanceof Double ? ((Double) values[i]).longValue()
										: null;
				((JLongField) fields[i]).setValue(value);
			}
			else if (fields[i] instanceof JDoubleField)
			{
				Double value =
						values[i] == null ? null : values[i] instanceof Double ? (Double) values[i]
								: values[i] instanceof Long ? ((Long) values[i]).doubleValue()
										: null;
				((JDoubleField) fields[i]).setValue(value);
			}
		}
	}

	private void enableFields()
	{
		boolean standard = fileOpen && !running;
		boolean mapnik = mapnikRadio.isSelected();

		byteRadio.setEnabled(standard);
		elevationRadio.setEnabled(standard && !mapnik);
		imageRadio.setEnabled(standard);
		// infoText.setEnabled(standard);
		// tileText.setEnabled(standard);
		int16Radio.setEnabled(standard);
		int32Radio.setEnabled(standard);
		float32Radio.setEnabled(standard);
		jpegRadio.setEnabled(standard);
		lztsField.setEnabled(standard);
		outsideCheck.setEnabled(standard && !mapnik);
		overviewsCheck.setEnabled(standard);
		reprojectCheck.setEnabled(standard && !mapnik);
		bilinearCheck.setEnabled(standard && !mapnik);
		bilinearOverviewsCheck.setEnabled(standard && !mapnik);
		overrideLevelsCheck.setEnabled(standard && !mapnik);
		pngRadio.setEnabled(standard);
		tilesizeField.setEnabled(standard);
		for (JComponent label : labels)
		{
			label.setEnabled(standard);
		}
		tileTypeLabel.setEnabled(standard);
		imageFormatLabel.setEnabled(standard);
		cellTypeLabel.setEnabled(standard);
		bandLabel.setEnabled(standard);

		levelsLabel.setEnabled(standard && mapnik);
		levelsSpinner.setEnabled(standard && mapnik);
		minLatitudeField.setEnabled(standard);
		maxLatitudeField.setEnabled(standard);
		minLongitudeField.setEnabled(standard);
		maxLongitudeField.setEnabled(standard);

		alphaCheck.setEnabled(pngRadio.isSelected() && standard && bandCount == 3 && !mapnik
				&& !elevationRadio.isSelected());
		bandCombo.setEnabled(bandCount > 1 && standard);
		overrideLevelsSpinner.setEnabled(overrideLevelsCheck.isSelected() && standard);

		for (JTextField field : outsideFields)
		{
			field.setEnabled(outsideCheck.isSelected() && standard);
		}
		ignoreBlankCheck.setEnabled(outsideCheck.isSelected() && standard);

		if (!elevationRadio.isSelected())
		{
			outsideCheck.setText("Set pixels outside dataset extents to:");
		}
		else
		{
			outsideCheck.setText("Use elevation NODATA value (using output cell type):");
		}

		replaceCheck.setEnabled(standard && !mapnik);
		replace1Label.setEnabled(replaceCheck.isSelected() && standard);
		replace2Label.setEnabled(replaceCheck.isSelected() && standard);
		replace3Label.setEnabled(replaceCheck.isSelected() && standard);
		replace4Label.setEnabled(replaceCheck.isSelected() && standard);
		replace5Label.setEnabled(replaceCheck.isSelected() && standard);
		replace6Label.setEnabled(replaceCheck.isSelected() && standard);
		for (JTextField field : minFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JTextField field : maxFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JTextField field : min2Fields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JTextField field : max2Fields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JTextField field : replaceFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}
		for (JTextField field : otherwiseFields)
		{
			field.setEnabled(replaceCheck.isSelected() && standard);
		}

		gdalRadio.setEnabled(!running);
		mapnikRadio.setEnabled(!running);
		browseGdalButton.setEnabled(!running);
		browseMapnikButton.setEnabled(!running);
		browseMapnikScriptButton.setEnabled(!running);
		browsePythonBinaryButton.setEnabled(!running);
		outputButton.setEnabled(!running);
		outputDirectory.setEnabled(!running);
		tileButton.setEnabled(validOptions && previewGenerated
				&& outputDirectory.getText().length() != 0 && !running);
		cancelButton.setEnabled(running);
		progress.setEnabled(running);

		visibledFields();
	}

	private void visibledFields()
	{
		boolean mapnik = mapnikRadio.isSelected();

		tileTypeLabel.setVisible(!mapnik);
		imageRadio.setVisible(!mapnik);
		elevationRadio.setVisible(!mapnik);

		boolean images = mapnik || imageRadio.isSelected();
		imageFormatLabel.setVisible(images);
		jpegRadio.setVisible(images);
		pngRadio.setVisible(images);
		ignoreBlankCheck.setVisible(images);

		alphaCheck.setVisible(pngRadio.isSelected() && bandCount == 3 && !mapnik
				&& !elevationRadio.isSelected());

		boolean elevations = elevationRadio.isSelected() && !mapnik;
		bandLabel.setVisible(elevations);
		bandCombo.setVisible(elevations);
		cellTypeLabel.setVisible(elevations);
		byteRadio.setVisible(elevations);
		int16Radio.setVisible(elevations);
		int32Radio.setVisible(elevations);
		float32Radio.setVisible(elevations);

		levelsLabel.setVisible(mapnik);
		levelsSpinner.setVisible(mapnik);
		overrideLevelsCheck.setVisible(!mapnik);
		overrideLevelsSpinner.setVisible(!mapnik);
		reprojectCheck.setVisible(!mapnik);
		bilinearCheck.setVisible(!mapnik);
		bilinearOverviewsCheck.setVisible(!mapnik);
		outsideCheck.setVisible(!mapnik);
		outsidePanel.setVisible(!mapnik);
		replaceCheck.setVisible(!mapnik);
		replace1Label.setVisible(!mapnik);
		replace2Label.setVisible(!mapnik);
		replace3Label.setVisible(!mapnik);
		replace4Label.setVisible(!mapnik);
		replace5Label.setVisible(!mapnik);
		replace6Label.setVisible(!mapnik);
		minPanel.setVisible(!mapnik);
		maxPanel.setVisible(!mapnik);
		min2Panel.setVisible(!mapnik);
		max2Panel.setVisible(!mapnik);
		withPanel.setVisible(!mapnik);
		otherwisePanel.setVisible(!mapnik);
	}

	private void recalculateTiles()
	{
		validOptions = fileOpen;

		if (mapnikRadio.isSelected())
		{
			Double minlat = minLatitudeField.getValue();
			Double minlon = minLongitudeField.getValue();
			Double maxlat = maxLatitudeField.getValue();
			Double maxlon = maxLongitudeField.getValue();
			if (minlat == null || minlon == null || maxlat == null || maxlon == null
					|| minlat >= maxlat || minlon >= maxlon)
			{
				validOptions = false;
			}
			else
			{
				sector = new Sector(minlat, minlon, maxlat, maxlon);
			}
		}

		boolean overviews = overviewsCheck.isSelected();
		Double lzts = lztsField.getValue();
		Integer tilesize = tilesizeField.getValue();
		if (tilesize != null && tilesize <= 0)
			tilesize = null;
		if (lzts != null && lzts <= 0)
			lzts = null;

		if (validOptions)
		{
			validOptions = lzts != null && tilesize != null;
			if (validOptions)
			{
				if (overrideLevelsCheck.isSelected())
				{
					levels = (Integer) overrideLevelsSpinner.getModel().getValue();
				}
				else
				{
					if (gdalRadio.isSelected())
					{
						levels = Util.levelCount(dataset, lzts, sector, tilesize);
					}
					else
					{
						levels = (Integer) levelsSpinner.getModel().getValue();
					}
					overrideLevelsSpinner.getModel().setValue(levels);
				}
			}
		}

		if (!validOptions)
		{
			tileText.setText("Invalid dataset or options");
		}
		else
		{
			String outDirText = outputDirectory.getText();
			File outDir = outDirText.length() == 0 ? null : new File(outDirText);

			String info = GDALUtil.getTileText(dataset, sector, lzts, levels, overviews);
			if (outDir == null)
			{
				info += System.getProperty("line.separator") + "Please select an output directory";
			}
			else
			{
				info +=
						System.getProperty("line.separator") + "Tiles will be saved to "
								+ outDir.getAbsolutePath();
			}
			tileText.setText(info);
		}
		tileText.select(0, 0);

		enableFields();
	}

	private void setupInputFile()
	{
		String preferenceKey = "Last Input Directory";
		JFileChooser chooser = new JFileChooser(preferences.get(preferenceKey, null));
		if (gdalRadio.isSelected())
		{
			chooser.setFileFilter(new GDALFileFilter());
		}
		else
		{
			chooser.setFileFilter(new XMLFileFilter());
		}
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			preferences.put(preferenceKey, file.getParentFile().getAbsolutePath());
			openDataset(file);
		}
	}

	private void setupPythonBinary()
	{
		String pb = MapnikUtil.getPythonBinary();
		JFileChooser chooser = new JFileChooser(pb);
		chooser.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().equalsIgnoreCase("python.exe");
			}

			@Override
			public String getDescription()
			{
				return "python.exe";
			}
		});
		chooser.setDialogTitle("Select python binary...");
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			MapnikUtil.setPythonBinary(file.getAbsolutePath());
		}
		updatePythonFields();
	}

	private void setupMapnikScript()
	{
		String ms = MapnikUtil.getMapnikScript();
		JFileChooser chooser = new JFileChooser(ms);
		chooser.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".py");
			}

			@Override
			public String getDescription()
			{
				return "Python scripts (*.py)";
			}
		});
		chooser.setDialogTitle("Select nik2img script...");
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			MapnikUtil.setMapnikScript(file.getAbsolutePath());
		}
		updatePythonFields();
	}

	private void updatePythonFields()
	{
		String pb = MapnikUtil.getPythonBinary();
		if (pb == null)
			pb = "";
		pythonBinaryField.setText(pb);
		String ms = MapnikUtil.getMapnikScript();
		if (ms == null)
			ms = "";
		mapnikScriptField.setText(ms);
	}

	private void generateTiles()
	{
		final TilerProgressReporter reporter = new TilerProgressReporter(this, logger);
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
				boolean overviews = overviewsCheck.isSelected();
				boolean reproject = reprojectCheck.isSelected();
				boolean bilinear = bilinearCheck.isSelected();
				boolean bilinearOverviews = bilinearOverviewsCheck.isSelected();
				boolean ignoreBlank = ignoreBlankCheck.isSelected();

				LogWriter logWriter = null;
				try
				{
					outDir.mkdirs();
					logWriter = new LogWriter(outDir);

					String imageFormat = getImageFormat();
					if (mapnikRadio.isSelected())
					{
						logWriter.startLog(TilingType.Mapnik, mapFile, outDir, sector, level,
								tilesize, lzts, imageFormat, false, 0, 0, false, false,
								infoText.getText(), tileText.getText(), null, null, null, null,
								false);

						Tiler.tileMapnik(mapFile, sector, level, tilesize, lzts, imageFormat,
								ignoreBlank, outDir, reporter);
						if (overviews && !reporter.isCancelled())
						{
							Overviewer.createImageOverviews(outDir, imageFormat, tilesize,
									tilesize, null, sector, lzts, bilinearOverviews, ignoreBlank,
									reporter);
						}
					}
					else
					{
						boolean isFloat = isFloat();
						NullableNumberArray outsideValues = null;
						MinMaxArray[] minMaxReplaces = null;
						NullableNumberArray replace = null;
						NullableNumberArray otherwise = null;

						if (outsideCheck.isSelected())
						{
							outsideValues = new NullableNumberArray(outputBandCount);
							for (int b = 0; b < outputBandCount; b++)
							{
								if (isFloat)
								{
									outsideValues.setDouble(b,
											((JDoubleField) outsideFields[b]).getValue());
								}
								else
								{
									outsideValues.setLong(b,
											((JLongField) outsideFields[b]).getValue());
								}
							}
						}
						if (replaceCheck.isSelected())
						{
							minMaxReplaces = new MinMaxArray[2];
							minMaxReplaces[0] = new MinMaxArray(outputBandCount);
							minMaxReplaces[1] = new MinMaxArray(outputBandCount);
							replace = new NullableNumberArray(outputBandCount);
							otherwise = new NullableNumberArray(outputBandCount);
							for (int b = 0; b < outputBandCount; b++)
							{
								if (isFloat)
								{
									minMaxReplaces[0].setMinMaxDouble(b,
											((JDoubleField) minFields[b]).getValue(),
											((JDoubleField) maxFields[b]).getValue());
									minMaxReplaces[1].setMinMaxDouble(b,
											((JDoubleField) min2Fields[b]).getValue(),
											((JDoubleField) max2Fields[b]).getValue());
									replace.setDouble(b,
											((JDoubleField) replaceFields[b]).getValue());
									otherwise.setDouble(b,
											((JDoubleField) otherwiseFields[b]).getValue());
								}
								else
								{
									minMaxReplaces[0].setMinMaxLong(b,
											((JLongField) minFields[b]).getValue(),
											((JLongField) maxFields[b]).getValue());
									minMaxReplaces[1].setMinMaxLong(b,
											((JLongField) min2Fields[b]).getValue(),
											((JLongField) max2Fields[b]).getValue());
									replace.setLong(b, ((JLongField) replaceFields[b]).getValue());
									otherwise.setLong(b,
											((JLongField) otherwiseFields[b]).getValue());
								}
							}
						}

						if (imageRadio.isSelected())
						{
							boolean addAlpha = pngRadio.isSelected() && alphaCheck.isSelected();

							logWriter.startLog(TilingType.Images, mapFile, outDir, sector, level,
									tilesize, lzts, imageFormat, addAlpha, 0, 0, bilinear,
									reproject, infoText.getText(), tileText.getText(),
									outsideValues, minMaxReplaces, replace, otherwise, isFloat);

							Tiler.tileImages(dataset, reproject, bilinear, sector, level, tilesize,
									lzts, imageFormat, addAlpha, outsideValues, ignoreBlank,
									minMaxReplaces, replace, otherwise, outDir, reporter);
							if (overviews && !reporter.isCancelled())
							{
								Overviewer.createImageOverviews(outDir, imageFormat, tilesize,
										tilesize, outsideValues, sector, lzts, bilinearOverviews,
										ignoreBlank, reporter);
							}
						}
						else if (elevationRadio.isSelected())
						{
							int bufferType = getElevationBufferType();
							int band = bandCombo.getSelectedIndex();

							logWriter.startLog(TilingType.Elevations, mapFile, outDir, sector,
									level, tilesize, lzts, imageFormat, false, band, bufferType,
									bilinear, reproject, infoText.getText(), tileText.getText(),
									outsideValues, minMaxReplaces, replace, otherwise, isFloat);

							NumberArray minmax = new NumberArray(2);
							if (isFloat)
							{
								minmax.setDouble(0, Double.MAX_VALUE);
								minmax.setDouble(1, -Double.MAX_VALUE);
							}
							else
							{
								minmax.setLong(0, Long.MAX_VALUE);
								minmax.setLong(1, Long.MIN_VALUE);
							}

							Tiler.tileElevations(dataset, reproject, bilinear, sector, level,
									tilesize, lzts, bufferType, band, outsideValues,
									minMaxReplaces, replace, otherwise, minmax, outDir, reporter);

							if (overviews && !reporter.isCancelled())
							{
								Overviewer.createElevationOverviews(outDir, tilesize, tilesize,
										bufferType, ByteOrder.LITTLE_ENDIAN, /*TODO remove hardcoded byteorder*/
										outsideValues, sector, lzts, bilinearOverviews,
										ignoreBlank, reporter);
							}

							if (isFloat)
							{
								reporter.getLogger().info(
										"Elevation bounds: min = " + minmax.getDouble(0)
												+ ", max = " + minmax.getDouble(1));
							}
							else
							{
								reporter.getLogger().info(
										"Elevation bounds: min = " + minmax.getLong(0) + ", max = "
												+ minmax.getLong(1));
							}

							logWriter.logMinMax(minmax, isFloat);
						}
					}

					reporter.done();
				}
				catch (IOException e)
				{
					reporter.getLogger().log(Level.SEVERE, e.getMessage(), e);
				}
				finally
				{
					if (logWriter != null)
						try
						{
							logWriter.finishLog();
						}
						catch (IOException e)
						{
						}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private int getElevationBufferType()
	{
		return byteRadio.isSelected() ? gdalconstConstants.GDT_Byte : int16Radio.isSelected()
				? gdalconstConstants.GDT_Int16 : int32Radio.isSelected()
						? gdalconstConstants.GDT_Int32 : gdalconstConstants.GDT_Float32;
	}

	private String getImageFormat()
	{
		return pngRadio.isSelected() ? "png" : "jpg";
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

	private static class XMLFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f)
		{
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription()
		{
			return "Mapnik mapfile (*.xml)";
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

	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		e.printStackTrace();

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		JOptionPane.showMessageDialog(frame, sw.getBuffer().toString(), "Exception",
				JOptionPane.ERROR_MESSAGE);
	}
}
