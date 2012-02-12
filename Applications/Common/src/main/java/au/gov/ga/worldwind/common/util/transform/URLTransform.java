package au.gov.ga.worldwind.common.util.transform;

/**
 * Defines a transformation from a url string to a transformed url string. For
 * example, this may be used to add a port number to urls that match a certain
 * pattern.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface URLTransform
{
	/**
	 * Transform the given url
	 * 
	 * @param url
	 *            Url to transform
	 * @return Transformed url
	 */
	String transformURL(String url);
}
