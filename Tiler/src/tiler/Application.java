package tiler;

import gdal.GDALTile;
import gdal.GDALUtil;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

import mapnik.MapnikUtil;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import util.DocumentAdapter;
import util.DocumentLogger;
import util.JDoubleField;
import util.JIntegerField;
import util.Prefs;
import util.ProgressReporterImpl;
import util.Sector;
import util.TilerException;

public class Application
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

		GDALUtil.init();
	}

	public final static String LOGGER = "TilerLogger";

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

	private JSpinner levelsSpinner;
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
	private JLabel replace3Label;
	private JLabel replace4Label;
	private JPanel minPanel;
	private JPanel maxPanel;
	private JPanel replacePanel;
	private JPanel otherwisePanel;
	private JIntegerField[] minFields = new JIntegerField[0];
	private JIntegerField[] maxFields = new JIntegerField[0];
	private JIntegerField[] replaceFields = new JIntegerField[0];
	private JIntegerField[] otherwiseFields = new JIntegerField[0];

	private JPanel imageCards;
	private JPanel dataModeCards;

	private JRadioButton jpegRadio;
	private JRadioButton pngRadio;
	private JCheckBox alphaCheck;

	private JRadioButton byteRadio;
	private JRadioButton int16Radio;
	private JRadioButton int32Radio;
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

	public static void main(String[] args)
	{
		new Application();
	}

	public Application()
	{
		if (!GDALUtil.isProjectionsSupported())
		{
			String message = "Before running this program, the GDAL_DATA environment variable should point to the directory containing\n"
					+ "'"
					+ GDALUtil.GCS_FILE
					+ "' (usually GDAL_DIR/data). Without this environment variable, map projections are unsupported.\n\n"
					+ "Do you wish to continue without projection support?";
			int value = JOptionPane.showConfirmDialog(null, message, "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (value != JOptionPane.YES_OPTION)
			{
				return;
			}
		}

		JLabel label;
		GridBagConstraints c;
		JScrollPane scrollPane;
		ButtonGroup bg;
		JPanel panel;
		Dimension size;
		ActionListener al;

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

		leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tlPanel,
				blPanel);
		rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, trPanel,
				brPanel);
		topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSplit,
				rightSplit);
		logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topSplit,
				bPanel);

		frame.add(logSplit, BorderLayout.CENTER);

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
		createLoggerPopupMenu();

		//TOP LEFT

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tlPanel.add(panel, c);

		label = new JLabel("Mode:");
		c = new GridBagConstraints();
		c.gridx = 0;
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
		panel.add(label, c);

		gdalFileField = new JTextField();
		gdalFileField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(gdalFileField, c);

		browseGdalButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
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
		scrollPane = new JScrollPane(infoText,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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
		panel.add(label, c);

		pythonBinaryField = new JTextField();
		pythonBinaryField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(pythonBinaryField, c);

		browsePythonBinaryButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
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
		panel.add(label, c);

		mapnikScriptField = new JTextField();
		mapnikScriptField.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(mapnikScriptField, c);

		browseMapnikScriptButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
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

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		mapnikCard.add(panel, c);

		label = new JLabel("Level count:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);
		labels.add(label);

		levelsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.WEST;
		panel.add(levelsSpinner, c);

		label = new JLabel("Min latitude:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);
		labels.add(label);

		minLatitudeField = new JDoubleField(-90d);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(minLatitudeField, c);

		label = new JLabel("Min longitude:");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);
		labels.add(label);

		minLongitudeField = new JDoubleField(-180d);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 4;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(minLongitudeField, c);

		label = new JLabel("Max latitude:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);
		labels.add(label);

		maxLatitudeField = new JDoubleField(90d);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(maxLatitudeField, c);

		label = new JLabel("Max longitude:");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 5;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);
		labels.add(label);

		maxLongitudeField = new JDoubleField(180d);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(maxLongitudeField, c);

		//BOTTOM LEFT

		previewCanvas = new JLabel();
		previewCanvas.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane = new JScrollPane(previewCanvas,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		previewCanvas.setBackground(Color.white);
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		blPanel.add(scrollPane, c);

		//TOP RIGHT

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

		imageCards = new JPanel(new CardLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		trPanel.add(imageCards, c);

		JPanel imageCard = new JPanel(new GridBagLayout());
		imageCards.add(imageCard, imageRadio.getText());

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
		imageCards.add(elevationCard, elevationRadio.getText());

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
				if (mapnikRadio.isSelected()
						&& !overrideLevelsCheck.isSelected())
				{
					overrideLevelsCheck.setSelected(true);
				}
				enableFields();
			}
		});

		overrideLevelsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100,
				1));
		c = new GridBagConstraints();
		c.gridx = 1;
		panel.add(overrideLevelsSpinner, c);

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

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 8;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1;
		trPanel.add(panel, c);

		replaceCheck = new JCheckBox("Set pixels with values:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		panel.add(replaceCheck, c);
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
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 32, 0, 0);
		panel.add(replace1Label, c);

		minPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(minPanel, c);

		replace2Label = new JLabel("and:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 32, 0, 0);
		panel.add(replace2Label, c);

		maxPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		panel.add(maxPanel, c);

		replace3Label = new JLabel("to:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 32, 0, 0);
		panel.add(replace3Label, c);

		replacePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		panel.add(replacePanel, c);

		replace4Label = new JLabel("otherwise:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, 32, 0, 0);
		panel.add(replace4Label, c);

		otherwisePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		panel.add(otherwisePanel, c);

		//BOTTOM RIGHT

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

		final String outputDirKey = "Last Output Directory";

		outputDirectory = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
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
					preferences.put(outputDirKey, text);
				}
			}
		});

		outputButton = new JButton("...");
		c = new GridBagConstraints();
		c.gridx = 2;
		panel.add(outputButton, c);
		outputButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(preferences.get(
						outputDirKey, null));
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					File dir = chooser.getSelectedFile();
					outputDirectory.setText(dir.getAbsolutePath());
					preferences.put(outputDirKey, dir.getAbsolutePath());
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
				String value = logSplit.getDividerLocation() + ","
						+ topSplit.getDividerLocation() + ","
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
				boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
				String value = width + "," + height + "," + x + "," + y + ","
						+ maximized;
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
		Level prefLevel = Level.FINE;
		try
		{
			String prefLevelStr = preferences.get(preferenceKey, prefLevel
					.getName());
			prefLevel = Level.parse(prefLevelStr);
		}
		catch (Exception e)
		{
		}
		logger.setLevel(prefLevel);

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

		if (!fileOpen)
		{
			//no file open
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

				if (gdalRadio.isSelected())
				{
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
						throw new TilerException(
								"Dataset contains 0 raster bands");
					}
					String projection = dataset.GetProjection();
					SpatialReference spatialReference = (projection == null
							|| projection.length() == 0 || !GDALUtil
							.isProjectionsSupported()) ? null
							: new SpatialReference(projection);
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
					info.append("Top left corner = ("
							+ sector.getMinLongitude() + ", "
							+ sector.getMinLatitude() + ")\n");
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
						info
								.append(spatialReference.ExportToPrettyWkt()
										+ "\n");
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
								GDALTile tile = new GDALTile(dataset, w, h,
										sector.getMinLatitude(), sector
												.getMinLongitude(), sector
												.getMaxLatitude(), sector
												.getMaxLongitude());
								BufferedImage image = tile.getAsImage();
								ImageIcon icon = new ImageIcon(image);
								previewCanvas.setIcon(icon);
								logger.fine("Preview generation complete");
							}
							catch (Exception e)
							{
								logger.warning(e.getMessage());
							}
						}
					});
					thread.setDaemon(true);
					thread.start();
				}
				else
				{
					infoText.setText("");
					this.bandCount = 0;
					this.mapFile = file;

					Sector sect = MapnikUtil.getSector(mapFile);
					if (sect == null)
					{
						sect = Sector.FULL_SPHERE;
						logger
								.warning("Could not read map sector, setting sector to full globe");
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
								File dst = File.createTempFile("preview",
										".png");
								dst.deleteOnExit();
								MapnikUtil.tile(sector, previewCanvas
										.getWidth(), previewCanvas.getHeight(),
										mapFile, dst, logger);
								BufferedImage image = ImageIO.read(dst);
								ImageIcon icon = new ImageIcon(image);
								previewCanvas.setIcon(icon);
								logger.fine("Preview generation complete");
							}
							catch (Exception e)
							{
								logger.warning(e.toString());
							}
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
		JRadioButton newDatasetSelected = gdalRadio.isSelected() ? gdalRadio
				: mapnikRadio;
		if (datasetSelected != newDatasetSelected)
		{
			int value = JOptionPane.YES_OPTION;
			if (fileOpen)
			{
				value = JOptionPane
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
		CardLayout cl = (CardLayout) imageCards.getLayout();
		if (elevationRadio.isSelected())
		{
			cl.show(imageCards, elevationRadio.getText());
		}
		else
		{
			cl.show(imageCards, imageRadio.getText());
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
		Integer[] otherwiseBackup = readIntegerFields(otherwiseFields);

		FocusListener fl = new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				JIntegerField jif = (JIntegerField) e.getSource();
				if (jif.getValue() == null)
					jif.setValue(0);
			}
		};

		outsidePanel.removeAll();
		minPanel.removeAll();
		maxPanel.removeAll();
		replacePanel.removeAll();
		otherwisePanel.removeAll();
		while (bandCombo.getItemCount() > 0)
		{
			bandCombo.removeItemAt(0);
		}
		outputBandCount = bandCount <= 0 ? 0 : bandCount == 3
				&& alphaCheck.isSelected() ? 4 : bandCount;
		outsideFields = new JIntegerField[outputBandCount];
		minFields = new JIntegerField[outputBandCount];
		maxFields = new JIntegerField[outputBandCount];
		replaceFields = new JIntegerField[outputBandCount];
		otherwiseFields = new JIntegerField[outputBandCount];
		for (int i = 0; i < outputBandCount; i++)
		{
			outsideFields[i] = new JIntegerField(0);
			Dimension size = outsideFields[i].getPreferredSize();
			size.width = 50;
			outsideFields[i].setMinimumSize(size);
			outsideFields[i].setPreferredSize(size);
			outsideFields[i].addFocusListener(fl);
			c = new GridBagConstraints();
			c.gridx = i;
			outsidePanel.add(outsideFields[i], c);

			minFields[i] = new JIntegerField(0);
			minFields[i].setMinimumSize(size);
			minFields[i].setPreferredSize(size);
			minFields[i].addFocusListener(fl);
			c = new GridBagConstraints();
			c.gridx = i;
			minPanel.add(minFields[i], c);

			maxFields[i] = new JIntegerField(0);
			maxFields[i].setMinimumSize(size);
			maxFields[i].setPreferredSize(size);
			maxFields[i].addFocusListener(fl);
			c = new GridBagConstraints();
			c.gridx = i;
			maxPanel.add(maxFields[i], c);

			replaceFields[i] = new JIntegerField(null);
			replaceFields[i].setMinimumSize(size);
			replaceFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			replacePanel.add(replaceFields[i], c);

			otherwiseFields[i] = new JIntegerField(null);
			otherwiseFields[i].setMinimumSize(size);
			otherwiseFields[i].setPreferredSize(size);
			c = new GridBagConstraints();
			c.gridx = i;
			otherwisePanel.add(otherwiseFields[i], c);

			bandCombo.addItem(new Integer(i + 1));
		}

		//restore values
		writeIntegerFields(outsideFields, outsideBackup);
		writeIntegerFields(minFields, minBackup);
		writeIntegerFields(maxFields, maxBackup);
		writeIntegerFields(replaceFields, replaceBackup);
		writeIntegerFields(otherwiseFields, otherwiseBackup);

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
		boolean mapnik = mapnikRadio.isSelected();

		byteRadio.setEnabled(standard);
		elevationRadio.setEnabled(standard && !mapnik);
		imageRadio.setEnabled(standard);
		//infoText.setEnabled(standard);
		//tileText.setEnabled(standard);
		int16Radio.setEnabled(standard);
		int32Radio.setEnabled(standard);
		jpegRadio.setEnabled(standard);
		lztsField.setEnabled(standard);
		outsideCheck.setEnabled(standard && !mapnik);
		overviewsCheck.setEnabled(standard);
		overrideLevelsCheck.setEnabled(standard && !mapnik);
		pngRadio.setEnabled(standard);
		tilesizeField.setEnabled(standard);
		for (JComponent label : labels)
		{
			label.setEnabled(standard);
		}

		levelsSpinner.setEnabled(standard);
		minLatitudeField.setEnabled(standard);
		maxLatitudeField.setEnabled(standard);
		minLongitudeField.setEnabled(standard);
		maxLongitudeField.setEnabled(standard);

		alphaCheck.setEnabled(pngRadio.isSelected() && standard
				&& bandCount != 4 && !mapnik);
		bandCombo.setEnabled(bandCount > 1 && standard);
		overrideLevelsSpinner.setEnabled(overrideLevelsCheck.isSelected()
				&& standard);

		for (JIntegerField field : outsideFields)
		{
			field.setEnabled(outsideCheck.isSelected() && standard);
		}

		replaceCheck.setEnabled(standard && !mapnik);
		replace1Label.setEnabled(replaceCheck.isSelected() && standard);
		replace2Label.setEnabled(replaceCheck.isSelected() && standard);
		replace3Label.setEnabled(replaceCheck.isSelected() && standard);
		replace4Label.setEnabled(replaceCheck.isSelected() && standard);
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
		for (JIntegerField field : otherwiseFields)
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
		tileButton.setEnabled(validOptions
				&& outputDirectory.getText().length() != 0 && !running);
		cancelButton.setEnabled(running);
		progress.setEnabled(running);
	}

	private void recalculateTiles()
	{
		validOptions = fileOpen;

		StringBuilder info = new StringBuilder();

		if (mapnikRadio.isSelected())
		{
			Double minlat = minLatitudeField.getValue();
			Double minlon = minLongitudeField.getValue();
			Double maxlat = maxLatitudeField.getValue();
			Double maxlon = maxLongitudeField.getValue();
			if (minlat == null || minlon == null || maxlat == null
					|| maxlon == null || minlat >= maxlat || minlon >= maxlon)
			{
				validOptions = false;
			}
			else
			{
				sector = new Sector(minlat, minlon, maxlat, maxlon);
			}
		}

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
					levels = (Integer) overrideLevelsSpinner.getModel()
							.getValue();
				}
				else
				{
					if (gdalRadio.isSelected())
					{
						levels = GDALUtil.levelCount(dataset, lzts, sector,
								tilesize);
					}
					else
					{
						levels = (Integer) levelsSpinner.getModel().getValue();
					}
					overrideLevelsSpinner.getModel().setValue(levels);
				}
				int[] tileCount = new int[levels];
				int totalCount = 0;
				boolean overviews = overviewsCheck.isSelected();

				for (int i = overviews ? 0 : levels - 1; i < levels; i++)
				{
					tileCount[i] = GDALUtil.tileCount(sector, i, lzts);
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

	private void setupInputFile()
	{
		String preferenceKey = "Last Input Directory";
		JFileChooser chooser = new JFileChooser(preferences.get(preferenceKey,
				null));
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
			preferences.put(preferenceKey, file.getParentFile()
					.getAbsolutePath());
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
				return f.isDirectory()
						|| f.getName().equalsIgnoreCase("python.exe");
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
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".py");
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
				Integer[] otherwise = null;
				boolean overviews = overviewsCheck.isSelected();
				if (outsideCheck.isSelected())
				{
					outsideValues = new int[outputBandCount];
					for (int b = 0; b < outputBandCount; b++)
					{
						Integer value = outsideFields[b].getValue();
						outsideValues[b] = value != null ? value : 0;
					}
				}
				if (replaceCheck.isSelected())
				{
					minReplace = new int[outputBandCount];
					maxReplace = new int[outputBandCount];
					replace = new Integer[outputBandCount];
					otherwise = new Integer[outputBandCount];
					for (int b = 0; b < outputBandCount; b++)
					{
						Integer value = minFields[b].getValue();
						minReplace[b] = value != null ? value : 0;
						value = maxFields[b].getValue();
						maxReplace[b] = value != null ? value : 0;
						replace[b] = replaceFields[b].getValue();
						otherwise[b] = otherwiseFields[b].getValue();
					}
				}

				String imageFormat = pngRadio.isSelected() ? "png" : "jpg";
				if (mapnikRadio.isSelected())
				{
					Tiler.tileMapnik(mapFile, sector, level, tilesize, lzts,
							imageFormat, outDir, reporter);
					if (overviews && !reporter.isCancelled())
					{
						Overviewer.createImageOverviews(outDir, imageFormat,
								tilesize, tilesize, outsideValues, sector,
								lzts, reporter);
					}
				}
				else if (imageRadio.isSelected())
				{
					boolean addAlpha = pngRadio.isSelected()
							&& alphaCheck.isSelected();

					Tiler.tileImages(dataset, sector, level, tilesize, lzts,
							imageFormat, addAlpha, outsideValues, minReplace,
							maxReplace, replace, otherwise, outDir, reporter);
					if (overviews && !reporter.isCancelled())
					{
						Overviewer.createImageOverviews(outDir, imageFormat,
								tilesize, tilesize, outsideValues, sector,
								lzts, reporter);
					}
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
							maxReplace, replace, otherwise, outDir, reporter);
					if (overviews && !reporter.isCancelled())
					{
						Overviewer.createElevationOverviews(outDir, tilesize,
								tilesize, bufferType, ByteOrder.LITTLE_ENDIAN, /*TODO remove hardcoded byteorder*/
								outsideValues, sector, lzts, reporter);
					}
				}

				reporter.done();
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

	private static class XMLFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f)
		{
			return f.isDirectory()
					|| f.getName().toLowerCase().endsWith(".xml");
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
}
