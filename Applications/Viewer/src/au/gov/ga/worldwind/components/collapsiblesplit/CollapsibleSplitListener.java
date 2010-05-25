package au.gov.ga.worldwind.components.collapsiblesplit;

public interface CollapsibleSplitListener
{
	public void weightChanged(float weight);
	public void resizableToggled(boolean resizable);
	public void expandedToggled(boolean expanded);

	public class CollapsibleSplitAdapter implements CollapsibleSplitListener
	{
		@Override
		public void expandedToggled(boolean expanded)
		{
		}

		@Override
		public void resizableToggled(boolean resizable)
		{
		}

		@Override
		public void weightChanged(float weight)
		{
		}
	}
}
