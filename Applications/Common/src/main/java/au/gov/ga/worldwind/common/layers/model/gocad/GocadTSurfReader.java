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
import java.nio.FloatBuffer;
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
 * {@link GocadReader} implementation for reading TSurf GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadTSurfReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*tsurf.*";

	private final static Pattern vertexPattern = Pattern
			.compile("P?VRTX\\s+(\\d+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern trianglePattern = Pattern.compile("TRGL\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");
	private final static Pattern colorPattern = Pattern.compile("\\*solid\\*color:.+");
	private final static Pattern namePattern = Pattern.compile("name:\\s*(.*)\\s*");
	private final static Pattern zpositivePattern = Pattern.compile("ZPOSITIVE\\s+(\\w+)\\s*");

	private GocadReaderParameters parameters;
	private List<Position> positions;
	private List<Integer> triangleIds;
	private Color color;
	private Map<Integer, Integer> vertexIdMap;
	private String name;
	private boolean zPositive = true;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
		positions = new ArrayList<Position>();
		triangleIds = new ArrayList<Integer>();
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

			if (parameters.getCoordinateTransformation() != null)
			{
				double[] transformed = new double[3];
				parameters.getCoordinateTransformation().TransformPoint(transformed, x, y, 0);
				x = transformed[0];
				y = transformed[1];
			}

			Position position = Position.fromDegrees(y, x, zPositive ? z : -z);

			if (vertexIdMap.containsKey(id))
			{
				throw new IllegalArgumentException("Duplicate vertex id: " + id);
			}
			vertexIdMap.put(id, positions.size());
			positions.add(position);
			return;
		}

		matcher = trianglePattern.matcher(line);
		if (matcher.matches())
		{
			int t1 = Integer.parseInt(matcher.group(1));
			int t2 = Integer.parseInt(matcher.group(2));
			int t3 = Integer.parseInt(matcher.group(3));
			triangleIds.add(t1);
			triangleIds.add(t2);
			triangleIds.add(t3);
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
		IntBuffer indicesBuffer = BufferUtil.newIntBuffer(triangleIds.size());
		for (Integer i : triangleIds)
		{
			if (!vertexIdMap.containsKey(i))
			{
				throw new IllegalArgumentException("Unknown vertex id: " + i);
			}
			indicesBuffer.put(vertexIdMap.get(i));
		}

		if (name == null)
		{
			name = "TSurf";
		}

		FastShape shape = new FastShape(positions, indicesBuffer, GL.GL_TRIANGLES);
		shape.setName(name);
		shape.setLighted(true);
		shape.setCalculateNormals(true);
		if (parameters.getColorMap() != null)
		{
			//TODO allow the user to specify which PVRTX property the color is defined by
			//for now, just assume the colormap is applied to elevations
			FloatBuffer colorBuffer = BufferUtil.newFloatBuffer(positions.size() * 4);
			for (Position position : positions)
			{
				Color color = parameters.getColorMap().calculateColor(position.elevation);
				colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
						.put(color.getAlpha() / 255f);
			}
			shape.setColorBufferElementSize(4);
			shape.setColorBuffer(colorBuffer);
		}
		else if (color != null)
		{
			shape.setColor(color);
		}
		
		/*double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
		for(Position position : positions)
		{
			min = Math.min(min, position.elevation);
			max = Math.max(max, position.elevation);
		}
		System.out.println(min + ", " + ((max + min) / 2) + ", " + max);*/
		
		return shape;
	}
}
