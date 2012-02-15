package au.gov.ga.worldwind.viewer.panels.layers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * JDialog that allows editing of layer nodes in the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerEditor extends JDialog
{
	private AbstractNode node;
	private LayerNode layer;
	private JTextField nameText;
	private JTextField layerText;
	private JTextField infoText;
	private JTextField iconText;
	private int returnValue = JOptionPane.CANCEL_OPTION;
	private JButton okButton;

	public LayerEditor(Window owner, String title, final AbstractNode node, ImageIcon icon)
	{
		super(owner, title, ModalityType.APPLICATION_MODAL);
		this.node = node;

		if (node instanceof LayerNode)
			this.layer = (LayerNode) node;

		setLayout(new BorderLayout());
		setIconImage(icon.getImage());

		Insets insets = new Insets(3, 1, 3, 1);
		GridBagConstraints c;
		JLabel label;
		JPanel panel, panel2, panel3;

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(panel, BorderLayout.CENTER);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		DocumentListener dl = new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				checkValidity();
			}
		};

		int i = 0;

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Layer"));
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = (Insets) insets.clone();
		panel.add(panel2, c);

		label = new JLabel("Name:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		nameText = new JTextField(node.getName());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(nameText, c);

		if (layer != null)
		{
			label = new JLabel("Layer URL:");
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = i;
			c.anchor = GridBagConstraints.EAST;
			c.insets = (Insets) insets.clone();
			panel2.add(label, c);

			layerText = new JTextField(toString(layer.getLayerURL()));
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = i++;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = (Insets) insets.clone();
			panel2.add(layerText, c);
		}

		label = new JLabel("Info URL:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		infoText = new JTextField(toString(node.getInfoURL()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(infoText, c);

		label = new JLabel("Icon URL:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		iconText = new JTextField(toString(node.getIconURL()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(iconText, c);

		//filler
		panel3 = new JPanel();
		c = new GridBagConstraints();
		c.gridy = i++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		panel2.add(panel3, c);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = JOptionPane.OK_OPTION;
				dispose();
			}
		});
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		pack();
		Dimension size = getSize();
		setMinimumSize(new Dimension(200, size.height));
		size.width = Math.max(400, Math.min(800, size.width + 20));
		setSize(size);

		setLocationRelativeTo(owner);
		checkValidity();

		nameText.getDocument().addDocumentListener(dl);
		infoText.getDocument().addDocumentListener(dl);
		iconText.getDocument().addDocumentListener(dl);

		if (layer != null)
		{
			layerText.getDocument().addDocumentListener(dl);
		}
	}

	private boolean checkValidity()
	{
		boolean valid = true;

		if (nameText.getText().length() == 0)
			valid = false;

		URL layerURL = layer == null ? null : toURL(layerText.getText());
		URL infoURL = toURL(infoText.getText());
		URL iconURL = toURL(iconText.getText());

		if (layer != null && (layerText.getText().length() == 0 || layerURL == null))
			valid = false;
		if (infoText.getText().length() != 0 && infoURL == null)
			valid = false;
		if (iconText.getText().length() != 0 && iconURL == null)
			valid = false;

		if (valid)
		{
			node.setName(nameText.getText());
			node.setInfoURL(infoURL);
			node.setIconURL(iconURL);
			if (layer != null)
			{
				layer.setLayerURL(layerURL);
			}
		}

		okButton.setEnabled(valid);
		return valid;
	}

	private String toString(URL url)
	{
		if (url == null)
			return "";
		return url.toExternalForm();
	}

	private URL toURL(String s)
	{
		try
		{
			return new URL(s);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public int getOkCancel()
	{
		nameText.requestFocusInWindow();
		setVisible(true);
		checkValidity();
		dispose();
		return returnValue;
	}
}
