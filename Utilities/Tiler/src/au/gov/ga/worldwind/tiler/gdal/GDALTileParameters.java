package au.gov.ga.worldwind.tiler.gdal;

import java.awt.Dimension;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.Sector;


public class GDALTileParameters
{
	public GDALTileParameters(Dataset dataset, Dimension size, Sector sector)
	{
		this.dataset = dataset;
		this.size = size;
		this.sector = sector;
	}
	
	public final Dataset dataset;
	public final Dimension size;
	public final Sector sector;
	
	public boolean addAlpha = false;
	public int selectedBand = -1;
	public boolean reprojectIfRequired = false;
	public boolean bilinearInterpolationIfRequired = true;

	//Use noData values for:
	//1. Pixels outside dataset extents
	//2. Ignoring pixels when calculating min/max
	//3. Ignoring pixels when bilinear magnifying
	public NullableNumberArray noData;
	
	//replacement variables
	public MinMaxArray[] minMaxs;
	public NullableNumberArray replacement;
	public NullableNumberArray otherwise;
}
