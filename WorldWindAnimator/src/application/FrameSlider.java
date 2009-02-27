package application;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemColor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class FrameSlider extends JComponent
{
	private final static int SLIDER_BORDER = 2;
	private final static int PIXELS_PER_MAJOR_TICK = 48;
	private final static int PIXELS_PER_MINOR_TICK = 8;
	private final static int MAJOR_TICK_LENGTH = 16;
	private final static int MINOR_TICK_LENGTH = 8;

	private int min, max, value;

	private boolean leftDown = false;
	private boolean rightDown = false;
	private boolean draggingSlider = false;
	private Point dragPoint = null;

	private Rectangle scrollRect;
	private Rectangle sliderRect;
	private Rectangle leftRect;
	private Rectangle rightRect;
	private int moverWidth, moverHeight;
	private float ascent;
	private int position;

	private int majorTicks;
	private int minorTicks;
	private int framesPerMajorTick;
	private int framesPerMinorTick;
	private int firstMajorTick;
	private int firstMinorTick;

	private boolean sizeDirty = true;
	private boolean positionDirty = true;

	public FrameSlider(int value, int min, int max)
	{
		setMin(min);
		setMax(max);
		setValue(value);
		setupMouseListeners();
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				dirtySize();
				repaint();
			}
		});
	}

	public int getMin()
	{
		return min;
	}

	public void setMin(int min)
	{
		this.min = min;
		dirtySize();
		repaint();
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
		dirtySize();
		repaint();
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = clamp(value, getMin(), getMax());
		dirtyPosition();
		repaint();
	}

	public int getLength()
	{
		return max - min + 1;
	}

	private void setupMouseListeners()
	{
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				boolean insideLeftRect = leftRect.contains(e.getPoint());
				boolean insideRightRect = rightRect.contains(e.getPoint());
				if (insideLeftRect || insideRightRect)
				{
					int newValue = insideLeftRect ? getValue() - 1
							: getValue() + 1;
					int positionDiff = calculatePositionFromFrame(newValue)
							- position;
					setValue(newValue);

					Point los = getLocationOnScreen();
					int x = los.x + e.getX() + positionDiff;
					int y = los.y + e.getY();
					try
					{
						Robot robot = new Robot();
						robot.mouseMove(x, y);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				else if (scrollRect.contains(e.getPoint())
						&& !sliderRect.contains(e.getPoint()))
				{
					int frame = calculateFrameFromPosition(e.getX());
					setValue(frame);
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (leftRect.contains(e.getPoint()))
				{
					leftDown = true;
				}
				else if (rightRect.contains(e.getPoint()))
				{
					rightDown = true;
				}
				else if (sliderRect.contains(e.getPoint()))
				{
					draggingSlider = true;
					dragPoint = new Point(e.getX() - sliderRect.x, e.getY()
							- sliderRect.y);
					System.out.println(dragPoint.x);
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				leftDown = false;
				rightDown = false;
				draggingSlider = false;
				repaint();
			}
		});

		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (leftDown && !leftRect.contains(e.getPoint()))
				{
					leftDown = false;
				}
				if (rightDown && !rightRect.contains(e.getPoint()))
				{
					rightDown = false;
				}
				if (draggingSlider)
				{
					int frame = calculateFrameFromPosition(e.getX()
							- dragPoint.x + sliderRect.width / 2);
					setValue(frame);
				}
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				leftDown = false;
				rightDown = false;
				repaint();
			}
		});
	}

	private int calculatePositionFromFrame(int frame)
	{
		frame = clamp(frame, getMin(), getMax());
		int position = (getWidth() - moverWidth) * (frame - getMin())
				/ (getLength() - 1) + moverWidth / 2;
		return position;
	}

	private int calculateFrameFromPosition(int position)
	{
		int min = moverWidth / 2;
		int max = getWidth() - moverWidth / 2;
		position = clamp(position, min, max);
		int frame = getMin() + getLength() * (position - min) / (max - min);
		return frame;
	}

	private int clamp(int value, int min, int max)
	{
		return (value > max ? max : value < min ? min : value);
	}

	private void calculatePositionIfDirty()
	{
		if (positionDirty)
		{
			positionDirty = false;
			position = calculatePositionFromFrame(getValue());
			int left = position - moverWidth / 2;
			leftRect.x = left;
			sliderRect.x = left + leftRect.width;
			rightRect.x = left + leftRect.width + sliderRect.width;
		}
	}

	private void calculateSizesIfDirty(Graphics2D g2)
	{
		if (sizeDirty)
		{
			sizeDirty = false;
			calculateControlSizes(g2);
			calculateTicks();
		}
	}

	private void dirtySize()
	{
		sizeDirty = true;
		dirtyPosition();
	}

	private void dirtyPosition()
	{
		positionDirty = true;
	}

	private void calculateControlSizes(Graphics2D g2)
	{
		int width = getWidth();
		FontMetrics fm = g2.getFontMetrics();
		String stringBoundsTest = "m " + getMax() + " / " + (getLength() - 1)
				+ " m";
		Rectangle2D stringBounds = fm.getStringBounds(stringBoundsTest, g2);
		ascent = fm.getLineMetrics(stringBoundsTest, g2).getAscent();
		int fontWidth = (int) Math.ceil(stringBounds.getWidth());
		int fontHeight = (int) Math.ceil(stringBounds.getHeight());

		int buttonWidth = fontHeight + SLIDER_BORDER * 2;
		int sliderWidth = fontWidth + SLIDER_BORDER * 2;
		int height = fontHeight + SLIDER_BORDER * 2;

		scrollRect = new Rectangle(0, 0, width, height);
		leftRect = new Rectangle(0, 0, buttonWidth, height);
		sliderRect = new Rectangle(buttonWidth, 0, sliderWidth, height);
		rightRect = new Rectangle(buttonWidth + sliderWidth, 0, buttonWidth,
				height);
		this.moverWidth = buttonWidth * 2 + sliderWidth;
		this.moverHeight = height;
	}

	private void calculateTicks()
	{
		int width = getWidth();
		int availableTickWidth = Math.max(1, width - moverWidth);
		int[] dividers = new int[] { 1, 2, 5 };
		int multiple = 1;
		minorTicks = getLength();
		framesPerMinorTick = 1;
		while (!(minorTicks == 0 || availableTickWidth / minorTicks >= PIXELS_PER_MINOR_TICK))
		{
			for (int i : dividers)
			{
				framesPerMinorTick = i * multiple;
				minorTicks = (int) (getLength() / framesPerMinorTick);
				if (minorTicks == 0
						|| availableTickWidth / minorTicks >= PIXELS_PER_MINOR_TICK)
					break;
			}
			multiple *= 10;
		}

		multiple = 1;
		majorTicks = getLength();
		framesPerMajorTick = 1;
		while (!(majorTicks == 0 || availableTickWidth / majorTicks >= PIXELS_PER_MAJOR_TICK))
		{
			for (int i : dividers)
			{
				framesPerMajorTick = i * multiple;
				majorTicks = (int) (getLength() / framesPerMajorTick);
				if (majorTicks == 0
						|| availableTickWidth / majorTicks >= PIXELS_PER_MAJOR_TICK)
					break;
			}
			multiple *= 10;
		}

		int firstMinorTick = min - (min + framesPerMinorTick - 1)
				% framesPerMinorTick + framesPerMinorTick - 1;
		int firstMajorTick = min - (min + framesPerMajorTick - 1)
				% framesPerMajorTick + framesPerMajorTick - 1;

		//add one if last tick equals max
		if (firstMinorTick + framesPerMinorTick * minorTicks <= max)
			minorTicks++;
		if (firstMajorTick + framesPerMajorTick * majorTicks <= max)
			majorTicks++;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		//TODO remove
		g2.setColor(new Color(225, 255, 255));
		g2.fillRect(0, 0, getWidth(), getHeight());

		Font font = Font.decode("");
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();

		//recalculate anything that's dirty
		calculateSizesIfDirty(g2);
		calculatePositionIfDirty();

		//system control colors
		Color light = SystemColor.controlLtHighlight;
		Color dark = SystemColor.controlDkShadow;
		Color control = SystemColor.control;
		Color text = SystemColor.controlText;

		//slider and button background
		g2.setColor(control);
		g2.fillRect(leftRect.x, 0, moverWidth, moverHeight);

		//slider and button borders
		drawRaisedRect(sliderRect, g2, light, dark, false);
		drawRaisedRect(leftRect, g2, light, dark, leftDown);
		drawRaisedRect(rightRect, g2, light, dark, rightDown);

		//slider text
		g2.setColor(text);
		String sliderText = value + " / " + (getLength() - 1);
		Rectangle2D stringBounds = fm.getStringBounds(sliderText, g2);
		int textY = (int) (moverHeight / 2 + ascent / 2 - 1);
		g2.drawString(sliderText,
				(int) (sliderRect.x + sliderRect.width / 2 - stringBounds
						.getWidth() / 2), textY);

		//button arrows
		int arrowSize = 4;
		int leftOffset = leftDown ? 0 : -1;
		drawArrow(leftRect, arrowSize, g2, false, leftOffset);
		int rightOffset = rightDown ? 1 : 0;
		drawArrow(rightRect, arrowSize, g2, true, rightOffset);

		//separator line
		g2.setColor(light);
		g2.drawLine(0, moverHeight + 1, getWidth(), moverHeight + 1);
		g2.setColor(dark);
		g2.drawLine(0, moverHeight + 2, getWidth(), moverHeight + 2);

		//ticks
		int availableTickWidth = getWidth() - moverWidth;
		int tickY = moverHeight + 3;

		g2.setColor(dark);
		for (int i = 0; i < minorTicks; i++)
		{
			int x = moverWidth / 2 + availableTickWidth
					* (firstMinorTick + (i * framesPerMinorTick) - min)
					/ (getLength() - 1);
			g2.drawLine(x, tickY, x, tickY + MINOR_TICK_LENGTH);
		}

		for (int i = 0; i < majorTicks; i++)
		{
			int frame = firstMajorTick + (i * framesPerMajorTick);
			int x = moverWidth / 2 + availableTickWidth * (frame - min)
					/ (getLength() - 1);
			g2.setColor(dark);
			g2.drawLine(x, tickY, x, tickY + MAJOR_TICK_LENGTH);

			g2.setColor(text);
			String frameString = String.valueOf(frame);
			stringBounds = fm.getStringBounds(frameString, g2);
			g2.drawString(frameString, (int) (x - stringBounds.getWidth() / 2),
					tickY + MAJOR_TICK_LENGTH + ascent);
		}

		//selection box
		g2.setColor(dark);
		g2.drawRect(position - 3, tickY, 6, MAJOR_TICK_LENGTH);
		g2.setColor(new Color(0, 0, 255, 128));
		g2.fillRect(position - 2, tickY + 1, 5, MAJOR_TICK_LENGTH - 1);

		//TODO draw any keyframes here
	}

	private void drawArrow(Rectangle inside, int arrowSize, Graphics2D g2,
			boolean right, int offset)
	{
		if (right)
			arrowSize = -arrowSize;
		g2.drawLine(inside.x + inside.width / 2 - arrowSize / 2 + offset,
				inside.height / 2, inside.x + inside.width / 2 + arrowSize / 2
						+ offset, inside.height / 2 - arrowSize);
		g2.drawLine(inside.x + inside.width / 2 - arrowSize / 2 + offset,
				inside.height / 2, inside.x + inside.width / 2 + arrowSize / 2
						+ offset, inside.height / 2 + arrowSize);
	}

	private void drawRaisedRect(Rectangle rect, Graphics2D g2, Color c1,
			Color c2, boolean lowered)
	{
		if (lowered)
		{
			Color temp = c1;
			c1 = c2;
			c2 = temp;
		}
		g2.setColor(c1);
		g2.drawLine(rect.x, rect.y, rect.x + rect.width - 2, rect.y);
		g2.drawLine(rect.x, rect.y + 1, rect.x, rect.y + rect.height - 1);
		g2.setColor(c2);
		g2.drawLine(rect.x + 1, rect.y + rect.height - 1, rect.x + rect.width
				- 2, rect.y + rect.height - 1);
		g2.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1,
				rect.y + rect.height - 1);
	}

	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.add(new FrameSlider(200, 0, 100));
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
