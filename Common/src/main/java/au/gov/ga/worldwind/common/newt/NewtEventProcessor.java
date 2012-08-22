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
package au.gov.ga.worldwind.common.newt;

import java.awt.Component;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.NEWTEventFiFo;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowUpdateEvent;

/**
 * Helper class that processes NEWT events from a NEWT {@link Window}, converts
 * the events to AWT events, and forwards the AWT events to AWT event listeners
 * provided in the constructor.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtEventProcessor extends NEWTEventFiFo implements com.jogamp.newt.event.MouseListener,
		com.jogamp.newt.event.KeyListener, com.jogamp.newt.event.WindowListener
{
	protected final KeyListener awtKeyHandler;
	protected final MouseListener awtMouseHandler;
	protected final MouseMotionListener awtMouseMotionHandler;
	protected final MouseWheelListener awtMouseWheelHandler;
	protected final FocusListener awtFocusListener;
	protected final Component awtSourceComponent;

	protected boolean mouseDragged = false;

	public NewtEventProcessor(KeyListener awtKeyHandler, MouseListener awtMouseHandler,
			MouseMotionListener awtMouseMotionHandler, MouseWheelListener awtMouseWheelHandler,
			FocusListener awtFocusListener, Component awtSourceComponent)
	{
		this.awtKeyHandler = awtKeyHandler;
		this.awtMouseHandler = awtMouseHandler;
		this.awtMouseMotionHandler = awtMouseMotionHandler;
		this.awtMouseWheelHandler = awtMouseWheelHandler;
		this.awtFocusListener = awtFocusListener;
		this.awtSourceComponent = awtSourceComponent;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		put(e);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		put(e);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		put(e);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		//AWT doesn't raise click events after a drag, but NEWT does, so follow AWT behaviour.
		if (!mouseDragged)
		{
			put(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		put(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		put(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		put(e);
		mouseDragged = false;
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		put(e);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		put(e);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		put(e);
		mouseDragged = true;
	}

	@Override
	public void mouseWheelMoved(MouseEvent e)
	{
		put(e);
	}

	@Override
	public void windowGainedFocus(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowLostFocus(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowResized(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowMoved(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowDestroyNotify(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowDestroyed(WindowEvent e)
	{
		put(e);
	}

	@Override
	public void windowRepaint(WindowUpdateEvent e)
	{
		put(e);
	}

	@Override
	public synchronized void put(NEWTEvent event)
	{
		super.put(event);

		//process the added event on the EDT:
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				forwardEvents();
			}
		};
		if (SwingUtilities.isEventDispatchThread())
		{
			task.run();
		}
		else
		{
			SwingUtilities.invokeLater(task);
		}
	}

	/**
	 * Forward the NEWT events captured by this listener to the AWT handlers.
	 */
	protected void forwardEvents()
	{
		NEWTEvent event;
		while ((event = get()) != null)
		{
			if (event instanceof KeyEvent)
			{
				java.awt.event.KeyEvent awtEvent =
						NewtEventConverter.createKeyEvent((KeyEvent) event, awtSourceComponent);
				switch (event.getEventType())
				{
				case KeyEvent.EVENT_KEY_PRESSED:
					awtKeyHandler.keyPressed(awtEvent);
					break;
				case KeyEvent.EVENT_KEY_RELEASED:
					awtKeyHandler.keyReleased(awtEvent);
					break;
				case KeyEvent.EVENT_KEY_TYPED:
					awtKeyHandler.keyTyped(awtEvent);
					break;
				}
			}
			else if (event instanceof MouseEvent)
			{
				java.awt.event.MouseEvent awtEvent =
						NewtEventConverter.createMouseEvent((MouseEvent) event, awtSourceComponent);
				switch (event.getEventType())
				{
				case MouseEvent.EVENT_MOUSE_CLICKED:
					awtMouseHandler.mouseClicked(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_DRAGGED:
					awtMouseMotionHandler.mouseDragged(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_ENTERED:
					awtMouseHandler.mouseEntered(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_EXITED:
					awtMouseHandler.mouseExited(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_MOVED:
					awtMouseMotionHandler.mouseMoved(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_PRESSED:
					awtMouseHandler.mousePressed(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_RELEASED:
					awtMouseHandler.mouseReleased(awtEvent);
					break;
				case MouseEvent.EVENT_MOUSE_WHEEL_MOVED:
					awtMouseWheelHandler.mouseWheelMoved((java.awt.event.MouseWheelEvent) awtEvent);
					break;

				}
			}
			else if (event instanceof WindowEvent)
			{
				java.awt.event.ComponentEvent awtEvent =
						NewtEventConverter.createComponentEvent((WindowEvent) event, awtSourceComponent);
				switch (event.getEventType())
				{
				case WindowEvent.EVENT_WINDOW_GAINED_FOCUS:
					awtFocusListener.focusGained((java.awt.event.FocusEvent) awtEvent);
					break;
				case WindowEvent.EVENT_WINDOW_LOST_FOCUS:
					awtFocusListener.focusLost((java.awt.event.FocusEvent) awtEvent);
					break;
				}
			}
		}
	}
}
