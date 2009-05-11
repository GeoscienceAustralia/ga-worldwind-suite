package tiler;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public class Tiler
{
	/*
	BUTTONS
	-------
	Open File
	Preview?
	
	INFORMATION
	-----------
	Dimensions
	Extents
	Band count
	Buffer format
	
	TILING OPTIONS
	--------------
	Level Zero Tile Size (degrees)
	Tile size (pixels)
	No data value (show value for each band)
	Create overviews (true/false)
	
	FOR IMAGES:
	Image format (JPEG or PNG)
	Save alpha channel (PNG only)
	
	FOR BILS:
	Output type (byte, int16, int32)
	Band to use (if more than one band)
	 */
	
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
	}

	public final Logger logger;
	private JFrame frame;

	public static void main(String[] args)
	{
		new Tiler();
	}

	public Tiler()
	{
		GridBagConstraints c;

		frame = new JFrame("Tiler");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new GridBagLayout());

		JPanel panel = new JPanel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(panel, c);

		JTextPane textLog = new JTextPane();
		JScrollPane paneScrollPane = new JScrollPane(textLog);
		paneScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		frame.add(paneScrollPane, c);
		logger = new DocumentLogger("TilerLogger", textLog.getStyledDocument());
		LogManager.getLogManager().addLogger(logger);

		logger.info("Started");

		frame.setSize(640, 480);
		frame.setVisible(true);
	}
}
