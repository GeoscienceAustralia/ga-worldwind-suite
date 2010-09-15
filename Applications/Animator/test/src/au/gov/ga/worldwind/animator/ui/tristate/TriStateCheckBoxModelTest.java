package au.gov.ga.worldwind.animator.ui.tristate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;

/**
 * Unit tests for the {@link TriStateCheckBox} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class TriStateCheckBoxModelTest
{

	private DefaultTriStateCheckBoxModel classToBeTested;
	
	private Mockery mockContext = new Mockery();
	
	@Before
	public void setup()
	{
		classToBeTested = new DefaultTriStateCheckBoxModel();
	}
	
	@Test
	public void testNextStateFromChecked()
	{
		classToBeTested.setCurrentState(State.CHECKED);
		
		classToBeTested.iterateState();
		
		assertFalse(classToBeTested.isChecked());
		assertTrue(classToBeTested.isUnchecked());
		assertFalse(classToBeTested.isPartiallyChecked());
	}
	
	@Test
	public void testNextStateFromUnchecked()
	{
		classToBeTested.setCurrentState(State.UNCHECKED);
		
		classToBeTested.iterateState();
		
		assertTrue(classToBeTested.isChecked());
		assertFalse(classToBeTested.isUnchecked());
		assertFalse(classToBeTested.isPartiallyChecked());
	}
	
	@Test
	public void testNextStateFromPartial()
	{
		classToBeTested.setCurrentState(State.PARTIAL);
		
		classToBeTested.iterateState();
		
		assertFalse(classToBeTested.isChecked());
		assertTrue(classToBeTested.isUnchecked());
		assertFalse(classToBeTested.isPartiallyChecked());
	}
	
	@Test
	public void testIterateStateFiresChange()
	{
		final TriStateEventListener listener = mockContext.mock(TriStateEventListener.class);
		mockContext.checking(new Expectations(){{
			oneOf(listener).stateChanged(with(classToBeTested), with(State.CHECKED), with(State.UNCHECKED));
		}});
		
		classToBeTested.addEventListener(listener);
		classToBeTested.iterateState();
	}
	
}
