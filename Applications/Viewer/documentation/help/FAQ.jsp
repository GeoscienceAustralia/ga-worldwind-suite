<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>GA 3D Data Viewer - Help - FAQ</title>
		
		<link href="style.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
		<h2>Frequently Asked Questions (FAQs)</h2>
		<p>Below are a number of frequently asked questions, along with answers.</p>
		<hr/>
		<ul>
			<li><a href="#q1">Why can't I see <em>layerX</em> on the globe?</a></li>
		</ul>
		<hr/>
		
		<a name="q1"/><h3>Why can't I see <em>layerX</em> on the globe?</h3>
		<p>
			There are a few reasons why a layer may not appear on the globe. Below are some of them,
			ordered roughly by likelihood.
			<ol>
				<li>
					<strong>It is not enabled</strong>
					<br/>
					Only enabled layers are shown on the globe. 
					Ensure that the check box in the Layers panel are next to the layer name is checked. 
				</li>
				<li>
					<strong>It's opacity is 0%</strong>
					<br/>
					If the layer's opacity is 0% it will be completely transparent and will not be visible on the globe.<br/>
					Ensure that the opacity of your layer is greater than 0%. This can be controlled using the opacity 
					slider in the layer panel.
				</li>
				<li>
					<strong>It is being obstructed by another layer</strong>
					<br/>
					Layers in the 3D globe are drawn in order from top-to-bottom. 
					If your layer is ordered above another layer with the same spatial extents, it may
					be completely obstructed.<br/> 
					Try moving your layer to the bottom of the layer tree, or
					adjusting the opacity of any potentially obstructing layers.
				</li>
				<li>
					<strong>The layer imagery has not loaded yet</strong>
					<br/>
					
				</li>
			</ol> 
		
		</p>
	</body>
</html>