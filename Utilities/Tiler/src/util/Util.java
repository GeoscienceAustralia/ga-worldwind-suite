package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.gdal.gdal.Dataset;

public class Util
{
	public static String fixNewlines(String s)
	{
		String newLine = System.getProperty("line.separator");
		return s.replaceAll("(\\r\\n)|(\\r)|(\\n)", newLine);
	}

	public static int levelCount(Dataset dataset, double lztd, Sector sector, int tilesize)
	{
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		double lonPixels = sector.getDeltaLongitude() / width;
		double latPixels = sector.getDeltaLatitude() / height;
		double texelSize = Math.min(latPixels, lonPixels);
		int level = (int) Math.ceil(Math.log10(texelSize * tilesize / lztd) / Math.log10(0.5)) + 1;
		return Math.max(level, 1);
	}

	public static int tileCount(Sector sector, int level, double lztsd)
	{
		int minX = getTileX(sector.getMinLongitude() + 1e-10, level, lztsd);
		int maxX = getTileX(sector.getMaxLongitude() - 1e-10, level, lztsd);
		int minY = getTileY(sector.getMinLatitude() + 1e-10, level, lztsd);
		int maxY = getTileY(sector.getMaxLatitude() - 1e-10, level, lztsd);
		return (maxX - minX + 1) * (maxY - minY + 1);
	}

	public static int getTileX(double longitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude + 180) / (lztsd * layerpow);
		return (int) X;
	}

	public static int getTileY(double latitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude + 90) / (lztsd * layerpow);
		return (int) Y;
	}

	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}

	public static int limitRange(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	public static List<Point> linePoints(float x1, float y1, float x2, float y2)
	{
		List<Point> points = new ArrayList<Point>();

		int x1i = Math.round(x1);
		int y1i = Math.round(y1);
		int x2i = Math.round(x2);
		int y2i = Math.round(y2);

		//handle special vertical/horizontal line cases
		if (x1i == x2i || y1i == y2i)
		{
			if (x1i == x2i)
			{
				for (int y = y1i; y <= y2i; y++)
					points.add(new Point(x1i, y));
			}
			else
			{
				for (int x = x1i; x <= x2i; x++)
					points.add(new Point(x, y1i));
			}
			return points;
		}

		boolean steep = Math.abs(x2 - x1) < Math.abs(y2 - y1);
		if (steep)
		{
			//swap x/y variables
			float temp = x1;
			x1 = y1;
			y1 = temp;
			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		if (x1 > x2)
		{
			//swap p1/p2 points
			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		float dx = x2 - x1;
		float dy = y2 - y1;
		float gradient = dy / dx;
		float intery = y1;
		int startX = Math.round(x1);
		int endX = Math.round(x2);
		float centerDiff = (startX - x1) * gradient;

		for (int x = startX; x <= endX; x++)
		{
			int y = Math.round(intery);
			int add = (intery + centerDiff - y < 0) ? -1 : 1;
			boolean first = add > 0 ^ gradient < 0;

			if (first)
				points.add(steep ? new Point(y, x) : new Point(x, y));

			points.add(steep ? new Point(y + add, x) : new Point(x, y + add));

			if (!first)
				points.add(steep ? new Point(y, x) : new Point(x, y));

			intery += gradient;
		}

		return points;
	}

	public static void main(String[] args)
	{
		final JFrame frame = new JFrame()
		{
			@Override
			public void paint(Graphics g)
			{
				int size = 20;
				Point offset = new Point(3 * size, 3 * size);

				g.setColor(Color.yellow);
				int off = 0;
				for (int x = 0; x < getWidth() / size; x++)
					g.drawLine(x * size + off, 0, x * size + off, getHeight());
				for (int y = 0; y < getHeight() / size; y++)
					g.drawLine(0, y * size + off, getWidth(), y * size + off);

				drawLine(g, size, offset, 3, 2, 25, 10);
				drawLine(g, size, offset, 0, 0, 5, 21);
				drawLine(g, size, offset, 14, 12, 30.0f, 13);
				drawLine(g, size, offset, 20.9f, 15.7f, 14.1f, 31.2f);
				drawLine(g, size, offset, 38.5f, 40.5f, 23.5f, 25.5f);
			}

			private void drawLine(Graphics g, int size, Point offset, float x1, float y1, float x2,
					float y2)
			{
				List<Point> points = linePoints(x1, y1, x2, y2);
				g.setColor(Color.black);
				for (Point p : points)
				{
					g.drawRect(offset.x + p.x * size, offset.y + p.y * size, size, size);
				}
				g.setColor(Color.green);
				g.drawLine(Math.round(x1 * size + size / 2f) + offset.x, Math.round(y1 * size
						+ size / 2f)
						+ offset.y, Math.round(x2 * size + size / 2f) + offset.x, Math.round(y2
						* size + size / 2f)
						+ offset.y);
			}
		};
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
