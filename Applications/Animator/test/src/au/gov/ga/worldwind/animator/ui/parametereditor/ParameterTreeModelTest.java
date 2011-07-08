package au.gov.ga.worldwind.animator.ui.parametereditor;

import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.WorldWindow;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;

/**
 * Unit tests for the {@link ParameterTreeModel} class
 */
public class ParameterTreeModelTest
{

	private Mockery mockContext;
	private WorldWindAnimationImpl animation;
	private ParameterTreeModel classUnderTest;

	@Before
	public void setup()
	{
		mockContext = new Mockery();
		animation = new WorldWindAnimationImpl(mockContext.mock(WorldWindow.class));
		classUnderTest = new ParameterTreeModel(animation);
	}
	
	@Test
	public void testModelAddedAsAnimationListener()
	{
		// Should be added after creation
		assertTrue(animation.getChangeListeners().contains(classUnderTest));
	}
	
	// TODO: Test tree change event fired on structural change
}
