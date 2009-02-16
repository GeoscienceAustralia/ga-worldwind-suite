package au.gov.ga.worldwind.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.settings.Settings.ProjectionMode;
import au.gov.ga.worldwind.settings.Settings.StereoMode;
import au.gov.ga.worldwind.util.JIntegerField;


public class SettingsDialog extends JDialog
{
	/*private final static Logger logger = Logger.getLogger(SettingsDialog.class
			.getName());*/

	private final static int SPACING = 5;

	private static Rectangle oldBounds;

	private Settings.Properties settings;

	private JSlider viewIteratorSpeedSlider;
	private JLabel viewIteratorSpeedLabel;
	private JSlider verticalExaggerationSlider;
	private JLabel verticalExaggerationLabel;
	private double verticalExaggeration;
	private JCheckBox showDownloadsCheck;

	private JRadioButton spanDisplayRadio;
	private JRadioButton singleDisplayRadio;
	private JLabel displayLabel;
	private JComboBox displayCombo;

	private JCheckBox stereoEnabledCheck;
	private JCheckBox hardwareStereoEnabledCheck;
	private JCheckBox stereoSwapCheck;
	private JLabel stereoModeLabel;
	private JComboBox stereoModeCombo;
	private JLabel projectionModeLabel;
	private JComboBox projectionModeCombo;
	private JLabel eyeSeparationLabel;
	private JSpinner eyeSeparationSpinner;
	private JLabel focalLengthLabel;
	private JSpinner focalLengthSpinner;
	private JCheckBox stereoCursorCheck;

	private JCheckBox proxyEnabledCheck;
	private JLabel proxyHostLabel;
	private JTextField proxyHostText;
	private JLabel proxyPortLabel;
	private JIntegerField proxyPortText;
	private JLabel nonProxyHostsLabel;
	private JTextField nonProxyHostsText;

	public SettingsDialog(JFrame frame)
	{
		super(frame, "Settings", true);
		settings = Settings.get();

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				cancel();
			}
		});

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel optionsPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel(new FlowLayout());

		mainPanel.add(optionsPanel, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel);
		optionsPanel.add(createTabs());

		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");
		buttonsPanel.add(ok);
		buttonsPanel.add(cancel);

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (verifyAndSave())
				{
					dispose();
				}
			}
		});

		Action cancelAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		};
		cancel.addActionListener(cancelAction);

		JRootPane rootPane = getRootPane();
		ok.setDefaultCapable(true);
		rootPane.setDefaultButton(ok);

		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap = rootPane
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", cancelAction);

		pack();
		if (oldBounds != null)
		{
			setBounds(oldBounds);
		}
		else
		{
			setLocationRelativeTo(frame);
		}

		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentMoved(ComponentEvent e)
			{
				oldBounds = getBounds();
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				oldBounds = getBounds();
			}
		});
	}

	private void cancel()
	{
		dispose();
	}

	private boolean verifyAndSave()
	{
		boolean proxyValid = true;

		boolean spanDisplays = spanDisplayRadio.isSelected();
		String displayId = ((DisplayObject) displayCombo.getSelectedItem()).device
				.getIDstring();

		boolean stereoEnabled = stereoEnabledCheck.isSelected();
		boolean hardwareStereoEnabled = hardwareStereoEnabledCheck.isSelected();
		boolean stereoSwap = stereoSwapCheck.isSelected();
		StereoMode stereoMode = (StereoMode) stereoModeCombo.getSelectedItem();
		ProjectionMode projectionMode = (ProjectionMode) projectionModeCombo
				.getSelectedItem();
		double eyeSeparation = (Double) eyeSeparationSpinner.getValue();
		double focalLength = (Double) focalLengthSpinner.getValue();
		boolean stereoCursor = stereoCursorCheck.isSelected();

		double viewIteratorSpeed = sliderToSpeed(viewIteratorSpeedSlider
				.getValue());
		boolean showDownloads = showDownloadsCheck.isSelected();

		boolean proxyEnabled = proxyEnabledCheck.isSelected();
		String proxyHost = proxyHostText.getText();
		int proxyPort = proxyPortText.getValue();

		String nonProxyHostsString = nonProxyHostsText.getText();
		String[] nph = nonProxyHostsString.split(",");
		String nonProxyHosts = "";
		for (String str : nph)
		{
			String trim = str.trim();
			if (trim.length() > 0)
			{
				nonProxyHosts += "|" + trim;
			}
		}
		nonProxyHosts = nonProxyHosts.length() == 0 ? nonProxyHosts
				: nonProxyHosts.substring(1);

		if (proxyEnabled)
		{
			if (proxyHost.length() == 0 || proxyPort <= 0)
			{
				proxyValid = false;

				showError(this, "Proxy values you entered are invalid.");
			}
		}

		boolean valid = proxyValid;

		if (valid)
		{
			settings.setSpanDisplays(spanDisplays);
			settings.setDisplayId(displayId);

			settings.setStereoEnabled(stereoEnabled);
			settings.setHardwareStereoEnabled(hardwareStereoEnabled);
			settings.setStereoSwap(stereoSwap);
			settings.setStereoMode(stereoMode);
			settings.setProjectionMode(projectionMode);
			settings.setEyeSeparation(eyeSeparation);
			settings.setFocalLength(focalLength);
			settings.setStereoCursor(stereoCursor);

			settings.setViewIteratorSpeed(viewIteratorSpeed);
			settings.setVerticalExaggeration(verticalExaggeration);
			settings.setShowDownloads(showDownloads);

			settings.setProxyEnabled(proxyEnabled);
			settings.setProxyHost(proxyHost);
			settings.setProxyPort(proxyPort);
			settings.setNonProxyHosts(nonProxyHosts);
		}

		return valid;
	}

	private static void showError(Component parent, String message)
	{
		JOptionPane.showMessageDialog(parent, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	@SuppressWarnings("unused")
	private static void showInfo(Component parent, String message)
	{
		JOptionPane.showMessageDialog(parent, message, "Info",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private JTabbedPane createTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Renderer", createRenderer());
		tabbedPane.addTab("Stereo", createStereo());
		tabbedPane.addTab("Network", createNetwork());
		tabbedPane.validate();
		return tabbedPane;
	}

	private Component createRenderer()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints c;

		JComponent fullscreen = createFullscreen();
		fullscreen.setBorder(BorderFactory.createTitledBorder("Fullscreen"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(fullscreen, c);


		JComponent other = createOther();
		other.setBorder(BorderFactory.createTitledBorder("Other"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(other, c);

		return panel;
	}

	private JComponent createStereo()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints c;

		JComponent stereo = createStereoPanel();
		stereo.setBorder(BorderFactory.createTitledBorder("Stereo"));
		c = new GridBagConstraints();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(stereo, c);

		enableStereoSettings();

		return panel;
	}

	private JComponent createNetwork()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints c;

		JComponent proxy = createProxy();
		proxy.setBorder(BorderFactory.createTitledBorder("Proxy"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(proxy, c);

		return panel;
	}

	private JComponent createFullscreen()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				displayLabel.setEnabled(singleDisplayRadio.isSelected());
				displayCombo.setEnabled(singleDisplayRadio.isSelected());
			}
		};

		spanDisplayRadio = new JRadioButton("Span all displays", settings
				.isSpanDisplays());
		spanDisplayRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(spanDisplayRadio, c);

		singleDisplayRadio = new JRadioButton("Use single display", !settings
				.isSpanDisplays());
		singleDisplayRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(singleDisplayRadio, c);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(spanDisplayRadio);
		buttonGroup.add(singleDisplayRadio);

		displayLabel = new JLabel("Display:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(displayLabel, c);

		//build list of display devices
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] screenDevices = ge.getScreenDevices();
		GraphicsDevice defaultDevice = ge.getDefaultScreenDevice();
		DisplayObject selectedDisplay = null;
		DisplayObject defaultDisplay = null;
		DisplayObject[] displays = new DisplayObject[screenDevices.length];
		for (int i = 0; i < screenDevices.length; i++)
		{
			displays[i] = new DisplayObject(screenDevices[i]);
			if (screenDevices[i].getIDstring().equals(settings.getDisplayId()))
			{
				selectedDisplay = displays[i];
			}
			if (screenDevices[i] == defaultDevice)
			{
				defaultDisplay = displays[i];
			}
		}
		if (selectedDisplay == null)
		{
			selectedDisplay = defaultDisplay;
		}

		displayCombo = new JComboBox(displays);
		displayCombo.setSelectedItem(selectedDisplay);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(displayCombo, c);

		al.actionPerformed(null);

		return panel;
	}

	private JComponent createStereoPanel()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		JPanel checks = new JPanel(new GridLayout(0, 2));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(checks, c);

		stereoEnabledCheck = new JCheckBox("Enable stereo");
		stereoEnabledCheck.setSelected(settings.isStereoEnabled());
		stereoEnabledCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableStereoSettings();
			}
		});
		checks.add(stereoEnabledCheck);

		stereoSwapCheck = new JCheckBox("Swap eyes");
		stereoSwapCheck.setSelected(settings.isStereoSwap());
		checks.add(stereoSwapCheck);

		stereoModeLabel = new JLabel("Stereo mode:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(stereoModeLabel, c);

		stereoModeCombo = new JComboBox(StereoMode.values());
		stereoModeCombo.setSelectedItem(settings.getStereoMode());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(stereoModeCombo, c);

		if (!Settings.isStereoSupported())
		{
			stereoModeCombo.removeItem(StereoMode.STEREO_BUFFER);
		}

		projectionModeLabel = new JLabel("Projection mode:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(projectionModeLabel, c);

		projectionModeCombo = new JComboBox(ProjectionMode.values());
		projectionModeCombo.setSelectedItem(settings.getProjectionMode());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(projectionModeCombo, c);
		projectionModeCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableStereoSettings();
			}
		});

		eyeSeparationLabel = new JLabel("Eye separation:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(eyeSeparationLabel, c);

		SpinnerModel eyeSeparationModel = new SpinnerNumberModel(settings
				.getEyeSeparation(), 0, 10, 0.1);
		eyeSeparationSpinner = new JSpinner(eyeSeparationModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(eyeSeparationSpinner, c);

		focalLengthLabel = new JLabel("Focal length:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(focalLengthLabel, c);

		SpinnerModel focalLengthModel = new SpinnerNumberModel(settings
				.getFocalLength(), 0, 10000, 1);
		focalLengthSpinner = new JSpinner(focalLengthModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(focalLengthSpinner, c);

		stereoCursorCheck = new JCheckBox("Stereo mouse cursor");
		stereoCursorCheck.setSelected(settings.isStereoCursor());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(stereoCursorCheck, c);

		hardwareStereoEnabledCheck = new JCheckBox(
				"Enable quad-buffered stereo support (requires restart)");
		hardwareStereoEnabledCheck.setSelected(settings
				.isHardwareStereoEnabled());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(hardwareStereoEnabledCheck, c);

		return panel;
	}

	private JComponent createOther()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		JPanel panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		JLabel label = new JLabel("Vertical exaggeration:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(label, c);

		verticalExaggeration = Settings.get().getVerticalExaggeration();
		verticalExaggerationSlider = new JSlider(0, 200,
				exaggerationToSlider(verticalExaggeration));
		/*Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);*/
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(verticalExaggerationSlider, c);

		verticalExaggerationSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double exaggeration = sliderToExaggeration(verticalExaggerationSlider
						.getValue());
				verticalExaggeration = Math.round(exaggeration * 10d) / 10d;
				verticalExaggerationLabel.setText(String.format("%1.1f",
						verticalExaggeration));
			}
		});

		verticalExaggerationLabel = new JLabel(String.format("%1.1f",
				verticalExaggeration));
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		panel2.add(verticalExaggerationLabel, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Camera animation speed:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(label, c);

		int min = 0;
		int max = 100;
		int value = speedToSlider(Settings.get().getViewIteratorSpeed());
		value = Math.max(min, Math.min(max, value));
		viewIteratorSpeedSlider = new JSlider(min, max, value);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(viewIteratorSpeedSlider, c);
		ChangeListener cl = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double value = sliderToSpeed(viewIteratorSpeedSlider.getValue());
				String format = "%1." + (value < 10 ? "2" : "1") + "f";
				viewIteratorSpeedLabel.setText(String.format(format, value));
			}
		};
		viewIteratorSpeedSlider.addChangeListener(cl);

		viewIteratorSpeedLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		panel2.add(viewIteratorSpeedLabel, c);

		cl.stateChanged(null);

		showDownloadsCheck = new JCheckBox("Display downloading tiles");
		showDownloadsCheck.setSelected(settings.isShowDownloads());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		c.anchor = GridBagConstraints.WEST;
		panel.add(showDownloadsCheck, c);

		return panel;
	}

	private JComponent createProxy()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		proxyEnabledCheck = new JCheckBox("Enable proxy");
		proxyEnabledCheck.setSelected(settings.isProxyEnabled());
		proxyEnabledCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableProxySettings();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyEnabledCheck, c);

		proxyHostLabel = new JLabel("Host:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyHostLabel, c);

		proxyHostText = new JTextField(settings.getProxyHost());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyHostText, c);

		proxyPortLabel = new JLabel("Port:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyPortLabel, c);

		proxyPortText = new JIntegerField(settings.getProxyPort());
		proxyPortText.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyPortText, c);

		nonProxyHostsLabel = new JLabel("Non-proxy hosts:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(nonProxyHostsLabel, c);


		String[] nph = settings.getNonProxyHosts().split("\\|");
		String nonProxyHosts = "";
		for (String str : nph)
		{
			String trim = str.trim();
			if (trim.length() > 0)
			{
				nonProxyHosts += "," + trim;
			}
		}
		nonProxyHosts = nonProxyHosts.length() == 0 ? nonProxyHosts
				: nonProxyHosts.substring(1);

		nonProxyHostsText = new JTextField(nonProxyHosts);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(nonProxyHostsText, c);

		enableProxySettings();

		return panel;
	}

	private void enableProxySettings()
	{
		boolean enabled = proxyEnabledCheck.isSelected();
		proxyHostLabel.setEnabled(enabled);
		proxyHostText.setEnabled(enabled);
		proxyPortLabel.setEnabled(enabled);
		proxyPortText.setEnabled(enabled);
		nonProxyHostsLabel.setEnabled(enabled);
		nonProxyHostsText.setEnabled(enabled);
	}

	private void enableStereoSettings()
	{
		boolean enabled = stereoEnabledCheck.isSelected();
		stereoModeLabel.setEnabled(enabled);
		stereoModeCombo.setEnabled(enabled);
		stereoSwapCheck.setEnabled(enabled);
		projectionModeLabel.setEnabled(enabled);
		projectionModeCombo.setEnabled(enabled);
		eyeSeparationLabel.setEnabled(enabled);
		eyeSeparationSpinner.setEnabled(enabled);
		stereoCursorCheck.setEnabled(enabled);
		boolean focalLengthEnabled = enabled
				&& projectionModeCombo.getSelectedItem() == ProjectionMode.ASYMMETRIC_FRUSTUM;
		focalLengthLabel.setEnabled(focalLengthEnabled);
		focalLengthSpinner.setEnabled(focalLengthEnabled);
	}

	private int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100d - y) / 100d);
		return (int) (x * 100d);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 100d;
		double y = Math.pow(10d, x) - (2 - x) / 2;
		return y;
	}

	private int speedToSlider(double speed)
	{
		double y = speed;
		double x = Math.log10(y * 10d) / 2;
		return (int) (x * 100d);
	}

	private double sliderToSpeed(int slider)
	{
		double x = slider;
		double y = Math.pow(10, x * 2 / 100 - 1);
		return y;
	}

	private class DisplayObject
	{
		public GraphicsDevice device;

		public DisplayObject(GraphicsDevice device)
		{
			this.device = device;
		}

		@Override
		public String toString()
		{
			return device.getIDstring();
		}
	}
}
