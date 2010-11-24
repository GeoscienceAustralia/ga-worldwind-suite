package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getSearchWmsSearchBoxMsgKey;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getSearchWmsSearchButtonLabelKey;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getSearchWmsTitleKey;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HintTextField;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;

/**
 * A dialog that allows the user to search for WMS servers, or enter the URL of a WMS server
 */
public class SearchWmsServerDialog extends JDialog
{
	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	private static final long serialVersionUID = 20101124L;

	private JPanel contentPane;
	
	private JTextField searchField;
	private JButton searchButton;
	
	private BasicAction searchAction;
	
	public SearchWmsServerDialog()
	{
		initialiseDialog();
		initialiseActions();
		addSearchBar();
	}

	private void initialiseDialog()
	{
		setModal(true);
		setTitle(getMessage(getSearchWmsTitleKey()));
		setPreferredSize(PREFERRED_SIZE);
		setSize(PREFERRED_SIZE);
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
	}
	
	private void initialiseActions()
	{
		searchAction = new BasicAction(getMessage(getSearchWmsSearchButtonLabelKey()), Icons.search.getIcon());
		searchAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchForWmsServers();
			}
		});
	}
	
	private void addSearchBar()
	{
		Container container = new JPanel();
		container.setLayout(new GridBagLayout());
		
		searchField = new HintTextField(getMessage(getSearchWmsSearchBoxMsgKey()));
		searchField.setColumns(50);
		
		GridBagConstraints searchFieldConstraints = new GridBagConstraints();
		searchFieldConstraints.gridy = 0;
		searchFieldConstraints.gridx = 0;
		searchFieldConstraints.anchor = GridBagConstraints.NORTH;
		searchFieldConstraints.fill = GridBagConstraints.BOTH;
		container.add(searchField, searchFieldConstraints);
		
		searchButton = new JButton(getMessage(getSearchWmsSearchButtonLabelKey()));
		GridBagConstraints searchButtonConstraints = new GridBagConstraints();
		searchFieldConstraints.gridy = 0;
		searchFieldConstraints.gridx = 1;
		searchFieldConstraints.anchor = GridBagConstraints.NORTH;
		searchFieldConstraints.fill = GridBagConstraints.NONE;
		container.add(searchButton, searchButtonConstraints);
		
		contentPane.add(container);
	}

	/**
	 * Search for WMS servers using the search term in the search box via the registered search service
	 */
	private void searchForWmsServers()
	{
		
	}
	
	/**
	 * @return The list of selected servers chosen by the user
	 */
	public List<WmsServerIdentifier> getSelectedServers()
	{
		return new ArrayList<WmsServerIdentifier>();
	}
	
}
