package au.gov.ga.worldwind.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.components.collapsiblesplit.l2fprod.CollapsibleGroup;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemePanel;

public class SideBar extends JPanel
{
	public SideBar(Theme theme)
	{
		super(new BorderLayout());

		final CollapsibleSplitPane pane = new CollapsibleSplitPane();
		pane.getLayout().setVertical(true);
		for (int i = 0; i < theme.getPanels().size(); i++)
			pane.getLayout().addPlaceholder("panel" + i, 1);

		int i = 0;
		for (ThemePanel panel : theme.getPanels())
		{
			CollapsibleGroup group = new CollapsibleGroup();
			pane.add(group, "panel" + i++);
			group.setScrollOnExpand(true);
			group.setLayout(new BorderLayout());
			group.setTitle(panel.getDisplayName());
			group.add(panel.getPanel(), BorderLayout.CENTER);
		}
		
		add(pane, BorderLayout.CENTER);
	}
}
