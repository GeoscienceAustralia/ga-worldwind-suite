/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.panels.layers;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

import au.gov.ga.worldwind.common.ui.ScrollableImage;
import au.gov.ga.worldwind.common.util.Icons;

/**
 * Simple helper class used for displaying legends in a pop-up dialog.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LegendViewer
{
	private static Map<URL, Dialog> dialogs = new HashMap<URL, Dialog>();

	public static void openLegend(final URL url, String title, final Frame frame)
	{
		if (dialogs.containsKey(url))
		{
			Dialog dialog = dialogs.get(url);
			dialog.setVisible(true);
			dialog.toFront();
			return;
		}

		final JDialog dialog = new JDialog(frame, title, false);
		dialog.setIconImage(Icons.legend.getIcon().getImage());
		dialogs.put(url, dialog);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dialogs.remove(url);
				dialog.dispose();
			}
		});

		ImageIcon loadingIcon = Icons.newLoadingIcon();
		loadingIcon.setImageObserver(dialog);

		final ScrollableImage si = new ScrollableImage(loadingIcon, 10);
		JScrollPane scroll = new JScrollPane(si);
		dialog.add(scroll, BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);

		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Image image = ImageIO.read(url);
					if (image == null)
						throw new IOException("Could not read legend image from URL: " + url);

					si.setIcon(new ImageIcon(image));
					dialog.pack();
					dialog.setLocationRelativeTo(frame);
				}
				catch (IOException e)
				{
					si.setIcon(Icons.error.getIcon());
				}
			}
		};

		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.setName("Legend downloader");
		thread.start();
	}
}
