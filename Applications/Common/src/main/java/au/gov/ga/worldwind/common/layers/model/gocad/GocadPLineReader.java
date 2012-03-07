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
package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.FastShape;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link GocadReader} implementation for reading PLine GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadPLineReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*pline.*";

	private final static Pattern vertexPattern = Pattern
			.compile("P?VRTX\\s+(\\d+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern segmentPattern = Pattern.compile("SEG\\s+(\\d+)\\s+(\\d+).*");
	private final static Pattern colorPattern = Pattern.compile("\\*line\\*color:.+");
	private final static Pattern namePattern = Pattern.compile("name:\\s*(.*)\\s*");
	private final static Pattern zpositivePattern = Pattern.compile("ZPOSITIVE\\s+(\\w+)\\s*");

	private GocadReaderParameters parameters;
	private List<Position> positions;
	private List<Integer> segmentIds;
	private Color color;
	private Map<Integer, Integer> vertexIdMap;
	private String name;
	private boolean zPositive = true;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
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
			z = zPositive ? z : -z;
			
			if(parameters.getCoordinateTransformation() != null)
			{
				double[] transformed = new double[3];
				parameters.getCoordinateTransformation().TransformPoint(transformed, x, y, z);
				x = transformed[0];
				y = transformed[1];
				z = transformed[2];
			}
			
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
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}
		
		matcher = zpositivePattern.matcher(line);
		if(matcher.matches())
		{
			zPositive = !matcher.group(1).equalsIgnoreCase("depth");
		}
	}

	@Override
	public FastShape end(URL context)
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

		if (name == null)
		{
			name = "PLine";
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
