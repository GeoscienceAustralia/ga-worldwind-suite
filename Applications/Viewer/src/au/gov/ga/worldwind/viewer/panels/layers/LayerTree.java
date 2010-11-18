package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;

import au.gov.ga.worldwind.common.ui.lazytree.LoadingTree;
import au.gov.ga.worldwind.common.util.Loader.LoadingListener;

public class LayerTree extends LoadingTree implements LoadingListener
{
	private final LayerEnabler enabler;

	public LayerTree(WorldWindow wwd, INode root)
	{
		super();
		setUI(new ClearableBasicTreeUI());

		enabler = new LayerEnabler(this, wwd);
		LayerTreeModel model = new LayerTreeModel(this, root, enabler);
		setModel(model);

		setCellRenderer(new LayerCellRenderer());
		setCellEditor(new LayerTreeCellEditor(this));
		setEditable(true);
		setShowsRootHandles(true);
		setRootVisible(false);
		setRowHeight(0);
		addTreeExpansionListener(model);
		model.expandNodes();
	}

	public LayerEnabler getEnabler()
	{
		return enabler;
	}

	public LayerTreeModel getLayerModel()
	{
		return (LayerTreeModel) getModel();
	}

	@Override
	public ClearableBasicTreeUI getUI()
	{
		return (ClearableBasicTreeUI) super.getUI();
	}

	public LayerCellRenderer getLayerCellRenderer()
	{
		return (LayerCellRenderer) getCellRenderer();
	}

	private static class LayerTreeCellEditor extends DefaultTreeCellEditor
	{
		public LayerTreeCellEditor(JTree tree)
		{
			super(tree, null);
		}

		@Override
		protected boolean shouldStartEditingTimer(EventObject event)
		{
			//only allow editing with edit button, not with mouse click
			return false;
		}
	}

	@Override
	public void loadingStateChanged(boolean isLoading)
	{
		repaint();
	}
}
