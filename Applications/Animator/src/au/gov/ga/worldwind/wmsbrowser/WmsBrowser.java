package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getWindowTitleKey;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * A browser tool used to locate layers residing in WMS browsers
 * <p/>
 * Allows users to:
 * <ul>
 * 	<li> Add WMS servers to a known server list
 * 	<li> Browse WMS layers available on known servers
 * </ul>
 */
public class WmsBrowser
{
	private JFrame frame;
	
	public WmsBrowser(String parentApplicationTitle)
	{
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.wmsbrowser.data.messages.wmsBrowserMessages");
		frame = new JFrame(parentApplicationTitle + " - " + getMessage(getWindowTitleKey()));
	}
	
	/** Show the WMS Browser tool */
	public void show()
	{
		invokeTaskOnEDT(new Runnable() {
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	/** Hide the WMS Browser tool */
	public void hide()
	{
		invokeTaskOnEDT(new Runnable() {
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(false);
			}
		});
	}
	
	private void invokeTaskOnEDT(Runnable task)
	{
		try
		{
			if (SwingUtilities.isEventDispatchThread())
			{
				task.run();
			}
			else
			{
				SwingUtilities.invokeAndWait(task);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
