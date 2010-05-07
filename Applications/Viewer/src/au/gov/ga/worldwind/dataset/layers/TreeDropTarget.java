package au.gov.ga.worldwind.dataset.layers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.image.BufferedImage;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class TreeDropTarget extends DropTarget
{
	private final static int OFFSET = 15;
	private final static int INSERT_AREA_HEIGHT = 12;
	private final static Insets AUTOSCROLL_INSETS = new Insets(20, 20, 20, 20);

	private Rectangle markerBounds;
	private Rectangle imageBounds = new Rectangle();
	private NodeMoveTransferHandler handler;
	private Point mostRecentLocation;

	public TreeDropTarget(NodeMoveTransferHandler handler)
	{
		super();
		this.handler = handler;
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
		JTree tree = (JTree) dtde.getDropTargetContext().getComponent();
		Point location = dtde.getLocation();
		clear(tree);
		updateDragMark(tree, location);
		paintImage(tree, location);
		autoscroll(tree, location);
		super.dragOver(dtde);
	}

	@Override
	public synchronized void dragExit(DropTargetEvent dte)
	{
		clear((JTree) dte.getDropTargetContext().getComponent());
		super.dragExit(dte);
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		clear((JTree) dtde.getDropTargetContext().getComponent());
		super.drop(dtde);
	}

	private final void paintImage(JTree tree, Point point)
	{
		BufferedImage image = handler.getDragImage(tree);
		if (image != null)
		{
			imageBounds.setRect(point.x - OFFSET, point.y - OFFSET, image.getWidth(), image
					.getHeight());
			tree.getGraphics().drawImage(image, imageBounds.x, imageBounds.y, tree);
		}
	}

	private void autoscroll(JTree tree, Point cursor)
	{
		Insets insets = AUTOSCROLL_INSETS;
		Rectangle outer = tree.getVisibleRect();
		Rectangle inner =
				new Rectangle(outer.x + insets.left, outer.y + insets.top, outer.width
						- (insets.left + insets.right), outer.height - (insets.top + insets.bottom));
		if (!inner.contains(cursor))
		{
			Rectangle scrollRect =
					new Rectangle(cursor.x - insets.left, cursor.y - insets.top, insets.left
							+ insets.right, insets.top + insets.bottom);
			tree.scrollRectToVisible(scrollRect);
		}
	}

	public void updateDragMark(JTree tree, Point location)
	{
		mostRecentLocation = location;
		int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x, location.y));
		TreePath path = tree.getPathForRow(row);
		if (path != null)
		{
			Rectangle rowBounds = tree.getPathBounds(path);

			//mark a tree node or draw an insertion marker?
			int y = rowBounds.y;
			int height = INSERT_AREA_HEIGHT / 2;
			if (y - height <= location.y && location.y <= y + height)
			{
				// we are inside an insertArea
				paintInsertMarker(tree, location);
			}
			else
			{
				// we are inside a node
				markNode(tree, location);
			}
		}
	}

	public Point getMostRecentDragLocation()
	{
		return mostRecentLocation;
	}

	private void markNode(JTree tree, Point location)
	{
		TreePath path = tree.getClosestPathForLocation(location.x, location.y);
		if (path != null)
		{
			tree.setSelectionPath(path);
			tree.expandPath(path);
		}
	}

	private void paintInsertMarker(JTree tree, Point location)
	{
		tree.clearSelection();
		int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x, location.y));
		TreePath path = tree.getPathForRow(row);
		if (path != null)
		{
			markerBounds = tree.getPathBounds(path);
			if (markerBounds != null)
			{
				Graphics g = tree.getGraphics();
				g.setColor(Color.black);
				g.drawLine(markerBounds.x, markerBounds.y, markerBounds.x + markerBounds.width,
						markerBounds.y);
			}
		}
	}

	private void clear(JTree tree)
	{
		if (imageBounds != null)
		{
			tree.paintImmediately(imageBounds);
		}
		if (markerBounds != null)
		{
			tree.paintImmediately(markerBounds.x, markerBounds.y, markerBounds.width, 1);
		}
	}
}