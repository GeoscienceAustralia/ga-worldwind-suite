package settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import settings.Settings.ProjectionMode;
import settings.Settings.StereoMode;

public class SettingsDialog extends JDialog
{
	private final static Logger logger = Logger.getLogger(SettingsDialog.class
			.getName());

	private final static int SPACING = 5;

	private static Rectangle oldBounds;

	private Settings settings;

	private JCheckBox stereoEnabledCheck;
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

		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});

		pack();
		//setResizable(false);
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

		boolean stereoEnabled = stereoEnabledCheck.isSelected();
		boolean stereoSwap = stereoSwapCheck.isSelected();
		StereoMode stereoMode = (StereoMode) stereoModeCombo.getSelectedItem();
		ProjectionMode projectionMode = (ProjectionMode) projectionModeCombo
				.getSelectedItem();
		double eyeSeparation = (Double) eyeSeparationSpinner.getValue();
		double focalLength = (Double) focalLengthSpinner.getValue();
		boolean stereoCursor = stereoCursorCheck.isSelected();

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
			try
			{
				settings.setStereoEnabled(stereoEnabled);
				settings.setStereoSwap(stereoSwap);
				settings.setStereoMode(stereoMode);
				settings.setProjectionMode(projectionMode);
				settings.setEyeSeparation(eyeSeparation);
				settings.setFocalLength(focalLength);
				settings.setStereoCursor(stereoCursor);

				settings.setProxyEnabled(proxyEnabled);
				settings.setProxyHost(proxyHost);
				settings.setProxyPort(proxyPort);
				settings.setNonProxyHosts(nonProxyHosts);

				settings.save();
			}
			catch (BackingStoreException e)
			{
				logger.severe("Error saving settings: " + e.toString());
			}
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
		tabbedPane.addTab("Network", createNetwork());
		tabbedPane.validate();
		return tabbedPane;
	}

	private Component createRenderer()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints c;

		JComponent stereo = createStereo();
		stereo.setBorder(BorderFactory.createTitledBorder("Stereo"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.weighty = 1;
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

	private JComponent createStereo()
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

		if (!Settings.get().isStereoSupported())
		{
			stereoModeCombo.removeItem(StereoMode.STEREOBUFFER);
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
				.getFocalLength(), 0, 1000, 1);
		focalLengthSpinner = new JSpinner(focalLengthModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(focalLengthSpinner, c);
		
		stereoCursorCheck = new JCheckBox("Stereo mouse cursor");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(stereoCursorCheck, c);

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
}
