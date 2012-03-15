/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
