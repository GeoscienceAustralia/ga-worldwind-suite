package panels.places;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PlaceSearchPanel extends JPanel
{
	//private WorldWindow wwd;
	private static PlaceLayer placeLayer = new PlaceLayer();

	public PlaceSearchPanel(WorldWindow wwd)
	{
		super(new GridBagLayout());
		GridBagConstraints c;

		//this.wwd = wwd;
		LayerList layers = wwd.getModel().getLayers();
		if (!layers.contains(placeLayer))
		{
			layers.add(placeLayer);
		}

		final JTextField text = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(text, c);

		JButton button = new JButton("Search");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(button, c);

		final JList list = new JList();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		list.setBorder(BorderFactory.createLoweredBevelBorder());
		add(list, c);

		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(text.getText().length() > 1)
				{
					search(text.getText());
				}
			}
		});
	}
	
	private void search(String text)
	{
		
	}
}
