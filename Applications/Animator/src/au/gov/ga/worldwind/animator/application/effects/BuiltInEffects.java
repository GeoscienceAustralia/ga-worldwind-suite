package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.application.effects.depthoffield.DepthOfFieldEffect;

public class BuiltInEffects
{
	public static void registerBuiltInEffects()
	{
		EffectRegistry.instance.registerEffect(DepthOfFieldEffect.class);
	}
}
