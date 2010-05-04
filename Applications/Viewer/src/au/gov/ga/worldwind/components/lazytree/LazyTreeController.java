package au.gov.ga.worldwind.components.lazytree;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingworker.SwingWorker;

import au.gov.ga.worldwind.components.lazytree.layertree.LazyLoadException;

public class LazyTreeController implements TreeWillExpandListener
{
	private SwingWorkerFactory<MutableTreeNode[], ?> workerFactory = new DefaultWorkerFactory();
	private DefaultTreeModel model;

	public LazyTreeController(DefaultTreeModel model)
	{
		this.model = model;
	}

	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException
	{
		TreePath path = event.getPath();
		Object lastPathComponent = path.getLastPathComponent();
		if (lastPathComponent instanceof LazyTreeNode)
		{
			collapseNode((LazyTreeNode) lastPathComponent, model);
		}
	}

	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException
	{
		TreePath path = event.getPath();
		Object lastPathComponent = path.getLastPathComponent();
		if (lastPathComponent instanceof LazyTreeNode)
		{
			expandNode((LazyTreeNode) lastPathComponent, model);
		}
	}

	public void collapseNode(final LazyTreeNode node,
			final DefaultTreeModel model)
	{
		if (node.isErrorLoading())
		{
			node.reset();
		}
	}

	public void expandNode(final LazyTreeNode node, final DefaultTreeModel model)
	{
		if (node.areChildrenLoaded())
		{
			return;
		}
		node.setChildren(createLoadingNode());
		createSwingWorker(node).execute();
	}

	protected MutableTreeNode createLoadingNode()
	{
		return new DefaultMutableTreeNode("Loading ...", false);
	}

	protected MutableTreeNode createErrorNode(Exception e)
	{
		String text;
		if (e instanceof LazyLoadException)
		{
			text = e.getMessage();
		}
		else
		{
			text = "Error: " + e.getLocalizedMessage();
		}
		return new DefaultMutableTreeNode(text, false);
	}

	protected IWorker<MutableTreeNode[]> getWorkerInterface(
			final LazyTreeNode node)
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
			}

			@Override
			public void error(Exception e)
			{
				node.setErrorLoading(true);
				node.setChildren(createErrorNode(e));
			}
		};
	}

	protected SwingWorker<MutableTreeNode[], ?> createSwingWorker(
			final LazyTreeNode node)
	{
		return getWorkerFactory().getInstance(getWorkerInterface(node));
	}

	public SwingWorkerFactory<MutableTreeNode[], ?> getWorkerFactory()
	{
		return workerFactory;
	}

	public void setWorkerFactory(
			SwingWorkerFactory<MutableTreeNode[], ?> workerFactory)
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

	public static class DefaultWorkerFactory implements
			SwingWorkerFactory<MutableTreeNode[], Object>
	{
		public SwingWorker<MutableTreeNode[], Object> getInstance(
				final IWorker<MutableTreeNode[]> worker)
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
