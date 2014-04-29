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

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.util.List;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffect;
import au.gov.ga.worldwind.animator.application.render.OffscreenRenderer;
import au.gov.ga.worldwind.common.effects.Effect;
import au.gov.ga.worldwind.common.render.ExtendedSceneController;

/**
 * A custom scene controller that supports {@link AnimatableEffect}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnimatorSceneController extends ExtendedSceneController
{
	private Dimension renderDimensions;
	private Animation animation;

	@Override
	public void doRepaint(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);

		super.doRepaint(dc);
	}
	
	@Override
	protected Dimension getDrawDimensions()
	{
		return renderDimensions != null ? renderDimensions : super.getDrawDimensions();
	}
	
	@Override
	public List<? extends Effect> getDrawEffects()
	{
		return animation.getEffects();
	}

	/**
	 * @return The current render dimensions (null if not rendering)
	 */
	public Dimension getRenderDimensions()
	{
		return renderDimensions;
	}

	/**
	 * Set the current render dimensions. This is called by the
	 * {@link OffscreenRenderer} when the rendering begins.
	 * 
	 * @param renderDimensions
	 */
	public void setRenderDimensions(Dimension renderDimensions)
	{
		this.renderDimensions = renderDimensions;
	}

	/**
	 * @return The current animation
	 */
	public Animation getAnimation()
	{
		return animation;
	}

	/**
	 * Set the current animation. This is called by the {@link Animator} when
	 * the animation changes.
	 * 
	 * @param animation
	 */
	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}
}
