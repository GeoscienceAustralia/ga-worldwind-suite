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
package au.gov.ga.worldwind.common.ui.lazytree;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Helper class for the {@link LazyTree}. Generates {@link SwingWorker}s used to
 * load lazy node's children whenever a lazy node is expanded.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyTreeController implements TreeWillExpandListener
{
	private SwingWorkerFactory<MutableTreeNode[], ?> workerFactory = new DefaultWorkerFactory();
	private LazyTree tree;
	private LazyTreeModel model;

	public LazyTreeController(LazyTree tree, LazyTreeModel model)
	{
		this.tree = tree;
		this.model = model;
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
	{
		TreePath path = event.getPath();
		Object lastPathComponent = path.getLastPathComponent();
		if (lastPathComponent instanceof LazyTreeNode)
		{
			collapseNode((LazyTreeNode) lastPathComponent);
		}
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
	{
		TreePath path = event.getPath();
		Object lastPathComponent = path.getLastPathComponent();
		if (lastPathComponent instanceof LazyTreeNode)
		{
			expandNode((LazyTreeNode) lastPathComponent);
		}
	}

	protected void collapseNode(final LazyTreeNode node)
	{
		//if expanding this node last time caused an error, collapsing it should reset it
		//so that next time it is expanded it can attempt loading again
		if (node.isErrorLoading())
		{
			node.reset();
		}
	}

	protected void expandNode(final LazyTreeNode node)
	{
		if (node.areChildrenLoaded())
		{
			return;
		}
		//show a loading node, and then try loading the children
		node.setChildren(createLoadingNode());
		createSwingWorker(node).execute();
	}

	protected MutableTreeNode createLoadingNode()
	{
		return new LoadingNode("Loading ...");
	}

	protected MutableTreeNode createErrorNode(Exception e)
	{
		String text;
		if (e instanceof LazyLoadException)
		{
			text = e.getMessage();
		}
		else if (e instanceof ExecutionException && e.getCause() instanceof LazyLoadException)
		{
			text = e.getCause().getMessage();
		}
		else
		{
			text = "Error: " + e.getLocalizedMessage();
		}
		return new ErrorNode(text);
	}

	protected SwingWorker<MutableTreeNode[], ?> createSwingWorker(LazyTreeNode node)
	{
		return getWorkerFactory().getInstance(getWorkerInterface(node));
	}

	protected IWorker<MutableTreeNode[]> getWorkerInterface(final LazyTreeNode node)
	{
		return new IWorker<MutableTreeNode[]>()
		{
			@Override
			public MutableTreeNode[] work() throws Exception
			{
				return node.loadChildren(model);
			}

			@Override
			public void done(MutableTreeNode[] nodes)
			{
				node.setChildren(nodes);
				tree.getUI().relayout();
			}

			@Override
			public void error(Exception e)
			{
				node.setErrorLoading(true);
				node.setChildren(createErrorNode(e));
			}
		};
	}

	public SwingWorkerFactory<MutableTreeNode[], ?> getWorkerFactory()
	{
		return workerFactory;
	}

	public void setWorkerFactory(SwingWorkerFactory<MutableTreeNode[], ?> workerFactory)
	{
		this.workerFactory = workerFactory;
	}

	public interface IWorker<T>
	{
		public void done(T result);

		public void error(Exception e);

		public T work() throws Exception;
	}

	public interface SwingWorkerFactory<T, V>
	{
		public SwingWorker<T, V> getInstance(final IWorker<T> worker);
	}

	public static class DefaultWorkerFactory implements SwingWorkerFactory<MutableTreeNode[], Object>
	{
		@Override
		public SwingWorker<MutableTreeNode[], Object> getInstance(final IWorker<MutableTreeNode[]> worker)
		{
			return new SwingWorker<MutableTreeNode[], Object>()
			{
				@Override
				protected void done()
				{
					try
					{
						worker.done(get());
					}
					catch (Exception e)
					{
						worker.error(e);
					}
				}

				@Override
				protected MutableTreeNode[] doInBackground() throws Exception
				{
					return worker.work();
				}
			};
		}
	}
}
