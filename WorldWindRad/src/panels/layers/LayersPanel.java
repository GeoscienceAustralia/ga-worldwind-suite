package panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class LayersPanel extends JPanel
{
	private StandardPanel standardPanel;
	private WorldWindow wwd;
	private Frame frame;

	public LayersPanel(WorldWindow wwd, Frame frame)
	{
		super(new BorderLayout());
		this.wwd = wwd;
		this.frame = frame;
		add(createTabs());
	}

	private JTabbedPane createTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Standard", createStandard());
		standardPanel.addLowerLayers();
		
		tabbedPane.addTab("Radioelements", createRadiometry());
		tabbedPane.addTab("Other", createOther());
		standardPanel.addUpperLayers();
		
		tabbedPane.doLayout();
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
		RadiometryPanel rp = new RadiometryPanel(wwd, frame);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(rp, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createOther()
	{
		OtherPanel op = new OtherPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(op, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}
	
	public void turnOffAtmosphere()
	{
		standardPanel.turnOffAtmosphere();
	}
}
