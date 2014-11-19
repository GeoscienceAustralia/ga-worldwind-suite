/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.application;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddEffectLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddElevationModelLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddExaggeratorLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddHeadLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddKeyMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddSunPositionLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAnimateClippingLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAutoKeyMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getClearClipLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getClipSectorLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDeleteKeyMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDynamicStereoMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getExitMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getFirstFrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getKeyValuesMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getLastFrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getLogEventsLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getNewMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getNext10FrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getNextFrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getOpenMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterValuesMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPreviewMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPreviewX10MenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPreviewX2MenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPrevious10FrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getPreviousFrameMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderHighResMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderMenuTooltipKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderStandardResMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getResizeToRenderDimensionsLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSaveAsMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSaveMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getScaleAnimationMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSetFrameCountMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSetProxyLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowAboutMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowCameraPathLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowCrosshairsLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowGridLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowParameterEditorMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowRuleOfThirdsLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowTutorialMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowUserGuideMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getShowWireframeMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSmoothEyeSpeedMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getStereoCameraMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getTargetModeMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getUseZoomScalingMenuLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import au.gov.ga.worldwind.animator.application.debug.DebugWriter;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.animator.view.AnimatorView;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.SelectableAction;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A factory used for creating and accessing action objects used in the Animator application
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorActionFactory
{
	private BasicAction newAnimationAction;
	private BasicAction openAnimationAction;
	private BasicAction saveAnimationAction;
	private BasicAction saveAnimationAsAction;
	private BasicAction exitAction;
	
	private BasicAction addKeyAction;
	private BasicAction deleteKeyAction;
	private SelectableAction autoKeyAction;
	private BasicAction setFrameCountAction;
	private BasicAction previousFrameAction;
	private BasicAction nextFrameAction;
	private BasicAction previous10FramesAction;
	private BasicAction next10FramesAction;
	private BasicAction firstFrameAction;
	private BasicAction lastFrameAction;
	
	private SelectableAction useScaledZoomAction;
	private BasicAction scaleAnimationAction;
	private BasicAction smoothEyeSpeedAction;
	private SelectableAction showWireframeAction;
	private SelectableAction targetModeAction;
	private BasicAction previewAction;
	private BasicAction previewX2Action;
	private BasicAction previewX10Action;
	private BasicAction renderAction;
	private BasicAction renderHiResAction;
	private BasicAction renderLowResAction;
	private BasicAction resizeToRenderDimensionsAction;
	private BasicAction addElevationModelAction;
	private BasicAction addExaggeratorAction;
	private BasicAction addEffectAction;
	private BasicAction addSunPositionAction;
	private BasicAction addHeadAction;
	private BasicAction clipSectorAction;
	private BasicAction clearClipAction;
	private BasicAction setProxyAction;
	private SelectableAction showCameraPathAction;
	private SelectableAction showGridAction;
	private SelectableAction showRuleOfThirdsAction;
	private SelectableAction showCrosshairsAction;
	
	private SelectableAction animateClippingAction;
	private SelectableAction stereoCameraAction;
	private SelectableAction dynamicStereoAction;
	
	private SelectableAction showParameterEditorAction;
	//private SelectableAction showWmsBrowserAction;
	
	private BasicAction showUserGuideAction;
	private BasicAction showTutorialAction;
	private BasicAction showAboutAction;
	
	private BasicAction debugKeyFramesAction;
	private BasicAction debugParameterValuesAction;
	private SelectableAction logAnimationEventsAction;
	
	private Animator targetApplication;

	public AnimatorActionFactory(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "An animator application is required");
		
		this.targetApplication = targetApplication;
		initialiseActions();
	}
	
	/**
	 * Initialise the actions used in the application
	 */
	private void initialiseActions()
	{
		// New
		newAnimationAction = new BasicAction(getMessage(getNewMenuLabelKey()), null);
		newAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		newAnimationAction.setIcon(Icons.newfile.getIcon());
		newAnimationAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.newFile();
			}
		});
		
		// Open
		openAnimationAction = new BasicAction(getMessage(getOpenMenuLabelKey()), null);
		openAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		openAnimationAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.open();
			}
		});
		
		// Save
		saveAnimationAction = new BasicAction(getMessage(getSaveMenuLabelKey()), null);
		saveAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		saveAnimationAction.setIcon(Icons.save.getIcon());
		saveAnimationAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.save();
			}
		});
		
		// Save as
		saveAnimationAsAction = new BasicAction(getMessage(getSaveAsMenuLabelKey()), null);
		saveAnimationAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		saveAnimationAsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		saveAnimationAsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.saveAs();
			}
		});
		
		// Exit
		exitAction = new BasicAction(getMessage(getExitMenuLabelKey()), null);
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		exitAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.quit();
			}
		});
		
		// Add key
		addKeyAction = new BasicAction(getMessage(getAddKeyMenuLabelKey()), null);
		addKeyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		addKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		addKeyAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.addFrame();
			}
		});
		
		// Delete key
		deleteKeyAction = new BasicAction(getMessage(getDeleteKeyMenuLabelKey()), null);
		deleteKeyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		deleteKeyAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.deleteSelectedKey();
			}
		});
		
		// Auto key
		autoKeyAction = new SelectableAction(getMessage(getAutoKeyMenuLabelKey()), null, false);
		autoKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		autoKeyAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setAutokey(autoKeyAction.isSelected());
			}
		});
		
		// Set frame count
		setFrameCountAction = new BasicAction(getMessage(getSetFrameCountMenuLabelKey()), null);
		setFrameCountAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		setFrameCountAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptToSetFrameCount();
			}
		});
		
		// Previous frame
		previousFrameAction = new BasicAction(getMessage(getPreviousFrameMenuLabelKey()), null);
		previousFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
		previousFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		previousFrameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToPreviousFrame();
			}
		});
		
		// Next frame
		nextFrameAction = new BasicAction(getMessage(getNextFrameMenuLabelKey()), null);
		nextFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
		nextFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		nextFrameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToNextFrame();
			}
		});
		
		// Previous 10 frame
		previous10FramesAction = new BasicAction(getMessage(getPrevious10FrameMenuLabelKey()), null);
		previous10FramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_DOWN_MASK));
		previous10FramesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToPrevious10Frame();
			}
		});
		
		// Next 10 frame
		next10FramesAction = new BasicAction(getMessage(getNext10FrameMenuLabelKey()), null);
		next10FramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_DOWN_MASK));
		next10FramesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToNext10Frame();
			}
		});
		
		// First frame
		firstFrameAction = new BasicAction(getMessage(getFirstFrameMenuLabelKey()), null);
		firstFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ActionEvent.CTRL_MASK));
		firstFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		firstFrameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToFirstFrame();
			}
		});
		
		// Last frame
		lastFrameAction = new BasicAction(getMessage(getLastFrameMenuLabelKey()), null);
		lastFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, ActionEvent.CTRL_MASK));
		lastFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		lastFrameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.moveToLastFrame();
			}
		});
		
		// Use scaled zoom
		useScaledZoomAction = new SelectableAction(getMessage(getUseZoomScalingMenuLabelKey()), null, targetApplication.getCurrentAnimation().isZoomScalingRequired());
		useScaledZoomAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
		useScaledZoomAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setZoomScalingRequired(useScaledZoomAction.isSelected());
			}
		});
		
		// Scale animation
		scaleAnimationAction = new BasicAction(getMessage(getScaleAnimationMenuLabelKey()), null);
		scaleAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		scaleAnimationAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.scaleAnimation();
			}
		});
		
		// Smooth eye speed
		smoothEyeSpeedAction = new BasicAction(getMessage(getSmoothEyeSpeedMenuLabelKey()), null);
		smoothEyeSpeedAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		smoothEyeSpeedAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.smoothEyeSpeed();
			}
		});
		
		// Show wireframe
		showWireframeAction = new SelectableAction(getMessage(getShowWireframeMenuLabelKey()), null, false);
		showWireframeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
		showWireframeAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.showWireframe(showWireframeAction.isSelected());
			}
		});
		
		// Target mode
		targetModeAction = new SelectableAction(getMessage(getTargetModeMenuLabelKey()), null,
				((AnimatorView) targetApplication.getCurrentAnimation().getWorldWindow().getView()).isTargetMode());
		targetModeAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.targetMode(targetModeAction.isSelected());
			}
		});
		
		// Preview
		previewAction = new BasicAction(getMessage(getPreviewMenuLabelKey()), null);
		previewAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		previewAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		previewAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.preview(1);
			}
		});
		
		// Preview x2
		previewX2Action = new BasicAction(getMessage(getPreviewX2MenuLabelKey()), null);
		previewX2Action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
		previewX2Action.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.preview(2);
			}
		});
		
		// Preview x10
		previewX10Action = new BasicAction(getMessage(getPreviewX10MenuLabelKey()), null);
		previewX10Action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK));
		previewX10Action.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.preview(10);
			}
		});
		
		// Render
		renderAction = new BasicAction(getMessage(getRenderMenuLabelKey()), getMessage(getRenderMenuTooltipKey()), Icons.render.getIcon());
		renderAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		renderAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		renderAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptForRender();
			}
		});
		
		// Render hi-res
		renderHiResAction = new BasicAction(getMessage(getRenderHighResMenuLabelKey()), null);
		renderHiResAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK|ActionEvent.CTRL_MASK));
		renderHiResAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.renderAnimation(1);
			}
		});
		
		// Render low-res
		renderLowResAction = new BasicAction(getMessage(getRenderStandardResMenuLabelKey()), null);
		renderLowResAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.renderAnimation(0);
			}
		});
		
		// Resize to render dimensions
		resizeToRenderDimensionsAction = new BasicAction(getMessage(getResizeToRenderDimensionsLabelKey()), null);
		resizeToRenderDimensionsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.resizeWindowToRenderDimensions();
			}
		});
		
		// Add elevation model
		addElevationModelAction = new BasicAction(getMessage(getAddElevationModelLabelKey()), Icons.exaggeration.getIcon());
		addElevationModelAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		addElevationModelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		addElevationModelAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptToAddElevationModel();
			}
		});
		
		// Add exaggerator
		addExaggeratorAction = new BasicAction(getMessage(getAddExaggeratorLabelKey()), null);
		addExaggeratorAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		addExaggeratorAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		addExaggeratorAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptToAddElevationExaggerator();
			}
		});
		
		addEffectAction = new BasicAction(getMessage(getAddEffectLabelKey()), null);
		addEffectAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptToAddEffect();
			}
		});
		
		addSunPositionAction = new BasicAction(getMessage(getAddSunPositionLabelKey()), null);
		addSunPositionAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.addSunPositionAnimatable();
			}
		});
		
		addHeadAction = new BasicAction(getMessage(getAddHeadLabelKey()), null);
		addHeadAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.addHeadAnimatable();
			}
		}); 
		
		clipSectorAction = new BasicAction(getMessage(getClipSectorLabelKey()), Icons.cut.getIcon());
		clipSectorAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.clipSector();
			}
		});

		clearClipAction = new BasicAction(getMessage(getClearClipLabelKey()), Icons.cutdelete.getIcon());
		clearClipAction.setEnabled(false);
		clearClipAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.clearClipping();
			}
		});
		
		setProxyAction = new BasicAction(getMessage(getSetProxyLabelKey()), null);
		setProxyAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.promptToSetProxy();
			}
		});
		
		// Show camera path
		showCameraPathAction = new SelectableAction(getMessage(getShowCameraPathLabelKey()), null, Settings.get().isCameraPathShown());
		showCameraPathAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setCameraPathVisible(showCameraPathAction.isSelected());
			}
		});
		
		// Show grid
		showGridAction = new SelectableAction(getMessage(getShowGridLabelKey()), null, Settings.get().isGridShown());
		showGridAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setGridVisible(showGridAction.isSelected());
			}
		});
		
		// Show rule of thirds
		showRuleOfThirdsAction = new SelectableAction(getMessage(getShowRuleOfThirdsLabelKey()), null, Settings.get().isRuleOfThirdsShown());
		showRuleOfThirdsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setRuleOfThirdsVisible(showRuleOfThirdsAction.isSelected());
			}
		});
		
		// Show crosshairs
		showCrosshairsAction = new SelectableAction(getMessage(getShowCrosshairsLabelKey()), null, Settings.get().isCrosshairsShown());
		showCrosshairsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setCrosshairsVisible(showCrosshairsAction.isSelected());
			}
		});
		
		animateClippingAction = new SelectableAction(getMessage(getAnimateClippingLabelKey()), null, false);
		animateClippingAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setCameraClippingAnimatable(animateClippingAction.isSelected());
			}
		});
		
		// Show parameter editor
		showParameterEditorAction = new SelectableAction(getMessage(getShowParameterEditorMenuLabelKey()), null, false);
		showParameterEditorAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		showParameterEditorAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setParameterEditorVisible(showParameterEditorAction.isSelected());
			}
		});
		
		// Show wms editor
		/*showWmsBrowserAction = new SelectableAction(getMessage(getShowWmsBrowserMenuLabelKey()), null, false);
		showWmsBrowserAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		showWmsBrowserAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setWmsBrowserVisible(showWmsBrowserAction.isSelected());
			}
		});*/
		
		showUserGuideAction = new BasicAction(getMessage(getShowUserGuideMenuLabelKey()), null);
		showUserGuideAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.showUserGuide();
			}
		});
		
		showTutorialAction = new BasicAction(getMessage(getShowTutorialMenuLabelKey()), null);
		showTutorialAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.showTutorials();
			}
		});
		
		showAboutAction = new BasicAction(getMessage(getShowAboutMenuLabelKey()), null);
		showAboutAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.showAboutDialog();
			}
		});
		
		// Debug key frames
		debugKeyFramesAction = new BasicAction(getMessage(getKeyValuesMenuLabelKey()), null);
		debugKeyFramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		debugKeyFramesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		debugKeyFramesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpKeyFrameValues("keyFrames.txt", targetApplication.getCurrentAnimation());
			}
		});
		
		// Debug parameter values
		debugParameterValuesAction = new BasicAction(getMessage(getParameterValuesMenuLabelKey()), null);
		debugParameterValuesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		debugParameterValuesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		debugParameterValuesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpParameterValues("parameterValues.txt",
												targetApplication.getCurrentAnimation(),
												targetApplication.getCurrentAnimation().getAllParameters(), 
												targetApplication.getCurrentAnimation().getFrameOfFirstKeyFrame(), 
												targetApplication.getCurrentAnimation().getFrameOfLastKeyFrame());
			}
		});
		
		logAnimationEventsAction = new SelectableAction(getMessage(getLogEventsLabelKey()), null, Settings.get().isAnimationEventsLogged());
		logAnimationEventsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setEnableAnimationEventLogging(logAnimationEventsAction.isSelected());
			}
		});
		
		stereoCameraAction = new SelectableAction(getMessage(getStereoCameraMenuLabelKey()), null, false);
		stereoCameraAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setUseStereoCamera(stereoCameraAction.isSelected());
				dynamicStereoAction.setEnabled(stereoCameraAction.isSelected());
			}
		});
		
		dynamicStereoAction = new SelectableAction(getMessage(getDynamicStereoMenuLabelKey()), null, true);
		dynamicStereoAction.setEnabled(false);
		dynamicStereoAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				targetApplication.setUseDynamicStereo(dynamicStereoAction.isSelected());
			}
		});
	}


	public BasicAction getNewAnimationAction()
	{
		return newAnimationAction;
	}

	public BasicAction getOpenAnimationAction()
	{
		return openAnimationAction;
	}

	public BasicAction getSaveAnimationAction()
	{
		return saveAnimationAction;
	}

	public BasicAction getSaveAnimationAsAction()
	{
		return saveAnimationAsAction;
	}

	public BasicAction getExitAction()
	{
		return exitAction;
	}

	public BasicAction getAddKeyAction()
	{
		return addKeyAction;
	}

	public BasicAction getDeleteKeyAction()
	{
		return deleteKeyAction;
	}

	public SelectableAction getAutoKeyAction()
	{
		return autoKeyAction;
	}

	public BasicAction getSetFrameCountAction()
	{
		return setFrameCountAction;
	}

	public BasicAction getPreviousFrameAction()
	{
		return previousFrameAction;
	}

	public BasicAction getNextFrameAction()
	{
		return nextFrameAction;
	}

	public BasicAction getPrevious10FramesAction()
	{
		return previous10FramesAction;
	}

	public BasicAction getNext10FramesAction()
	{
		return next10FramesAction;
	}

	public BasicAction getFirstFrameAction()
	{
		return firstFrameAction;
	}

	public BasicAction getLastFrameAction()
	{
		return lastFrameAction;
	}

	public SelectableAction getUseScaledZoomAction()
	{
		return useScaledZoomAction;
	}

	public BasicAction getScaleAnimationAction()
	{
		return scaleAnimationAction;
	}

	public BasicAction getSmoothEyeSpeedAction()
	{
		return smoothEyeSpeedAction;
	}

	public BasicAction getPreviewAction()
	{
		return previewAction;
	}

	public BasicAction getPreviewX2Action()
	{
		return previewX2Action;
	}

	public BasicAction getPreviewX10Action()
	{
		return previewX10Action;
	}

	public BasicAction getRenderAction()
	{
		return renderAction;
	}
	
	public BasicAction getRenderHiResAction()
	{
		return renderHiResAction;
	}

	public BasicAction getRenderLowResAction()
	{
		return renderLowResAction;
	}

	public BasicAction getResizeToRenderDimensionsAction()
	{
		return resizeToRenderDimensionsAction;
	}

	public BasicAction getAddElevationModelAction()
	{
		return addElevationModelAction;
	}

	public BasicAction getAddExaggeratorAction()
	{
		return addExaggeratorAction;
	}

	public BasicAction getAddEffectAction()
	{
		return addEffectAction;
	}
	
	public BasicAction getAddSunPositionAction()
	{
		return addSunPositionAction;
	}

	public BasicAction getAddHeadAction()
	{
		return addHeadAction;
	}

	public BasicAction getSetProxyAction()
	{
		return setProxyAction;
	}

	public BasicAction getDebugKeyFramesAction()
	{
		return debugKeyFramesAction;
	}

	public BasicAction getDebugParameterValuesAction()
	{
		return debugParameterValuesAction;
	}

	public SelectableAction getShowCameraPathAction()
	{
		return showCameraPathAction;
	}

	public SelectableAction getShowGridAction()
	{
		return showGridAction;
	}

	public SelectableAction getShowRuleOfThirdsAction()
	{
		return showRuleOfThirdsAction;
	}
	
	public SelectableAction getShowCrosshairsAction()
	{
		return showCrosshairsAction;
	}
	
	public SelectableAction getLogAnimationEventsAction()
	{
		return logAnimationEventsAction;
	}
	
	public SelectableAction getShowParameterEditorAction()
	{
		return showParameterEditorAction;
	}

	public SelectableAction getStereoCameraAction()
	{
		return stereoCameraAction;
	}

	public SelectableAction getDynamicStereoAction()
	{
		return dynamicStereoAction;
	}
	
	/*public SelectableAction getShowWmsBrowserAction()
	{
		return showWmsBrowserAction;
	}*/
	
	public SelectableAction getAnimateClippingAction()
	{
		return animateClippingAction;
	}
	
	public BasicAction getShowUserGuideAction()
	{
		return showUserGuideAction;
	}
	
	public BasicAction getShowTutorialAction()
	{
		return showTutorialAction;
	}
	
	public BasicAction getShowAboutAction()
	{
		return showAboutAction;
	}
	
	public SelectableAction getShowWireframeAction()
	{
		return showWireframeAction;
	}

	public SelectableAction getTargetModeAction()
	{
		return targetModeAction;
	}

	public BasicAction getClipSectorAction()
	{
		return clipSectorAction;
	}

	public BasicAction getClearClipAction()
	{
		return clearClipAction;
	}
}
