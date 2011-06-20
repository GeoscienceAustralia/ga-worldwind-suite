<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>GA 3D Data Viewer - Help - FAQ</title>
		
		<link href="style.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
		
		<a name="top"/>
		<h2>The WMS Browser</h2>
		<p>
			The <span class="UIElementReference">WMS Browser</span> tool provides even more data for the user to explore. 
			The tool allows the user to browse, search and import data from WMS servers around the world. 
		</p>
		<hr/>
		<ul>
			<li><a href="#openWms">To launch the WMS Browser</a></li>
			<li><a href="#useWms">Using the WMS Browser</a></li>
			<li><a href="#addWms">Adding more WMS servers</a></li>
		</ul>
		<hr/>
		
		<a name="openWms"/><h3>To launch the WMS Browser</h3>
		<p>
			<b>Option 1:</b>
			<em>File&gt;Browse for WMS layers...</em>
			<br/><br/>
			<img src="images/wms_launchBrowser.jpg" alt="From the file menu, selec 'Browse for WMS layers...'"/>
		</p>
		<p>
			<b>Option 2:</b>
			From the toolbar, click the "Launch WMS Browser" button
			<br/>
			<img src="images/wms_launchToolbar.jpg" alt="From the toolbar, click the 'Launch WMS Browser' button"/>
		</p>
		<a href="#top">[Top]</a>
		
		<a name="useWms"/><h3>Using the WMS Browser</h3>
		<p>
			Once launched, the <span class="UIElementReference">WMS Browser</span> shows a list of known WMS servers, along 
			with the layers they contain. When a layer is selected, the layer information panel will show metadata about the layer,
			and a preview of the layer will be shown in the layer preview panel.
		</p>
		<p>
			To view a layer in the main 3D view:
		</p>
		<ol>
			<li>
				Open the WMS server layers by clicking the '<strong>+</strong>' next to the WMS sever name
			</li>
			<li>
				Then select a layer name. The layer will be displayed in the 2D viewer panel, and any metadata (bounding box, legend etc.)
				will be displayed in the layer information panel.
				<br/><br/>
				<img src="images/wms_selectLayer.jpg" alt="Selecting a WMS layer"/>
			</li>
			<li>
				Once the layer has been selected, either (a) <em>Right click&gt;Use layer</em> or 
				(b) Click the "Use layer" button in the WMS Servers panel toolbar.
				<br/><br/>
				<img src="images/wms_useLayer.jpg" alt="Using a WMS layer"/>
			</li>
		</ol>
		<p>
			The imported layer will be located under the "<span class="UIElementReference">WMS Layers</span>" node. It can now be 
			manipulated in the same way as any other layer in the 3D Data Viewer.
		</p>
		<img src="images/wms_afterImport.jpg" alt="The imported WMS layer will be found under 'WMS Layers'"/><br/>
		<a href="#top">[Top]</a>
		
		<a name="addWms"/><h3>Adding more WMS servers</h3>
		<p>
			More data can be loaded into the <span class="UIElementReference">WMS Browser</span> by adding additional servers to the 
			WMS servers list. This can be done by either using the URL of a WMS server directly, or by searching configured CSW catalogues
			for layers that contain specified keywords.
		</p>
		<p>
			To add a new WMS server: 
		</p>
		<ol>
			<li>
				Hit the "Add WMS Layer" button in the WMS Servers toolbar (the green plus symbol)
			</li>
			<li>
				Enter the WMS server URL, or a search term, into the search window and hit "Search"
				<br/><br/>
				<img src="images/wms_searchServer.jpg" alt="Searching for a new WMS server"/>
			</li>
			<li>
				Once the search results are shown, select one or more servers of interest and click "OK".
				These servers will now appear in the <span class="UIElementReference">WMS Browser</span> servers panel.
			</li>
		</ol>
		<a href="#top">[Top]</a>
	</body>
</html>