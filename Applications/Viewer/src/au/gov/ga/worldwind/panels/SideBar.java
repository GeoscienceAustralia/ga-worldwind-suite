package au.gov.ga.worldwind.panels;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitConstraints;
import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitLayout;
import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitListener;
import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.components.collapsiblesplit.l2fprod.CollapsibleGroup;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemePanel;
import au.gov.ga.worldwind.theme.ThemePiece;
import au.gov.ga.worldwind.theme.ThemePanel.ThemePanelListener;

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

		for (ThemePanel panel : theme.getPanels())
		{
			CollapsibleGroup group = new CollapsibleGroup();
			group.setVisible(panel.isOn());
			group.setCollapsed(!panel.isExpanded());
			group.setScrollOnExpand(true);
			group.setLayout(new BorderLayout());
			group.setTitle(panel.getDisplayName());
			group.add(panel.getPanel(), BorderLayout.CENTER);

			CollapsibleSplitConstraints c = new CollapsibleSplitConstraints();
			c.expanded = panel.isExpanded();
			c.resizable = panel.isResizable();
			c.weight = panel.getWeight();

			pane.add(group, c);
			groups.add(group);

			PanelListener listener = new PanelListener(panel, group);
			pane.addListener(group, listener);
			panel.addListener(listener);
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
		if (isVisible() != anyVisible)
		{
			setVisible(anyVisible);
		}
		validate();
	}

	private class PanelListener implements ThemePanelListener, CollapsibleSplitListener
	{
		private final ThemePanel panel;
		private final CollapsibleGroup group;

		public PanelListener(ThemePanel panel, CollapsibleGroup group)
		{
			this.panel = panel;
			this.group = group;
		}

		@Override
		public void onToggled(ThemePiece source)
		{
			group.setVisible(source.isOn());
			refreshVisibility();
		}

		@Override
		public void displayNameChanged(ThemePiece source)
		{
			group.setTitle(source.getDisplayName());
		}

		@Override
		public void expandedToggled(ThemePanel source)
		{
			group.setCollapsed(!source.isExpanded());
		}

		@Override
		public void resizableToggled(ThemePanel source)
		{
			pane.getLayout().setResizable(group, source.isResizable());
			pane.validate();
		}

		@Override
		public void weightChanged(ThemePanel source)
		{
			pane.getLayout().setWeight(group, source.getWeight());
			pane.validate();
		}

		@Override
		public void expandedToggled(boolean expanded)
		{
			panel.setExpanded(expanded);
		}

		@Override
		public void weightChanged(float weight)
		{
			panel.setWeight(weight);
		}

		@Override
		public void resizableToggled(boolean resizable)
		{
		}
	}
}
