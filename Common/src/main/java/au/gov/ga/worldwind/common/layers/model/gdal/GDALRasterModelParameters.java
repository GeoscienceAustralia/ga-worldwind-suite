package au.gov.ga.worldwind.common.layers.model.gdal;

import gov.nasa.worldwind.avlist.AVList;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.ColorMap;

/**
 * Parameters used to control how a GDAL-supported raster is converted to a model
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelParameters
{

	/** The raster band to use for model generation */
	private int band = 1;
	
	/** The maximum variance used for mesh simplification */
	private float maxVariance = 0;
	
	/** The color map to apply to the model data */
	private ColorMap colorMap;
	
	public GDALRasterModelParameters()
	{
		//use defaults
	}

	/**
	 * Construct a new instance of this class, using the params to initialise values
	 * 
	 * @param params Default parameters
	 */
	public GDALRasterModelParameters(AVList params)
	{
		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if (cm != null)
		{
			setColorMap(cm);
		}

		Double d = (Double) params.getValue(AVKeyMore.MAX_VARIANCE);
		if (d != null)
		{
			setMaxVariance(d.floatValue());
		}
		
		Integer i = (Integer)params.getValue(AVKeyMore.TARGET_BAND);
		if (i != null)
		{
			setBand(i);
		}
	}

	/**
	 * @return The raster band to use for the model (defaults to 1)
	 */
	public int getBand()
	{
		return band;
	}

	/**
	 * @param band the band to set
	 */
	public void setBand(int band)
	{
		this.band = band;
	}
	
	/**
	 * @return The max variance to use for mesh simplification (defaults to 0)
	 */
	public float getMaxVariance()
	{
		return maxVariance;
	}

	/**
	 * @param maxVariance the maxVariance to set
	 */
	public void setMaxVariance(float maxVariance)
	{
		this.maxVariance = maxVariance;
	}
	
	/**
	 * @return the colour map to apply to the loaded data
	 */
	public ColorMap getColorMap()
	{
		return colorMap;
	}
	
	/**
	 * @param colorMap the colorMap to set
	 */
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}
	
}
