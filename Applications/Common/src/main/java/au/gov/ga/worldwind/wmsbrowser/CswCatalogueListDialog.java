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
package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HintTextField;
import au.gov.ga.worldwind.common.ui.ListBackedModel;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;
import au.gov.ga.worldwind.common.util.Util;

/**
 * A dialog for adding/removing CSW catalogues.
 * <p/>
 * Updates the {@link WmsBrowserSettings} instance, and provides access to the new list of CSW catalogues.
 */
public class CswCatalogueListDialog extends JDialog
{
	private static final long serialVersionUID = 20101207L;

	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	
	private static final Logger logger = Logging.logger();
	
	private JPanel contentPane;
	
	// The list manipulation panel
	private BasicAction addCatalogueAction;
	private BasicAction removeSelectedCataloguesAction;
	private JTextField catalogueEntryField;
	
	// The catalogue list
	private JList catalogueList;
	private ListBackedModel<String> knownCatalogues = new ListBackedModel<String>(); // Stored as strings to improve membership testing performance
	private ReadWriteLock catalogueListLock = new LenientReadWriteLock();
	
	// The button panel
	private BasicAction okAction;
	private BasicAction cancelAction;
	private int response = JOptionPane.CANCEL_OPTION;
	
	public CswCatalogueListDialog()
	{
		initialiseDialog();
		initialiseActions();

		int verticalOrder = 0;
		addBlurb(verticalOrder++);
		addCatalogueEntry(verticalOrder++);
		addCatalogueList(verticalOrder++);
		addButtonPanel(verticalOrder++);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				knownCatalogues.clear();
				for (URL serverUrl : WmsBrowserSettings.get().getCswCatalogueServers())
				{
					knownCatalogues.add(serverUrl.toExternalForm());
				}
				
				catalogueList.requestFocus();
				response = JOptionPane.CANCEL_OPTION; // Default to cancel in case the user closes other than via buttons
				removeSelectedCataloguesAction.setEnabled(!catalogueList.isSelectionEmpty());
			}
		});
	}
	
	private void initialiseDialog()
	{
		setModal(true);
		setTitle(getMessage(getEditCswTitleKey()));
		setPreferredSize(PREFERRED_SIZE);
		setSize(PREFERRED_SIZE);
		setIconImage(Icons.wmsbrowser.getIcon().getImage());
		
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
	}
	
	private void initialiseActions()
	{
		okAction = new BasicAction(getMessage(getTermOkKey()), null);
		okAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.OK_OPTION;
				try
				{
					ArrayList<URL> newCatalogueUrls = new ArrayList<URL>();
					for (String urlString : knownCatalogues)
					{
						newCatalogueUrls.add(new URL(urlString));
					}
					WmsBrowserSettings.get().setCswCatalogueServers(newCatalogueUrls);
				}
				catch (MalformedURLException x)
				{
					// Shouldn't happen - all input is validated, but just in case
					logger.log(Level.WARNING, "Exception when converting catalogue list to URLs", x);
				}
				
				dispose();
			}
		});
		
		cancelAction = new BasicAction(getMessage(getTermCancelKey()), null);
		cancelAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		
		addCatalogueAction = new BasicAction(getMessage(getEditCswAddCatalogueLabelKey()), Icons.add.getIcon());
		addCatalogueAction.setToolTipText(getMessage(getEditCswAddCatalogueTooltipKey()));
		addCatalogueAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addCatalogueToList();
			}
		});
		
		removeSelectedCataloguesAction = new BasicAction(getMessage(getEditCswRemoveCatalogueLabelKey()), Icons.remove.getIcon());
		removeSelectedCataloguesAction.setToolTipText(getMessage(getEditCswAddCatalogueTooltipKey()));
		removeSelectedCataloguesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				removeSelectedCatalogues();
			}
		});
	}
	
	private void addBlurb(int verticalOrder)
	{
		JLabel blurb = new JLabel(getMessage(getEditCswBlurbKey(), getMessage(getEditCswTitleKey())));
		blurb.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(10, 30, 10, 0)));
		
		GridBagConstraints containerConstraints = new GridBagConstraints();
		containerConstraints.gridy = verticalOrder;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		containerConstraints.ipadx = 20;
		contentPane.add(blurb, containerConstraints);
	}
	
	private void addCatalogueEntry(int verticalOrder)
	{
		Container container = new JPanel();
		container.setLayout(new GridBagLayout());

		catalogueEntryField = new HintTextField(getMessage(getEditCswCatalogueEntryBoxMsgKey()));
		catalogueEntryField.setColumns(50);
		catalogueEntryField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doAdd");
		catalogueEntryField.getActionMap().put("doAdd", addCatalogueAction);

		GridBagConstraints searchFieldConstraints = new GridBagConstraints();
		searchFieldConstraints.gridy = 0;
		searchFieldConstraints.gridx = 0;
		searchFieldConstraints.anchor = GridBagConstraints.NORTH;
		searchFieldConstraints.fill = GridBagConstraints.BOTH;
		container.add(catalogueEntryField, searchFieldConstraints);

		JButton addButton = new JButton(addCatalogueAction);
		GridBagConstraints searchButtonConstraints = new GridBagConstraints();
		searchButtonConstraints.gridy = 0;
		searchButtonConstraints.gridx = 1;
		searchButtonConstraints.anchor = GridBagConstraints.NORTH;
		searchButtonConstraints.fill = GridBagConstraints.NONE;
		container.add(addButton, searchButtonConstraints);

		GridBagConstraints containerConstraints = new GridBagConstraints();
		containerConstraints.gridy = verticalOrder;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		contentPane.add(container, containerConstraints);
	}
	
	private void addCatalogueList(int verticalOrder)
	{
		catalogueList = new JList();
		catalogueList.setModel(knownCatalogues);
		catalogueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		catalogueList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				removeSelectedCataloguesAction.setEnabled(!catalogueList.isSelectionEmpty());
			}
		});
		
		JScrollPane listScroller = new JScrollPane(catalogueList);
		listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = verticalOrder;
		constraints.gridx = 0;
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
		constraints.fill = GridBagConstraints.BOTH;
		contentPane.add(listScroller, constraints);
	}
	
	private void addButtonPanel(int verticalOrder)
	{
		JToolBar buttonPanel = new JToolBar();
		buttonPanel.setFloatable(false);
		
		buttonPanel.add(removeSelectedCataloguesAction).setHideActionText(true);
		
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		JButton okButton = new JButton(okAction);
		JButton cancelButton = new JButton(cancelAction);
		okCancelPanel.add(okButton);
		okCancelPanel.add(cancelButton);
		
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okCancelPanel);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = verticalOrder;
		constraints.gridx = 0;
		constraints.weighty = 0;
		constraints.weightx = 0;
		constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
		constraints.fill = GridBagConstraints.BOTH;
		contentPane.add(buttonPanel, constraints);
	}
	
	private void addCatalogueToList()
	{

		String enteredString = catalogueEntryField.getText();
		if (Util.isBlank(enteredString))
		{
			return;
		}

		// Check the url
		URL catalogueUrl;
		try
		{
			catalogueUrl = new URL(enteredString);
		}
		catch (MalformedURLException e)
		{
			// To support the case where the user doesn't enter a protocol, try the http protocol by default
			try
			{
				enteredString = "http://" + enteredString;
				catalogueUrl = new URL(enteredString);
			}
			catch (MalformedURLException e2)
			{
				JOptionPane.showMessageDialog(this, getMessage(getEditCswInvalidUrlMsgKey()), getMessage(getEditCswInvalidUrlTitleKey()), JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		catalogueListLock.writeLock().lock();
		try
		{
			// Don't duplicate urls
			if (knownCatalogues.contains(catalogueUrl))
			{
				JOptionPane.showMessageDialog(this, getMessage(getEditCswDuplicateUrlMsgKey()), getMessage(getEditCswDuplicateUrlTitleKey()), JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			knownCatalogues.add(enteredString);
			catalogueEntryField.setText("");
		}
		finally
		{
			catalogueListLock.writeLock().unlock();
		}

		validate();
		repaint();
	}
	
	private void removeSelectedCatalogues()
	{
		Object[] selectedUrls = catalogueList.getSelectedValues();
		if (selectedUrls == null || selectedUrls.length == 0)
		{
			return;
		}
		
		catalogueListLock.writeLock().lock();
		try
		{
			for (Object o : selectedUrls)
			{
				knownCatalogues.remove(o);
			}
		}
		finally
		{
			catalogueListLock.writeLock().unlock();
		}
		
		validate();
		repaint();
	}
	
	/**
	 * @return The user's response to the dialog. See {@link JOptionPane} for constant values.
	 */
	public int getResponse()
	{
		return response;
	}
}
