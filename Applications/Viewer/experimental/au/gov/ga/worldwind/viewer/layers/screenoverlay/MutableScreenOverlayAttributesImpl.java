package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import java.awt.Color;
import java.net.URL;
import java.util.zip.CRC32;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * A default mutable implementation of the {@link ScreenOverlayAttributes} interface.
 * <p/>
 * Provides some sensible defaults that can be used with minimal configuration.
 */
public class MutableScreenOverlayAttributesImpl implements ScreenOverlayAttributes
{

	private static final ScreenOverlayPosition DEFAULT_OVERLAY_POSITION = ScreenOverlayPosition.CENTRE;
	private static final LengthExpression DEFAULT_LENGTH = new LengthExpression("80%");
	private static final int DEFAULT_BORDER_WIDTH = 2;
	private static final int NO_BORDER_WIDTH = 0;
	private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;
	
	private static final String ID_PREFIX = "ScreenOverlay";
	
	private URL sourceUrl;
	private String sourceHtml;
	private String sourceId;
	
	private ScreenOverlayPosition position = DEFAULT_OVERLAY_POSITION;
	
	private LengthExpression minHeight = DEFAULT_LENGTH;
	private LengthExpression maxHeight = null; // Fixed height
	private LengthExpression minWidth = DEFAULT_LENGTH;
	private LengthExpression maxWidth = null; // Fixed width
	
	private boolean drawBorder = true;
	private Color borderColor = DEFAULT_BORDER_COLOR;
	private int borderWidth = DEFAULT_BORDER_WIDTH;
	
	public MutableScreenOverlayAttributesImpl(URL sourceUrl)
	{
		Validate.notNull(sourceUrl, "A source URL is required");
		this.sourceUrl = sourceUrl;
	}
	
	public MutableScreenOverlayAttributesImpl(String sourceHtml)
	{
		Validate.notBlank(sourceHtml, "Source html is required");
		this.sourceHtml = sourceHtml;
	}
	
	@Override
	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	@Override
	public String getSourceHtml()
	{
		return sourceHtml;
	}

	@Override
	public String getSourceId()
	{
		if (sourceId == null)
		{
			CRC32 checksum = new CRC32();
			if (sourceUrl != null)
			{
				checksum.update((sourceUrl.toExternalForm()).getBytes());
			}
			else
			{
				checksum.update(sourceHtml.getBytes());
			}
			sourceId = ID_PREFIX + checksum.getValue();
		}
		return sourceId;
	}
	
	@Override
	public ScreenOverlayPosition getPosition()
	{
		return position;
	}

	public void setPosition(ScreenOverlayPosition position)
	{
		this.position = (position == null ? DEFAULT_OVERLAY_POSITION : position);
	}
	
	@Override
	public LengthExpression getMinHeight()
	{
		return minHeight;
	}
	
	public void setMinHeight(LengthExpression minHeight)
	{
		if (minHeight == null)
		{
			minHeight = maxHeight == null ? DEFAULT_LENGTH : maxHeight;
		}
		
		this.minHeight = minHeight;
	}

	@Override
	public LengthExpression getMaxHeight()
	{
		return maxHeight == null ? minHeight : maxHeight;
	}

	public void setMaxHeight(LengthExpression maxHeight)
	{
		this.maxHeight = maxHeight;
	}
	
	@Override
	public float getHeight(float screenHeight)
	{
		return Math.max(1, Math.min(getMinHeight().getLength(screenHeight), getMaxHeight().getLength(screenHeight)));
	}

	@Override
	public LengthExpression getMinWidth()
	{
		return minWidth;
	}
	
	public void setMinWidth(LengthExpression minWidth)
	{
		this.minWidth = minWidth;
	}

	@Override
	public LengthExpression getMaxWidth()
	{
		return maxWidth == null ? minWidth : maxWidth;
	}
	
	public void setMaxWidth(LengthExpression maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	@Override
	public float getWidth(float screenWidth)
	{
		return Math.max(1, Math.min(getMinWidth().getLength(screenWidth), getMaxWidth().getLength(screenWidth)));
	}

	@Override
	public boolean isDrawBorder()
	{
		return drawBorder;
	}

	public void setDrawBorder(boolean drawBorder)
	{
		this.drawBorder = drawBorder;
	}
	
	@Override
	public Color getBorderColor()
	{
		return drawBorder ? borderColor : null;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = (borderColor == null ? DEFAULT_BORDER_COLOR : borderColor);
	}
	
	@Override
	public int getBorderWidth()
	{
		return drawBorder ? borderWidth : NO_BORDER_WIDTH;
	}
	
	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = (borderWidth < 0 ? DEFAULT_BORDER_WIDTH : borderWidth);
	}

}
