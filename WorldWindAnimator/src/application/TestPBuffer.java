package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLPbuffer;
import javax.swing.JFrame;

import com.sun.opengl.util.Screenshot;

public class TestPBuffer extends JFrame
{
	public static void main(String[] args)
	{
		JFrame frame = new TestPBuffer();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private GLPbuffer pbuffer;

	public TestPBuffer()
	{
		Model model = new BasicModel();
		WorldWindowPBuffer wwd = new WorldWindowPBuffer(model, 640, 480);
		pbuffer = wwd.getPbuffer();
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		GLContext context = pbuffer.createContext(null);
		context.makeCurrent();
		BufferedImage image = Screenshot.readToBufferedImage(640, 480);
		context.release();
		context.destroy();
		g.drawImage(image, 0, 0, null);
	}
}