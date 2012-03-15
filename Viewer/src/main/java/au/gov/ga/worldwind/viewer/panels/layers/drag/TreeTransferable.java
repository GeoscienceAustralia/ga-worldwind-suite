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
package au.gov.ga.worldwind.viewer.panels.layers.drag;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * {@link Transferable} implementation used for transfering layers from the
 * layers tree or dataset tree to a location in the layers tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TreeTransferable implements Transferable
{
	private JTree source;
	private TreePath[] paths;
	private static final DataFlavor[] flavors = new DataFlavor[] { DataFlavor.stringFlavor };

	public TreeTransferable(JTree source, TreePath... data)
	{
		this.source = source;
		this.paths = data;
	}

	public JTree getSource()
	{
		return source;
	}

	public TreePath[] getPaths()
	{
		return paths;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return source.toString(); //useless, but at least serializable
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor == DataFlavor.stringFlavor;
	}
}
