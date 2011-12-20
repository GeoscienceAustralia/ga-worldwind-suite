package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

import au.gov.ga.worldwind.common.util.FastShape;

/**
 * {@link GocadReader} implementation for reading PLine GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadPLineReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*pline.*";
	private final static String VERTEX_REGEX = "P?VRTX\\s+(\\d+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*";
	private final static String SEGMENT_REGEX = "SEG\\s+(\\d+)\\s+(\\d+).*";
	private final static String COLOR_REGEX = "\\*line\\*color:.+";
	private final static String NAME_REGEX = "name:\\s*(.*)\\s*";

	private List<Position> positions;
	private List<Integer> segmentIds;
	private Color color;
	private Map<Integer, Integer> vertexIdMap;
	private String name;
	private final Pattern vertexPattern = Pattern.compile(VERTEX_REGEX);
	private final Pattern segmentPattern = Pattern.compile(SEGMENT_REGEX);
	private final Pattern colorPattern = Pattern.compile(COLOR_REGEX);
	private final Pattern namePattern = Pattern.compile(NAME_REGEX);

	@Override
	public void begin()
	{
		positions = new ArrayList<Position>();
		segmentIds = new ArrayList<Integer>();
		vertexIdMap = new HashMap<Integer, Integer>();
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher;

		matcher = vertexPattern.matcher(line);
		if (matcher.matches())
		{
			int id = Integer.parseInt(matcher.group(1));
			double x = Double.parseDouble(matcher.group(2));
			double y = Double.parseDouble(matcher.group(3));
			double z = Double.parseDouble(matcher.group(4));
			Position position = Position.fromDegrees(y, x, z);

			if (vertexIdMap.containsKey(id))
			{
				throw new IllegalArgumentException("Duplicate vertex id: " + id);
			}
			vertexIdMap.put(id, positions.size());
			positions.add(position);
			return;
		}

		matcher = segmentPattern.matcher(line);
		if (matcher.matches())
		{
			int s1 = Integer.parseInt(matcher.group(1));
			int s2 = Integer.parseInt(matcher.group(2));
			segmentIds.add(s1);
			segmentIds.add(s2);
			return;
		}

		matcher = colorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
		}
	}

	@Override
	public FastShape end()
	{
		IntBuffer indicesBuffer = BufferUtil.newIntBuffer(segmentIds.size());
		for (Integer i : segmentIds)
		{
			if (!vertexIdMap.containsKey(i))
			{
				throw new IllegalArgumentException("Unknown vertex id: " + i);
			}
			indicesBuffer.put(vertexIdMap.get(i));
		}
		FastShape shape = new FastShape(positions, indicesBuffer, GL.GL_LINES);
		shape.setName(name);
		if (color != null)
		{
			shape.setColor(color);
		}
		return shape;
	}
}
