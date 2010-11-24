package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HintTextField;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.search.WmsServerSearchService;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;

/**
 * A dialog that allows the user to search for WMS servers, or enter the URL of a WMS server
 */
public class SearchWmsServerDialog extends JDialog
{
	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	private static final long serialVersionUID = 20101124L;

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
	private List<WmsServer> searchResults = new ArrayList<WmsServer>();
	private ReadWriteLock searchResultsLock = new LenientReadWriteLock();
	private JPanel resultsPanel;
	
	public SearchWmsServerDialog(WmsServerSearchService searchService)
	{
		Validate.notNull(searchService, "A search service is required");
		this.searchService = searchService;
		
		initialiseDialog();
		initialiseActions();
		initialiseSearchingIndicator();
		addSearchBar();
		addResultsPanel();
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				searchButton.requestFocus();
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
		searchAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchForWmsServers();
			}
		});
		
		cancelSearchAction = new BasicAction(getMessage(getSearchWmsCancelSearchButtonLabelKey()), Icons.remove.getIcon());
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
		
		searchButton = new JButton(searchAction);
		GridBagConstraints searchButtonConstraints = new GridBagConstraints();
		searchButtonConstraints.gridy = 0;
		searchButtonConstraints.gridx = 1;
		searchButtonConstraints.anchor = GridBagConstraints.NORTH;
		searchButtonConstraints.fill = GridBagConstraints.NONE;
		container.add(searchButton, searchButtonConstraints);
		
		GridBagConstraints containerConstraints = new GridBagConstraints();
		containerConstraints.gridy = 0;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 0;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.HORIZONTAL;
		containerConstraints.ipady = 10;
		contentPane.add(container, containerConstraints);
	}

	private void addResultsPanel()
	{
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setBackground(Color.WHITE);
		
		JScrollPane container = new JScrollPane(resultsPanel);
		container.setOpaque(false);
		
		GridBagConstraints containerConstraints = new GridBagConstraints();
		containerConstraints.gridy = 1;
		containerConstraints.gridx = 0;
		containerConstraints.weighty = 1;
		containerConstraints.weightx = 1;
		containerConstraints.ipady = 10;
		containerConstraints.anchor = GridBagConstraints.NORTH;
		containerConstraints.fill = GridBagConstraints.BOTH;
		contentPane.add(container, containerConstraints);
		
		updateSearchResultsPanel();
	}
	
	private void updateSearchResultsPanel()
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

			for (WmsServer server : searchResults)
			{
				resultsPanel.add(new SearchResult(server));
			}
			
			validate();
			repaint();
		}
		finally
		{
			searchResultsLock.readLock().unlock();
		}
	}
	
	private void showNoResultsMessage()
	{
		resultsPanel.removeAll();
		resultsPanel.add(noResultsMessage);
		validate();
		repaint();
	}
	
	private void showSearchingMessage()
	{
		// TODO: Make display icon
		resultsPanel.removeAll();
		resultsPanel.add(searchingIndicator);
		validate();
		repaint();
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
	
	private void setSearchResults(List<WmsServer> results)
	{
		try
		{
			searchResultsLock.writeLock().lock();
			searchResults.clear();
			searchResults.addAll(results);
		}
		finally
		{
			searchResultsLock.writeLock().unlock();
		}
	}
	
	/**
	 * @return The list of selected servers chosen by the user
	 */
	public List<WmsServerIdentifier> getSelectedServers()
	{
		return new ArrayList<WmsServerIdentifier>();
	}
	
	/**
	 * A runnable task that performs a search with the given search string
	 */
	private class SearchTask implements Runnable
	{
		private ExecutorService executor;
		private String searchString;

		private Future<List<WmsServer>> searchFuture; 
		
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
			List<WmsServer> oldResults = new ArrayList<WmsServer>(searchResults);
			
			showSearchingMessage();
			
			// Perform the search on a separate thread
			searchFuture = executor.submit(new Callable<List<WmsServer>>(){
				@Override
				public List<WmsServer> call() throws Exception
				{
					List<WmsServer> searchResults = searchService.searchForServers(searchString);
					return searchResults;
				}
			});
			
			List<WmsServer> searchResults = oldResults;
			try
			{
				searchResults = searchFuture.get();
			}
			catch (Exception e)
			{
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
			return searchFuture.isDone();
		}
	}
	
	/**
	 * A component that displays a single server search result
	 */
	private static class SearchResult extends JPanel
	{
		private static final long serialVersionUID = 20101124L;

		public SearchResult(WmsServer server)
		{
			add(new JLabel(server.getName()));
		}
	}
}
