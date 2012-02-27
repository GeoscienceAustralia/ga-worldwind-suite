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
package au.gov.ga.worldwind.common.ui;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Displays the default NASA World Wind splashscreen, and hides when WorldWindow
 * starts rendering.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SplashScreen
{
	private BufferedImage image;
	private JDialog dialog;

	public SplashScreen()
	{
		this(null);
	}

	public SplashScreen(JFrame owner)
	{
		dialog = new JDialog(owner);
		InputStream is = SplashScreen.class.getResourceAsStream("/images/400x230-splash-nww.png");
		if (is != null)
		{
			try
			{
				image = ImageIO.read(is);
			}
			catch (IOException e)
			{
			}
		}
		if (image != null)
		{
			JLabel label = new JLabel(new ImageIcon(image));
			dialog.add(label, BorderLayout.CENTER);

			dialog.setUndecorated(true);
			dialog.setResizable(false);
			dialog.setSize(image.getWidth(), image.getHeight());
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setLayout(new BorderLayout());
		}
	}

	/**
	 * Hide the splash screen when WorldWindow raises a rendering event.
	 * 
	 * @param wwd
	 *            WorldWindow to listen to
	 */
	public void addRenderingListener(final WorldWindow wwd)
	{
		//hide splash screen when first frame is rendered
		wwd.addRenderingListener(new RenderingListener()
		{
			@Override
			public void stageChanged(RenderingEvent event)
			{
				if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
				{
					dialog.dispose();
					wwd.removeRenderingListener(this);
				}
			}
		});
	}
}
