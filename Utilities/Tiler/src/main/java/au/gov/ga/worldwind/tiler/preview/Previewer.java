package au.gov.ga.worldwind.tiler.preview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.gdal.gdal.gdal;

import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.util.FileFilters.DirectoryFileFilter;
import au.gov.ga.worldwind.tiler.util.Util;

/**
 * JFrame which provides the ability to preview tilesets.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Previewer extends JFrame
{
	private File directory;
	private String extension;
	private double lzts;
	private boolean elevations = false;
	private int type;
	private int typeSize;
	private boolean floatingPoint;
	private Double nodata;
	private int width;
	private int height;

	private BufferedImage[] images;
	private String[] labels;
	private int xpos = 0, ypos = 0, xposOld = Integer.MAX_VALUE, yposOld = Integer.MAX_VALUE;
	private int level = 0, levelOld = Integer.MAX_VALUE;
	private int minlevel, maxlevel;
	private int xCount, yCount;
	private int dragX, dragY;
	private int dragXPos, dragYPos;
	private int xoffset = 0, yoffset = 0;

	public Previewer(File directory, String extension, double lzts)
	{
		this.directory = directory;
		this.extension = extension;
		this.lzts = lzts;
		initilize();
	}

	public Previewer(File directory, String extension, int elevationDataType, int width, int height, Double nodata,
			double lzts)
	{
		this.elevations = true;
		this.directory = directory;
		this.extension = extension;
		this.lzts = lzts;
		this.type = elevationDataType;
		this.typeSize = gdal.GetDataTypeSize(this.type) / 8;
		this.floatingPoint = GDALTile.isTypeFloatingPoint(this.type);
		this.width = width;
		this.height = height;
		this.nodata = nodata;
		initilize();
	}

	private void initilize()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent we)
			{
				onExit();
			}
		});
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Rectangle bounds = getRootPane().getBounds();
				int x = e.getX() - bounds.x - xoffset;
				int y = bounds.height - (e.getY() - bounds.y - yoffset);

				double tileW = bounds.width / (double) xCount;
				double tileH = bounds.height / (double) yCount;
				int tileX = (int) Math.floor(x / tileW) + xpos;
				int tileY = (int) Math.floor(y / tileH) + ypos;

				if (e.isControlDown())
				{
					int index = (tileY - ypos + 1) * (xCount + 2) + (tileX - xpos + 1);
					JDialog dialog = new ImageView(images[index]);
					dialog.setSize(800, 800);
					dialog.setVisible(true);
				}
				else
				{
					boolean left = e.getButton() == MouseEvent.BUTTON1;
					int newLevel = left ? level + 1 : level - 1;
					newLevel = Math.max(minlevel, Math.min(maxlevel, newLevel));
					if (newLevel == level)
						return;

					level = newLevel;
					if (left)
					{
						xpos = tileX * 2 - xCount / 2;
						ypos = tileY * 2 - yCount / 2;
					}
					else
					{
						xpos = tileX / 2 - xCount / 2;
						ypos = tileY / 2 - yCount / 2;
					}
					if (xCount % 2 == 0)
						xoffset = (int) (-tileW / 2);
					if (yCount % 2 == 0)
						yoffset = (int) (-tileH / 2);

					loadImages();
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				dragX = e.getX() - xoffset;
				dragY = e.getY() - yoffset;
				dragXPos = xpos;
				dragYPos = ypos;
			}
		});
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Rectangle bounds = getRootPane().getBounds();
				double tileW = bounds.width / (double) xCount;
				double tileH = bounds.height / (double) yCount;

				int draggedX = e.getX() - dragX;
				int draggedY = e.getY() - dragY;
				int sizeX = (int) (draggedX / tileW);
				int sizeY = (int) (draggedY / tileH);
				xoffset = (int) (draggedX - sizeX * tileW);
				yoffset = (int) (draggedY - sizeY * tileH);
				xpos = dragXPos - sizeX;
				ypos = dragYPos + sizeY;
				loadImages();
				repaint();
			}
		});

		calculateLevels();
		loadImages();

		setSize(1000, 500);
		setVisible(true);

		createBufferStrategy(2);
	}

	private void onExit()
	{
	}

	private void calculateLevels()
	{
		xCount = (int) (360d / lzts);
		yCount = (int) (180d / lzts);

		File[] dirs = directory.listFiles(new DirectoryFileFilter());
		minlevel = Integer.MAX_VALUE;
		maxlevel = Integer.MIN_VALUE;
		for (int i = 0; i < dirs.length; i++)
		{
			try
			{
				int num = Integer.parseInt(dirs[i].getName());
				if (num < minlevel)
					minlevel = num;
				if (num > maxlevel)
					maxlevel = num;
			}
			catch (NumberFormatException e)
			{
			}
		}
	}

	private void loadImages()
	{
		setTitle("World Wind Tile Previewer - Level " + level + ", Position (" + xpos + "," + ypos + ")");

		if (level == levelOld && xpos == xposOld && ypos == yposOld)
			return;

		int size = (xCount + 2) * (yCount + 2);
		if (images == null)
		{
			images = new BufferedImage[size];
			labels = new String[size];
		}

		Rectangle dontReload = null;
		if (level == levelOld)
		{
			int deltax = xpos - xposOld;
			int deltay = ypos - yposOld;
			dontReload =
					new Rectangle(Math.max(-deltax, 0), Math.max(-deltay, 0),
							Math.max(xCount - Math.abs(deltax) + 2, 0), Math.max(yCount - Math.abs(deltay) + 2, 0));

			if (!dontReload.isEmpty())
			{
				BufferedImage[] imagesNew = new BufferedImage[size];
				String[] labelsNew = new String[size];

				for (int y = dontReload.y; y < dontReload.height + dontReload.y; y++)
				{
					for (int x = dontReload.x; x < dontReload.width + dontReload.x; x++)
					{
						int indexNew = x + y * (xCount + 2);
						int indexOld = (x + deltax) + (y + deltay) * (xCount + 2);
						imagesNew[indexNew] = images[indexOld];
						labelsNew[indexNew] = labels[indexOld];
					}
				}

				images = imagesNew;
				labels = labelsNew;
			}
		}

		levelOld = level;
		xposOld = xpos;
		yposOld = ypos;

		File levelDir = new File(directory, String.valueOf(level));

		int index = 0;
		for (int y = -1; y < yCount + 1; y++)
		{
			for (int x = -1; x < xCount + 1; x++)
			{
				if (dontReload == null || !dontReload.contains(x + 1, y + 1))
				{
					int X = x + xpos;
					int Y = y + ypos;
					labels[index] = X + "," + Y;

					File rowDir = new File(levelDir, Util.paddedInt(Y, 4));
					File file = new File(rowDir, Util.paddedInt(Y, 4) + "_" + Util.paddedInt(X, 4) + "." + extension);
					images[index] = null;
					if (file.exists())
					{
						try
						{
							images[index] = loadImage(file);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				index++;
			}
		}
	}

	@Override
	public void update(Graphics g)
	{
		paint(g);
	}

	@Override
	public void paint(Graphics pg)
	{
		BufferStrategy bf = this.getBufferStrategy();
		Graphics g = null;

		try
		{
			g = bf.getDrawGraphics();

			Rectangle drawRect = getRootPane().getBounds();
			double tileW = drawRect.width / (double) xCount;
			double tileH = drawRect.height / (double) yCount;

			Graphics2D g2 = (Graphics2D) g;

			g2.translate(drawRect.x, drawRect.y);

			g2.setBackground(Color.white);
			g2.clearRect(0, 0, drawRect.width, drawRect.height);

			// draw transparent background
			int transparentSquareSize = 15;
			g2.setColor(new Color(220, 220, 220));
			for (int y = 0; y < drawRect.height; y += transparentSquareSize)
			{
				for (int x = 0; x < drawRect.width; x += transparentSquareSize)
				{
					if ((x / transparentSquareSize % 2 == 0) ^ (y / transparentSquareSize % 2 == 0))
					{
						g2.fillRect(x, y, transparentSquareSize, transparentSquareSize);
					}
				}
			}

			g2.translate(xoffset, yoffset);

			int index = 0;
			for (int y = -1; y < yCount + 1; y++)
			{
				int y1 = (int) ((yCount - y - 1) * tileH);
				int h = (int) ((yCount - y) * tileH) - y1;
				for (int x = -1; x < xCount + 1; x++)
				{
					int x1 = (int) (x * tileW);
					int w = (int) ((x + 1) * tileW) - x1;

					if (images[index] == null)
					{
						g2.setColor(new Color(255, 200, 200));
					}
					else
					{
						g2.setColor(Color.darkGray);
						g2.drawImage(images[index], x1, y1, w, h, null);
					}
					g2.drawRect(x1, y1, w - 1, h - 1);

					if (labels[index] != null)
					{
						g2.setColor(Color.black);
						int stringWidth = g2.getFontMetrics().stringWidth(labels[index]);
						g2.drawString(labels[index], x1 + w / 2 - stringWidth / 2, y1 + h / 2);
					}

					index++;
				}
			}
		}
		finally
		{
			if (g != null)
				g.dispose();
		}
		bf.show();
	}

	private BufferedImage loadImage(File file) throws IOException
	{
		if (!elevations)
			return ImageIO.read(file);
		else
		{
			FileInputStream fis = null;

			ByteBuffer bb = ByteBuffer.allocate((int) file.length());
			bb.order(ByteOrder.LITTLE_ENDIAN); // TODO fix hard coded byte order
			int length = bb.limit() / typeSize;

			try
			{
				fis = new FileInputStream(file);
				fis.getChannel().read(bb);
				bb.rewind();
			}
			finally
			{
				if (fis != null)
					fis.close();
			}

			double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
			for (int i = 0; i < width * height && i < length; i++)
			{
				double value = getBufferValue(bb);
				if (nodata != null && value == nodata)
					continue;
				if (value < min)
					min = value;
				if (value > max)
					max = value;
			}

			if (max == min)
				max = min + 1;

			int pixels = width * height;
			int[] offsets = new int[4];
			offsets[3] = pixels;

			bb.rewind();
			byte[] bytes = new byte[pixels * 2];
			for (int i = 0; i < width * height && i < length; i++)
			{
				double value = getBufferValue(bb);
				bytes[i] = (byte) (255d * (value - min) / (max - min));
				bytes[i + pixels] = (byte) (nodata != null && value == nodata ? 0 : 255);
			}

			DataBuffer dataBuffer = new DataBufferByte(bytes, bytes.length);
			SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, offsets);
			WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			image.setData(raster);
			return image;
		}
	}

	private double getBufferValue(ByteBuffer buffer)
	{
		if (floatingPoint)
			return GDALTile.getDoubleValue(buffer, type);
		else
			return GDALTile.getLongValue(buffer, type);
	}

	private class ImageView extends JDialog
	{
		private BufferedImage image;

		public ImageView(BufferedImage image)
		{
			super(Previewer.this, true);
			this.image = image;

			swapBackground();

			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					swapBackground();
				}
			});
		}

		private void swapBackground()
		{
			if (getBackground() != Color.white)
			{
				setBackground(Color.white);
				setForeground(new Color(220, 220, 220));
			}
			else
			{
				setBackground(Color.black);
				setForeground(new Color(35, 35, 35));
			}
		}

		@Override
		public void paint(Graphics g)
		{
			Rectangle drawRect = getRootPane().getBounds();

			Graphics2D g2 = (Graphics2D) g;
			g2.translate(drawRect.x, drawRect.y);

			g2.setBackground(getBackground());
			g2.clearRect(0, 0, drawRect.width, drawRect.height);

			int transparentSquareSize = 15;
			g2.setColor(getForeground());
			for (int y = 0; y < drawRect.height; y += transparentSquareSize)
			{
				for (int x = 0; x < drawRect.width; x += transparentSquareSize)
				{
					if ((x / transparentSquareSize % 2 == 0) ^ (y / transparentSquareSize % 2 == 0))
					{
						g2.fillRect(x, y, transparentSquareSize, transparentSquareSize);
					}
				}
			}

			if (image != null)
			{
				g.drawImage(image, 0, 0, drawRect.width, drawRect.height, null);
			}
		}

		@Override
		public void update(Graphics g)
		{
			paint(g);
		}
	}
}
