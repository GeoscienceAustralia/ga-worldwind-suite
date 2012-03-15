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
package au.gov.ga.worldwind.animator.application.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * The recently used files menu item
 * <p/>
 * Gets the list of recently used files from the {@link Settings} class,
 * and renders each item as a separate menu item.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RecentlyUsedFilesMenuList
{
	/** The list of menu items */
	private List<FileMenuItem> menuItems = new ArrayList<FileMenuItem>(Settings.MAX_NUMBER_RECENT_FILES);
	
	/** The placeholder to use when there are no recent files listed */
	private JMenuItem menuPlaceholder;

	/** The animator application this menu is linked to */
	private Animator application;
	
	/**
	 * Constructor.
	 */
	public RecentlyUsedFilesMenuList(Animator animator)
	{
		// Initialise the menu placeholder
		menuPlaceholder = new JMenuItem(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getNoRecentFileMessageKey()));
		menuPlaceholder.setEnabled(false);
		
		this.application = animator;
	}
	
	/**
	 * Add the recently used files list to the provided menu
	 * 
	 * @param menu The menu to add the files list to
	 */
	public void addToMenu(JMenu menu)
	{
		if (menu == null)
		{
			return;
		}
		menu.add(menuPlaceholder);
		
		// Initialise the list of menu items
		menuItems = new ArrayList<FileMenuItem>(Settings.MAX_NUMBER_RECENT_FILES);
		for (int i = 0; i < Settings.MAX_NUMBER_RECENT_FILES; i++)
		{
			final FileMenuItem item = new FileMenuItem();
			item.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					application.open(item.getLinkedFile(), true);
					
				}
			});
			
			item.setMnemonic(0x30 + i); // ASCII codes for 0-9 are 0x30-0x39
			menuItems.add(item);
			menu.add(item);
		}
		
		updateMenuItems();
	}

	/**
	 * Update the menu items in the list
	 */
	public void updateMenuItems()
	{
		List<File> recentlyUsedFiles = Settings.get().getRecentFiles();
		for (int i = 0; i < Settings.MAX_NUMBER_RECENT_FILES; i++)
		{
			if (i < recentlyUsedFiles.size())
			{
				initialiseMenuItem(i, menuItems.get(i), recentlyUsedFiles.get(i));
			}
			else
			{
				menuItems.get(i).setVisible(false);
				menuItems.get(i).setEnabled(false);
			}
		}
		
		menuPlaceholder.setVisible(recentlyUsedFiles.isEmpty());
	}
	
	/**
	 * Initialise the provided menu item
	 */
	private void initialiseMenuItem(int i, FileMenuItem menuItem, File file)
	{
		String menuText = i+1 + ". " + file.getName();
		menuItem.setText(menuText);
		menuItem.setLinkedFile(file);
		menuItem.setEnabled(true);
		menuItem.setVisible(true);
	}
	
	/**
	 * Simple extension of {@link JMenuItem} that contains a linked file
	 */
	private static class FileMenuItem extends JMenuItem
	{
		private static final long serialVersionUID = 20100906L;

		/** The file this menu item is linked with */
		private File linkedFile;

		/**
		 * @return the linkedFile
		 */
		public File getLinkedFile()
		{
			return linkedFile;
		}

		/**
		 * @param linkedFile the linkedFile to set
		 */
		public void setLinkedFile(File linkedFile)
		{
			this.linkedFile = linkedFile;
		}
		
		
	}
	
}
