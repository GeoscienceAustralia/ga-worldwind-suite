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
package au.gov.ga.worldwind.viewer.theme;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.WorldWindow;

import java.net.URL;
import java.util.List;

import javax.swing.JFrame;

import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.layers.ThemeLayersPanel;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

public interface Theme extends Disposable
{
	/**
	 * Setup this theme. This should call setup() on each of this theme's panels
	 * and huds.
	 * 
	 * @param frame
	 * @param wwd
	 */
	void setup(JFrame frame, WorldWindow wwd);

	/**
	 * @return The {@link WorldWindow} currently associated with this theme
	 */
	WorldWindow getWwd();

	/**
	 * @return The main {@link JFrame} for this application
	 */
	JFrame getFrame();

	/**
	 * @return The name/label of this theme
	 */
	String getName();

	/**
	 * @return Should the menu bar be displayed?
	 */
	boolean hasMenuBar();

	/**
	 * @return Should the tool bar be displayed?
	 */
	boolean hasToolBar();

	/**
	 * @return Should the status bar be displayed?
	 */
	boolean hasStatusBar();

	/**
	 * @return Should the WMS browser be enabled?
	 */
	boolean hasWms();

	/**
	 * @return Should the added layers be persisted to the layers persistance
	 *         file on exit? If false, the default layers are added for this
	 *         theme each time the application is started.
	 */
	boolean isPersistLayers();

	/**
	 * @return The filename to which to persist layers to (if relative, file is
	 *         saved in the user directory, see
	 *         {@link SettingsUtil#getUserDirectory()}.
	 */
	String getLayerPersistanceFilename();

	/**
	 * @return List of extra WorldWindData cache locations
	 */
	List<String> getCacheLocations();

	/**
	 * @return List of {@link IDataset}s associated with this theme
	 */
	List<IDataset> getDatasets();

	/**
	 * @return List of layers hard-coded in this theme. If the
	 *         {@link ThemeLayersPanel} is not used, these layers are added and
	 *         enabled invisibly.
	 */
	List<ThemeLayer> getLayers();

	/**
	 * @return List of {@link ThemeHUD}s associated with this theme
	 */
	List<ThemeHUD> getHUDs();

	/**
	 * @return List of {@link ThemePanel}s to make available when using this
	 *         theme
	 */
	List<ThemePanel> getPanels();

	/**
	 * @return True if a {@link LayersPanel} exists in this theme's panels list
	 */
	boolean hasLayersPanel();

	/**
	 * @return True if a {@link ThemeLayersPanel} exists in this theme's panels
	 *         list
	 */
	boolean hasThemeLayersPanel();

	/**
	 * @return The {@link LayersPanel} in this theme, if any
	 */
	LayersPanel getLayersPanel();

	/**
	 * @return Initial view center latitude to use when starting the application
	 */
	Double getInitialLatitude();

	/**
	 * @return Initial view center longitude to use when starting the
	 *         application
	 */
	Double getInitialLongitude();

	/**
	 * @return Initial camera altitude to use when starting the application
	 */
	Double getInitialAltitude();

	/**
	 * @return Initial camera heading to use when starting the application
	 */
	Double getInitialHeading();

	/**
	 * @return Initial camera pitch to use when starting the application
	 */
	Double getInitialPitch();

	/**
	 * @return Initial vertical exaggeration to use when starting the
	 *         application. If this is set, the stored Settings vertical
	 *         exaggeration is ignored.
	 */
	Double getVerticalExaggeration();

	/**
	 * @return Initial field of view to use when starting the application. If
	 *         this is set, the stored Settings field of view is ignored.
	 */
	Double getFieldOfView();

	/**
	 * @return Should the places be persisted?
	 */
	boolean isPersistPlaces();

	/**
	 * @return Is the places persistance filename set?
	 */
	boolean isPlacesPersistanceFilenameSet();

	/**
	 * @return The name to use when saving/initialising places files
	 */
	String getPlacesPersistanceFilename();

	/**
	 * @return The path a places file to use when initialising the places list
	 *         on theme load
	 */
	URL getPlacesInitialisationPath();
}
