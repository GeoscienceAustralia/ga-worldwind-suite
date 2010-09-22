package au.gov.ga.worldwind.animator.ui.frameslider;

import java.awt.Color;
import java.awt.Dimension;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.application.LAFConstants;

/**
 * A Swing component used to display a frame slider.
 * <p/>
 * Provides hooks to listen in on changes to frames, as well as changes to 
 * the slider state.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public class FrameSlider extends JComponent
{
	private static final long serialVersionUID = 20100824L;

	private static final Font DEFAULT_FONT = Font.decode("");
	private final static Color KEY_COLOR = LAFConstants.getKeyColor();
	private final static Color HIGHLIGHTED_KEY_COLOR = LAFConstants.getHighlightedKeyColor();
	private static final Color KEY_SELECTOR_COLOR = LAFConstants.getKeySelectorColor();
	private static final Color HIGHLIGHTED_KEY_SELECTOR_COLOR = LAFConstants.getHighlightedKeySelectorColor();
	
	private final static int SLIDER_BORDER = 2;
	private final static int PIXELS_PER_MAJOR_TICK = 48;
	private final static int PIXELS_PER_MINOR_TICK = 8;
	private final static int MAJOR_TICK_LENGTH = 16;
	private final static int MINOR_TICK_LENGTH = 8;

	private int min;
	private int max;
	private int value;

	private boolean leftDown = false;
	private boolean rightDown = false;
	private boolean draggingSlider = false;
	private Point dragPoint = null;
	private boolean draggingKeyFrame = false;
	private int draggingKeyFrameIndex;

	private Rectangle scrollRect;
	private Rectangle sliderRect;
	private Rectangle leftRect;
	private Rectangle rightRect;
	private Dimension moverDimension;
	private float ascent;
	private int position;

	private int numberOfMajorTicks;
	private int numberOfMinorTicks;
	private int framesPerMajorTick;
	private int framesPerMinorTick;
	private int firstMajorTick;
	private int firstMinorTick;

	private boolean sizeDirty = true;
	private boolean positionDirty = true;

	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private List<ChangeFrameListener> changeFrameListeners = new ArrayList<ChangeFrameListener>();

	private List<Integer> keys = new ArrayList<Integer>();
	private List<Integer> highlightedKeys = new ArrayList<Integer>();
	private List<Rectangle> keyRects = new ArrayList<Rectangle>();

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

		Dimension size = new Dimension(0, 54);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setSize(size);
	}

	public int getMin()
	{
		return min;
	}

	public void setMin(int min)
	{
		if (getMin() != min)
		{
			this.min = min;
			dirtySize();
			repaint();
		}
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		if (getMax() != max)
		{
			this.max = max;
			dirtySize();
			repaint();
		}
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		value = clamp(value, getMin(), getMax());
		if (this.value != value)
		{
			this.value = value;
			dirtyPosition();
			repaint();
			notifyChangeListeners();
		}
	}

	public int getLength()
	{
		return getMax() - getMin() + 1;
	}

	public void addKey(int frame)
	{
		keys.add(frame);
		keyRects.add(new Rectangle());
		repaint();
	}
	
	public void removeKey(int frame)
	{
		int index = keys.indexOf(frame);
		keys.remove(index);
		keyRects.remove(index);
		repaint();
	}

	public int getKey(int index)
	{
		return keys.get(index);
	}

	public void highlightKeys(Collection<Integer> keysToHighlight)
	{
		if (keysToHighlight == null)
		{
			highlightedKeys.clear();
			return;
		}
		highlightedKeys = new ArrayList<Integer>(keysToHighlight);
		repaint();
	}
	
	public void clearHighlightedKeys()
	{
		highlightedKeys.clear();
		repaint();
	}
	
	public int keyCount()
	{
		return keys.size();
	}

	public void clearKeys()
	{
		keys.clear();
		keyRects.clear();
	}

	private void setupMouseListeners()
	{
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int keyFrameIndex = -1;
				for (int i = 0; i < keyRects.size(); i++)
				{
					if (keyRects.get(i).contains(e.getPoint()))
					{
						keyFrameIndex = i;
						break;
					}
				}

				boolean insideLeftRect = leftRect.contains(e.getPoint());
				boolean insideRightRect = rightRect.contains(e.getPoint());
				if (insideLeftRect || insideRightRect)
				{
					int newValue = insideLeftRect ? getValue() - 1 : getValue() + 1;
					int positionDiff = calculatePositionFromFrame(newValue) - position;
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
					}
				}
				else if (keyFrameIndex >= 0)
				{
					int frame = getKey(keyFrameIndex);
					setValue(frame);
				}
				else if (scrollRect.contains(e.getPoint()) && !draggingSlider && !draggingKeyFrame)
				{
					int frame = calculateFrameFromPosition(e.getX() + 1);
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
					dragPoint = new Point(e.getX() - sliderRect.x, e.getY() - sliderRect.y);
				}
				else
				{
					for (int i = 0; i < keyRects.size(); i++)
					{
						Rectangle keyRect = keyRects.get(i);
						if (keyRect.contains(e.getPoint()))
						{
							draggingKeyFrame = true;
							draggingKeyFrameIndex = i;
							dragPoint = new Point(e.getX() - keyRect.x, e.getY() - keyRect.y);
							break;
						}
					}
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				leftDown = false;
				rightDown = false;
				draggingSlider = false;
				draggingKeyFrame = false;
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
					int frame = calculateFrameFromPosition(e.getX() - dragPoint.x + sliderRect.width / 2);
					setValue(frame);
				}
				else if (draggingKeyFrame)
				{
					Rectangle keyRect = keyRects.get(draggingKeyFrameIndex);
					int oldFrame = keys.get(draggingKeyFrameIndex);
					int newFrame = calculateFrameFromPosition(e.getX() - dragPoint.x + keyRect.width / 2);
					keys.remove(draggingKeyFrameIndex);
					keys.add(draggingKeyFrameIndex, newFrame);
					
					if (isHighlightedKey(oldFrame))
					{
						highlightedKeys.remove((Integer)oldFrame);
						highlightedKeys.add(newFrame);
					}
					
					notifyChangeFrameListeners(draggingKeyFrameIndex, oldFrame, newFrame);
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
		int position = (getWidth() - moverDimension.width) * (frame - getMin()) / (getLength() - 1) + moverDimension.width / 2;
		return position;
	}

	private int calculateFrameFromPosition(int position)
	{
		int min = moverDimension.width / 2;
		int max = getWidth() - moverDimension.width / 2;
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
			int left = position - moverDimension.width / 2;
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
		FontMetrics fm = g2.getFontMetrics();
		String stringBoundsTest = "m " + getMax() + " / " + (getLength() - 1) + " m";
		Rectangle2D stringBounds = fm.getStringBounds(stringBoundsTest, g2);
		ascent = fm.getLineMetrics(stringBoundsTest, g2).getAscent();
		int fontWidth = (int) Math.ceil(stringBounds.getWidth());
		int fontHeight = (int) Math.ceil(stringBounds.getHeight());

		int buttonWidth = fontHeight + SLIDER_BORDER * 2;
		int sliderWidth = fontWidth + SLIDER_BORDER * 2;
		int height = fontHeight + SLIDER_BORDER * 2;

		scrollRect = new Rectangle(0, 0, getWidth(), height + 3 + MAJOR_TICK_LENGTH);
		leftRect = new Rectangle(0, 0, buttonWidth, height);
		sliderRect = new Rectangle(buttonWidth, 0, sliderWidth, height);
		rightRect = new Rectangle(buttonWidth + sliderWidth, 0, buttonWidth, height);
		moverDimension = new Dimension(buttonWidth * 2 + sliderWidth, height);
	}

	private void calculateTicks()
	{
		int width = getWidth();
		int availableTickWidth = Math.max(1, width - moverDimension.width);
		int[] dividers = new int[] { 1, 2, 5 };
		int multiple = 1;
		numberOfMinorTicks = getLength();
		framesPerMinorTick = 1;
		while (!(numberOfMinorTicks == 0 || availableTickWidth / numberOfMinorTicks >= PIXELS_PER_MINOR_TICK))
		{
			for (int i : dividers)
			{
				framesPerMinorTick = i * multiple;
				numberOfMinorTicks = (int) (getLength() / framesPerMinorTick);
				if (numberOfMinorTicks == 0 || availableTickWidth / numberOfMinorTicks >= PIXELS_PER_MINOR_TICK)
				{
					break;
				}
			}
			multiple *= 10;
		}

		multiple = 1;
		numberOfMajorTicks = getLength();
		framesPerMajorTick = 1;
		while (!(numberOfMajorTicks == 0 || availableTickWidth / numberOfMajorTicks >= PIXELS_PER_MAJOR_TICK))
		{
			for (int i : dividers)
			{
				framesPerMajorTick = i * multiple;
				numberOfMajorTicks = (int) (getLength() / framesPerMajorTick);
				if (numberOfMajorTicks == 0 || availableTickWidth / numberOfMajorTicks >= PIXELS_PER_MAJOR_TICK)
				{
					break;
				}
			}
			multiple *= 10;
		}

		int firstMinorTick = getMin() - (getMin() + framesPerMinorTick - 1) % framesPerMinorTick + framesPerMinorTick - 1;
		int firstMajorTick = getMin() - (getMin() + framesPerMajorTick - 1) % framesPerMajorTick + framesPerMajorTick - 1;

		//add one if last tick equals max
		if (firstMinorTick + framesPerMinorTick * numberOfMinorTicks <= getMax())
		{
			numberOfMinorTicks++;
		}
		if (firstMajorTick + framesPerMajorTick * numberOfMajorTicks <= getMax())
		{
			numberOfMajorTicks++;
		}
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setFont(DEFAULT_FONT);

		//recalculate anything that's dirty
		calculateSizesIfDirty(g2);
		calculatePositionIfDirty();

		//system control colors
		Color light = SystemColor.controlLtHighlight;
		Color dark = SystemColor.controlDkShadow;
		Color control = SystemColor.control;
		Color text = SystemColor.controlText;

		paintSlider(g2, control, light, dark);
		paintSliderText(g2, text);
		paintButtonArrows(g2, text);
		paintSeparatorLine(g2, light, dark);

		int availableTickWidth = getWidth() - moverDimension.width;
		int tickY = moverDimension.height + 3;
		
		paintMinorTicks(g2, dark, availableTickWidth, tickY);
		int tickHeight = paintMajorTicks(g2, dark, text, availableTickWidth, tickY);

		//keyframes
		paintKeyFrames(g2, dark, tickY);

		//selection box
		paintSelectionBox(g2, dark, tickY);

		Dimension size = new Dimension(0, tickY + tickHeight);
		setMinimumSize(size);
		setPreferredSize(size);
	}

	private void paintSelectionBox(Graphics2D g2, Color borderColor, int top)
	{
		// Paint the border
		g2.setColor(borderColor);
		g2.drawRect(position - 3, top, 6, MAJOR_TICK_LENGTH);
		
		// Paint the filled area
		g2.setColor(hasKeyAtValue() ? HIGHLIGHTED_KEY_SELECTOR_COLOR : KEY_SELECTOR_COLOR);
		g2.fillRect(position - 2, top + 1, 5, MAJOR_TICK_LENGTH - 1);
	}

	private boolean hasKeyAtValue()
	{
		return keys.contains(getValue());
	}

	private void paintKeyFrames(Graphics2D g2, Color border, int top)
	{
		for (int i = 0; i < keys.size(); i++)
		{
			int key = keys.get(i);
			
			Rectangle rect = keyRects.get(i);
			int pos = calculatePositionFromFrame(key);
			rect.x = pos - 3;
			rect.y = top;
			rect.width = 6;
			rect.height = MINOR_TICK_LENGTH;
			
			g2.setColor(border);
			g2.drawRect(rect.x, rect.y, rect.width, rect.height);
			
			g2.setColor(isHighlightedKey(key) ? HIGHLIGHTED_KEY_COLOR : KEY_COLOR);
			g2.fillRect(rect.x + 1, rect.y + 1, rect.width - 1, rect.height - 1);
		}
	}

	private boolean isHighlightedKey(int key)
	{
		return highlightedKeys.contains(key);
	}

	private int paintMajorTicks(Graphics2D g2, Color tick, Color text, int availableTickWidth, int tickY)
	{
		int tickHeight = MAJOR_TICK_LENGTH;
		for (int i = 0; i < numberOfMajorTicks; i++)
		{
			int frame = firstMajorTick + (i * framesPerMajorTick);
			int x = moverDimension.width / 2 + availableTickWidth * (frame - getMin()) / (getLength() - 1);
			g2.setColor(tick);
			g2.drawLine(x, tickY, x, tickY + MAJOR_TICK_LENGTH);

			g2.setColor(text);
			String frameString = String.valueOf(frame);
			Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(frameString, g2);
			g2.drawString(frameString, (int) (x - stringBounds.getWidth() / 2), tickY + MAJOR_TICK_LENGTH + ascent);
			tickHeight = Math.max(tickHeight, (int) (MAJOR_TICK_LENGTH + stringBounds.getHeight()));
		}
		
		return tickHeight;
	}

	private void paintMinorTicks(Graphics2D g2, Color dark, int availableTickWidth, int tickY)
	{
		g2.setColor(dark);
		for (int i = 0; i < numberOfMinorTicks; i++)
		{
			int x = moverDimension.width / 2 + availableTickWidth * (firstMinorTick + (i * framesPerMinorTick) - getMin()) / (getLength() - 1);
			g2.drawLine(x, tickY, x, tickY + MINOR_TICK_LENGTH);
		}
	}

	private void paintSlider(Graphics2D g2, Color control, Color light, Color dark)
	{
		g2.setColor(control);
		g2.fillRect(leftRect.x, 0, moverDimension.width, moverDimension.height);

		//slider and button borders
		drawRaisedRect(g2, sliderRect, light, dark, false);
		drawRaisedRect(g2, leftRect, light, dark, leftDown);
		drawRaisedRect(g2, rightRect, light, dark, rightDown);
	}

	private void paintSliderText(Graphics2D g2, Color text)
	{
		g2.setColor(text);
		String sliderText = getValue() + " / " + (getLength() - 1);
		Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(sliderText, g2);
		int textY = (int) (moverDimension.height / 2 + ascent / 2 - 1);
		g2.drawString(sliderText, (int) (sliderRect.x + sliderRect.width / 2 - stringBounds.getWidth() / 2), textY);
	}

	private void paintButtonArrows(Graphics2D g2, Color lineColor)
	{
		int arrowSize = 4;
		int leftOffset = leftDown ? 0 : -1;
		g2.setColor(lineColor);
		drawArrow(g2, leftRect, arrowSize, leftOffset, false);
		int rightOffset = rightDown ? 1 : 0;
		drawArrow(g2, rightRect, arrowSize, rightOffset, true);
	}

	private void drawArrow(Graphics2D g2, Rectangle inside, int arrowSize, int offset, boolean right)
	{
		if (right)
		{
			arrowSize = -arrowSize;
		}
		
		g2.drawLine(inside.x + inside.width / 2 - arrowSize / 2 + offset, inside.height / 2, inside.x + inside.width / 2 + arrowSize / 2 + offset, inside.height / 2 - arrowSize);
		g2.drawLine(inside.x + inside.width / 2 - arrowSize / 2 + offset, inside.height / 2, inside.x + inside.width / 2 + arrowSize / 2 + offset, inside.height / 2 + arrowSize);
	}
	
	private void paintSeparatorLine(Graphics2D g2, Color light, Color dark)
	{
		g2.setColor(light);
		g2.drawLine(0, moverDimension.height + 1, getWidth(), moverDimension.height + 1);
		g2.setColor(dark);
		g2.drawLine(0, moverDimension.height + 2, getWidth(), moverDimension.height + 2);
	}

	private void drawRaisedRect(Graphics2D g2, Rectangle rect, Color c1, Color c2, boolean lowered)
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
		g2.drawLine(rect.x + 1, rect.y + rect.height - 1, rect.x + rect.width - 2, rect.y + rect.height - 1);
		g2.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);
	}

	public void addChangeListener(ChangeListener changeListener)
	{
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener)
	{
		changeListeners.remove(changeListener);
	}

	private void notifyChangeListeners()
	{
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener changeListener : changeListeners)
		{
			changeListener.stateChanged(e);
		}
	}

	public void addChangeFrameListener(ChangeFrameListener changeFrameListener)
	{
		changeFrameListeners.add(changeFrameListener);
	}

	public void removeChangeFrameListener(ChangeFrameListener changeFrameListener)
	{
		changeFrameListeners.remove(changeFrameListener);
	}

	private void notifyChangeFrameListeners(int index, int oldFrame, int newFrame)
	{
		for (ChangeFrameListener changeFrameListener : changeFrameListeners)
		{
			changeFrameListener.frameChanged(index, oldFrame, newFrame);
		}
	}
}
