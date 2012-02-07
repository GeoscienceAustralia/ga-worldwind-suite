package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;

import java.nio.ByteOrder;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.ColorMap;
import au.gov.ga.worldwind.common.util.CoordinateTransformationUtil;

public class GocadReaderParameters
{
	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	private int voxetSubsamplingU = 1;
	private int voxetSubsamplingV = 1;
	private int voxetSubsamplingW = 1;
	private boolean voxetDynamicSubsampling = true;
	private int voxetDynamicSubsamplingSamplesPerAxis = 50;
	private boolean voxetBilinearMinification = true;
	private CoordinateTransformation coordinateTransformation = null;
	private ColorMap colorMap = null;

	public GocadReaderParameters()
	{
		//use defaults
	}

	public GocadReaderParameters(AVList params)
	{
		ByteOrder bo = (ByteOrder) params.getValue(AVKey.BYTE_ORDER);
		if (bo != null)
			setByteOrder(bo);

		Integer i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_U);
		if (i != null)
			setVoxetSubsamplingU(i);

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_V);
		if (i != null)
			setVoxetSubsamplingV(i);

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_W);
		if (i != null)
			setVoxetSubsamplingW(i);

		Boolean b = (Boolean) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING);
		if (b != null)
			setVoxetDynamicSubsampling(b);

		i = (Integer) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING_SAMPLES_PER_AXIS);
		if (i != null)
			setVoxetDynamicSubsamplingSamplesPerAxis(i);

		b = (Boolean) params.getValue(AVKeyMore.BILINEAR_MINIFICATION);
		if (b != null)
			setVoxetBilinearMinification(b);

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
			setCoordinateTransformation(CoordinateTransformationUtil.getTransformationToWGS84(s));
		
		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if(cm != null)
			setColorMap(cm);
	}

	public int getVoxetSubsamplingU()
	{
		return voxetSubsamplingU;
	}

	public void setVoxetSubsamplingU(int voxetSubsamplingU)
	{
		this.voxetSubsamplingU = voxetSubsamplingU;
	}

	public int getVoxetSubsamplingV()
	{
		return voxetSubsamplingV;
	}

	public void setVoxetSubsamplingV(int voxetSubsamplingV)
	{
		this.voxetSubsamplingV = voxetSubsamplingV;
	}

	public int getVoxetSubsamplingW()
	{
		return voxetSubsamplingW;
	}

	public void setVoxetSubsamplingW(int voxetSubsamplingW)
	{
		this.voxetSubsamplingW = voxetSubsamplingW;
	}

	public boolean isVoxetDynamicSubsampling()
	{
		return voxetDynamicSubsampling;
	}

	public void setVoxetDynamicSubsampling(boolean voxetDynamicSubsampling)
	{
		this.voxetDynamicSubsampling = voxetDynamicSubsampling;
	}

	public int getVoxetDynamicSubsamplingSamplesPerAxis()
	{
		return voxetDynamicSubsamplingSamplesPerAxis;
	}

	public void setVoxetDynamicSubsamplingSamplesPerAxis(int voxetDynamicSubsamplingSamplesPerAxis)
	{
		this.voxetDynamicSubsamplingSamplesPerAxis = voxetDynamicSubsamplingSamplesPerAxis;
	}

	public boolean isVoxetBilinearMinification()
	{
		return voxetBilinearMinification;
	}

	public void setVoxetBilinearMinification(boolean voxetBilinearMinification)
	{
		this.voxetBilinearMinification = voxetBilinearMinification;
	}

	public ByteOrder getByteOrder()
	{
		return byteOrder;
	}

	public void setByteOrder(ByteOrder byteOrder)
	{
		this.byteOrder = byteOrder;
	}

	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	public void setCoordinateTransformation(CoordinateTransformation coordinateTransformation)
	{
		this.coordinateTransformation = coordinateTransformation;
	}

	public ColorMap getColorMap()
	{
		return colorMap;
	}

	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}
}
