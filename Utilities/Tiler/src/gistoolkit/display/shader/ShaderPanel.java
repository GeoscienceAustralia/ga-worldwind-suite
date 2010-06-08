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

package gistoolkit.display.shader;

import gistoolkit.display.Shader;
import javax.swing.JPanel;
/**
 * Abstract Class to allow shaders to generate GUI interfaces for editing just them.
 * Because each shader is different, perhaps radically from the others, there needs
 * to be some mechanism for the users to edit the attributes of their particular shader.
 * This abstract class allows the shaders to present a custom user interface to the
 * clients.
 */
public abstract class ShaderPanel extends JPanel{
    
    /** called when a shader is to be edited*/
    public abstract void setShader(Shader inShader);
    
    /** called when the editing is complete, and the shader is to be used */
    public abstract Shader getShader();

}

