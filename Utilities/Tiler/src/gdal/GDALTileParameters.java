package gdal;

import java.awt.Dimension;

import org.gdal.gdal.Dataset;

import util.MinMaxArray;
import util.NullableNumberArray;
import util.Sector;

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
