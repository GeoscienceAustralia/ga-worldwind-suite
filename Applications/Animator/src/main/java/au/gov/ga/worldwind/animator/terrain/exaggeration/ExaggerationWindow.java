package au.gov.ga.worldwind.animator.terrain.exaggeration;

/**
 * A window in which elevation exaggeration is applied.
 * <p/>
 * Allows construction of a double linked-list of windows so that window offsets can be cached
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class ExaggerationWindow
{
	private Double lowerBoundary = null;
	private Double exaggeration = 1.0;

	private Double windowOffset = null;

	private ExaggerationWindow lowerWindow = null;
	private ExaggerationWindow upperWindow = null;
	
	public ExaggerationWindow(Double lowerBoundary, Double exaggeration)
	{
		this.lowerBoundary = lowerBoundary;
		this.exaggeration = exaggeration;
	}

	public Double applyExaggeration(double elevation)
	{
		if (elevationIsInWindow(elevation))
		{
			if (windowIsAboveZero())
			{
				return ((elevation - getLowerBoundary()) * exaggeration) + getOffsetToApplyInWindow();
			}
			else
			{
				return ((elevation - getUpperBoundary()) * exaggeration) + getOffsetToApplyInWindow();
			}
		}
		return null;
	}
	
	/**
	 * @return The offset at the 'top' of this window
	 */
	public double getWindowOffset()
	{
		if (windowOffset == null)
		{
			calculateOffset();
		}
		return windowOffset;
	}
	
	/**
	 * @return The offset to apply to elevations inside this window
	 */
	private double getOffsetToApplyInWindow()
	{
		if (windowIsAboveZero())
		{
			if (lowerWindow == null)
			{
				return 0.0;
			}
			return lowerWindow.getWindowOffset();
		}
		else
		{
			if (upperWindow == null)
			{
				return 0.0;
			}
			return upperWindow.getWindowOffset();
		}
	}

	private void calculateOffset()
	{
		if (windowIsAboveZero())
		{
			if (getUpperBoundary() != null && getLowerBoundary() != null)
			{
				windowOffset = (getUpperBoundary() - getLowerBoundary()) * exaggeration + getOffsetToApplyInWindow();
			}
			else
			{
				windowOffset = 0.0;
			}
		}
		else
		{
			if (getUpperBoundary() != null && getLowerBoundary() != null)
			{
				windowOffset = (getLowerBoundary() - getUpperBoundary()) * exaggeration + getOffsetToApplyInWindow();
			}
			else
			{
				windowOffset = getOffsetToApplyInWindow();
			}
		}
	}
	
	public boolean elevationIsInWindow(double elevation)
	{
		return getLowerBoundary() != null && getLowerBoundary() <= elevation &&
			(getUpperBoundary() == null || getUpperBoundary() > elevation);
	}
	
	private boolean windowIsAboveZero()
	{
		return getLowerBoundary() != null && getLowerBoundary() >= 0.0;
	}
	
	public Double getLowerBoundary()
	{
		return lowerBoundary;
	}
	
	public Double getUpperBoundary()
	{
		return upperWindow == null ? null : upperWindow.getLowerBoundary();
	}
	
	public void setLowerWindow(ExaggerationWindow window)
	{
		this.lowerWindow = window;
		if (window != null)
		{
			lowerWindow.upperWindow = this;
		}
	}
	
	public void setUpperWindow(ExaggerationWindow window)
	{
		this.upperWindow = window;
		if (window != null)
		{
			upperWindow.lowerWindow = this;
		}
	}
	
	@Override
	public String toString()
	{
		return "{" + getLowerBoundary() + " - x" + exaggeration + " - " + getUpperBoundary() + "}";
	}
}