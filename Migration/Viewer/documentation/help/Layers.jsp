<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>3D Data Viewer - Help - Layers</title>
		
		<link href="style.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
		<a name="top"/>
		<h2>Layers</h2>
		<p>
			Layers are the discrete unit of data in the 3D Data Viewer. Each layer represents some specific piece of data, 
			be it satellite imagery or country boundaries, a localised gravity survey or the names of capital cities. 
		</p>
		<p>
			The 3D Data Viewer supports having multiple layers visible on the globe at any given time, which opens up 
			the ability to create rich visualisations of a vast array of spatial and geoscience data. Geophysical data can
			be easily viewed at the same time as topographic shaded relief data and administrative boundaries, for example. 
		</p>
		<p>
			Layers are managed through the <span class="UIElementReference">Layers panel</span>, located on the left side of 
			the screen. Layers are organised into a tree structure, in much the same way as a file browser on your computer,
			with layers being leaf nodes, grouped into folders. Many operations that can be applied to a single layer can also
			be applied to a group of layers, making working with multiple layers easier.
		</p>
		<hr/>
		<ul>
			<li><a href="#onOff">Turning layers on/off</a></li>
			<li><a href="#expandCollapse">Expanding and Collapsing Collections</a></li>
			<li><a href="#addFromDatasets">Add Layers from Datasets to the Layers panel</a></li>
			<li><a href="#multipleLayers">Working with Multiple Layers</a></li>
			<li><a href="#removeLayers">Remove Layers from the Layers panel</a></li>
			<li><a href="#layerOpacity">Layer opacity</a></li>
		</ul>
		<hr/>
		
		<a name="onOff"/><h3>Turning layers on/off</h3>
		<p>
			Click on the <img src="images/box_checked.png" alt="Check Box" width="13" height="13" /> check box next to a 
			layer to turn the layer on or off. Some layers such as the <strong>Landsat</strong> layer are only visible when 
			you get closer to the globe.
		</p>
		<p>
			Multiple layers can be turned on/off at the same time by selecting a number of layers and using the enable/disable buttons
			in the <span class="UIElementReference">Layers panel</span> toolbar.
		</p>
		<a href="#top">[Top]</a>


		<a name="expandCollapse"/><h3>Expanding and Collapsing Collections</h3>
		<p>
			Click on the <img src="images/button_collapse.png" alt="Collapse Button" width="9" height="9" /> minus button next to a
			Collection in the Layers or Datasets panels to collapse the collection.
		</p>
		<p>
			Click on the <img src="images/button_expand.png" alt="Collapse Button" width="9" height="9" /> plus button next to a 
			collapsed Collection to expand it. <br />
			<img src="images/Getting_Started_image002.jpg" alt="Collapse the Standard Menu" width="385" height="196" />
		</p>
		<a href="#top">[Top]</a>
	
	
		<a name="addFromDatasets"/><h3>Add Layers from Datasets to the Layers panel</h3>
		<p>
			Only layers in the Layers panel are able to be viewed in the Data Viewer. Layers can be added by clicking on the 
			<img src="images/button_add.png" alt="Add Layer button" width="16" height="16" align="texttop" /> Add Layer button 
			next to a layer in the Datasets panel. Layers can also be added to the Layers panel by clicking and dragging them 
			from the Datasets panel to the desired position in the Layers panel.
		</p>
		<a href="#top">[Top]</a>	
	
		
		<a name="multipleLayers"/><h3>Working with Multiple Layers</h3>
		<p>
			Multiple layers can be added at once using the standard windows operations of 
			&quot;Shift-Click&quot; and &quot;Ctrl+Click&quot;.
		</p>
		<p>
			To select a continuous group of layers, select the first layer by clicking on it, then holding the 
			&quot;Shift&quot; key down, click on the last layer in the group.
		</p>
		<p>
			To select a discontinuous group of layers, select the first layer by clicking on it, then while holding the 
			&quot;Ctrl&quot; key down, click on each layer you want to add to the group.
		</p>
		<p>
			The selected group of layers can now be moved, added to or removed from the Layers panel, or turned on or off.
		</p>
		<table width="200" border="0" cellpadding="5">
			<tr>
				<td>
					<div align="center">
						<img src="images/layers_bulk_selection.png" alt="Selecting Multiple Layers - Shift Click" width="297" height="347" border="1" /><br />
						<em>Shift + Click to select multiple layers</em>
					</div>
				</td>
				<td>
					<div align="center">
						<img src="images/layers_multiple_selection.png" alt="Selecting Multiple Layers - Ctrl Click" width="297" height="347" border="1" />
						<em>Ctrl + Click to select multiple layers</em><br />
					</div>
				</td>
			</tr>
		</table>
		<a href="#top">[Top]</a>

		<a name="removeLayers"/><h3>Remove Layers from the Layers panel</h3>
		<p>
			Layers can be removed from the Layers panel by selecting the layer in
			the Layers panel and then clicking the <img src="images/button_delete.png" alt="Delete Button" width="23" height="23" align="absmiddle" />
			delete button. Multiple layers and Collections can be deleted from the Layers panel using this button.
		</p>
		<p>
			Layers can also be removed from the Datasets panel by clicking the 
			<img src="images/button_remove.png" alt="Remove Button" width="16" height="16" align="absmiddle" /> 
			remove button next to the appropriate layer.
		</p>
		<a href="#top">[Top]</a>
	
		<a name="layerOpacity"/><h3>Layer opacity</h3>
		<p> 
			The 3D Data Viewer supports having multiple layers overlayed over the same spatial region. 
			When this happens, the layers are drawn in the order they appear in the layer tree, from top to bottom 
			(e.g a layer at the top of the tree will be obstructed by one below it in the tree).  
		</p>
		<p>
			In order to see layers that are 'beneath' others, the opacity of layers can be controlled using the layer 
			opacity slider in the layers panel toolbar. 
		</p>
		<p>
			Layer opacity is also extremely useful when viewing subsurface datasets (earthquakes, seismic etc). These datasets
			 are located below the surface of the digital globe and therefore can be obscured from view by layer data on the surface.
		</p>
		<p>
			To control the opacity of a layer on the globe select the layer in the layers panel so it is highlighted and then slide the
			Layer Opacity slider to the left. This will reduce the layers opacity, making the current layer more transparent, and allowing 
			subsurface data to be more visible. When layer opacity is anything other than 100% (completely opaque), an indication of the
			opacity will appear next to the layer name in the layers panel. 
		</p>
		<img src="images/layers_opacity.jpg" alt="Layer opacity is controlled via the opacity slider" /><br/>
		<a href="#top">[Top]</a>
	</body>
</html>