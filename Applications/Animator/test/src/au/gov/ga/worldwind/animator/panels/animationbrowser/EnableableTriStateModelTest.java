package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;
import au.gov.ga.worldwind.animator.util.Enableable;

/**
 * Unit tests for the {@link EnableableTriStateModel} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class EnableableTriStateModelTest
{

	private EnableableTriStateModel classToBeTested;
	
	private MockEnableable mockValue;
	
	@Before
	public void setup()
	{
		mockValue = new MockEnableable();
		classToBeTested = new EnableableTriStateModel(mockValue);
	}
	
	@Test
	public void testIsCheckedWhenAllChildrenEnabledAndEnabled()
	{
		mockValue.enabled = true;
		mockValue.allChildrenEnabled = true;
		mockValue.hasEnabledChildren = true;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.CHECKED, result);
	}
	
	@Test
	public void testIsCheckedWhenNoChildrenAndEnabled()
	{
		mockValue.enabled = true;
		mockValue.allChildrenEnabled = true;
		mockValue.hasEnabledChildren = false;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.CHECKED, result);
	}
	
	@Test
	public void testIsCheckedWhenEnabledAndSomeChildrenDisabled()
	{
		mockValue.enabled = true;
		mockValue.allChildrenEnabled = false;
		mockValue.hasEnabledChildren = true;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.PARTIAL, result);
	}
	
	@Test
	public void testIsCheckedWhenDisabledAndAllChildrenEnabled()
	{
		mockValue.enabled = false;
		mockValue.allChildrenEnabled = true;
		mockValue.hasEnabledChildren = true;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.PARTIAL, result);
	}
	
	@Test
	public void testIsCheckedWhenDisabledAndSomeChildrenEnabled()
	{
		mockValue.enabled = false;
		mockValue.allChildrenEnabled = false;
		mockValue.hasEnabledChildren = true;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.PARTIAL, result);
	}
	
	@Test
	public void testIsCheckedWhenDisabledAndNoChildrenEnabled()
	{
		mockValue.enabled = false;
		mockValue.allChildrenEnabled = false;
		mockValue.hasEnabledChildren = false;
		
		State result = classToBeTested.getCurrentState();
		
		assertEquals(State.UNCHECKED, result);
	}
	
	/**
	 * A mock implementation of the {@link Enableable} interface that allows the results of each method to be explicitly set
	 *
	 */
	private static final class MockEnableable implements Enableable
	{
		boolean enabled;
		boolean allChildrenEnabled;
		boolean hasEnabledChildren;
		
		
		@Override
		public boolean isEnabled()
		{
			return enabled;
		}

		@Override
		public boolean isAllChildrenEnabled()
		{
			return allChildrenEnabled;
		}

		@Override
		public boolean hasEnabledChildren()
		{
			return hasEnabledChildren;
		}

		@Override
		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
		
	}
	
}
