package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.globes.Globe;

/**
 * This class exists to make the UTMCoordConverter class accessible (it is
 * currently package-private).
 * 
 * @author Michael de Hoog
 */
public class UTMCoordConverterPublic extends UTMCoordConverter
{
	public UTMCoordConverterPublic(Globe globe)
	{
		super(globe);
	}

	public UTMCoordConverterPublic(double a, double f)
	{
		super(a, f);
	}
}
