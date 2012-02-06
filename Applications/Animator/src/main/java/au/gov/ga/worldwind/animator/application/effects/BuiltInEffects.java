package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.application.effects.depthoffield.DepthOfFieldEffect;
import au.gov.ga.worldwind.animator.application.effects.edge.EdgeDetectionEffect;

/**
 * Helper class that simply registers the known built-in effects with the
 * {@link EffectRegistry}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BuiltInEffects
{
	public static void registerBuiltInEffects()
	{
		EffectRegistry.instance.registerEffect(DepthOfFieldEffect.class);
		EffectRegistry.instance.registerEffect(EdgeDetectionEffect.class);
	}
}
