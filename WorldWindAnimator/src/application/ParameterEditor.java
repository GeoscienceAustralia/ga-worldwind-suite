package application;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import path.Parameter;
import camera.vector.Vector2;

public class ParameterEditor extends JComponent
{
	private List<Parameter> parameters = new ArrayList<Parameter>();

	private Parameter parameter;
	private KeyFramePoint[] points;

	public ParameterEditor()
	{
		parameter = new Parameter();
		parameter.addKey(0, 100);
		parameter.addKey(1000, 200);
		parameter.addKey(2000, 50);
		parameter.setInPercent(1, 0.1, 150);
		points = calculatePoints(parameter);

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

	private void drawParameter(Parameter parameter, Graphics2D g2, Color color)
	{
		int ff = parameter.getFirstFrame();
		int lf = parameter.getLastFrame();
		double yMin = parameter.getMinimumValue();
		double yMax = parameter.getMaximumValue();
		int frameJump = Math.max(1, (lf - ff + 1) / getWidth());

		g2.setColor(color);
		double start = parameter.getValue(ff);
		for (int frame = ff + 1; frame <= lf; frame += frameJump)
		{
			double end = parameter.getValue(frame);

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
				g2.draw(new Line2D.Double(point.point, point.in));
			if (point.out != null)
				g2.draw(new Line2D.Double(point.point, point.out));
		}

		g2.setColor(color.darker().darker());
		for (KeyFramePoint point : points)
		{
			if (point.in != null)
				g2.fill(new Ellipse2D.Double(point.in.x - 2, point.in.y - 2, 4,
						4));
			if (point.out != null)
				g2.fill(new Ellipse2D.Double(point.out.x - 2, point.out.y - 2,
						4, 4));
		}

		//g2.setColor(Color.blue);
		for (KeyFramePoint point : points)
		{
			g2.fill(new Ellipse2D.Double(point.point.x - 2, point.point.y - 2,
					4, 4));
		}
	}

	private double getX(double frame, int firstFrame, int lastFrame)
	{
		double w = getWidth() - 20;
		double window = lastFrame - firstFrame + 1;
		return w * (frame - firstFrame) / window + 10;
	}

	private double getY(double value, double min, double max)
	{
		double h = getHeight() - 20;
		double window = max - min;
		return h - h * (value - min) / window + 10;
	}

	private KeyFramePoint[] calculatePoints(Parameter parameter)
	{
		int firstFrame = parameter.getFirstFrame();
		int lastFrame = parameter.getLastFrame();
		double yMinDraw = parameter.getMinimumValue();
		double yMaxDraw = parameter.getMaximumValue();
		double frameWindow = lastFrame - firstFrame + 1;
		double yWindow = yMaxDraw - yMinDraw;

		points = new KeyFramePoint[parameter.size()];
		for (int i = 0; i < parameter.size(); i++)
		{
			KeyFramePoint kfp = new KeyFramePoint();
			points[i] = kfp;

			int frame = parameter.getFrame(i);
			kfp.point = new Point2D.Double((frame - firstFrame) / frameWindow,
					(parameter.getValue(frame) - yMinDraw) / yWindow);

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
		JFrame frame = new JFrame("Bezier");
		frame.setSize(640, 480);
		frame.add(new ParameterEditor());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static class KeyFramePoint
	{
		public Point2D.Double point, in, out;
	}
}
