/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.ga.worldwind.common.ui.collapsiblesplit.l2fprod;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

/**
 * Basic implementation of the <code>JLinkButton</code> UI. <br>
 */
public class LinkButtonUI extends BasicButtonUI
{
	public static ComponentUI createUI(JComponent c)
	{
		return new LinkButtonUI();
	}

	private static Rectangle viewRect = new Rectangle();
	private static Rectangle textRect = new Rectangle();
	private static Rectangle iconRect = new Rectangle();
	private static MouseListener handCursorListener = new HandCursor();

	protected int dashedRectGapX;
	protected int dashedRectGapY;
	protected int dashedRectGapWidth;
	protected int dashedRectGapHeight;
	private Color focusColor;

	@Override
	protected void installDefaults(AbstractButton b)
	{
		super.installDefaults(b);

		b.setOpaque(false);
		b.setBorderPainted(false);
		b.setRolloverEnabled(true);

		dashedRectGapX = UIManager.getInt("ButtonUI.dashedRectGapX");
		dashedRectGapY = UIManager.getInt("ButtonUI.dashedRectGapY");
		dashedRectGapWidth = UIManager.getInt("ButtonUI.dashedRectGapWidth");
		dashedRectGapHeight = UIManager.getInt("ButtonUI.dashedRectGapHeight");
		focusColor = UIManager.getColor("ButtonUI.focus");

		b.setHorizontalAlignment(AbstractButton.LEFT);
	}

	@Override
	protected void installListeners(AbstractButton b)
	{
		super.installListeners(b);
		b.addMouseListener(handCursorListener);
	}

	@Override
	protected void uninstallListeners(AbstractButton b)
	{
		super.uninstallListeners(b);
		b.removeMouseListener(handCursorListener);
	}

	protected Color getFocusColor()
	{
		return focusColor;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		AbstractButton b = (AbstractButton) c;
		ButtonModel model = b.getModel();

		FontMetrics fm = g.getFontMetrics();

		Insets i = c.getInsets();

		viewRect.x = i.left;
		viewRect.y = i.top;
		viewRect.width = b.getWidth() - (i.right + viewRect.x);
		viewRect.height = b.getHeight() - (i.bottom + viewRect.y);

		textRect.x = textRect.y = textRect.width = textRect.height = 0;
		iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

		Font f = c.getFont();
		g.setFont(f);

		// layout the text and icon
		String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b
				.getIcon(), b.getVerticalAlignment(), b
				.getHorizontalAlignment(), b.getVerticalTextPosition(), b
				.getHorizontalTextPosition(), viewRect, iconRect, textRect, b
				.getText() == null ? 0 : b.getIconTextGap());

		clearTextShiftOffset();

		// perform UI specific press action, e.g. Windows L&F shifts text
		if (model.isArmed() && model.isPressed())
		{
			paintButtonPressed(g, b);
		}

		// Paint the Icon
		if (b.getIcon() != null)
		{
			paintIcon(g, c, iconRect);
		}

		Composite oldComposite = ((Graphics2D) g).getComposite();

		if (model.isRollover())
		{
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
		}

		if (text != null && !text.equals(""))
		{
			View v = (View) c.getClientProperty(BasicHTML.propertyKey);
			if (v != null)
			{
				textRect.x += getTextShiftOffset();
				textRect.y += getTextShiftOffset();
				v.paint(g, textRect);
				textRect.x -= getTextShiftOffset();
				textRect.y -= getTextShiftOffset();
			}
			else
			{
				paintText(g, b, textRect, text);
			}
		}

		if (b.isFocusPainted() && b.hasFocus())
		{
			// paint UI specific focus
			paintFocus(g, b, viewRect, textRect, iconRect);
		}

		((Graphics2D) g).setComposite(oldComposite);
	}

	@Override
	protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect,
			Rectangle textRect, Rectangle iconRect)
	{
		if (b.getParent() instanceof JToolBar)
		{
			// Windows doesn't draw the focus rect for buttons in a toolbar.
			return;
		}

		// focus painted same color as text
		int width = b.getWidth();
		int height = b.getHeight();
		g.setColor(getFocusColor());
		BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
				width - dashedRectGapWidth, height - dashedRectGapHeight);
	}

	@Override
	protected void paintButtonPressed(Graphics g, AbstractButton b)
	{
		setTextShiftOffset();
	}

	static class HandCursor extends MouseAdapter
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			e.getComponent().setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			e.getComponent().setCursor(Cursor.getDefaultCursor());
		}
	}
}
