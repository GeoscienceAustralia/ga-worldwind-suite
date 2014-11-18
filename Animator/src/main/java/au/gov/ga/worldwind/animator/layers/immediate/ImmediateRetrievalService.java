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
package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.retrieve.BasicRetrievalService;
import gov.nasa.worldwind.retrieve.RetrievalFuture;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.FutureTask;

/**
 * A {@link RetrievalService} that blocks on calls to
 * {@link #runRetriever(Retriever)}, returning only when retrieval has
 * completed.
 * <p/>
 * Delegates to an internal instance of the {@link BasicRetrievalService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ImmediateRetrievalService implements RetrievalService
{
	private final RetrievalService delegate = new BasicRetrievalService();

	@Override
	public void onMessage(Message msg)
	{
		delegate.onMessage(msg);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		delegate.propertyChange(evt);
	}

	@Override
	public void setRetrieverPoolSize(int poolSize)
	{
		delegate.setRetrieverPoolSize(poolSize);
	}

	@Override
	public int getRetrieverPoolSize()
	{
		return delegate.getRetrieverPoolSize();
	}

	@Override
	public boolean hasActiveTasks()
	{
		return delegate.hasActiveTasks();
	}

	@Override
	public boolean isAvailable()
	{
		return delegate.isAvailable();
	}

	@Override
	public boolean contains(Retriever retriever)
	{
		return delegate.contains(retriever);
	}

	@Override
	public int getNumRetrieversPending()
	{
		return delegate.getNumRetrieversPending();
	}

	@Override
	public void shutdown(boolean immediately)
	{
		delegate.shutdown(immediately);
	}

	@Override
	public Collection<Object> getValues()
	{
		return delegate.getValues();
	}

	@Override
	public String getStringValue(String key)
	{
		return delegate.getStringValue(key);
	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{
		return delegate.getEntries();
	}

	@Override
	public boolean hasKey(String key)
	{
		return delegate.hasKey(key);
	}

	@Override
	public Object removeKey(String key)
	{
		return delegate.removeKey(key);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		delegate.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		delegate.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		delegate.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		delegate.removePropertyChangeListener(listener);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		delegate.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		delegate.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public AVList copy()
	{
		return delegate.copy();
	}

	@Override
	public AVList clearList()
	{
		return delegate.clearList();
	}

	@Override
	public RetrievalFuture runRetriever(Retriever retriever)
	{
		if (retriever == null)
		{
			String msg = Logging.getMessage("nullValue.RetrieverIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}
		if (retriever.getName() == null)
		{
			String message = Logging.getMessage("nullValue.RetrieverNameIsNull");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		// Add with secondary priority that removes most recently added requests first.
		return this.runRetriever(retriever, (Long.MAX_VALUE - System.currentTimeMillis()));
	}

	@Override
	public synchronized RetrievalFuture runRetriever(Retriever retriever, double priority)
	{
		if (!ImmediateMode.isImmediate())
			return delegate.runRetriever(retriever, priority);

		RetrievalTask task = new RetrievalTask(retriever);
		task.run(); //run the task
		Logging.logger().info("Running retrieval immediately: " + retriever.getName());
		return task;
	}

	@Override
	public Object setValue(String key, Object value)
	{
		return delegate.setValue(key, value);
	}

	@Override
	public AVList setValues(AVList avList)
	{
		return delegate.setValues(avList);
	}

	@Override
	public Object getValue(String key)
	{
		return delegate.getValue(key);
	}

	private static class RetrievalTask extends FutureTask<Retriever> implements RetrievalFuture
	{
		private Retriever retriever;

		private RetrievalTask(Retriever retriever)
		{
			super(retriever);
			this.retriever = retriever;
		}

		@Override
		public Retriever getRetriever()
		{
			return this.retriever;
		}
	}

	@Override
	public void setSSLExceptionListener(SSLExceptionListener listener)
	{
		delegate.setSSLExceptionListener(listener);
	}

	@Override
	public SSLExceptionListener getSSLExceptionListener()
	{
		return delegate.getSSLExceptionListener();
	}
}
