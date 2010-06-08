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

package gistoolkit.projection;

import javax.swing.*;
/**
 * Abstract Class to allow projections to generate GUI interfaces for editing just them.
 * Because each projections is different, perhaps radically from the others, there needs
 * to be some mechanism for the users to edit the attributes of their particular projections.
 * This abstract class allows the projections to present a custom user interface to the
 * clients.
 */
public abstract class ProjectionPanel extends JPanel {

    /** Creates new ProjectionPanel */
    public ProjectionPanel() {
    }
    
    /** Set the projection to be edited */
    public abstract void setProjection(Projection inProjection);
    
    /** Retrieve the edited projection */
    public abstract Projection getProjection();

}
