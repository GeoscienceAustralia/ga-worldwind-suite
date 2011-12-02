package au.gov.ga.worldwind.animator.application.settings;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.effects.EffectDialog;
import au.gov.ga.worldwind.animator.application.effects.EffectRegistry;
import au.gov.ga.worldwind.common.ui.JIntegerField;

/**
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 *
 */
public class ProxyDialog extends JDialog
{
	/**
	 * Show a new {@link EffectDialog}, and block until the user has selected an
	 * effect.
	 * 
	 * @param parent
	 *            Dialog's parent
	 * @return The effect class selected, or null if the user cancelled.
	 */
	public static void show(Frame parent)
	{
		ProxyDialog dialog = new ProxyDialog(parent);
		dialog.setVisible(true);

		if (dialog.response != JOptionPane.OK_OPTION)
		{
			return;
		}

		Settings.get().setProxyEnabled(dialog.enabled.isSelected());
		Settings.get().setProxyHost(dialog.host.getText());
		int port = Settings.get().getProxyPort();
		try
		{
			port = Integer.valueOf(dialog.port.getText());
		}
		catch(Exception e)
		{
			//ignore
		}
		Settings.get().setProxyPort(port);
		Settings.get().setProxyType((Settings.ProxyType)dialog.type.getSelectedItem());
	}

	private JButton okButton;
	private JButton cancelButton;
	private int response;
	
	private JCheckBox enabled;
	private JTextField host;
	private JIntegerField port;
	private JComboBox type;

	private ProxyDialog(Frame parent)
	{
		super(parent, getMessage(getProxyDialogTitleKey()), true);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				response = JOptionPane.CLOSED_OPTION;
				dispose();
			}
		});

		JLabel label;
		GridBagConstraints c;
		JPanel rootPanel = new JPanel(new GridBagLayout());
		setContentPane(rootPanel);
		int spacing = 5;
		int row = -1;
		
		enabled = new JCheckBox(getMessage(getProxyEnabledLabelKey()));
		enabled.setSelected(Settings.get().isProxyEnabled());
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(spacing, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.WEST;
		rootPanel.add(enabled, c);
		
		label = new JLabel(getMessage(getProxyHostLabelKey()));
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);

		host = new JTextField(Settings.get().getProxyHost());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(host, c);
		
		label = new JLabel(getMessage(getProxyPortLabelKey()));
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);
		
		port = new JIntegerField(Settings.get().getProxyPort());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(port, c);
		
		label = new JLabel(getMessage(getProxyTypeLabelKey()));
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);
		
		type = new JComboBox(Settings.ProxyType.values());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(type, c);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		rootPanel.add(buttonPanel, c);

		okButton = new JButton(getMessage(getTermOkKey()));
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.OK_OPTION;
				dispose();
			}
		});
		getRootPane().setDefaultButton(okButton);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, spacing, spacing, spacing);
		buttonPanel.add(okButton, c);

		cancelButton = new JButton(getMessage(getTermCancelKey()));
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		buttonPanel.add(cancelButton, c);

		pack();
		setSize(300, getHeight());
		setLocationRelativeTo(parent);
	}
}
