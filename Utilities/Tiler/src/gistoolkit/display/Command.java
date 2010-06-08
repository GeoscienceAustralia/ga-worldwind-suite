/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2002, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package gistoolkit.display;

/**
 * Represents a command that can be executed.
 */
public interface Command {
	/**
	 * called to execute this command from a button
	 */
	public void execute();
	/**
	 * called from the trigger of the draw model
	 */
	public void executeDraw(DrawModel inDrawModel);
	/**
	 * Used to set the GISDisplay of the command.
	 */
	 public void setGISDisplay(GISDisplay inDisplay);
	/**
	 * called when the draw model has been removed
	 */
	public void removeDraw(DrawModel inDrawModel);}