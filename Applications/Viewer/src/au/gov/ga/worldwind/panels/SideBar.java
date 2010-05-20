package au.gov.ga.worldwind.panels;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitLayout;
import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.components.collapsiblesplit.l2fprod.CollapsibleGroup;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemePanel;
import au.gov.ga.worldwind.theme.ThemePiece.ThemePieceListener;

public class SideBar extends JPanel implements ThemePieceListener
{
	private List<CollapsibleGroup> groups = new ArrayList<CollapsibleGroup>();
	private List<ThemePanel> panels = new ArrayList<ThemePanel>();

	public SideBar(Theme theme)
	{
		super(new BorderLayout());

		final CollapsibleSplitPane pane = new CollapsibleSplitPane();
		CollapsibleSplitLayout layout = pane.getLayout();
		layout.setVertical(true);

		int i = 0;
		for (ThemePanel panel : theme.getPanels())
		{
			String placeholderName = "panel" + i++;
			layout.addPlaceholder(placeholderName, panel.getWeight(), panel.isResizable());

			CollapsibleGroup group = new CollapsibleGroup();
			group.setVisible(panel.isOn());
			group.setScrollOnExpand(true);
			group.setLayout(new BorderLayout());
			group.setTitle(panel.getDisplayName());
			group.add(panel.getPanel(), BorderLayout.CENTER);

			pane.add(group, placeholderName);
			groups.add(group);
			panels.add(panel);

			panel.addListener(this);
		}

		add(pane, BorderLayout.CENTER);
	}

	@Override
	public void onToggled(boolean on)
	{
		refreshVisibility();
	}

	public void refreshVisibility()
	{
		boolean anyVisible = false;
		for (int i = 0; i < groups.size(); i++)
		{
			boolean visible = panels.get(i).isOn();
			groups.get(i).setVisible(visible);
			anyVisible |= visible;
		}
		setVisible(anyVisible);
		repaint();
	}
}
