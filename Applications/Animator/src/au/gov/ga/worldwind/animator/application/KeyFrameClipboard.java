package au.gov.ga.worldwind.animator.application;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCopyKeyFrameLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCutKeyFrameLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPasteKeyFrameLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.ui.frameslider.ChangeFrameListener;
import au.gov.ga.worldwind.animator.ui.frameslider.CurrentFrameChangeListener;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.ui.BasicAction;

/**
 * A clipboard that holds a keyframe and can perform cut-copy-paste operations 
 */
public class KeyFrameClipboard implements CurrentFrameChangeListener, ChangeFrameListener, AnimationEventListener, ChangeOfAnimationListener
{
	/** The  key frame currently in the clipboard */
	private KeyFrame clipboardKeyFrame;
	
	private Animation animation;
	
	private BasicAction copyAction;
	private BasicAction cutAction;
	private BasicAction pasteAction;
	
	public KeyFrameClipboard(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		initialiseActions();
		updateActionEnabledStatus();
	}
	
	private void initialiseActions()
	{
		copyAction = new BasicAction(getMessage(getCopyKeyFrameLabelKey()), Icons.copy.getIcon());
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		copyAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				copySelectedKeyFrame();
			}
		});
		
		cutAction = new BasicAction(getMessage(getCutKeyFrameLabelKey()), Icons.cut.getIcon());
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		cutAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cutSelectedKeyFrame();
			}
		});
		
		pasteAction = new BasicAction(getMessage(getPasteKeyFrameLabelKey()), Icons.paste.getIcon());
		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		pasteAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				pasteSelectedKeyFrame();
			}
		});
	}

	public boolean isKeyFrameInClipboard()
	{
		return clipboardKeyFrame != null;
	}
	
	public KeyFrame getKeyFrameInClipboard()
	{
		return clipboardKeyFrame;
	}
	
	public void copySelectedKeyFrame()
	{
		KeyFrame selectedKeyFrame = getSelectedKeyFrame();
		if (selectedKeyFrame != null)
		{
			clipboardKeyFrame = selectedKeyFrame.clone();
			updateActionEnabledStatus();
		}
	}
	
	public void cutSelectedKeyFrame()
	{
		KeyFrame selectedKeyFrame = getSelectedKeyFrame();
		if (selectedKeyFrame != null)
		{
			clipboardKeyFrame = selectedKeyFrame.clone();
			animation.removeKeyFrame(selectedKeyFrame);
			updateActionEnabledStatus();
		}
	}

	public void pasteSelectedKeyFrame()
	{
		if (!isKeyFrameInClipboard())
		{
			return;
		}
		animation.insertKeyFrame(createKeyFrameForCurrentFrame(), true);
		updateActionEnabledStatus();
	}
	
	public void clearClipboard()
	{
		clipboardKeyFrame = null;
		pasteAction.setEnabled(false);
	}
	
	public void updateAnimation(Animation newAnimation)
	{
		Validate.notNull(newAnimation, "An animation is required");
		this.animation = newAnimation;
		clearClipboard();
	}
	
	private KeyFrame createKeyFrameForCurrentFrame()
	{
		return new KeyFrameImpl(getCurrentFrame(), clipboardKeyFrame.getParameterValues());
	}
	
	private KeyFrame getSelectedKeyFrame()
	{
		return animation.getKeyFrame(getCurrentFrame());
	}

	private int getCurrentFrame()
	{
		return animation.getCurrentFrame();
	}

	public BasicAction getCopyAction()
	{
		return copyAction;
	}

	public BasicAction getCutAction()
	{
		return cutAction;
	}

	public BasicAction getPasteAction()
	{
		return pasteAction;
	}

	@Override
	public void currentFrameChanged(int newFrame)
	{
		updateActionEnabledStatus();
	}
	
	@Override
	public void frameChanged(int index, int oldFrame, int newFrame)
	{
		updateActionEnabledStatus();
	}
	
	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (event.getRootCause().getValue() instanceof KeyFrame)
		{
			updateActionEnabledStatus();
		}
	}
	
	private void updateActionEnabledStatus()
	{
		boolean hasKeyAtFrame = getSelectedKeyFrame() != null;
		cutAction.setEnabled(hasKeyAtFrame);
		copyAction.setEnabled(hasKeyAtFrame);
		
		pasteAction.setEnabled(isKeyFrameInClipboard());
	}
}
