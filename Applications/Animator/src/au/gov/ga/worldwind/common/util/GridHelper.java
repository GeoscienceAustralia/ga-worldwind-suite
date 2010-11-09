package au.gov.ga.worldwind.common.util;



/**
 * A helper class used to calculate grid spacings etc.
 */
public class GridHelper
{
	private static final Range<Integer> DEFAULT_GRID_SIZE = new Range<Integer>(5, 20);
	
	
	/** A container class that holds calculated grid properties */
	public static class GridProperties
	{
		/** The location of the first grid line to draw, in pixels */
		private int firstGridLine;

		/** The value of the first grid line */
		private double firstGridLineValue;
		
		/** The grid spacing to use, in pixels */
		private int gridSpacing;
		
		/** The value change per grid line */
		private double valueChangePerGridLine;

		public GridProperties(int firstGridLine, int gridSpacing, double firstGridLineValue, double valueChangePerGridLine)
		{
			this.firstGridLine = firstGridLine;
			this.firstGridLineValue = firstGridLineValue;
			this.gridSpacing = gridSpacing;
			this.valueChangePerGridLine = valueChangePerGridLine;
		}

		public int getFirstGridLineLocation()
		{
			return firstGridLine;
		}

		public double getFirstGridLineValue()
		{
			return firstGridLineValue;
		}
		
		public int getGridSpacing()
		{
			return gridSpacing;
		}

		public double getValueChangePerGridLine()
		{
			return valueChangePerGridLine;
		}
		
		@Override
		public String toString()
		{
			return "Grid[Start: " + firstGridLine + ", Spacing: " + gridSpacing + ", Value change: " + valueChangePerGridLine + "]"; 
		}
	}
	
	/** A builder class to use for building grids */
	public static class GridBuilder
	{
		private Range<Integer> gridSize = DEFAULT_GRID_SIZE;
		private Integer numPixels;
		private Range<Double> valueRange;
		
		public GridBuilder ofSize(Range<Integer> gridSize)
		{
			this.gridSize = gridSize;
			return this;
		}
		
		public GridBuilder toFitIn(int numPixels)
		{
			this.numPixels = numPixels;
			return this;
		}
		
		public GridBuilder forValueRange(Range<Double> valueRange)
		{
			this.valueRange = valueRange;
			return this;
		}
		
		public GridProperties build()
		{
			if (gridSize == null || numPixels == null || valueRange == null)
			{
				throw new IllegalStateException("Not enough information provided to build a grid. Please use the builder methods to provide required information");
			}
			
			return calculateGridProperties();
		}

		private GridProperties calculateGridProperties()
		{
			int pixelsPerGridLine = -1;
			double valueChangePerGridLine = Math.pow(10, (int)Math.log10(valueRange.getMaxValue()));
			
			double valueDelta = valueRange.getMaxValue() - valueRange.getMinValue();
			
			// Prefer 1/2s, then 1/4s, then 1/5s
			double[] dividers = new double[]{1, 0.5, 0.75, 0.25, 0.8, 0.6, 0.4, 0.2};
			
			// Incrementally decrease the value change until it falls within the grid size
			while (!gridSize.contains(pixelsPerGridLine))
			{
				for (double d : dividers)
				{
					double candidateValueChange = valueChangePerGridLine * d;
					
					pixelsPerGridLine = (int)((numPixels / valueDelta) * candidateValueChange);
					
					if (gridSize.contains(pixelsPerGridLine))
					{
						valueChangePerGridLine = candidateValueChange;
						break;
					}
				}
				if (gridSize.contains(pixelsPerGridLine))
				{
					break;
				}
				
				valueChangePerGridLine *= 0.1;
			}
			
			// Find the first multiple of the grid step size
			double valuePerPixel = valueDelta / numPixels;
			double minRemainder = Double.MAX_VALUE;
			int firstGridLine = 0;
			double firstGridLineValue = 0;
			for (int i = 0; i < pixelsPerGridLine; i++)
			{
				double valueAtPixel = (valuePerPixel * i) + valueRange.getMinValue();
				double remainder = valueAtPixel % valueChangePerGridLine;
				if (remainder < minRemainder)
				{
					firstGridLine = i;
					firstGridLineValue = valueAtPixel;
					minRemainder = remainder;
				}
			}
			return new GridProperties(firstGridLine, pixelsPerGridLine, firstGridLineValue, valueChangePerGridLine);
		}
		
	}
	
	/** Use the builder methods to create grids */
	private GridHelper(){}
	
	public static GridBuilder createGrid()
	{
		return new GridBuilder();
	}
	
}
