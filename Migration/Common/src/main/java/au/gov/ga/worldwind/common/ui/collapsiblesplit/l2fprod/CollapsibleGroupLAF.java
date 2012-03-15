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
package au.gov.ga.worldwind.common.ui.collapsiblesplit.l2fprod;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class CollapsibleGroupLAF
{
	public static void init()
	{
		Font taskPaneFont = UIManager.getFont("Label.font");
		if (taskPaneFont == null)
		{
			taskPaneFont = new Font("Dialog", Font.PLAIN, 12);
		}
		taskPaneFont = taskPaneFont.deriveFont(Font.BOLD);
		//Color menuBackground = new ColorUIResource(SystemColor.menu);

		UIManager.put(CollapsibleGroup.UI_CLASS_ID, GlossyCollapsibleGroupUI.class
				.getCanonicalName());
		UIManager.put("TaskPaneGroup.font", new FontUIResource(taskPaneFont));
		/*UIManager.put("TaskPaneGroup.background", UIManager
				.getColor("List.background"));
		UIManager.put("TaskPaneGroup.specialTitleBackground",
				new ColorUIResource(menuBackground.darker()));
		UIManager.put("TaskPaneGroup.titleBackgroundGradientStart",
				menuBackground);
		UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd",
				menuBackground);
		UIManager.put("TaskPaneGroup.titleForeground", new ColorUIResource(
				SystemColor.menuText));
		UIManager.put("TaskPaneGroup.specialTitleForeground",
				new ColorUIResource(SystemColor.menuText).brighter());
		UIManager.put("TaskPaneGroup.animate", Boolean.TRUE);
		UIManager.put("TaskPaneGroup.focusInputMap",
				new UIDefaults.LazyInputMap(new Object[]
				{ "ENTER", "toggleExpanded", "SPACE", "toggleExpanded" }));*/

		UIManager.put("TaskPaneGroup.background", new ColorUIResource(245, 245, 245));
		UIManager.put("TaskPaneGroup.titleForeground", new ColorUIResource(Color.black));
		UIManager.put("TaskPaneGroup.specialTitleBackground", new ColorUIResource(188, 188, 188));
		UIManager.put("TaskPaneGroup.specialTitleForeground", new ColorUIResource(Color.black));
		UIManager.put("TaskPaneGroup.titleBackgroundGradientStart", new ColorUIResource(250, 250,
				250));
		UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd",
				new ColorUIResource(188, 188, 188));
		UIManager.put("TaskPaneGroup.borderColor", new ColorUIResource(97, 97, 97));
		UIManager.put("TaskPaneGroup.titleOver", new ColorUIResource(125, 125, 97));
		UIManager.put("TaskPaneGroup.specialTitleOver", new ColorUIResource(125, 125, 97));
	}
}
