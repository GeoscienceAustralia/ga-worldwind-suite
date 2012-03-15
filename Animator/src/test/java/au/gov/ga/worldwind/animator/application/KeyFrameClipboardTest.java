package au.gov.ga.worldwind.animator.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * Unit tests for the {@link KeyFrameClipboard} class
 */
public class KeyFrameClipboardTest
{
	private static final int FRAME_WITH_KEY = 10;
	private static final int FRAME_WITHOUT_KEY = 11;

	private Mockery mockContext;
	private KeyFrameClipboard classToBeTested;
	private Animation animation;
	private States test;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		test = mockContext.states("test").startsAs("setup");
		
		initialiseAnimation();
		
		classToBeTested = new KeyFrameClipboard(animation);
		
		test.become("run");
	}

	private void initialiseAnimation()
	{
		animation = mockContext.mock(Animation.class);

		insertKeyFrame(10);
		insertKeyFrame(20);
		insertKeyFrame(30);
		
		mockContext.checking(new Expectations(){{
			allowing(animation).getCurrentFrame(); when(test.is("setup")); will(returnValue(0));
			allowing(animation).getKeyFrame(with(any(Integer.class)));will(returnValue(null));
		}});
	}
	
	@Test
	public void testCopyWithSelectedKeyPutsKeyInClipboard()
	{
		setCurrentFrame(FRAME_WITH_KEY);
		
		classToBeTested.copySelectedKeyFrame();
		
		assertTrue(classToBeTested.isKeyFrameInClipboard());
		assertEquals(animation.getKeyFrame(FRAME_WITH_KEY).getName(), classToBeTested.getKeyFrameInClipboard().getName());
	}
	
	@Test
	public void testCopyWithNoSelectedKeyDoesNotPutKeyInClipboard()
	{
		setCurrentFrame(FRAME_WITHOUT_KEY);

		classToBeTested.copySelectedKeyFrame();
		
		assertFalse(classToBeTested.isKeyFrameInClipboard());
		assertEquals(null, classToBeTested.getKeyFrameInClipboard());
	}
	
	@Test
	public void testCutWithSelectedKeyPutsKeyInClipboardRemovesKeyFromAnimation()
	{
		setCurrentFrame(FRAME_WITH_KEY);

		expectRemovalOfKeyFrame(FRAME_WITH_KEY);
		
		classToBeTested.cutSelectedKeyFrame();
		
		assertTrue(classToBeTested.isKeyFrameInClipboard());
	}
	
	@Test
	public void testCutWithNoSelectedKeyDoesNotPutKeyInClipboard()
	{
		setCurrentFrame(FRAME_WITHOUT_KEY);

		classToBeTested.cutSelectedKeyFrame();
		
		assertFalse(classToBeTested.isKeyFrameInClipboard());
		assertEquals(null, classToBeTested.getKeyFrameInClipboard());
	}
	
	@Test
	public void testCopyWithSelectedKeyFrameEnablesPasteAction()
	{
		assertFalse(classToBeTested.getPasteAction().isEnabled());
		
		setCurrentFrame(FRAME_WITH_KEY);
		classToBeTested.copySelectedKeyFrame();
		
		assertTrue(classToBeTested.getPasteAction().isEnabled());
	}
	
	@Test
	public void testCutWithSelectedKeyFrameEnablesPasteAction()
	{
		assertFalse(classToBeTested.getPasteAction().isEnabled());
		
		setCurrentFrame(FRAME_WITH_KEY);
		expectRemovalOfKeyFrame(FRAME_WITH_KEY);
		classToBeTested.cutSelectedKeyFrame();
		
		assertTrue(classToBeTested.getPasteAction().isEnabled());
	}
	
	@Test
	public void testCopyWithNoSelectedKeyFrameDoesNotEnablePasteAction()
	{
		assertFalse(classToBeTested.getPasteAction().isEnabled());
		
		setCurrentFrame(FRAME_WITHOUT_KEY);
		classToBeTested.copySelectedKeyFrame();
		
		assertFalse(classToBeTested.getPasteAction().isEnabled());
	}
	
	@Test
	public void testCutWithNoSelectedKeyFrameDoesNotEnablePasteAction()
	{
		assertFalse(classToBeTested.getPasteAction().isEnabled());
		
		setCurrentFrame(FRAME_WITHOUT_KEY);
		classToBeTested.cutSelectedKeyFrame();
		
		assertFalse(classToBeTested.getPasteAction().isEnabled());
	}
	
	@Test
	public void testPasteWithNoKeyFrameInClipboardDoesNotAttemptInsert()
	{
		assertFalse(classToBeTested.isKeyFrameInClipboard());
		
		expectNoCallToInsertKeyFrame();
		
		classToBeTested.pasteSelectedKeyFrame();
	}
	
	@Test
	public void testPasteWithKeyFrameInClipboardDoesAttemptInsert()
	{
		setCurrentFrame(FRAME_WITH_KEY);
		classToBeTested.copySelectedKeyFrame();
		assertTrue(classToBeTested.isKeyFrameInClipboard());
		
		expectCallToInsertKeyFrame();
		
		classToBeTested.pasteSelectedKeyFrame();
	}
	
	@Test
	public void testCurrentChangeFrameToKeyFrameEnablesCutCopyActions()
	{
		setCurrentFrame(FRAME_WITH_KEY);
		classToBeTested.currentFrameChanged(FRAME_WITH_KEY);
		
		assertTrue(classToBeTested.getCopyAction().isEnabled());
		assertTrue(classToBeTested.getCutAction().isEnabled());
	}
	
	@Test
	public void testCurrentChangeFrameToNonKeyFrameDisablesCutCopyActions()
	{
		setCurrentFrame(FRAME_WITHOUT_KEY);
		classToBeTested.currentFrameChanged(FRAME_WITHOUT_KEY);
		
		assertFalse(classToBeTested.getCopyAction().isEnabled());
		assertFalse(classToBeTested.getCutAction().isEnabled());
	}
	
	private void expectNoCallToInsertKeyFrame()
	{
		mockContext.checking(new Expectations(){{
			never(animation).insertKeyFrame(with(any(KeyFrame.class)), with(any(Boolean.class)));
		}});
	}
	
	private void expectCallToInsertKeyFrame()
	{
		mockContext.checking(new Expectations(){{
			oneOf(animation).insertKeyFrame(with(any(KeyFrame.class)), with(any(Boolean.class)));
		}});
	}


	private void expectRemovalOfKeyFrame(final int frame)
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(animation).removeKeyFrame(with(animation.getKeyFrame(frame)));
		}});
	}

	private void insertKeyFrame(final int frame)
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(animation).getKeyFrame(with(frame));will(returnValue(createKeyFrame(frame)));
		}});
	}
		
	private void setCurrentFrame(final int frame)
	{
		mockContext.checking(new Expectations(){{
			allowing(animation).getCurrentFrame(); when(test.is("run")); will(returnValue(frame)); 
		}});
	}
	
	private KeyFrame createKeyFrame(final int frame)
	{
		final ParameterValue pv = mockContext.mock(ParameterValue.class, "PV" + frame);
		mockContext.checking(new Expectations(){{
			allowing(pv).setFrame(with(any(Integer.class)));
			allowing(pv).addChangeListener(with(any(AnimationEventListener.class)));
			allowing(pv).getOwner();will(returnValue(mockContext.mock(Parameter.class, "P" + frame)));
			allowing(pv).clone();will(returnValue(pv));
		}});
		KeyFrame result = new KeyFrameImpl(frame, pv);
		result.setName("TestKeyFrame" + frame);
		return result;
	}
}
