package au.gov.ga.worldwind.tiler.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gdal.gdal.gdal;

import au.gov.ga.worldwind.tiler.application.Tiler.TilingType;
import au.gov.ga.worldwind.tiler.mapnik.MapnikUtil;
import au.gov.ga.worldwind.tiler.util.BufferedLineWriter;
import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.Sector;


public class LogWriter
{
	private FileWriter logWriter;
	private BufferedLineWriter writer;

	public LogWriter(File outDir) throws IOException
	{
		File logFile = new File(outDir, "tiler_" + getDateTime() + ".log");
		logWriter = new FileWriter(logFile);
		writer = new BufferedLineWriter(logWriter);
	}

	public void startLog(TilingType type, File mapFile, File outDir, Sector sector, int level,
			int tilesize, double lzts, String imageFormat, boolean addAlpha, int band,
			int bufferType, boolean bilinear, boolean reproject, String infoText, String tileText,
			NullableNumberArray outsideValues, MinMaxArray[] minMaxReplaces,
			NullableNumberArray replace, NullableNumberArray otherwise, boolean isFloat)
			throws IOException
	{
		if (type == TilingType.Mapnik)
		{
			writer.writeLine("Input file: " + mapFile.getAbsolutePath());
			writer.writeLine("Python binary: " + MapnikUtil.getPythonBinary());
			writer.writeLine("Mapnik script: " + MapnikUtil.getMapnikScript());
			writer.writeLine("Sector: " + sector);
			writer.writeLine("Output directory: " + outDir.getAbsolutePath());
			writer.writeLine("Level count: " + level);
			writer.writeLine("Tilesize: " + tilesize);
			writer.writeLine("Level zero tile size: " + lzts);
			writer.writeLine("Image format: " + imageFormat);
		}
		else
		{
			writer.writeLine("Input file: " + mapFile.getAbsolutePath());
			writer.newLine();

			writer.writeLine(infoText);
			writer.newLine();

			writer.writeLine(tileText);
			writer.newLine();

			writer.writeLine("Output parameters:");
			writer.writeLine("Sector: " + sector);
			writer.writeLine("Output directory: " + outDir.getAbsolutePath());
			writer.writeLine("Level count: " + level);
			writer.writeLine("Tilesize: " + tilesize);
			writer.writeLine("Level zero tile size: " + lzts);
			writer.writeLine("Bilinear magnification: " + bilinear);
			writer.writeLine("Reproject if required: " + reproject);
			if (outsideValues != null)
			{
				writer.writeLine("Set outside values: " + outsideValues.toString(isFloat));
			}
			if (minMaxReplaces != null)
			{
				writer.writeLine("Replace min 1: " + minMaxReplaces[0].toString(isFloat, true));
				writer.writeLine("Replace max 1: " + minMaxReplaces[0].toString(isFloat, false));
				writer.writeLine("Replace min 2: " + minMaxReplaces[1].toString(isFloat, true));
				writer.writeLine("Replace max 2: " + minMaxReplaces[1].toString(isFloat, false));

				writer.writeLine("Replace with: " + replace.toString(isFloat));
				writer.writeLine("Otherwise: " + otherwise.toString(isFloat));
			}

			if (type == TilingType.Images)
			{
				writer.writeLine("Image format: " + imageFormat);
				writer.writeLine("Add alpha: " + addAlpha);
			}
			else if (type == TilingType.Elevations)
			{
				writer.writeLine("Band: " + band);
				writer.writeLine("Buffer type: " + gdal.GetDataTypeName(bufferType));
			}
		}
		writer.flush();
	}

	public void logMinMax(NumberArray minmax, boolean isFloat) throws IOException
	{
		writer.newLine();
		writer.writeLine("Min/Max: ");
		writer.writeLine(minmax.toString(isFloat));
		writer.flush();
	}

	public void finishLog() throws IOException
	{
		if (logWriter != null)
			logWriter.close();
	}

	private String getDateTime()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
