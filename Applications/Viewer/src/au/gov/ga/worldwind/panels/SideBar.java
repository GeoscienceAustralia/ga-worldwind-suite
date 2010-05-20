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
import au.gov.ga.worldwind.theme.ThemePiece;
import au.gov.ga.worldwind.theme.ThemePanel.ThemePanelAdapter;

public class SideBar extends JPanel
{
	private CollapsibleSplitPane pane;
	private List<CollapsibleGroup> groups = new ArrayList<CollapsibleGroup>();

	public SideBar(Theme theme)
	{
		super(new BorderLayout());

		pane = new CollapsibleSplitPane();
		CollapsibleSplitLayout layout = pane.getLayout();
		layout.setVertical(true);

		int i = 0;
		for (ThemePanel panel : theme.getPanels())
		{
			String placeholderName = "panel" + i++;
			layout.addPlaceholder(placeholderName, panel.getWeight(), panel.isResizable());

			CollapsibleGroup group = new CollapsibleGroup();
			group.setVisible(panel.isOn());
			group.setCollapsed(!panel.isExpanded());
			group.setScrollOnExpand(true);
			group.setLayout(new BorderLayout());
			group.setTitle(panel.getDisplayName());
			group.add(panel.getPanel(), BorderLayout.CENTER);

			pane.add(group, placeholderName);
			groups.add(group);

			panel.addListener(new Listener(placeholderName, group));
		}

		add(pane, BorderLayout.CENTER);
	}

	public void refreshVisibility()
	{
		boolean anyVisible = false;
		for (CollapsibleGroup group : groups)
		{
			if (group.isVisible())
			{
				anyVisible = true;
				break;
			}
		}
		setVisible(anyVisible);
		validate();
	}

	private class Listener extends ThemePanelAdapter
	{
		private String placeholderName;
		private CollapsibleGroup group;

		public Listener(String placeholderName, CollapsibleGroup group)
		{
			this.placeholderName = placeholderName;
			this.group = group;
		}

		@Override
		public void onToggled(ThemePiece source)
		{
			group.setVisible(source.isOn());
			refreshVisibility();
		}

		@Override
		public void expandedToggled(ThemePanel source)
		{
			group.setCollapsed(!source.isExpanded());
		}

		@Override
		public void displayNameChanged(ThemePiece source)
		{
			group.setTitle(source.getDisplayName());
		}

		@Override
		public void resizableToggled(ThemePanel source)
		{
			pane.getLayout().setResizable(placeholderName, source.isResizable());
			pane.validate();
		}

		@Override
		public void weightChanged(ThemePanel source)
		{
			pane.getLayout().setWeight(placeholderName, source.getWeight());
			pane.validate();
		}
	}
}
