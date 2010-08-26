package au.gov.ga.worldwind.viewer.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollableImage extends JLabel implements Scrollable, MouseMotionListener
{
	private int maxUnitIncrement = 1;
	private boolean missingPicture = false;

	public ScrollableImage(ImageIcon i, int m)
	{
		super(i);
		if (i == null)
		{
			missingPicture = true;
			setText("No picture found.");
			setHorizontalAlignment(CENTER);
			setOpaque(true);
			setBackground(Color.white);
		}
		maxUnitIncrement = m;

		//Let the user scroll by dragging to outside the window.
		setAutoscrolls(true); //enable synthetic drag events
		addMouseMotionListener(this); //handle mouse drags
	}

	//Methods required by the MouseMotionListener interface:
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		//The user is dragging us, so scroll!
		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		scrollRectToVisible(r);
	}

	@Override
	public Dimension getPreferredSize()
	{
		if (missingPicture)
		{
			return new Dimension(320, 480);
		}
		else
		{
			return super.getPreferredSize();
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		//Get the current position.
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL)
		{
			currentPosition = visibleRect.x;
		}
		else
		{
			currentPosition = visibleRect.y;
		}

		//Return the number of pixels between currentPosition
		//and the nearest tick mark in the indicated direction.
		if (direction < 0)
		{
			int newPosition =
					currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		}
		else
		{
			return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
		}
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		if (orientation == SwingConstants.HORIZONTAL)
		{
			return visibleRect.width - maxUnitIncrement;
		}
		else
		{
			return visibleRect.height - maxUnitIncrement;
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public void setMaxUnitIncrement(int pixels)
	{
		maxUnitIncrement = pixels;
	}
}
