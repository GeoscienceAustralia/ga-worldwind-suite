package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;

import util.Icons;

public class DockablePanel extends AbstractDockable
{
	private JPanel mainPanel;
	private GradientPanel draggablePanel;
	private JPanel headerPanel;
	private JLabel titleLabel;
	private JToolBar toolbar;

	static
	{
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addPropertyChangeListener(new PropertyChangeListener()
				{
					private DockablePanel currentPanel;

					public void propertyChange(PropertyChangeEvent evt)
					{
						if ("focusOwner".equals(evt.getPropertyName()))
						{
							Component newComp = (Component) evt.getNewValue();
							if (newComp != null)
							{
								DockablePanel newPanel = getDockablePanelContaining(newComp);
								if (newPanel != null
										&& newPanel != currentPanel)
								{
									if (currentPanel != null)
									{
										currentPanel.focusLost();
									}
									newPanel.focusGained();
									currentPanel = newPanel;
								}
							}
						}
					}
				});
	}

	@SuppressWarnings("unchecked")
	public DockablePanel(String id, String title, Icon icon,
			Component component, boolean closeable, boolean maximizable)
	{
		super(id);

		titleLabel = new JLabel(title, icon, JLabel.LEADING);
		titleLabel.setForeground(SystemColor.activeCaptionText);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.setBorderPainted(false);
		toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

		if (maximizable)
		{
			final JButton button = new JButton(Icons.maximize);
			button.setFocusable(false);
			button.setBackground(toolbar.getBackground());
			toolbar.add(button);
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DockingPort dockingPort = DockingManager
							.getRootDockingPort(getComponent());
					if (DockingManager.isMaximized(DockablePanel.this)
							|| (dockingPort != null && dockingPort
									.getDockables().size() > 1))
					{
						DockingManager.toggleMaximized(DockablePanel.this);
					}

					if (DockingManager.isMaximized(DockablePanel.this))
					{
						button.setIcon(Icons.restore);
					}
					else
					{
						button.setIcon(Icons.maximize);
					}
				}
			});
		}
		if (closeable)
		{
			JButton button = new JButton(Icons.close);
			button.setFocusable(false);
			button.setBackground(toolbar.getBackground());
			toolbar.add(button);
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DockingManager.close(DockablePanel.this);
				}
			});
		}

		draggablePanel = new GradientPanel(new BorderLayout(),
				SystemColor.activeCaption, toolbar.getBackground());
		draggablePanel.add(titleLabel, BorderLayout.WEST);

		headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(draggablePanel, BorderLayout.CENTER);
		headerPanel.add(toolbar, BorderLayout.EAST);
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new RaisedHeaderBorder());
		//headerPanel.setBorder(new ShadowBorder());

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(headerPanel, BorderLayout.PAGE_START);
		mainPanel.add(component, BorderLayout.CENTER);
		mainPanel.setBorder(new ShadowBorder());

		setTabText(title);
		getDragSources().add(draggablePanel);
		getFrameDragSources().add(draggablePanel);
		DockingManager.registerDockable(this);
		
		focusLost();
	}
	
	public String getTitle()
	{
		return getTabText();
	}

	@Override
	public Component getComponent()
	{
		return mainPanel;
	}

	private void focusGained()
	{
		titleLabel.setForeground(SystemColor.activeCaptionText);
		draggablePanel.setStart(SystemColor.activeCaption);
	}

	private void focusLost()
	{
		titleLabel.setForeground(SystemColor.textInactiveText);
		draggablePanel.setStart(SystemColor.control);
	}

	private static DockablePanel getDockablePanelContaining(Component component)
	{
		if (component == null)
			return null;

		Dockable dockable = null;
		while (component != null)
		{
			dockable = DockingManager.getDockable(component);
			if (dockable != null && dockable instanceof DockablePanel)
			{
				break;
			}
			component = component.getParent();
		}
		if (dockable != null)
		{
			return (DockablePanel) dockable;
		}
		return null;
	}

	private static class GradientPanel extends JPanel
	{
		public GradientPanel(LayoutManager lm, Color start, Color end)
		{
			super(lm);
			setForeground(start);
			setBackground(end);
		}
		
		public void setStart(Color start)
		{
			setForeground(start);
		}
		
		public void setEnd(Color end)
		{
			setBackground(end);
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (!isOpaque())
			{
				return;
			}
			int width = getWidth();
			int height = getHeight();

			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(new GradientPaint(0, 0, getForeground(), width, 0,
					getBackground()));
			g2.fillRect(0, 0, width, height);
		}
	}

	private static class RaisedHeaderBorder extends AbstractBorder
	{
		private static final Insets INSETS = new Insets(1, 1, 1, 0);

		public Insets getBorderInsets(Component c)
		{
			return INSETS;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int w,
				int h)
		{
			g.translate(x, y);
			g.setColor(UIManager.getColor("controlLtHighlight"));
			g.fillRect(0, 0, w, 1);
			g.fillRect(0, 1, 1, h - 1);
			g.setColor(UIManager.getColor("controlShadow"));
			g.fillRect(0, h - 1, w, 1);
			g.translate(-x, -y);
		}
	}

	private static class ShadowBorder extends AbstractBorder
	{
		private static final Insets INSETS = new Insets(1, 1, 3, 3);

		public Insets getBorderInsets(Component c)
		{
			return INSETS;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int w,
				int h)
		{
			Color shadow = UIManager.getColor("controlShadow");
			if (shadow == null)
			{
				shadow = Color.GRAY;
			}
			Color lightShadow = new Color(shadow.getRed(), shadow.getGreen(),
					shadow.getBlue(), 170);
			Color lighterShadow = new Color(shadow.getRed(), shadow.getGreen(),
					shadow.getBlue(), 70);
			g.translate(x, y);

			g.setColor(shadow);
			g.fillRect(0, 0, w - 3, 1);
			g.fillRect(0, 0, 1, h - 3);
			g.fillRect(w - 3, 1, 1, h - 3);
			g.fillRect(1, h - 3, w - 3, 1);
			// Shadow line 1
			g.setColor(lightShadow);
			g.fillRect(w - 3, 0, 1, 1);
			g.fillRect(0, h - 3, 1, 1);
			g.fillRect(w - 2, 1, 1, h - 3);
			g.fillRect(1, h - 2, w - 3, 1);
			// Shadow line2
			g.setColor(lighterShadow);
			g.fillRect(w - 2, 0, 1, 1);
			g.fillRect(0, h - 2, 1, 1);
			g.fillRect(w - 2, h - 2, 1, 1);
			g.fillRect(w - 1, 1, 1, h - 2);
			g.fillRect(1, h - 1, w - 2, 1);
			g.translate(-x, -y);
		}
	}
}
