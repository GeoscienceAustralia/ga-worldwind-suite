package au.gov.ga.worldwind.viewer.panels.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.view.FreeView;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;

public class ViewPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	private JRadioButton orbitRadio;
	private JRadioButton flyRadio;
	private JRadioButton freeRadio;

	public ViewPanel()
	{
		super(new GridBagLayout());
		GridBagConstraints c;
		JPanel panel;

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		ButtonGroup bg = new ButtonGroup();
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setupView();
			}
		};

		orbitRadio = new JRadioButton("Orbit", true);
		bg.add(orbitRadio);
		orbitRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1d / 3d;
		panel.add(orbitRadio, c);

		freeRadio = new JRadioButton("Free");
		bg.add(freeRadio);
		freeRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.weightx = 1d / 3d;
		panel.add(freeRadio, c);

		flyRadio = new JRadioButton("Fly");
		bg.add(flyRadio);
		flyRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1d / 3d;
		panel.add(flyRadio, c);
	}

	@Override
	public Icon getIcon()
	{
		return Icons.view.getIcon();
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		wwd.setView(new ViewStateBasicOrbitView());

		wwd.addRenderingListener(new RenderingListener()
		{
			@Override
			public void stageChanged(RenderingEvent event)
			{
				System.out.println(wwd.getView().getEyePoint() + ", "
						+ wwd.getView().getCenterPoint() + ", " + wwd.getView().getHeading() + ", "
						+ wwd.getView().getPitch());
			}
		});
	}

	@Override
	public void dispose()
	{
	}

	protected void setupView()
	{
		if (wwd == null)
			return;

		View oldView = wwd.getView();
		View view = null;

		if (orbitRadio.isSelected() && !(oldView instanceof ViewStateBasicOrbitView))
		{
			view = new ViewStateBasicOrbitView();
		}
		else if (flyRadio.isSelected() && !(oldView instanceof ViewStateBasicFlyView))
		{
			view = new ViewStateBasicFlyView();
		}
		else if (freeRadio.isSelected() && !(oldView instanceof FreeView))
		{
			view = new FreeView();
		}

		if (view == null)
			return;

		view.copyViewState(oldView);
		wwd.setView(view);
		wwd.redraw();
	}
}
