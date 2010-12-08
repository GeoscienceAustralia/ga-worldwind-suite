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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HintTextField;
import au.gov.ga.worldwind.common.ui.SelectableLabel;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.search.CSWSearchService;
import au.gov.ga.worldwind.wmsbrowser.search.ChainingSearchService;
import au.gov.ga.worldwind.wmsbrowser.search.CompoundSearchService;
import au.gov.ga.worldwind.wmsbrowser.search.DirectUrlSearchService;
import au.gov.ga.worldwind.wmsbrowser.search.WmsServerSearchResult;
import au.gov.ga.worldwind.wmsbrowser.search.WmsServerSearchService;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * A dialog that allows the user to search for WMS servers, or enter the URL of a WMS server
 */
public class SearchWmsServerDialog extends JDialog
{
	private static final long serialVersionUID = 20101124L;
	
	private static final Color STRIPE_EVEN = Color.WHITE;
	private static final Color STRIPE_ODD = Color.LIGHT_GRAY;
	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);

	private static final Logger logger = Logging.logger();
	
	private WmsServerSearchService searchService;
	
	private JPanel contentPane;
	
	// The search panel
	private JTextField searchField;
	private JButton searchButton;
	private BasicAction searchAction;
	private BasicAction cancelSearchAction;
	private SearchTask currentSearch;
	private ExecutorService searcher = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Master WMS search thread"));
	private JComponent searchingIndicator;
	
	// The results panel
	private JLabel noResultsMessage = new JLabel(getMessage(getSearchWmsNoResultsMsgKey()));
	private List<WmsServerSearchResult> searchResults = new ArrayList<WmsServerSearchResult>();
	private List<SearchResultPanel> searchResultPanels = new ArrayList<SearchResultPanel>();
	private List<WmsServer> selectedResults = new ArrayList<WmsServer>();
	private ReadWriteLock searchResultsLock = new LenientReadWriteLock();
	private JPanel resultsPanel;
	private JLabel resultCountLabel;
	
	// The button panel
	private BasicAction okAction;
	private BasicAction cancelAction;
	private BasicAction selectAllAction;
	private BasicAction deselectAllAction;
	private boolean catalogueEditAllowed = true; // Catalogue edit only permitted when using the default search service
	private BasicAction editCatalogueListAction;
	private int response = JOptionPane.CANCEL_OPTION;
	private JScrollPane resultScroller;
	
	public SearchWmsServerDialog(WmsServerSearchService searchService)
	{
		Validate.notNull(searchService, "A search service is required");
		this.searchService = searchService;
		catalogueEditAllowed = false;
		init();
	}

	public SearchWmsServerDialog()
	{
		this.searchService = createDefaultSearchService();
		catalogueEditAllowed = true;
		init();
	}
	
	/**
	 * Perform common initialisation
	 */
	private void init()
	{
		initialiseDialog();
		initialiseActions();
		initialiseSearchingIndicator();
		
		GridBagConstraints containerConstraints = new GridBagConstraints();
		containerConstraints.gridy = -1;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		containerConstraints.ipadx = 20;
		
		addBlurb(containerConstraints);
		addSearchBar(containerConstraints);
		addResultsPanel(containerConstraints);
		addButtonPanel(containerConstraints);
		
		updateSearchResultsPanel();
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				searchButton.requestFocus();
				response = JOptionPane.CANCEL_OPTION; // Default to cancel in case the user closes other than via buttons
			}
		});
	}
	
	private void initialiseDialog()
	{
		setModal(true);
		setTitle(getMessage(getSearchWmsTitleKey()));
		setPreferredSize(PREFERRED_SIZE);
		setSize(PREFERRED_SIZE);
		
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
	}
	
	private void initialiseActions()
	{
		searchAction = new BasicAction(getMessage(getSearchWmsSearchButtonLabelKey()), Icons.search.getIcon());
		searchAction.setToolTipText(getMessage(getSearchWmsSearchButtonTooltipKey()));
		searchAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchForWmsServers();
			}
		});
		
		cancelSearchAction = new BasicAction(getMessage(getSearchWmsCancelSearchButtonLabelKey()), Icons.remove.getIcon());
		cancelSearchAction.setToolTipText(getMessage(getSearchWmsCancelSearchButtonTooltipKey()));
		cancelSearchAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (currentSearch != null)
				{
					currentSearch.cancel();
				}
			}
		});
		
		okAction = new BasicAction(getMessage(getTermOkKey()), null);
		okAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.OK_OPTION;
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
		
		selectAllAction = new BasicAction(getMessage(getSearchWmsSelectAllLabelKey()), Icons.checkall.getIcon());
		selectAllAction.setToolTipText(getMessage(getSearchWmsSelectAllTooltipKey()));
		selectAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchResultsLock.writeLock().lock();
				try
				{
					selectedResults.clear();
					for (SearchResultPanel result : searchResultPanels)
					{
						result.select();
					}
				}
				finally
				{
					searchResultsLock.writeLock().unlock();
				}
				validate();
				repaint();
			}
		});
		
		deselectAllAction = new BasicAction(getMessage(getSearchWmsDeselectAllLabelKey()), Icons.uncheckall.getIcon());
		deselectAllAction.setToolTipText(getMessage(getSearchWmsDeselectAllTooltipKey()));
		deselectAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchResultsLock.writeLock().lock();
				try
				{
					selectedResults.clear();
					for (SearchResultPanel result : searchResultPanels)
					{
						result.deselect();
					}
				}
				finally
				{
					searchResultsLock.writeLock().unlock();
				}
				validate();
				repaint();
			}
		});
		
		editCatalogueListAction = new BasicAction(getMessage(getSearchWmsEditCswListLabelKey()), null);
		editCatalogueListAction.setToolTipText(getMessage(getSearchWmsEditCswListTooltipKey()));
		editCatalogueListAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Cancel any current search
				if (currentSearch != null)
				{
					currentSearch.cancel();
				}
				
				// Show the CSW dialog
				CswCatalogueListDialog catalogueDialog = new CswCatalogueListDialog();
				catalogueDialog.setVisible(true);
				
				int response = catalogueDialog.getResponse();
				if (response == JOptionPane.CANCEL_OPTION)
				{
					return;
				}
				
				// Update the search service
				searchService = createDefaultSearchService();
			}
		});
	}
	
	private void initialiseSearchingIndicator()
	{
		searchingIndicator = new JPanel();
		searchingIndicator.setOpaque(false);
		
		JButton cancelSearchButton = new JButton(cancelSearchAction);
		cancelSearchButton.setHideActionText(true);
		
		searchingIndicator.add(new JLabel(getMessage(getSearchWmsSearchingMsgKey()), Icons.newLoadingIcon(), SwingConstants.CENTER));
		searchingIndicator.add(cancelSearchButton);
	}

	private void addBlurb(GridBagConstraints containerConstraints)
	{
		JLabel blurb = new JLabel(getMessage(getSearchWmsBlurbKey(), getMessage(getSearchWmsTitleKey()), getMessage(getSearchWmsEditCswListLabelKey())));
		blurb.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(10, 30, 10, 0)));
		
		containerConstraints.gridy++;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		containerConstraints.ipadx = 20;
		contentPane.add(blurb, containerConstraints);
	}
	
	private void addSearchBar(GridBagConstraints containerConstraints)
	{
		Container container = new JPanel();
		container.setLayout(new GridBagLayout());
		
		searchField = new HintTextField(getMessage(getSearchWmsSearchBoxMsgKey()));
		searchField.setColumns(50);
		searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doSearch");
		searchField.getActionMap().put("doSearch", searchAction);
		
		GridBagConstraints searchFieldConstraints = new GridBagConstraints();
		searchFieldConstraints.gridy = 0;
		searchFieldConstraints.gridx = 0;
		searchFieldConstraints.anchor = GridBagConstraints.NORTH;
		searchFieldConstraints.fill = GridBagConstraints.BOTH;
		container.add(searchField, searchFieldConstraints);
		
		searchButton = new JButton(searchAction);
		GridBagConstraints searchButtonConstraints = new GridBagConstraints();
		searchButtonConstraints.gridy = 0;
		searchButtonConstraints.gridx = 1;
		searchButtonConstraints.anchor = GridBagConstraints.NORTH;
		searchButtonConstraints.fill = GridBagConstraints.NONE;
		container.add(searchButton, searchButtonConstraints);
		
		containerConstraints.gridy++;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		contentPane.add(container, containerConstraints);
	}

	private void addResultsPanel(GridBagConstraints containerConstraints)
	{
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setBackground(Color.WHITE);
		
		resultScroller = new JScrollPane(resultsPanel);
		resultScroller.setOpaque(false);
		
		containerConstraints.gridy++;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 1;
		containerConstraints.weightx = 1;
		containerConstraints.ipady = 10;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.BOTH;
		contentPane.add(resultScroller, containerConstraints);
	}
	
	private void addButtonPanel(GridBagConstraints containerConstraints)
	{
		JToolBar utilityPanel = new JToolBar();

		utilityPanel.setFloatable(false);
		utilityPanel.setRollover(true);
		utilityPanel.add(selectAllAction);
		utilityPanel.add(deselectAllAction);
		
		if (catalogueEditAllowed)
		{
			JButton catalogueButton = utilityPanel.add(editCatalogueListAction);
			catalogueButton.setContentAreaFilled(false);
		}
		
		utilityPanel.add(Box.createHorizontalGlue());
		
		resultCountLabel = new JLabel();
		resultCountLabel.setFont(resultCountLabel.getFont().deriveFont(Font.ITALIC).deriveFont(10f));
		resultCountLabel.setOpaque(false);
		utilityPanel.add(resultCountLabel);
		
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		JButton okButton = new JButton(okAction);
		JButton cancelButton = new JButton(cancelAction);
		okCancelPanel.add(okButton);
		okCancelPanel.add(cancelButton);
		
		utilityPanel.add(okCancelPanel);
		
		containerConstraints.gridy++;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.weightx = 0;
		containerConstraints.anchor = GridBagConstraints.BASELINE_TRAILING;
		containerConstraints.fill = GridBagConstraints.BOTH;
		contentPane.add(utilityPanel, containerConstraints);
	}
	
	/**
	 * @return A default search service that first attempts direct URL lookup, then CSW catalogue searching
	 */
	private WmsServerSearchService createDefaultSearchService()
	{
		ChainingSearchService searchService = new ChainingSearchService();
		searchService.addService(new DirectUrlSearchService());
		
		CompoundSearchService cswCatalogueSearchService = new CompoundSearchService();
		for (URL cswCatalogueUrl : WmsBrowserSettings.get().getCswCatalogueServers())
		{
			cswCatalogueSearchService.addService(new CSWSearchService(cswCatalogueUrl));
		}
		searchService.addService(cswCatalogueSearchService);
		
		return searchService;
	}
	
	private void updateSearchResultsPanel()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				try
				{
					searchResultsLock.readLock().lock();
					
					// If no results, show the appropriate message
					if (searchResults.isEmpty())
					{
						showNoResultsMessage();
						return;
					}

					// Otherwise, add each result in order
					resultsPanel.removeAll();
					searchResultPanels.clear();

					GridBagConstraints constraints = new GridBagConstraints();
					constraints.anchor = GridBagConstraints.LINE_START;
					constraints.gridx = 0;
					constraints.gridy = 0;
					constraints.ipady = 10;
					constraints.fill = GridBagConstraints.HORIZONTAL;
					constraints.weightx = 1;
					for (WmsServerSearchResult searchResult : searchResults)
					{
						constraints.gridx = 0;
						SearchResultPanel searchResultPanel = new SearchResultPanel(searchResult);
						searchResultPanel.setBackground(constraints.gridy % 2 == 0 ? STRIPE_EVEN : STRIPE_ODD);
						resultsPanel.add(searchResultPanel, constraints);
						searchResultPanels.add(searchResultPanel);
						constraints.gridy++;
					}
					
					constraints.weighty = 1;
					constraints.fill = GridBagConstraints.BOTH;
					resultsPanel.add(Box.createVerticalGlue(), constraints);
					
					resultCountLabel.setText(getMessage(getSearchWmsResultCountMsgKey(), searchResults.size()));
					
					validate();
					repaint();
				}
				catch (Throwable e)
				{
					logger.log(Level.FINE, "Exception occurred during updateSearchResultsPanel", e);
				}
				finally
				{
					searchResultsLock.readLock().unlock();
				}
			}
		});
	}
	
	private void showNoResultsMessage()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				resultsPanel.removeAll();
				resultsPanel.add(noResultsMessage);
				resultCountLabel.setText(getMessage(getSearchWmsResultCountMsgKey(), 0));
				validate();
				repaint();
			}
		});
	}
	
	private void showSearchingMessage()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				resultsPanel.removeAll();
				resultsPanel.add(searchingIndicator);
				validate();
				repaint();
			}
		});
	}
	
	/**
	 * Search for WMS servers using the search term in the search box via the registered search service
	 */
	private void searchForWmsServers()
	{
		if (currentSearch != null && !currentSearch.isDone())
		{
			currentSearch.cancel();
		}
		currentSearch = new SearchTask(searchField.getText());
		searcher.submit(currentSearch);
	}
	
	private void setSearchResults(List<WmsServerSearchResult> results)
	{
		try
		{
			searchResultsLock.writeLock().lock();
			searchResults.clear();
			searchResults.addAll(results);
			
			// Update the selected results list to include only those present in the new results
			List<WmsServer> tmpSelectedResults = new ArrayList<WmsServer>();
			for (WmsServerSearchResult searchResult : searchResults)
			{
				if (selectedResults.contains(searchResult.getWmsServer()))
				{
					tmpSelectedResults.add(searchResult.getWmsServer());
				}
			}
			selectedResults = tmpSelectedResults;
		}
		finally
		{
			searchResultsLock.writeLock().unlock();
		}
	}
	
	/**
	 * @return The list of selected servers chosen by the user
	 */
	public List<WmsServer> getSelectedServers()
	{
		 return selectedResults;
	}
	
	private boolean isSelected(WmsServer server)
	{
		try
		{
			searchResultsLock.readLock().lock();
			return selectedResults.contains(server);
		}
		finally
		{
			searchResultsLock.readLock().unlock();
		}
	}
	
	private void toggleSelection(WmsServer server)
	{
		if (isSelected(server))
		{
			deselect(server);
		}
		else
		{
			select(server);
		}
	}
	
	private void select(WmsServer server)
	{
		try
		{
			searchResultsLock.writeLock().lock();
			if (!selectedResults.contains(server));
			{
				selectedResults.add(server);
			}
		}
		finally
		{
			searchResultsLock.writeLock().unlock();
		}
	}
	
	private void deselect(WmsServer server)
	{
		try
		{
			searchResultsLock.writeLock().lock();
			selectedResults.remove(server);
		}
		finally
		{
			searchResultsLock.writeLock().unlock();
		}
	}
	
	public int getResponse()
	{
		return response;
	}
	
	/**
	 * A runnable task that performs a search with the given search string
	 */
	private class SearchTask implements Runnable
	{
		private ExecutorService executor;
		private String searchString;

		private Future<List<WmsServerSearchResult>> searchFuture; 
		
		public SearchTask(String searchString)
		{
			this.searchString = searchString;
			executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("WMS search task - " + searchString));
		}
		
		@Override
		public void run()
		{
			// Return immediately if nothing to search on
			if (Util.isBlank(searchString))
			{
				return;
			}
			
			// Cache the old results so we can restore them if the task is cancelled
			List<WmsServerSearchResult> oldResults = new ArrayList<WmsServerSearchResult>(searchResults);
			
			showSearchingMessage();
			
			// Perform the search on a separate thread
			searchFuture = executor.submit(new Callable<List<WmsServerSearchResult>>(){
				@Override
				public List<WmsServerSearchResult> call() throws Exception
				{
					List<WmsServerSearchResult> searchResults = searchService.searchForServers(searchString);
					return searchResults;
				}
			});
			
			List<WmsServerSearchResult> searchResults = oldResults;
			try
			{
				searchResults = searchFuture.get();
			}
			catch (Exception e)
			{
				if (!(e instanceof InterruptedException))
				{
					logger.log(Level.FINE, e.getMessage());
				}
				// Nothing - task has been cancelled and original results will be restored
			}
			
			// Update the results as appropriate
			setSearchResults(searchResults);
			updateSearchResultsPanel();
			
			executor.shutdownNow();
		}
		
		public void cancel()
		{
			searchFuture.cancel(true);
		}
		
		public boolean isDone()
		{
			return searchFuture == null || searchFuture.isDone();
		}
	}
	
	/**
	 * A component that displays a single server search result
	 * <p/>
	 * Presents some metadata, along with a checkbox to select a result for 
	 */
	private class SearchResultPanel extends JPanel
	{
		private static final long serialVersionUID = 20101124L;

		private final Font HEADING_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, UIManager.getFont("Label.font").getSize2D() * 1.4f);
		private final Font PUBLISHER_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
		private final Font URL_FONT = UIManager.getFont("Label.font").deriveFont(Font.ITALIC);
		
		private WmsServerSearchResult searchResult;

		private JCheckBox selector;

		public SearchResultPanel(WmsServerSearchResult searchResult)
		{
			Validate.notNull(searchResult, "A server is required");
			this.searchResult = searchResult;
			
			setLayout(new GridBagLayout());
			
			addAbstract();
			addHeading();
			addUrl();
			
			addPublisher();
			addSelector();
		}

		public void select()
		{
			selector.setSelected(true);
			SearchWmsServerDialog.this.select(searchResult.getWmsServer());
		}
		
		public void deselect()
		{
			selector.setSelected(false);
			SearchWmsServerDialog.this.deselect(searchResult.getWmsServer());
		}
		
		private void addHeading()
		{
			SelectableLabel headingLabel = new SelectableLabel(searchResult.getTitle());
			headingLabel.setFont(HEADING_FONT);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 0;
			add(headingLabel, constraints);
		}

		private void addUrl()
		{
			SelectableLabel urlLabel = new SelectableLabel(searchResult.getCapabilitiesUrl().toExternalForm());
			urlLabel.setFont(URL_FONT);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 0;
			add(urlLabel, constraints);
		}

		private void addAbstract()
		{
			SelectableLabel abstractLabel = new SelectableLabel(searchResult.getAbstract());
			abstractLabel.setLineWrap(true);
			abstractLabel.setWrapStyleWord(true);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 2;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			constraints.weighty = 1;
			add(abstractLabel, constraints);
		}

		private void addPublisher()
		{
			if (Util.isBlank(searchResult.getPublisher()))
			{
				return;
			}
			
			SelectableLabel publisherLabel = new SelectableLabel(searchResult.getPublisher());
			publisherLabel.setFont(PUBLISHER_FONT);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 3;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 0;
			constraints.weighty = 1;
			add(publisherLabel, constraints);
		}
		
		private void addSelector()
		{
			selector = new JCheckBox();
			selector.setOpaque(false);
			selector.setSelected(isSelected(searchResult.getWmsServer()));
			selector.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					toggleSelection(searchResult.getWmsServer());
				}
			});
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridheight = GridBagConstraints.REMAINDER;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.NONE;
			constraints.weightx = 1;
			constraints.weighty = 1;
			add(selector, constraints);
		}
	}
}
