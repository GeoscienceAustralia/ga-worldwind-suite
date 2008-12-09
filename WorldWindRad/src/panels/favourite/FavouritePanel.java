package panels.favourite;

import gov.nasa.worldwind.WorldWindow;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import util.FlatJButton;
import util.Icons;

public class FavouritePanel extends JPanel
{
	private WorldWindow wwd;
	private ListModel listModel;
	private JList list;
	private FlatJButton edit;
	private FlatJButton delete;

	public FavouritePanel(final WorldWindow wwd)
	{
		super(new GridBagLayout());
		GridBagConstraints c;

		this.wwd = wwd;

		listModel = new DefaultListModel();
		list = new JList(listModel);
		//list.setCellRenderer(new CustomRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane, c);

		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				selectionChanged();
				if (e.getClickCount() == 2)
				{
					flyToSelection();
				}
			}
		});

		JPanel panel = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		FlatJButton add = new FlatJButton(Icons.add);
		panel.add(add);
		add.setToolTipText("Add");

		edit = new FlatJButton(Icons.edit);
		panel.add(edit);
		edit.setToolTipText("Edit selected");

		delete = new FlatJButton(Icons.delete);
		panel.add(delete);
		delete.setToolTipText("Delete selected");
		
		selectionChanged();
	}

	private void flyToSelection()
	{

	}
	
	private void selectionChanged()
	{
		boolean enabled = list.getSelectedIndex() >= 0;
		edit.setEnabled(enabled);
		delete.setEnabled(enabled);
	}
}
