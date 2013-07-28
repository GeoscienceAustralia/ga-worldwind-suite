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
package au.gov.ga.worldwind.androidremote.shared.model;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.androidremote.shared.MessageIO;

/**
 * Represents an item in a tree hierarchy. Could be a Layer, Dataset, or Place.
 * Used for transferring item information between devices, such as name, enabled
 * state, opacity, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public final class Item
{
	private static final Charset charset = Charset.forName("UTF-8");

	private String name;
	private boolean enabled;
	private boolean leaf;
	private float opacity; //for layers only
	private Item parent;
	private final List<Item> children = new ArrayList<Item>();

	public Item()
	{
		this(null);
	}

	public Item(Item parent)
	{
		this.parent = parent;
	}

	/**
	 * @return This item's name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set this item's name.
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Is this item enabled?
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Enable/disable this item.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @return Is this item a leaf in the tree hierarchy (ie has no children)?
	 */
	public boolean isLeaf()
	{
		return leaf;
	}

	/**
	 * Set this item as a leaf in the tree hierarchy (ie has no children).
	 * 
	 * @param layer
	 */
	public void setLeaf(boolean layer)
	{
		this.leaf = layer;
	}

	/**
	 * @return This item's opacity.
	 */
	public float getOpacity()
	{
		return opacity;
	}

	/**
	 * Set this item's opacity.
	 * 
	 * @param opacity
	 */
	public void setOpacity(float opacity)
	{
		this.opacity = opacity;
	}

	/**
	 * @return This item's parent item (null if root).
	 */
	public Item getParent()
	{
		return parent;
	}

	/**
	 * Set this item's parent item.
	 * 
	 * @param parent
	 */
	public void setParent(Item parent)
	{
		this.parent = parent;
	}

	/**
	 * @return A list of this item's child items.
	 */
	public List<Item> getChildren()
	{
		return children;
	}

	/**
	 * Add an item as a child of this item.
	 * 
	 * @param child
	 */
	public void addChild(Item child)
	{
		children.add(child);
	}

	/**
	 * Insert an item as a child of this item at a specific index in the child
	 * list.
	 * 
	 * @param index
	 * @param child
	 */
	public void insertChild(int index, Item child)
	{
		children.add(index, child);
	}

	/**
	 * Remove a child item of this item.
	 * 
	 * @param child
	 */
	public void removeChild(Item child)
	{
		children.remove(child);
	}

	/**
	 * @param child
	 * @return Index of the given child in this item's children list.
	 */
	public int indexOfChild(Item child)
	{
		return children.indexOf(child);
	}

	/**
	 * @return Index of this item in it's parent's children list (-1 if root).
	 */
	public int indexWithinParent()
	{
		if (getParent() == null)
		{
			return -1;
		}
		return getParent().indexOfChild(this);
	}

	/**
	 * @return Number of children this item has.
	 */
	public int childCount()
	{
		return children.size();
	}

	/**
	 * @param index
	 * @return Child item at the given index.
	 */
	public Item getChild(int index)
	{
		if (0 <= index && index < children.size())
		{
			return children.get(index);
		}
		return null;
	}

	/**
	 * @return Depth of this node in the tree (0 if root).
	 */
	public int depth()
	{
		int count = 0;
		Item parent = this;
		while ((parent = parent.getParent()) != null)
		{
			count++;
		}
		return count;
	}

	/**
	 * @return Array containing the index of the items in their parent's child
	 *         list, up to but not including the root item. This item's index is
	 *         the last item in the array.
	 */
	public int[] indicesToRoot()
	{
		int depth = depth();
		int[] indices = new int[depth];
		Item parent = this;
		while (depth > 0)
		{
			indices[--depth] = parent.indexWithinParent();
			parent = parent.getParent();
		}
		return indices;
	}

	/**
	 * @return Number of bytes required to store this item in a byte
	 *         array/buffer.
	 */
	public int getLength()
	{
		int length = 4; //id
		length += 4; //name length
		length += name == null ? 0 : charset.encode(name).limit(); //name string
		length += 1; //enabled
		length += 1; //layer
		length += 4; //opacity
		length += 4; //children length
		for (Item child : children)
		{
			length += child.getLength(); //children
		}
		return length;
	}

	/**
	 * Store this item in the given byte buffer.
	 * 
	 * @param buffer
	 */
	public void toBytes(ByteBuffer buffer)
	{
		//name
		if (name == null)
		{
			MessageIO.intToBytes(-1, buffer);
		}
		else
		{
			ByteBuffer nameBuffer = charset.encode(name);
			MessageIO.intToBytes(nameBuffer.limit(), buffer);
			buffer.put(nameBuffer);
		}

		//enabled
		buffer.put((byte) (enabled ? 1 : 0));

		//layer
		buffer.put((byte) (leaf ? 1 : 0));

		//opacity
		MessageIO.floatToBytes(opacity, buffer);

		//children
		MessageIO.intToBytes(children.size(), buffer);
		for (Item child : children)
		{
			child.toBytes(buffer);
		}
	}

	/**
	 * Read an item from the given byte buffer.
	 * 
	 * @param buffer
	 * @return New Item created from the bytes in the buffer.
	 * @see #toBytes(ByteBuffer)
	 */
	public static Item fromBytes(ByteBuffer buffer)
	{
		Item item = new Item();

		//name
		int nameLength = MessageIO.bytesToInt(buffer);
		if (nameLength < 0)
		{
			item.setName(null);
		}
		else
		{
			item.setName(MessageIO.bytesToString(buffer, nameLength, charset));
		}

		//enabled
		item.setEnabled(buffer.get() != 0);

		//layer
		item.setLeaf(buffer.get() != 0);

		//opacity
		item.setOpacity(MessageIO.bytesToFloat(buffer));

		//children
		int childCount = MessageIO.bytesToInt(buffer);
		for (int i = 0; i < childCount; i++)
		{
			Item child = Item.fromBytes(buffer);
			item.addChild(child);
			child.setParent(item);
		}

		return item;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
