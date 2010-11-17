package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getWindowTitleKey;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * A browser tool used to locate layers residing in WMS browsers
 * <p/>
 * Allows users to:
 * <ul>
 * 	<li> Add WMS servers to a known server list
 * 	<li> Browse WMS layers available on known servers
 *  <li> View information about a selected layer
 *  <li> Export a layer as a WW compatible layer definition for use in the Viewer and Animator
 * </ul>
 */
public class WmsBrowser
{
	private JFrame frame;
	private JSplitPane splitPane;
	
	public WmsBrowser(String parentApplicationTitle)
	{
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.wmsbrowser.data.messages.wmsBrowserMessages");
		
		initialiseWindow(parentApplicationTitle);
		initialiseWindowContents();
	}

	private void initialiseWindow(String parentApplicationTitle)
	{
		frame = new JFrame(parentApplicationTitle + " - " + getMessage(getWindowTitleKey()));
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				WmsBrowserSettings.get().setSplitLocation(splitPane.getDividerLocation());
				WmsBrowserSettings.save();
			}
		});
	}
	
	private void initialiseWindowContents()
	{
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(WmsBrowserSettings.get().getSplitLocation());
		panel.add(splitPane, BorderLayout.CENTER);
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
	
	/**
	 * Invokes the provided runnable task on the EDT. Use to ensure 
	 * GUI updates are performed on the EDT, where they should be.
	 */
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
