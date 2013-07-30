/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.settings;

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
import javax.swing.ImageIcon;
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

import au.gov.ga.worldwind.common.ui.JIntegerField;
import au.gov.ga.worldwind.common.util.Proxy;
import au.gov.ga.worldwind.common.util.Proxy.ProxyType;
import au.gov.ga.worldwind.common.view.stereo.StereoMode;

/**
 * {@link JDialog} used to display/edit settings.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SettingsDialog extends JDialog
{
	private final static int SPACING = 5;

	private static Rectangle oldBounds;

	private Settings settings;

	private JSlider verticalExaggerationSlider;
	private JLabel verticalExaggerationLabel;
	private double verticalExaggeration;
	private JSlider fovSlider;
	private JLabel fovLabel;
	private JButton fov45Button;
	private JSlider viewIteratorSpeedSlider;
	private JLabel viewIteratorSpeedLabel;
	private JIntegerField annotationsPauseText;
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
	private JCheckBox dynamicStereoCheck;
	private JLabel eyeSeparationMultiplierLabel;
	private JSpinner eyeSeparationMultiplierSpinner;
	private JLabel eyeSeparationLabel;
	private JSpinner eyeSeparationSpinner;
	private JLabel focalLengthLabel;
	private JSpinner focalLengthSpinner;
	private JCheckBox stereoCursorCheck;

	private JCheckBox proxyEnabledCheck;
	private JCheckBox proxyUseSystemCheck;
	private JLabel proxyTypeLabel;
	private JComboBox proxyTypeCombo;
	private JLabel proxyHostLabel;
	private JTextField proxyHostText;
	private JLabel proxyPortLabel;
	private JIntegerField proxyPortText;
	private JLabel proxyNonProxyHostsLabel;
	private JTextField proxyNonProxyHostsText;

	public SettingsDialog(JFrame frame, String title, ImageIcon icon)
	{
		super(frame, title, true);
		setIconImage(icon.getImage());
		settings = Settings.get();

		addWindowListener(new WindowAdapter()
		{
			@Override
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
			@Override
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
			@Override
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
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
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
		boolean valid = true;

		boolean spanDisplays = spanDisplayRadio.isSelected();
		String displayId = ((DisplayObject) displayCombo.getSelectedItem()).device.getIDstring();

		boolean stereoEnabled = stereoEnabledCheck.isSelected();
		boolean hardwareStereoEnabled = hardwareStereoEnabledCheck.isSelected();
		boolean stereoSwap = stereoSwapCheck.isSelected();
		boolean dynamicStereo = dynamicStereoCheck.isSelected();
		StereoMode stereoMode = (StereoMode) stereoModeCombo.getSelectedItem();
		double eyeSeparation = (Double) eyeSeparationSpinner.getValue();
		double focalLength = (Double) focalLengthSpinner.getValue();
		double eyeSeparationMultiplier = (Double) eyeSeparationMultiplierSpinner.getValue();
		boolean stereoCursor = stereoCursorCheck.isSelected();

		double fieldOfView = fovSlider.getValue();
		double viewIteratorSpeed = sliderToSpeed(viewIteratorSpeedSlider.getValue());
		Integer annotationsPause = annotationsPauseText.getValue();
		boolean showDownloads = showDownloadsCheck.isSelected();

		Proxy proxy = new Proxy();
		proxy.setEnabled(proxyEnabledCheck.isSelected());
		proxy.setUseSystem(proxyUseSystemCheck.isSelected());
		proxy.setType((ProxyType) proxyTypeCombo.getSelectedItem());
		proxy.setHost(proxyHostText.getText());
		Integer port = proxyPortText.getValue();
		if (port != null)
		{
			proxy.setPort(port);
		}
		proxy.setNonProxyHosts(proxyNonProxyHostsText.getText());

		if (proxy.isEnabled())
		{
			if (!proxy.isUseSystem() && (proxy.getHost().length() == 0 || port == null))
			{
				valid = false;
				showError(this, "Proxy values you entered are invalid.");
			}
		}

		if (valid)
		{
			settings.setSpanDisplays(spanDisplays);
			settings.setDisplayId(displayId);

			settings.setStereoEnabled(stereoEnabled);
			settings.setHardwareStereoEnabled(hardwareStereoEnabled);
			settings.setSwapEyes(stereoSwap);
			settings.setStereoMode(stereoMode);
			settings.setDynamicStereo(dynamicStereo);
			settings.setEyeSeparationMultiplier(eyeSeparationMultiplier);
			settings.setEyeSeparation(eyeSeparation);
			settings.setFocalLength(focalLength);
			settings.setStereoCursor(stereoCursor);

			settings.setFieldOfView(fieldOfView);
			settings.setViewIteratorSpeed(viewIteratorSpeed);
			settings.setVerticalExaggeration(verticalExaggeration);
			if (annotationsPause != null)
			{
				settings.setPlacesPause(annotationsPause);
			}
			settings.setShowDownloads(showDownloads);

			settings.setProxy(proxy);
		}

		return valid;
	}

	private static void showError(Component parent, String message)
	{
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	@SuppressWarnings("unused")
	private static void showInfo(Component parent, String message)
	{
		JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
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
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(fullscreen, c);


		JComponent other = createOther();
		other.setBorder(BorderFactory.createTitledBorder("Other"));
		c = new GridBagConstraints();
		c.gridy = 1;
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
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
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayLabel.setEnabled(singleDisplayRadio.isSelected());
				displayCombo.setEnabled(singleDisplayRadio.isSelected());
			}
		};

		spanDisplayRadio = new JRadioButton("Span all displays", settings.isSpanDisplays());
		spanDisplayRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(spanDisplayRadio, c);

		singleDisplayRadio = new JRadioButton("Use single display", !settings.isSpanDisplays());
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
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
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
		int i = 0;

		JPanel checks = new JPanel(new GridLayout(0, 2));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(checks, c);

		ActionListener stereoActionListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableStereoSettings();
			}
		};

		stereoEnabledCheck = new JCheckBox("Enable stereo");
		stereoEnabledCheck.setSelected(settings.isStereoEnabled());
		stereoEnabledCheck.addActionListener(stereoActionListener);
		checks.add(stereoEnabledCheck);

		stereoSwapCheck = new JCheckBox("Swap eyes");
		stereoSwapCheck.setSelected(settings.isSwapEyes());
		checks.add(stereoSwapCheck);

		stereoModeLabel = new JLabel("Stereo mode:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(stereoModeLabel, c);

		stereoModeCombo = new JComboBox(StereoMode.values());
		stereoModeCombo.setSelectedItem(settings.getStereoMode());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i;
		c.weightx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(stereoModeCombo, c);

		if (!Settings.isStereoSupported())
		{
			stereoModeCombo.removeItem(StereoMode.STEREO_BUFFER);
		}

		dynamicStereoCheck = new JCheckBox("Compute focal length dynamically");
		dynamicStereoCheck.setSelected(settings.isDynamicStereo());
		dynamicStereoCheck.addActionListener(stereoActionListener);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(dynamicStereoCheck, c);

		eyeSeparationMultiplierLabel = new JLabel("Eye exaggeration:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(eyeSeparationMultiplierLabel, c);

		SpinnerModel eyeSeparationMultiplierModel =
				new SpinnerNumberModel(settings.getEyeSeparationMultiplier(), 0, 10, 0.01);
		eyeSeparationMultiplierSpinner = new JSpinner(eyeSeparationMultiplierModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(eyeSeparationMultiplierSpinner, c);

		eyeSeparationLabel = new JLabel("Eye separation:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(eyeSeparationLabel, c);

		SpinnerModel eyeSeparationModel = new SpinnerNumberModel(settings.getEyeSeparation(), 0, 1e6, 0.1);
		eyeSeparationSpinner = new JSpinner(eyeSeparationModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(eyeSeparationSpinner, c);

		focalLengthLabel = new JLabel("Focal length:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(focalLengthLabel, c);

		SpinnerModel focalLengthModel = new SpinnerNumberModel(settings.getFocalLength(), 0, 1e8, 1);
		focalLengthSpinner = new JSpinner(focalLengthModel);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(focalLengthSpinner, c);

		stereoCursorCheck = new JCheckBox("Stereo mouse cursor");
		stereoCursorCheck.setSelected(settings.isStereoCursor());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(stereoCursorCheck, c);

		hardwareStereoEnabledCheck = new JCheckBox("Enable quad-buffered stereo support (requires restart)");
		hardwareStereoEnabledCheck.setSelected(settings.isHardwareStereoEnabled());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++i;
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
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		JLabel label = new JLabel("Vertical exaggeration:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(label, c);

		verticalExaggeration = Settings.get().getVerticalExaggeration();
		verticalExaggerationSlider = new JSlider(0, 2000, exaggerationToSlider(verticalExaggeration));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(verticalExaggerationSlider, c);

		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				double exaggeration = sliderToExaggeration(verticalExaggerationSlider.getValue());
				if (e != null)
					verticalExaggeration = exaggeration;

				String format = "%1." + (exaggeration < 10 ? "2" : exaggeration < 100 ? "1" : "0") + "f";
				String value = String.format(format, exaggeration);
				if (value.indexOf('.') < 0)
					value += ".";

				verticalExaggerationLabel.setText(value + " x");
			}
		};
		verticalExaggerationSlider.addChangeListener(cl);

		verticalExaggerationLabel = new JLabel("");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		panel2.add(verticalExaggerationLabel, c);

		cl.stateChanged(null);


		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Field of view:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(label, c);

		int min = 1;
		int max = 90;
		int value = (int) Math.round(Settings.get().getFieldOfView());
		value = Math.max(min, Math.min(max, value));
		fovSlider = new JSlider(min, max, value);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(fovSlider, c);
		cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int value = fovSlider.getValue();
				fovLabel.setText(value + "\u00B0");
			}
		};
		fovSlider.addChangeListener(cl);

		fovLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, 0, 0, 0);
		panel2.add(fovLabel, c);

		cl.stateChanged(null);

		fov45Button = new JButton("45\u00B0");
		c = new GridBagConstraints();
		c.gridx = 3;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel2.add(fov45Button, c);
		fov45Button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				fovSlider.setValue(45);
			}
		});


		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Camera animation speed:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(label, c);

		min = 0;
		max = 100;
		value = speedToSlider(Settings.get().getViewIteratorSpeed());
		value = Math.max(min, Math.min(max, value));
		viewIteratorSpeedSlider = new JSlider(min, max, value);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel2.add(viewIteratorSpeedSlider, c);
		cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				double value = sliderToSpeed(viewIteratorSpeedSlider.getValue());
				String format = "%1." + (value < 10 ? "2" : "1") + "f";
				viewIteratorSpeedLabel.setText(String.format(format, value) + " x");
			}
		};
		viewIteratorSpeedSlider.addChangeListener(cl);

		viewIteratorSpeedLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		panel2.add(viewIteratorSpeedLabel, c);

		cl.stateChanged(null);


		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Annotation pause when playing:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel2.add(label, c);

		annotationsPauseText = new JIntegerField(settings.getPlacesPause());
		annotationsPauseText.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(annotationsPauseText, c);

		label = new JLabel("ms");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.insets = new Insets(SPACING, 0, 0, SPACING);
		panel2.add(label, c);

		showDownloadsCheck = new JCheckBox("Display downloading tiles");
		showDownloadsCheck.setSelected(settings.isShowDownloads());
		c = new GridBagConstraints();
		c.gridy = 4;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		c.anchor = GridBagConstraints.WEST;
		panel.add(showDownloadsCheck, c);

		return panel;
	}

	private JComponent createProxy()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());
		int row = -1;

		Proxy proxy = settings.getProxy();

		proxyEnabledCheck = new JCheckBox("Enable proxy");
		proxyEnabledCheck.setSelected(proxy.isEnabled());
		proxyEnabledCheck.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableProxySettings();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyEnabledCheck, c);

		proxyUseSystemCheck = new JCheckBox("Use system proxy");
		proxyUseSystemCheck.setSelected(proxy.isUseSystem());
		proxyUseSystemCheck.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableProxySettings();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyUseSystemCheck, c);

		proxyTypeLabel = new JLabel("Proxy type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyTypeLabel, c);

		proxyTypeCombo = new JComboBox(ProxyType.values());
		proxyTypeCombo.setSelectedItem(proxy.getType());
		proxyTypeCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableProxySettings();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyTypeCombo, c);

		proxyHostLabel = new JLabel("Host:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyHostLabel, c);

		proxyHostText = new JTextField(proxy.getHost());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyHostText, c);

		proxyPortLabel = new JLabel("Port:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyPortLabel, c);

		proxyPortText = new JIntegerField(proxy.getPort());
		proxyPortText.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, 0, SPACING);
		panel.add(proxyPortText, c);

		proxyNonProxyHostsLabel = new JLabel("Non-proxy hosts:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = ++row;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(SPACING, SPACING, 0, 0);
		panel.add(proxyNonProxyHostsLabel, c);

		proxyNonProxyHostsText = new JTextField(proxy.getNonProxyHosts());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(proxyNonProxyHostsText, c);

		enableProxySettings();

		return panel;
	}

	private void enableProxySettings()
	{
		boolean enabled = proxyEnabledCheck.isSelected();
		boolean useSystem = proxyUseSystemCheck.isSelected();
		boolean http = proxyTypeCombo.getSelectedItem() == ProxyType.HTTP;
		proxyUseSystemCheck.setEnabled(enabled);
		proxyTypeLabel.setEnabled(enabled && !useSystem);
		proxyTypeCombo.setEnabled(enabled && !useSystem);
		proxyHostLabel.setEnabled(enabled && !useSystem);
		proxyHostText.setEnabled(enabled && !useSystem);
		proxyPortLabel.setEnabled(enabled && !useSystem);
		proxyPortText.setEnabled(enabled && !useSystem);
		proxyNonProxyHostsLabel.setEnabled(enabled && !useSystem && http);
		proxyNonProxyHostsText.setEnabled(enabled && !useSystem && http);
	}

	private void enableStereoSettings()
	{
		boolean enabled = stereoEnabledCheck.isSelected();
		stereoModeLabel.setEnabled(enabled);
		stereoModeCombo.setEnabled(enabled);
		stereoSwapCheck.setEnabled(enabled);
		dynamicStereoCheck.setEnabled(enabled);
		boolean dynamic = dynamicStereoCheck.isSelected();
		eyeSeparationMultiplierLabel.setEnabled(enabled && dynamic);
		eyeSeparationMultiplierSpinner.setEnabled(enabled && dynamic);
		eyeSeparationLabel.setEnabled(enabled && !dynamic);
		eyeSeparationSpinner.setEnabled(enabled && !dynamic);
		focalLengthLabel.setEnabled(enabled && !dynamic);
		focalLengthSpinner.setEnabled(enabled && !dynamic);
		stereoCursorCheck.setEnabled(enabled);
	}

	private int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (1000d - y) / 1000d);
		return (int) (x * 1000d);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 1000d;
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
