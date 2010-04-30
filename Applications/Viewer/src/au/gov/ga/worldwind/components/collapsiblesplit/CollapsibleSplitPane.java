package au.gov.ga.worldwind.components.collapsiblesplit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

public class CollapsibleSplitPane extends JPanel implements CollapseListener
{
	private boolean continuousLayout = true;
	private boolean dragging = false;
	private int dragIndex;
	private Point dragOffset = new Point();
	private Point dragStart = new Point();
	private Point dragActual;
	private Dimension dividerDimensions = new Dimension();
	private DividerPainter dividerPainter;

	public CollapsibleSplitPane()
	{
		super(new CollapsibleSplitLayout());
		dividerPainter = new DefaultDividerPainter();
		InputHandler inputHandler = new InputHandler();
		addMouseListener(inputHandler);
		addMouseMotionListener(inputHandler);
		addKeyListener(inputHandler);
		setFocusable(true);
	}

	@Override
	public CollapsibleSplitLayout getLayout()
	{
		return (CollapsibleSplitLayout) super.getLayout();
	}

	@Override
	public void setLayout(LayoutManager mgr)
	{
		if (!(mgr instanceof CollapsibleSplitLayout))
			throw new IllegalArgumentException("layout must be instance of "
					+ CollapsibleSplitLayout.class.getSimpleName());
		super.setLayout(mgr);
	}

	@Override
	public void collapseToggled(ICollapsible component, boolean collapsed)
	{
		if (!(component instanceof Component)) //redundant, but here just in case subclasses do something wrong
			throw new IllegalArgumentException(
					"ICollapsible is not an instance of Component");

		String placeholderName = getLayout().getPlaceholderName(
				(Component) component);
		int dividerAbove = getLayout().dividerAboveComponentBounds(
				(Component) component);
		System.out.println("DIVIDER ABOVE = " + dividerAbove);
		Rectangle bounds = null;
		if (dividerAbove >= 0)
		{
			bounds = getLayout().dividerBounds(dividerAbove);
		}
		getLayout().setExpanded(placeholderName, !component.isCollapsed());
		doLayout();
		if (bounds != null && dividerAbove == 0)
		{
			System.out.println(getLayout().setDividerPosition(dividerAbove, bounds.x, bounds.y));
		}
		doLayout();
	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index)
	{
		super.addImpl(comp, constraints, index);
		if (comp != null && comp instanceof ICollapsible)
		{
			((ICollapsible) comp).addCollapseListener(this);
		}
	}

	@Override
	public void remove(int index)
	{
		Component c = getComponent(index);
		if (c != null && c instanceof ICollapsible)
		{
			((ICollapsible) c).removeCollapseListener(this);
		}
		super.remove(index);
	}

	public boolean isContinuousLayout()
	{
		return continuousLayout;
	}

	public void setContinuousLayout(boolean continuousLayout)
	{
		this.continuousLayout = continuousLayout;
	}

	@Override
	protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		if (dragging && !isContinuousLayout() && dragActual != null)
		{
			Rectangle bounds = new Rectangle(dragActual, dividerDimensions);
			dividerPainter.paint(g, bounds);
		}
	}

	public static abstract class DividerPainter
	{
		public abstract void paint(Graphics g, Rectangle bounds);
	}

	private class DefaultDividerPainter extends DividerPainter
	{
		public void paint(Graphics g, Rectangle bounds)
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.black);
			g2d.fill(bounds);
		}
	}

	private void updateCursor(int x, int y, boolean inside)
	{
		if (dragging)
		{
			return;
		}
		int cursorID = Cursor.DEFAULT_CURSOR;
		if (inside)
		{
			int index = getLayout().dividerAt(x, y);
			if (index >= 0)
			{
				cursorID = getLayout().isVertical() ? Cursor.N_RESIZE_CURSOR
						: Cursor.E_RESIZE_CURSOR;
			}
		}
		setCursor(Cursor.getPredefinedCursor(cursorID));
	}

	private void startDrag(int x, int y)
	{
		if (!dragging)
		{
			dragIndex = getLayout().dividerAt(x, y);
			if (dragIndex >= 0)
			{
				Rectangle dividerBounds = getLayout().dividerBounds(dragIndex);
				if (dividerBounds != null)
				{
					dragStart.x = x;
					dragStart.y = y;
					dragOffset.x = dividerBounds.x - x;
					dragOffset.y = dividerBounds.y - y;
					dragging = true;
					dividerDimensions.width = dividerBounds.width;
					dividerDimensions.height = dividerBounds.height;
				}
			}
		}
	}

	private void updateDrag(int x, int y)
	{
		if (dragging)
		{
			setDividerPosition(dragIndex, x + dragOffset.x, y + dragOffset.y);
		}
	}

	private void finishDrag(int x, int y)
	{
		if (dragging)
		{
			setDividerPosition(dragIndex, x + dragOffset.x, y + dragOffset.y);
			dragging = false;
			if (!isContinuousLayout())
			{
				revalidate();
				repaint();
			}
		}
	}

	private void cancelDrag()
	{
		if (dragging)
		{
			setDividerPosition(dragIndex, dragStart.x + dragOffset.x,
					dragStart.y + dragOffset.y);
			dragging = false;
			if (!isContinuousLayout())
			{
				revalidate();
				repaint(); //TODO
			}
		}
	}

	private void setDividerPosition(int index, int x, int y)
	{
		dragActual = getLayout().setDividerPosition(index, x, y);
		if (isContinuousLayout())
		{
			revalidate();
			//repaintDragLimits();
			repaint(); //TODO
		}
		else
		{
			//repaint(oldBounds.union(bounds)); //TODO calculate repaint region for non-continuous layout
			repaint();
		}
	}

	private class InputHandler extends MouseInputAdapter implements KeyListener
	{
		public void mouseEntered(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), true);
		}

		public void mouseMoved(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), true);
		}

		public void mouseExited(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), false);
		}

		public void mousePressed(MouseEvent e)
		{
			startDrag(e.getX(), e.getY());
		}

		public void mouseReleased(MouseEvent e)
		{
			finishDrag(e.getX(), e.getY());
		}

		public void mouseDragged(MouseEvent e)
		{
			updateDrag(e.getX(), e.getY());
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				cancelDrag();
			}
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
		}
	}
}
