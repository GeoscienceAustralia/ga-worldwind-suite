package au.gov.ga.worldwind.common.layers.kml.relativeio;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;

/**
 * Defines some extra properties for {@link KMLDoc}s that allow for better
 * resolving of relative document paths.
 * 
 * (Preferably the ideas in this package will be pushed into the WWJ SDK
 * someday).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface RelativeKMLDoc extends KMLDoc
{
	/**
	 * @return the original HREF element for this {@link KMLDoc} in the KML.
	 */
	String getHref();

	/**
	 * @return the parent {@link KMLDoc} from which this doc was referenced
	 *         (null if this is the root {@link KMLDoc}).
	 */
	KMLDoc getParent();

	/**
	 * KMZ files should be treated as containers (directories) when resolving
	 * relative paths. For instance, when resolving a parent directory with
	 * '..', the KMZ file should act as a directory.
	 * 
	 * @return whether this {@link KMLDoc} should act as a container (if it's a
	 *         KMZ file)
	 */
	boolean isContainer();
}
