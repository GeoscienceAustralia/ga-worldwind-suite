package au.gov.ga.worldwind.common.layers.model.gocad;

import java.net.URL;

import au.gov.ga.worldwind.common.util.FastShape;

/**
 * A reader that creates a {@link FastShape} from a GOCAD file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface GocadReader
{
	/**
	 * Called before reading any lines.
	 */
	void begin();

	/**
	 * Parse a line from the GOCAD file. The HEADER line and the END line are
	 * not passed to this function.
	 * 
	 * @param line
	 *            Single line read from the GOCAD file
	 */
	void addLine(String line);

	/**
	 * Called after reading all the lines from this GOCAD object. A
	 * {@link FastShape} can be created from the read geometry and returned.
	 * 
	 * @param context
	 *            URL context in which this object is being read; can be used to
	 *            resolve relative references
	 * 
	 * @return A {@link FastShape} containing the geometry read
	 */
	FastShape end(URL context);
}
