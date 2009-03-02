package application;

public class AWTInputHandler extends gov.nasa.worldwind.awt.AWTInputHandler
{
	public AWTInputHandler()
	{
		super();
		getViewInputBroker().setSmoothViewChanges(false);
	}
}
