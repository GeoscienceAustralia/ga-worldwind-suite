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
package au.gov.ga.worldwind.animator.application.effects.edge;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getEdgeDetectionNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.effects.EffectBase;
import au.gov.ga.worldwind.common.render.FrameBuffer;

/**
 * Example {@link Effect} that convolves the input with a kernel matrix,
 * producing different filter effects like blurring, edge detection, sharpening,
 * embossing, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EdgeDetectionEffect extends EffectBase
{
	private final EdgeShader edgeShader = new EdgeShader();

	public EdgeDetectionEffect(String name, Animation animation)
	{
		super(name, animation);
	}

	protected EdgeDetectionEffect()
	{
		super();
	}

	@Override
	public Effect createWithAnimation(Animation animation)
	{
		return new EdgeDetectionEffect(null, animation);
	}

	@Override
	public String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getEdgeDetectionEffectElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		return new EdgeDetectionEffect(name, animation);
	}

	@Override
	public String getDefaultName()
	{
		return getMessageOrDefault(getEdgeDetectionNameKey(), "Edge Detection");
	}

	@Override
	protected void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions, FrameBuffer frameBuffer)
	{
		GL gl = dc.getGL();

		edgeShader.createIfRequired(gl);
		try
		{
			edgeShader.use(gl, dimensions.width, dimensions.height);
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId(), frameBuffer.getDepth().getId());
		}
		finally
		{
			edgeShader.unuse(gl);
		}
	}

	@Override
	protected void releaseEffect(DrawContext dc)
	{
		edgeShader.deleteIfCreated(dc.getGL());
	}
}
