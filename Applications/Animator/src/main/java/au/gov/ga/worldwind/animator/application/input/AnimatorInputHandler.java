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
package au.gov.ga.worldwind.animator.application.input;

import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickedObjectList;

import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * An extension of the {@link AWTInputHandler} that generates 
 * {@link SelectEvent}s in response to right-click dragging etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorInputHandler extends AWTInputHandler
{
	
	@Override
	public void mouseDragged(MouseEvent mouseEvent)
	{
		if (getWorldWindow() == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (mouseEvent == null)
        {
            return;
        }

        Point prevMousePoint = this.getMousePoint();
        this.setMousePoint(mouseEvent.getPoint());
        this.callMouseDraggedListeners(mouseEvent);

        if (MouseEvent.BUTTON1_DOWN_MASK == mouseEvent.getModifiersEx() || MouseEvent.BUTTON3_DOWN_MASK == mouseEvent.getModifiersEx())
        {
            PickedObjectList pickedObjects = this.getObjectsAtButtonPress();
            if (isDragging
                || (pickedObjects != null && pickedObjects.getTopPickedObject() != null
                && !pickedObjects.getTopPickedObject().isTerrain()))
            {
                isDragging = true;
                callSelectListeners(new DragSelectEvent(getWorldWindow(), SelectEvent.DRAG, mouseEvent, pickedObjects, prevMousePoint));
            }
        }

        if (!isDragging)
        {
            if (!mouseEvent.isConsumed())
            {
                getWorldWindow().getView().getViewInputHandler().mouseDragged(mouseEvent);
            }
        }

        // Redraw to update the current position and selection.
        if (getWorldWindow().getSceneController() != null)
        {
        	getWorldWindow().getSceneController().setPickPoint(mouseEvent.getPoint());
        	getWorldWindow().redraw();
        }
	}
	
}
