package au.gov.ga.worldwind.tiler.gdal;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.Sector;

/**
 * Container class which stores the parameters to use when generating tiles from
 * a GDAL dataset.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GDALTileParameters
{
	public GDALTileParameters(Dataset dataset, Dimension size, Sector sector)
	{
		this.dataset = dataset;
		this.size = size;
		this.sector = sector;
		this.sourceRectangle = null;
	}

	public GDALTileParameters(Dataset dataset, Dimension size, Rectangle sourceRectangle)
	{
		this.dataset = dataset;
		this.size = size;
		this.sector = null;
		this.sourceRectangle = sourceRectangle;
	}

	/**
	 * GDAL dataset to read.
	 */
	public final Dataset dataset;

	/**
	 * Tile dimensions.
	 */
	public final Dimension size;

	/**
	 * Tile sector to read (in dataset coordinates). If this is null, the
	 * sourceRectangle below must be specified.
	 */
	public final Sector sector;

	/**
	 * Data rectangle to read (in image coordinates). If this is null, the
	 * sector above is used to calculate the image extents to read.
	 */
	public final Rectangle sourceRectangle;

	/**
	 * Should an alpha channel be added to this tile? The source dataset must
	 * have exactly 3 bands. Defaults to false.
	 */
	public boolean addAlpha = false;

	/**
	 * Dataset band to use for the tile. Elevation data must be tiled from a
	 * single band. Defaults to -1 (no band selected).
	 */
	public int selectedBand = -1;

	/**
	 * Should the dataset be reprojected to WGS84 if not already?
	 */
	public boolean reprojectIfRequired = false;

	/**
	 * Should bilinear interpolation be used when reprojecting or resizing the
	 * tile?
	 */
	public boolean bilinearInterpolationIfRequired = true;

	/**
	 * Values in the dataset that represent nodata. These values are used for:
	 * <ul>
	 * <li>Pixels outside the dataset extends</li>
	 * <li>Ignoring pixels when calculating min/max elevations</li>
	 * <li>Ignoring pixels when bilinear magnifying overviews</li>
	 * <li>Determining whether a tile is blank</li>
	 * </ul>
	 */
	public NullableNumberArray noData;

	/**
	 * Ranges between which to replace values.
	 */
	public MinMaxArray[] minMaxs;

	/**
	 * Values to use when replacing dataset values that are within the given
	 * {@link GDALTileParameters#minMaxs} ranges. If the values within are null,
	 * the original values are used.
	 */
	public NullableNumberArray replacement;

	/**
	 * Values to use when replacing dataset values that aren't within the given
	 * {@link GDALTileParameters#minMaxs} ranges. If the values within are null,
	 * the original values are used.
	 */
	public NullableNumberArray otherwise;
}
