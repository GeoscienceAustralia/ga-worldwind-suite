package au.gov.ga.worldwind.viewer.components;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

public class ImageDialog extends JDialog
{
	public ImageDialog(Frame frame, String title, boolean modal, final Image image, ImageIcon icon)
	{
		super(frame, title, modal);
		setIconImage(icon.getImage());
		setLayout(new BorderLayout());
		ScrollableImage si = new ScrollableImage(new ImageIcon(image), 10);
		JScrollPane scroll = new JScrollPane(si);
		add(scroll, BorderLayout.CENTER);
		pack();
	}
}
