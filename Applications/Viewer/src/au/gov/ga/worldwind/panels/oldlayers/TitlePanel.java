package au.gov.ga.worldwind.panels.oldlayers;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TitlePanel extends JPanel
{
	public TitlePanel(String[] title, String[] subtitle, int top, int bottom)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		if(top > 0)
		{
			add(Box.createRigidArea(new Dimension(0, top)));
		}

		for (String t : title)
		{
			JLabel label = new JLabel(t);
			label.setFont(Font.decode("Dialog-BOLD-12"));
			label.setAlignmentX(CENTER_ALIGNMENT);
			add(label);
		}
		for (String t : subtitle)
		{
			JLabel label = new JLabel(t);
			label.setFont(Font.decode("Dialog-BOLD-11"));
			label.setAlignmentX(CENTER_ALIGNMENT);
			add(label);
		}
		
		if(bottom > 0)
		{
			add(Box.createRigidArea(new Dimension(0, bottom)));
		}
	}
}
