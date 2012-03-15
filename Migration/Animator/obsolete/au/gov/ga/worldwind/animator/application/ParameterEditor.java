package au.gov.ga.worldwind.animator.application;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.math.vector.Vector2;

import au.gov.ga.worldwind.animator.animation.OldParameter;


public class ParameterEditor extends JComponent
{
	private OldParameter parameter;
	private KeyFramePoint[] points;

	public ParameterEditor(final OldParameter parameter)
	{
		this.parameter = parameter;
		points = calculatePoints(parameter);

		parameter.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				Thread thread = new Thread()
				{
					@Override
					public void run()
					{
						points = calculatePoints(parameter);
						repaint();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{

			}
		});
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(rh);

		drawParameter(parameter, g2, new Color(0, 128, 0));
	}

	private void drawParameter(OldParameter parameter, Graphics2D g2, Color color)
	{
		int ff = parameter.getFirstFrame();
		int lf = parameter.getLastFrame() + 1;
		double yMin = parameter.getMinimumValue();
		double yMax = parameter.getMaximumValue();
		int frameJump = Math.max(1, (lf - ff + 1) / getWidth());

		g2.setColor(color);
		double start = parameter.getInterpolatedValue(ff);
		for (int frame = ff + 1; frame <= lf; frame += frameJump)
		{
			double end = parameter.getInterpolatedValue(frame);

			double x1 = getX(frame - frameJump, ff, lf);
			double x2 = getX(frame, ff, lf);
			double y1 = getY(start, yMin, yMax);
			double y2 = getY(end, yMin, yMax);

			g2.draw(new Line2D.Double(x1, y1, x2, y2));

			start = end;
		}

		g2.setColor(color.brighter().brighter());
		for (KeyFramePoint point : points)
		{
			if (point.in != null)
				g2.draw(new Line2D.Double(getX(point.point.x, 0, 1), getY(
						point.point.y, 0, 1), getX(point.in.x, 0, 1), getY(
						point.in.y, 0, 1)));
			if (point.out != null)
				g2.draw(new Line2D.Double(getX(point.point.x, 0, 1), getY(
						point.point.y, 0, 1), getX(point.out.x, 0, 1), getY(
						point.out.y, 0, 1)));
		}

		g2.setColor(color.darker().darker());
		for (KeyFramePoint point : points)
		{
			if (point.in != null)
				g2.fill(new Ellipse2D.Double(getX(point.in.x, 0, 1) - 2, getY(
						point.in.y, 0, 1) - 2, 4, 4));
			if (point.out != null)
				g2.fill(new Ellipse2D.Double(getX(point.out.x, 0, 1) - 2, getY(
						point.out.y, 0, 1) - 2, 4, 4));
		}

		//g2.setColor(Color.blue);
		for (KeyFramePoint point : points)
		{
			g2.fill(new Ellipse2D.Double(getX(point.point.x, 0, 1) - 2, getY(
					point.point.y, 0, 1) - 2, 4, 4));
		}
	}

	private double getX(double frame, int firstFrame, int lastFrame)
	{
		double w = getWidth() - 20;
		double window = lastFrame - firstFrame;
		return w * (frame - firstFrame) / window + 10;
	}

	private double getY(double value, double min, double max)
	{
		double h = getHeight() - 20;
		double window = max - min;
		return h - h * (value - min) / window + 10;
	}

	private KeyFramePoint[] calculatePoints(OldParameter parameter)
	{
		int firstFrame = parameter.getFirstFrame();
		int lastFrame = parameter.getLastFrame();
		double yMinDraw = parameter.getMinimumValue();
		double yMaxDraw = parameter.getMaximumValue();
		double frameWindow = lastFrame - firstFrame + 1;
		double yWindow = yMaxDraw - yMinDraw;

		KeyFramePoint[] points = new KeyFramePoint[parameter.size()];
		for (int i = 0; i < parameter.size(); i++)
		{
			KeyFramePoint kfp = new KeyFramePoint();
			points[i] = kfp;

			int frame = parameter.getFrame(i);
			kfp.point = new Point2D.Double((frame - firstFrame) / frameWindow,
					(parameter.getInterpolatedValue(frame) - yMinDraw)
							/ yWindow);

			Vector2 in = parameter.getIn(i);
			if (in != null)
				kfp.in = new Point2D.Double((in.x - firstFrame) / frameWindow,
						(in.y - yMinDraw) / yWindow);

			Vector2 out = parameter.getOut(i);
			if (out != null)
				kfp.out = new Point2D.Double(
						(out.x - firstFrame) / frameWindow, (out.y - yMinDraw)
								/ yWindow);
		}
		return points;
	}

	public static void main(String[] args)
	{
		OldParameter parameter = new OldParameter();
		parameter.addKey(0, -100);
		parameter.addKey(100, 200);
		parameter.addKey(2000, 300);
		parameter.addKey(2500, -50);

		parameter.smooth(0);
		parameter.smooth(1);
		parameter.smooth(2);
		parameter.smooth(3);

		JFrame frame = new JFrame("Bezier");
		frame.setSize(640, 480);
		frame.add(new ParameterEditor(parameter));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static class KeyFramePoint
	{
		public Point2D.Double point, in, out;
	}
}
