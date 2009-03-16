package application;

import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLPbuffer;

public class PBufferDelegate implements GLPbuffer
{
	protected GLPbuffer buffer;

	public PBufferDelegate(GLPbuffer buffer)
	{
		this.buffer = buffer;
	}

	public void bindTexture()
	{
		buffer.bindTexture();
	}

	public void destroy()
	{
		buffer.destroy();
	}

	public int getFloatingPointMode()
	{
		return buffer.getFloatingPointMode();
	}

	public void releaseTexture()
	{
		buffer.releaseTexture();
	}

	public void addGLEventListener(GLEventListener listener)
	{
		buffer.addGLEventListener(listener);
	}

	public void display()
	{
		buffer.display();
	}

	public boolean getAutoSwapBufferMode()
	{
		return buffer.getAutoSwapBufferMode();
	}

	public GLContext getContext()
	{
		return buffer.getContext();
	}

	public GL getGL()
	{
		return buffer.getGL();
	}

	public void removeGLEventListener(GLEventListener listener)
	{
		buffer.removeGLEventListener(listener);
	}

	public void repaint()
	{
		buffer.repaint();
	}

	public void setAutoSwapBufferMode(boolean onOrOff)
	{
		buffer.setAutoSwapBufferMode(onOrOff);
	}

	public void setGL(GL gl)
	{
		buffer.setGL(gl);
	}

	public GLContext createContext(GLContext shareWith)
	{
		return buffer.createContext(shareWith);
	}

	public GLCapabilities getChosenGLCapabilities()
	{
		return buffer.getChosenGLCapabilities();
	}

	public int getHeight()
	{
		return buffer.getHeight();
	}

	public int getWidth()
	{
		return buffer.getWidth();
	}

	public void setRealized(boolean realized)
	{
		buffer.setRealized(realized);
	}

	public void setSize(int width, int height)
	{
		buffer.setSize(width, height);
	}

	public void swapBuffers() throws GLException
	{
		buffer.swapBuffers();
	}

	public void addComponentListener(ComponentListener l)
	{
		buffer.addComponentListener(l);
	}

	public void addFocusListener(FocusListener l)
	{
		buffer.addFocusListener(l);
	}

	public void addHierarchyBoundsListener(HierarchyBoundsListener l)
	{
		buffer.addHierarchyBoundsListener(l);
	}

	public void addHierarchyListener(HierarchyListener l)
	{
		buffer.addHierarchyListener(l);
	}

	public void addInputMethodListener(InputMethodListener l)
	{
		buffer.addInputMethodListener(l);
	}

	public void addKeyListener(KeyListener l)
	{
		buffer.addKeyListener(l);
	}

	public void addMouseListener(MouseListener l)
	{
		buffer.addMouseListener(l);
	}

	public void addMouseMotionListener(MouseMotionListener l)
	{
		buffer.addMouseMotionListener(l);
	}

	public void addMouseWheelListener(MouseWheelListener l)
	{
		buffer.addMouseWheelListener(l);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		buffer.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener)
	{
		buffer.addPropertyChangeListener(propertyName, listener);
	}

	public void removeComponentListener(ComponentListener l)
	{
		buffer.removeComponentListener(l);
	}

	public void removeFocusListener(FocusListener l)
	{
		buffer.removeFocusListener(l);
	}

	public void removeHierarchyBoundsListener(HierarchyBoundsListener l)
	{
		buffer.removeHierarchyBoundsListener(l);
	}

	public void removeHierarchyListener(HierarchyListener l)
	{
		buffer.removeHierarchyListener(l);
	}

	public void removeInputMethodListener(InputMethodListener l)
	{
		buffer.removeInputMethodListener(l);
	}

	public void removeKeyListener(KeyListener l)
	{
		buffer.removeKeyListener(l);
	}

	public void removeMouseListener(MouseListener l)
	{
		buffer.removeMouseListener(l);
	}

	public void removeMouseMotionListener(MouseMotionListener l)
	{
		buffer.removeMouseMotionListener(l);
	}

	public void removeMouseWheelListener(MouseWheelListener l)
	{
		buffer.removeMouseWheelListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		buffer.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener)
	{
		buffer.removePropertyChangeListener(propertyName, listener);
	}
}
