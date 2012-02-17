/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

import java.awt.image.*;

/**
 * A filter which performs a simple 3x3 sharpening operation.
 */
public class SharpenFilter extends ConvolveFilter {
	
	private static float[] sharpenMatrix = {
		 0.0f, -0.2f,  0.0f,
		-0.2f,  1.8f, -0.2f,
		 0.0f, -0.2f,  0.0f
	};

	public SharpenFilter() {
		super(sharpenMatrix);
	}

	public String toString() {
		return "Blur/Sharpen";
	}
}
