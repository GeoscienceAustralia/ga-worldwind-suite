package au.gov.ga.worldwind.tiler.gdal;

import org.gdal.gdal.gdal;

/**
 * {@link Exception} that retrieves error information from the GDAL library
 * whenever instaciated.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GDALException extends Exception
{
	private final int errorNo;
	private final int errorType;
	private final String errorMsg;

	public GDALException()
	{
		this(gdal.GetLastErrorNo(), gdal.GetLastErrorType(), gdal.GetLastErrorMsg());
	}

	public GDALException(int errorNo, int errorType, String errorMsg)
	{
		super("GDAL error " + errorNo + " - " + errorMsg);
		this.errorNo = errorNo;
		this.errorType = errorType;
		this.errorMsg = errorMsg;
	}

	public int getErrorNo()
	{
		return errorNo;
	}

	public int getErrorType()
	{
		return errorType;
	}

	public String getErrorMsg()
	{
		return errorMsg;
	}
}
