package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.application.effects.depthoffield.DepthOfFieldEffect;
import au.gov.ga.worldwind.animator.application.effects.edge.EdgeDetectionEffect;

public class BuiltInEffects
{
	public static void registerBuiltInEffects()
	{
		EffectRegistry.instance.registerEffect(DepthOfFieldEffect.class);
		EffectRegistry.instance.registerEffect(EdgeDetectionEffect.class);
	}
}
