package au.gov.ga.worldwind.common.layers.model.gdal;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.gdal.GDALUtils;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;

import au.gov.ga.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.worldwind.common.layers.model.ModelLayer;
import au.gov.ga.worldwind.common.layers.model.ModelProvider;
import au.gov.ga.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.URLUtil;

/**
 * A {@link ModelProvider} that reads a band from a GDAL-supported raster file
 * and treats band values as depth/elevation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelProvider extends AbstractDataProvider<ModelLayer> implements ModelProvider
{

	private static final double FEET_TO_METRES = 0.3048;
	
	private Sector sector = null;
	private GDALRasterModelParameters modelParameters = null;
	
	public GDALRasterModelProvider(GDALRasterModelParameters parameters)
	{
		this.modelParameters = parameters;
	}

	@Override
	public Sector getSector()
	{
		return sector;
	}

	@Override
	protected boolean doLoadData(URL url, ModelLayer layer)
	{
		File file = URLUtil.urlToFile(url);
		
		// TODO: Add zip support
		
		Dataset gdalDataset;
		try
		{
			gdalDataset = GDALUtils.open(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		AVList rasterParameters = GDALUtils.extractRasterParameters(gdalDataset);
		this.sector = (Sector)rasterParameters.getValue(AVKey.SECTOR);
		
		List<Position> positions = readPositionsFromDataset(gdalDataset, rasterParameters);
		
		BinaryTriangleTree btt = new BinaryTriangleTree(positions, gdalDataset.GetRasterXSize(), gdalDataset.GetRasterYSize());
		btt.setForceGLTriangles(true);
		FastShape shape = btt.buildMesh(modelParameters.getMaxVariance());
		
		layer.addShape(shape);
		
		return true;
	}

	private List<Position> readPositionsFromDataset(Dataset gdalDataset, AVList rasterParameters)
	{
		Band band = getModelBand(gdalDataset);
		
		int columns = band.getXSize();
		int rows = band.getYSize();
		
		ByteBuffer buffer = band.ReadRaster_Direct(0, 0, columns, rows, band.getDataType());
		buffer.rewind();
		
		List<Position> positions = new ArrayList<Position>();
		
		double y = sector.getMinLatitude().degrees;
		double dy = (Double)rasterParameters.getValue(AVKey.PIXEL_HEIGHT);
		
		double x = sector.getMinLongitude().degrees;
		double dx = (Double)rasterParameters.getValue(AVKey.PIXEL_WIDTH);
		
		double elevationOffset = getOffset(band);
		double elevationScale = getScale(band);
		boolean elevationIsInMetres = !AVKey.UNIT_FOOT.equals(rasterParameters.getStringValue(AVKey.ELEVATION_UNIT));
		
		int dataType = band.getDataType();
		
		System.out.println("**** Buffer limit: " + buffer.limit());
		System.out.println("**** Buffer remaining: " + buffer.remaining());
		System.out.println("**** Raster size:" +  columns + "x" + rows);
		
		for (int u = 0; u < rows; u++)
		{
			for (int v = 0; v < columns; v++)
			{
				double elevation = toElevation(elevationIsInMetres, elevationOffset, elevationScale, getValue(buffer, dataType));
				positions.add(new Position(Angle.fromDegreesLatitude(y), 
										   Angle.fromDegreesLongitude(x), 
										   elevation));
				y += dy;
				x += dx;
			}
		}
		return positions;
	}
	
	private double getScale(Band band)
	{
		Double[] vals = new Double[1];
		band.GetScale(vals);
		return vals[0] != null ? vals[0] : 1.0;
	}

	private double getOffset(Band band)
	{
		Double[] vals = new Double[1];
		band.GetOffset(vals);
		return vals[0] != null ? vals[0] : 0.0;
	}

	private double toElevation(boolean isInMetres, double offset, double scale, double value)
	{
		double raw = offset + (scale * value);
		
		return isInMetres ? raw : FEET_TO_METRES * raw;
	}

	private double getValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			return buffer.getFloat();
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			return buffer.getDouble();
		}
		else if (bufferType == gdalconstConstants.GDT_Byte)
		{
			return buffer.get() & 0xff;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			return buffer.getShort();
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			return buffer.getInt();
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			return getUInt16(buffer);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			return getUInt32(buffer);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}
	
	private static int getUInt16(ByteBuffer buffer)
	{
		int first = 0xff & buffer.get();
		int second = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 8 | second);
		}
		else
		{
			return (first | second << 8);
		}
	}
	
	private static long getUInt32(ByteBuffer buffer)
	{
		long first = 0xff & buffer.get();
		long second = 0xff & buffer.get();
		long third = 0xff & buffer.get();
		long fourth = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 24l | second << 16l | third << 8l | fourth);
		}
		else
		{
			return (first | second << 8l | third << 16l | fourth << 24l);
		}
	}
	
	private Band getModelBand(Dataset gdalDataset)
	{
		int band = modelParameters.getBand();
		if (band > gdalDataset.GetRasterCount())
		{
			Logging.logger().warning("Cannot use specified band " + band + ". Raster dataset only has " + gdalDataset.GetRasterCount() + " bands.");
			return gdalDataset.GetRasterBand(1);
		}
		return gdalDataset.GetRasterBand(band);
	}

}
