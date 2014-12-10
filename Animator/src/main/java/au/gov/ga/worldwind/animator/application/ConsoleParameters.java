/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.worldwind.animator.application;

import com.beust.jcommander.Parameter;

/**
 * Animator console parameters.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ConsoleParameters
{
	@Parameter(names = { "-i", "-input" }, description = "The input animation file to render.", required = true)
	public String inputFile = null;

	@Parameter(names = { "-o", "-output" }, description = "The output frame file (to which the frame number and the file extension is appended).", required = true)
	public String outputFile = null;

	@Parameter(names = { "-l", "-lod" }, description = "The level of detail to use for the elevation model.", required = false)
	public double lod = 1.0;

	@Parameter(names = { "-s", "-start" }, description = "Override the first frame to render.", required = false)
	public Integer start = null;

	@Parameter(names = { "-e", "-end" }, description = "Override the last frame to render.", required = false)
	public Integer end = null;

	@Parameter(names = { "-w", "-width" }, description = "Override the width of the rendered frames.", required = false)
	public Integer width = null;

	@Parameter(names = { "-h", "-height" }, description = "Override the height of the rendered frames.", required = false)
	public Integer height = null;

	@Parameter(names = { "-?", "-help" }, description = "Print these command line usage instructions.")
	public boolean showUsage = false;
}
