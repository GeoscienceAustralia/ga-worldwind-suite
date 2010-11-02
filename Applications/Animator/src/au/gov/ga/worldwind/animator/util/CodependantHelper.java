package au.gov.ga.worldwind.animator.util;

import java.util.HashSet;
import java.util.Set;

public class CodependantHelper
{
	private final Armable armableOwner;
	private final Enableable enableableOwner;
	private final Set<Armable> codependantArmables = new HashSet<Armable>();
	private final Set<Enableable> codependantEnableable = new HashSet<Enableable>();

	public CodependantHelper(Armable armableOwner, Enableable enableableOwner)
	{
		this.armableOwner = armableOwner;
		this.enableableOwner = enableableOwner;
	}

	public void addCodependantArmable(Armable armable)
	{
		if (armableOwner != null && !codependantArmables.contains(armable))
		{
			codependantArmables.add(armable);
			armable.connectCodependantArmable(armableOwner);
		}
	}

	public void setCodependantArmed(boolean armed)
	{
		for (Armable armable : codependantArmables)
		{
			if (armable.isArmed() != armed)
			{
				armable.setArmed(armed);
			}
		}
	}

	public void addCodependantEnableable(Enableable enableable)
	{
		if (enableableOwner != null && !codependantEnableable.contains(enableable))
		{
			codependantEnableable.add(enableable);
			enableable.connectCodependantEnableable(enableableOwner);
		}
	}

	public void setCodependantEnabled(boolean enabled)
	{
		for (Enableable enableable : codependantEnableable)
		{
			if (enableable.isEnabled() != enabled)
			{
				enableable.setEnabled(enabled);
			}
		}
	}
}
