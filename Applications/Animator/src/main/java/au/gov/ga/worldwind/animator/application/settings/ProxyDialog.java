package au.gov.ga.worldwind.animator.application.settings;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyDialogTitleKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyEnabledLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyHostLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyNonProxyHostsLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyPortLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyTypeLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getProxyUseSystemLabelKey;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.gov.ga.worldwind.common.ui.JIntegerField;
import au.gov.ga.worldwind.common.util.Proxy;
import au.gov.ga.worldwind.common.util.Proxy.ProxyType;

/**
 * {@link JDialog} used for setting up the proxy settings.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ProxyDialog extends JDialog
{
	/**
	 * Show a new {@link ProxyDialog}, and block until the user has completed.
	 * 
	 * @param parent
	 *            Dialog's parent
	 */
	public static void show(Frame parent)
	{
		ProxyDialog dialog = new ProxyDialog(parent);
		dialog.setVisible(true);

		if (dialog.response != JOptionPane.OK_OPTION)
		{
			return;
		}

		Proxy proxy = new Proxy();
		proxy.setEnabled(dialog.enabled.isSelected());
		proxy.setUseSystem(dialog.useSystem.isSelected());
		Integer port = dialog.port.getValue();
		if (port != null)
		{
			proxy.setPort(port);
		}
		proxy.setHost(dialog.host.getText());
		proxy.setNonProxyHosts(dialog.nonProxyHosts.getText());
		proxy.setType((ProxyType) dialog.type.getSelectedItem());
	}

	private JButton okButton;
	private JButton cancelButton;
	private int response;

	private JCheckBox enabled;
	private JCheckBox useSystem;
	private JComboBox type;
	private JTextField host;
	private JIntegerField port;
	private JTextField nonProxyHosts;
	private List<JLabel> labels = new ArrayList<JLabel>();

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

		Proxy proxy = Settings.get().getProxy();

		enabled = new JCheckBox(getMessage(getProxyEnabledLabelKey()));
		enabled.setSelected(proxy.isEnabled());
		enabled.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(spacing, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.WEST;
		rootPanel.add(enabled, c);

		useSystem = new JCheckBox(getMessage(getProxyUseSystemLabelKey()));
		useSystem.setSelected(proxy.isUseSystem());
		useSystem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.WEST;
		rootPanel.add(useSystem, c);

		label = new JLabel(getMessage(getProxyTypeLabelKey()));
		labels.add(label);
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);

		type = new JComboBox(ProxyType.values());
		type.setSelectedItem(proxy.getType());
		type.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableFields();
			}
		});
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(type, c);

		label = new JLabel(getMessage(getProxyHostLabelKey()));
		labels.add(label);
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);

		host = new JTextField(proxy.getHost());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(host, c);

		label = new JLabel(getMessage(getProxyPortLabelKey()));
		labels.add(label);
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);

		port = new JIntegerField(proxy.getPort());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(port, c);

		label = new JLabel(getMessage(getProxyNonProxyHostsLabelKey()));
		labels.add(label);
		c = new GridBagConstraints();
		c.gridy = ++row;
		c.insets = new Insets(0, spacing, spacing, spacing);
		c.anchor = GridBagConstraints.EAST;
		rootPanel.add(label, c);

		nonProxyHosts = new JTextField(proxy.getNonProxyHosts());
		c = new GridBagConstraints();
		c.gridy = row;
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		rootPanel.add(nonProxyHosts, c);

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
		
		enableFields();
	}

	private void enableFields()
	{
		boolean e = enabled.isSelected();
		boolean s = useSystem.isSelected();
		boolean h = type.getSelectedItem() == ProxyType.HTTP;
		useSystem.setEnabled(e);
		for (JLabel label : labels)
		{
			label.setEnabled(e && !s);
		}
		type.setEnabled(e && !s);
		host.setEnabled(e && !s);
		port.setEnabled(e && !s);
		nonProxyHosts.setEnabled(e && !s && h);
	}
}
