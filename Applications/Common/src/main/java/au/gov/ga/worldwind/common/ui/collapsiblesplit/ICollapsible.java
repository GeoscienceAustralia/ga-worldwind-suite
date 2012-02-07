package au.gov.ga.worldwind.common.ui.collapsiblesplit;

public interface ICollapsible
{
	public void addCollapseListener(CollapseListener listener);
	public void removeCollapseListener(CollapseListener listener);
	public boolean isCollapsed();
	public void setCollapsed(boolean collapsed);
}
