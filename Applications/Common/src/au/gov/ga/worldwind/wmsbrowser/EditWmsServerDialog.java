package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.Util.isEmpty;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * A simple dialog box used to edit WMS server details
 */
class EditWmsServerDialog extends JDialog
{
	private static final Dimension SIZE = new Dimension(600, 150);
	
	private JPanel contentFrame; 

	private BasicAction okAction;
	private BasicAction cancelAction;
	
	private JTextField serverNameField;
	private JTextField serverUrlField;
	
	private int response = JOptionPane.CANCEL_OPTION;
	
	public EditWmsServerDialog()
	{
		contentFrame = new JPanel();
		contentFrame.setLayout(new BoxLayout(contentFrame, BoxLayout.Y_AXIS));
		
		setLocationRelativeTo(null);
		setTitle(getMessage(getEditServerDialogTitleKey()));
		setContentPane(contentFrame);
		setModal(true);
		setSize(SIZE);
		setPreferredSize(SIZE);
		
		initialiseActions();
		addFields();
		addButtonBar();
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				response = JOptionPane.CANCEL_OPTION;
			}
		});
	}
	
	private void initialiseActions()
	{
		okAction = new BasicAction(getMessage(getTermOkKey()), null);
		okAction.setToolTipText(getMessage(getEditServerDialogOkButtonTooltipKey()));
		okAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				closeDialogWithResponse(JOptionPane.OK_OPTION);
			}
		});
		
		cancelAction = new BasicAction(getMessage(getTermCancelKey()), null);
		cancelAction.setToolTipText(getMessage(getEditServerDialogCancelButtonTooltipKey()));
		cancelAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				closeDialogWithResponse(JOptionPane.CANCEL_OPTION);
			}
		});
	}
	
	private void addFields()
	{
		JLabel serverNameLabel = new JLabel(getMessage(getEditServerDialogEditNameLabelKey()));
		serverNameField = new JTextField();
		serverNameField.setToolTipText(getMessage(getEditServerDialogEditNameTooltipKey()));
		
		JLabel serverUrlLabel = new JLabel(getMessage(getEditServerDialogEditUrlLabelKey()));
		serverUrlField = new JTextField();
		serverUrlField.setToolTipText(getMessage(getEditServerDialogEditUrlTooltipKey()));
		
		JPanel container = new JPanel();
		GroupLayout layout = new GroupLayout(container);
		container.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		Component vglue = Box.createVerticalGlue();

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup(Alignment.TRAILING)
						.addComponent(serverNameLabel)
						.addComponent(serverUrlLabel)
				).addGroup(
					layout.createParallelGroup(Alignment.LEADING)
						.addComponent(serverNameField)
						.addComponent(serverUrlField)
				)
				.addComponent(vglue)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup()
						.addComponent(serverNameLabel)
						.addComponent(serverNameField)
						.addComponent(vglue)
				).addGroup(
					layout.createParallelGroup()
						.addComponent(serverUrlLabel)
						.addComponent(serverUrlField)
				)
		);
		
		contentFrame.add(container);
	}
	
	private void addButtonBar()
	{
		JPanel container = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		container.add(new JButton(cancelAction));
		container.add(new JButton(okAction));
		contentFrame.add(container);
	}
	
	private void closeDialogWithResponse(int response)
	{
		if (response == JOptionPane.OK_OPTION)
		{
			String[] validationMessages = validateData();
			if (!isEmpty(validationMessages))
			{
				String userMessage = getMessage(getEditServerDialogInvalidDetailsMessageKey());
				for (String validationMessage : validationMessages)
				{
					userMessage += "    - " + validationMessage + "\n";
				}
				JOptionPane.showMessageDialog(this, 
											  userMessage, 
											  getMessage(getEditServerDialogInvalidDetailsTitleKey()), 
											  JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		this.response = response;
		setVisible(false);
	}
	
	/** @return any validation messages to be shown to the user, or empty array if validation succeeds. */
	private String[] validateData()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		// Validate that the URL is actually valid
		try
		{
			new URL(getServerUrlString());
		}
		catch (MalformedURLException e)
		{
			result.add(getMessage(getEditServerDialogInvalidUrlMessageKey()));
		}
		
		return result.toArray(new String[0]);
	}
	
	public int getResponse()
	{
		return response;
	}
	
	public void setCurrentServer(WmsServer server)
	{
		this.serverNameField.setText(server.getName());
		this.serverUrlField.setText(server.getCapabilitiesUrl().toExternalForm());
	}
	
	public String getServerName()
	{
		return serverNameField.getText();
	}
	
	public String getServerUrlString()
	{
		return serverUrlField.getText();
	}
	
	/**
	 * @return The server URL, or <code>null</code> if the user entered and invalid URL
	 */
	public URL getServerUrl()
	{
		try
		{
			return new URL(getServerUrlString());
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
}