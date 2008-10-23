package application;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.retrieve.BasicRetrievalService;
import gov.nasa.worldwind.retrieve.RetrievalFuture;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class ReportingRetrievalService implements RetrievalService
{
	private RetrievalService impl;
	private Set<Retriever> retrievers = new HashSet<Retriever>();
	
	public ReportingRetrievalService()
	{
		impl = new BasicRetrievalService();
	}
	
	public Set<Retriever> getRetrievers()
	{
		Set<Retriever> retain = new HashSet<Retriever>();
		for(Retriever retriever : retrievers)
		{
			if(contains(retriever))
			{
				retain.add(retriever);
			}
		}
		retrievers.retainAll(retain);
		return retain;
	}

	public boolean contains(Retriever retriever)
	{
		return impl.contains(retriever);
	}

	public int getNumRetrieversPending()
	{
		return impl.getNumRetrieversPending();
	}

	public int getRetrieverPoolSize()
	{
		return impl.getRetrieverPoolSize();
	}

	public boolean hasActiveTasks()
	{
		return impl.hasActiveTasks();
	}

	public boolean isAvailable()
	{
		return impl.isAvailable();
	}

	public RetrievalFuture runRetriever(Retriever retriever)
	{
		retrievers.add(retriever);
		return impl.runRetriever(retriever);
	}

	public RetrievalFuture runRetriever(Retriever retriever, double priority)
	{
		retrievers.add(retriever);
		return impl.runRetriever(retriever, priority);
	}

	public void setRetrieverPoolSize(int poolSize)
	{
		impl.setRetrieverPoolSize(poolSize);
	}

	public void shutdown(boolean immediately)
	{
		impl.shutdown(immediately);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener)
	{
		impl.addPropertyChangeListener(propertyName, listener);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		impl.addPropertyChangeListener(listener);
	}

	public AVList clearList()
	{
		return impl.clearList();
	}

	public AVList copy()
	{
		return impl.copy();
	}

	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue)
	{
		impl.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		impl.firePropertyChange(propertyChangeEvent);
	}

	public Set<Entry<String, Object>> getEntries()
	{
		return impl.getEntries();
	}

	public String getStringValue(String key)
	{
		return impl.getStringValue(key);
	}

	public Object getValue(String key)
	{
		return impl.getValue(key);
	}

	public Collection<Object> getValues()
	{
		return impl.getValues();
	}

	public boolean hasKey(String key)
	{
		return impl.hasKey(key);
	}

	public void removeKey(String key)
	{
		impl.removeKey(key);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener)
	{
		impl.removePropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		impl.removePropertyChangeListener(listener);
	}

	public void setValue(String key, Object value)
	{
		impl.setValue(key, value);
	}

	public void setValues(AVList avList)
	{
		impl.setValues(avList);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		impl.propertyChange(evt);
	}
	
}
