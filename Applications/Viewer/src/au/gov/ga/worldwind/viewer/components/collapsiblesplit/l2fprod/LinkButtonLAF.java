package au.gov.ga.worldwind.viewer.components.collapsiblesplit.l2fprod;

import javax.swing.UIManager;

public class LinkButtonLAF
{
	public static void init()
	{
		UIManager.put(LinkButton.UI_CLASS_ID, LinkButtonUI.class
				.getCanonicalName());
	}
}
