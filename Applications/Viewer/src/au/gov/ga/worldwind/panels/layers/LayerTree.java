package au.gov.ga.worldwind.panels.layers;

import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;

import au.gov.ga.worldwind.components.lazytree.LoadingTree;

public class LayerTree extends LoadingTree
{
	public LayerTree(INode root, LayerEnabler enabler)
	{
		super();
		LayerTreeModel model = new LayerTreeModel(this, root, enabler);
		setModel(model);

		setUI(new ClearableBasicTreeUI());
		setCellRenderer(new LayerCellRenderer());
		setCellEditor(new LayerTreeCellEditor(this));
		setEditable(true);
		setShowsRootHandles(true);
		setRootVisible(false);
		setRowHeight(0);
		addTreeExpansionListener(model);
		model.expandNodes();
	}

	@Override
	public LayerTreeModel getModel()
	{
		return (LayerTreeModel) super.getModel();
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
}
