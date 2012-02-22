package au.gov.ga.worldwind.tiler.application;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Main class of the Tiler. Runs the {@link Application} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Executable
{
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

	public static void main(String[] args)
	{
		try
		{
			start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void start() throws Exception
	{
		// start the application
		Application.start();
	}
}
