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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

/**
 * {@link JDialog} used for displaying an image. The image is scrollable if
 * larger than the dialog.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
