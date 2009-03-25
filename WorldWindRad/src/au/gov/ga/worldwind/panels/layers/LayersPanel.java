package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import au.gov.ga.worldwind.layers.user.UserLayerPanel;
import au.gov.ga.worldwind.layers.user.UserLayers;

public class LayersPanel extends JPanel
{
	private JTabbedPane tabbedPane;
	private StandardPanel standardPanel;
	private WorldWindow wwd;
	private Frame frame;
	private UserLayerPanel userPanel;
	private boolean containsUserPanel = false;

	public LayersPanel(WorldWindow wwd, Frame frame)
	{
		super(new BorderLayout());
		this.wwd = wwd;
		this.frame = frame;
		add(createTabs());
	}

	private JTabbedPane createTabs()
	{
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Standard", createStandard());
		standardPanel.addLowerLayers();

		tabbedPane.addTab("Radiometrics", createRadiometry());
		tabbedPane.addTab("Geophysics", createGeophysics());
		standardPanel.addUpperLayers();

		userPanel = new UserLayerPanel(wwd, frame);

		tabbedPane.validate();
		tabbedPane.setSelectedIndex(1);
		return tabbedPane;
	}

	private JComponent createStandard()
	{
		standardPanel = new StandardPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(standardPanel, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createRadiometry()
	{
		RadiometricsPanel rp = new RadiometricsPanel(wwd, frame);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(rp, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		Dimension size = rp.getPreferredSize();
		size.width += 10; //include for border
		scrollPane.setPreferredSize(size);
		return scrollPane;
	}

	private JComponent createGeophysics()
	{
		GeophysicsPanel op = new GeophysicsPanel(wwd, frame);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(op, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	public void updateUserLayers()
	{
		userPanel.updateLayers();
		if (containsUserPanel ^ !UserLayers.isEmpty()) //i love XOR!
		{
			if (containsUserPanel)
			{
				tabbedPane.removeTabAt(3);
				containsUserPanel = false;
			}
			else
			{
				tabbedPane.addTab("User layers", userPanel);
				containsUserPanel = true;
			}
		}
	}

	public void turnOffAtmosphere()
	{
		standardPanel.turnOffAtmosphere();
	}
}
