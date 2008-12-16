package bookmarks;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import util.FlatJButton;
import util.Icons;

public class BookmarkManager extends JDialog
{
	private JList list;
	private DefaultListModel model;
	private Bookmark draggingBookmark;
	private FlatJButton rename, delete, up, down;

	public BookmarkManager(Frame frame, String title)
	{
		super(frame, title, true);

		GridBagConstraints c;
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(createContents(), c);

		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(separator, c);

		JButton okButton = new JButton("OK");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.EAST;
		add(okButton, c);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}

	private JPanel createContents()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		model = new DefaultListModel();
		list = new JList(model);
		list.setBorder(BorderFactory.createLoweredBevelBorder());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 4;
		panel.add(list, c);

		rename = new FlatJButton(Icons.edit);
		rename.restrictSize();
		rename.setToolTipText("Rename");
		final ActionListener renameAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Bookmark bookmark = (Bookmark) list.getSelectedValue();
				renameBookmark(bookmark, BookmarkManager.this);
			}
		};
		rename.addActionListener(renameAL);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		panel.add(rename, c);

		delete = new FlatJButton(Icons.delete);
		delete.setToolTipText("Delete");
		delete.restrictSize();
		final ActionListener deleteAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Bookmark bookmark = (Bookmark) list.getSelectedValue();
				deleteBookmark(bookmark, BookmarkManager.this);
			}
		};
		delete.addActionListener(deleteAL);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		panel.add(delete, c);

		up = new FlatJButton(Icons.up);
		up.setToolTipText("Move up");
		up.restrictSize();
		up.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Bookmark bookmark = (Bookmark) list.getSelectedValue();
				if (bookmark != null)
				{
					Bookmarks.moveUp(bookmark);
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		panel.add(up, c);

		down = new FlatJButton(Icons.down);
		down.setToolTipText("Move down");
		down.restrictSize();
		down.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Bookmark bookmark = (Bookmark) list.getSelectedValue();
				if (bookmark != null)
				{
					Bookmarks.moveDown(bookmark);
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTH;
		panel.add(down, c);

		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem rename = new JMenuItem("Rename");
		popupMenu.add(rename);
		rename.addActionListener(renameAL);
		JMenuItem delete = new JMenuItem("Delete");
		popupMenu.add(delete);
		delete.addActionListener(deleteAL);

		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					int index = list.locationToIndex(e.getPoint());
					Rectangle rect = list.getCellBounds(index, index);
					if (rect.contains(e.getPoint()))
					{
						list.setSelectedIndex(index);
						popupMenu.show(list, e.getX(), e.getY());
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 2)
				{
					renameAL.actionPerformed(null);
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				int index = list.locationToIndex(e.getPoint());
				draggingBookmark = (Bookmark) model.elementAt(index);
				setEnabledButtons();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				draggingBookmark = null;
			}
		});

		list.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (draggingBookmark != null)
				{
					int index = list.locationToIndex(e.getPoint());
					Bookmarks.moveTo(draggingBookmark, index);
				}
			}
		});

		BookmarkListener bl = new BookmarkListener()
		{
			public void modified()
			{
				Bookmark selected = (Bookmark) list.getSelectedValue();
				model.clear();
				for (Bookmark bookmark : Bookmarks.iterable())
				{
					model.addElement(bookmark);
				}
				if (selected != null)
				{
					list.setSelectedValue(selected, true);
				}
				setEnabledButtons();
			}
		};
		Bookmarks.addBookmarkListener(bl);
		bl.modified();

		setEnabledButtons();

		return panel;
	}

	private void setEnabledButtons()
	{
		int index = list.getSelectedIndex();
		rename.setEnabled(index >= 0);
		delete.setEnabled(index >= 0);
		up.setEnabled(index > 0);
		down.setEnabled(index >= 0 && index < model.getSize() - 1);
	}

	public static void addBookmark(Component parent, WorldWindow wwd)
	{
		String name = JOptionPane.showInputDialog(parent,
				"Please enter a name", "Add bookmark",
				JOptionPane.QUESTION_MESSAGE);
		if (name != null && name.length() > 0)
		{
			View view = wwd.getView();
			if (view instanceof OrbitView)
			{
				OrbitView orbitView = (OrbitView) view;
				Bookmark bookmark = new Bookmark(name, orbitView
						.getCenterPosition(), orbitView.getHeading(), orbitView
						.getPitch(), orbitView.getZoom());
				Bookmarks.add(bookmark);
			}
		}
	}

	public static void renameBookmark(Bookmark bookmark, Component parent)
	{
		if (bookmark != null)
		{
			Object name = JOptionPane.showInputDialog(parent,
					"Enter the new name", "Rename bookmark",
					JOptionPane.QUESTION_MESSAGE, null, null, bookmark.name);
			if (name instanceof String)
			{
				Bookmarks.rename(bookmark, (String) name);
			}
		}
	}

	public static void deleteBookmark(Bookmark bookmark, Component parent)
	{
		if (bookmark != null)
		{
			int value = JOptionPane.showConfirmDialog(parent,
					"Are you sure you want to delete the bookmark '"
							+ bookmark.name + "'?", "Delete bookmark",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (value == JOptionPane.YES_OPTION)
			{
				Bookmarks.delete(bookmark);
			}
		}
	}
}
