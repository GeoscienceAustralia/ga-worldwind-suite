/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package au.gov.ga.worldwind.viewer.panels.other;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.*;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.ui.FlatJButton;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

/**
 * Modified version of the
 * {@link gov.nasa.worldwindx.examples.GoToCoordinatePanel} class. Contains more
 * information about supported coordinate formats.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GoToCoordinatePanel extends JPanel
{
	private WorldWindow wwd;
	private JTextField coordInput;
	private JLabel resultLabel;
	private boolean inputValid = false;
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public GoToCoordinatePanel(WorldWindow wwd)
	{
		this(wwd, false);
	}

	private GoToCoordinatePanel(WorldWindow wwd, boolean inDialog)
	{
		super(new GridBagLayout());
		this.wwd = wwd;
		makePanel(inDialog);
	}

	private void makePanel(boolean inDialog)
	{
		GridBagConstraints c;

		int row = 0;

		String prefix = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		String supported = "<html>";
		supported += getMessage(getGotoLocationSupportsMessageKey()) + ":<br>";
		supported += prefix + "-27.0 133.5<br>";
		supported += prefix + "27.0S 133.5E<br>";
		supported += prefix + "-27\u00B00'0\" 133\u00B030'0\"<br>";
		supported += prefix + "27d0'0\"S 133d30'0\"E<br>";
		supported += prefix + "53J 351167E 7012680N<br>";
		supported += prefix + "53JLL 51167 12680<br>";
		supported += "</html>";

		JLabel supportedLabel = new JLabel(supported);
		c = new GridBagConstraints();
		c.gridy = row++;
		c.anchor = GridBagConstraints.WEST;
		add(supportedLabel, c);

		JLabel space = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridy = row++;
		c.anchor = GridBagConstraints.WEST;
		add(space, c);

		JLabel label = new JLabel(getMessage(getGotoLocationPromptLabelKey()));
		c = new GridBagConstraints();
		c.gridy = row++;
		c.anchor = GridBagConstraints.WEST;
		add(label, c);

		coordInput = new JTextField(30);
		coordInput.setToolTipText(getMessage(getGotoLocationPromptTooltipKey()));
		c = new GridBagConstraints();
		c.gridy = row;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(coordInput, c);

		if (!inDialog)
		{
			FlatJButton go = new FlatJButton(Icons.run.getIcon());
			go.restrictSize();
			go.setToolTipText(getMessage(getGotoLocationGotoTooltipKey()));
			c = new GridBagConstraints();
			c.gridx = row;
			c.gridy = 1;
			add(go, c);

			ActionListener al = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					gotoCoords();
				}
			};
			go.addActionListener(al);
			coordInput.addActionListener(al);
		}
		row++;

		resultLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridy = row++;
		add(resultLabel, c);

		coordInput.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateResult();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateResult();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateResult();
			}
		});

		updateResult();
	}

	private void updateResult()
	{
		LatLon latLon = stringToLatLon(coordInput.getText());
		updateResult(latLon, false);
	}

	private LatLon stringToLatLon(String s)
	{
		Globe globe = wwd.getModel().getGlobe();
		LatLon ll = Util.computeLatLonFromString(s, globe);
		if (ll == null)
		{
			ll = Util.computeLatLonFromUTMString(s, globe, false);
		}
		return ll;
	}

	private void updateResult(LatLon latlon, boolean showInvalid)
	{
		inputValid = latlon != null;
		if (latlon != null)
		{
			resultLabel.setText(String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0", latlon.getLatitude().degrees,
					latlon.getLongitude().degrees));
		}
		else if (showInvalid)
		{
			resultLabel.setText(getMessage(getGotoLocationInvalidMessageKey()));
		}
		else
		{
			resultLabel.setText(" ");
		}

		notifyChangeListeners();
	}

	private void gotoCoords()
	{
		LatLon latLon = stringToLatLon(coordInput.getText());
		updateResult(latLon, true);
		if (latLon != null)
		{
			OrbitView view = (OrbitView) wwd.getView();

			Position beginCenter = view.getCenterPosition();
			Position center = new Position(latLon, 0);
			long lengthMillis = SettingsUtil.getScaledLengthMillis(beginCenter, center);

			view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter, center,
					view.getHeading(), view.getHeading(), view.getPitch(), view.getPitch(), view.getZoom(),
					view.getZoom(), lengthMillis, WorldWind.ABSOLUTE));
			wwd.redraw();
		}
	}

	private void addChangeListener(ChangeListener listener)
	{
		listeners.add(listener);
	}

	private void notifyChangeListeners()
	{
		for (ChangeListener listener : listeners)
		{
			listener.stateChanged(null);
		}
	}

	public static void showGotoDialog(Frame owner, WorldWindow wwd, String title, ImageIcon icon)
	{
		final JDialog dialog = new JDialog(owner, title, true);
		dialog.setIconImage(icon.getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new GridBagLayout());
		dialog.add(panel);
		GridBagConstraints c;

		int SPACING = 10;

		final GoToCoordinatePanel gtp = new GoToCoordinatePanel(wwd, true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(gtp, c);

		final JButton ok = new JButton(getMessage(getTermOkKey()));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, 0);
		panel.add(ok, c);
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (gtp.inputValid)
				{
					gtp.gotoCoords();
					dialog.dispose();
				}
			}
		});

		JButton cancel = new JButton(getMessage(getTermCancelKey()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(cancel, c);
		Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		};
		cancel.addActionListener(cancelAction);

		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				ok.setEnabled(gtp.inputValid);
			}
		};
		gtp.addChangeListener(cl);
		cl.stateChanged(null);

		JRootPane rootPane = dialog.getRootPane();
		ok.setDefaultCapable(true);
		rootPane.setDefaultButton(ok);

		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", cancelAction);

		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
	}
}
