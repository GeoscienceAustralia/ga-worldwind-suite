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
package au.gov.ga.worldwind.common.ui.lazytree;

/**
 * 
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILazyTreeObject extends ITreeObject
{
	/**
	 * This method is called by the lazy tree when this node is expanded for the
	 * first time. Subclasses should use this method to load their own children.
	 * If a known error occurs, this method should throw a LazyLoadException,
	 * which will display the exception's message as a child node.
	 * 
	 * @throws Exception
	 *             If loading the children fails for some reason
	 */
	public void load() throws Exception;

	public void addListener(LazyLoadListener listener);

	public void removeListener(LazyLoadListener listener);
}
