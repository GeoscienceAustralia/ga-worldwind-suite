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
package au.gov.ga.worldwind.common.ui.collapsiblesplit;

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

/**
 * {@link JPanel} subclass that acts as a container for {@link ICollapsible}
 * components. It uses the {@link CollapsibleSplitLayout} to layout its
 * children, and implements the draggable dividers between its child components.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	/**
	 * Add the given listener to the given component.
	 * 
	 * @param comp
	 * @param listener
	 */
	public void addListener(Component comp, CollapsibleSplitListener listener)
	{
		getLayout().addListener(comp, listener);
	}

	/**
	 * Remove the given listener from the given component.
	 * 
	 * @param comp
	 * @param listener
	 */
	public void removeListener(Component comp, CollapsibleSplitListener listener)
	{
		getLayout().removeListener(comp, listener);
	}

	@Override
	public void collapseToggled(ICollapsible component, boolean collapsed)
	{
		if (!(component instanceof Component)) //redundant, but here just in case subclasses do something wrong
			throw new IllegalArgumentException("ICollapsible is not an instance of Component");

		int dividerAbove = getLayout().dividerAboveComponentBounds((Component) component);
		Rectangle bounds = null;
		if (dividerAbove >= 0)
		{
			bounds = getLayout().dividerBounds(dividerAbove);
		}
		getLayout().setExpanded((Component) component, !component.isCollapsed());
		doLayout();
		if (bounds != null && dividerAbove == 0)
		{
			getLayout().setDividerPosition(dividerAbove, bounds.x, bounds.y);
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

	/**
	 * @return Should the layout be updated during dragging the dividers, or
	 *         just at the end of the drag?
	 */
	public boolean isContinuousLayout()
	{
		return continuousLayout;
	}

	/**
	 * Enable/disable updating the layout when dragging the dividers. If true,
	 * the layout is updated while dragging, if false it is only updated at the
	 * end of the drag.
	 * 
	 * @param continuousLayout
	 */
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

	/**
	 * Class responsible for painting dividers whilst they are being dragged.
	 */
	public static abstract class DividerPainter
	{
		public abstract void paint(Graphics g, Rectangle bounds);
	}

	/**
	 * Default divider painter, simply fills the divider with black.
	 */
	private class DefaultDividerPainter extends DividerPainter
	{
		@Override
		public void paint(Graphics g, Rectangle bounds)
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.black);
			g2d.fill(bounds);
		}
	}

	/**
	 * Update the cursor when the user is dragging or hovering over a divider.
	 * 
	 * @param x
	 * @param y
	 * @param inside
	 */
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
				cursorID = getLayout().isVertical() ? Cursor.N_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR;
			}
		}
		setCursor(Cursor.getPredefinedCursor(cursorID));
	}

	/**
	 * Called when the user begins dragging a divider.
	 * 
	 * @param x
	 *            mouse x-coordinate
	 * @param y
	 *            mouse y-coordinate
	 */
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

	/**
	 * Called when the mouse moves while dragging a divider.
	 * 
	 * @param x
	 *            mouse x-coordinate
	 * @param y
	 *            mouse y-coordinate
	 */
	private void updateDrag(int x, int y)
	{
		if (dragging)
		{
			setDividerPosition(dragIndex, x + dragOffset.x, y + dragOffset.y);
		}
	}

	/**
	 * Called when the user finishes dragging a divider.
	 * 
	 * @param x
	 *            mouse x-coordinate
	 * @param y
	 *            mouse y-coordinate
	 */
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

	/**
	 * Called when the user cancels a drag operation (ie presses ESC while
	 * dragging).
	 */
	private void cancelDrag()
	{
		if (dragging)
		{
			setDividerPosition(dragIndex, dragStart.x + dragOffset.x, dragStart.y + dragOffset.y);
			dragging = false;
			if (!isContinuousLayout())
			{
				revalidate();
				repaint(); //TODO
			}
		}
	}

	/**
	 * Helper method that calls setDividerPosition on the layout, and then
	 * repaints the component.
	 * 
	 * @param index
	 * @param x
	 * @param y
	 */
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

	/**
	 * Helper class that handles mouse and keyboard input for this component.
	 */
	private class InputHandler extends MouseInputAdapter implements KeyListener
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), true);
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), true);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			updateCursor(e.getX(), e.getY(), false);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			startDrag(e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			finishDrag(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			updateDrag(e.getX(), e.getY());
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				cancelDrag();
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
		}
	}
}
