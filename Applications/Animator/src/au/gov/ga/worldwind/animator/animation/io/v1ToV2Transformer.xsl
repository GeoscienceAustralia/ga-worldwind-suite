<?xml version="1.0" encoding="UTF-8" standalone="no" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<worldWindAnimation version="2.0">
			<xsl:param name="frameCount" select='//stateObject[@name="frameCount"]'/>
			<xsl:param name="zoomRequired" select='boolean(//stateObject[@name="scaledZoom"])'/>
			<animation frameCount="{$frameCount}" zoomRequired="{$zoomRequired}">
				<renderParameters>
					<frameRate>25</frameRate>
					<width><xsl:value-of select='//stateObject[@name="width"]'/></width>
					<height><xsl:value-of select='//stateObject[@name="height"]'/></height>
				</renderParameters>
				<animatableObjects>
            		<camera name="Render Camera">
            			<eyeLat>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Eye Latitude">
		                    	<xsl:for-each select="//stateObject[@name='eyeLat']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="eyeLatFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="eyeLatValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="eyeLatInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="eyeLatInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="eyeLatOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="eyeLatOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="eyeLatLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$eyeLatFrame}" inPercent="{$eyeLatInPercent}" inValue="{$eyeLatInValue}" locked="{$eyeLatLocked}" outPercent="{$eyeLatOutPercent}" outValue="{$eyeLatOutValue}" type="BEZIER" value="{$eyeLatValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </eyeLat>
		                <eyeLon>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Eye Longitude">
		                    	<xsl:for-each select="//stateObject[@name='eyeLon']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="eyeLonFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="eyeLonValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="eyeLonInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="eyeLonInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="eyeLonOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="eyeLonOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="eyeLonLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$eyeLonFrame}" inPercent="{$eyeLonInPercent}" inValue="{$eyeLonInValue}" locked="{$eyeLonLocked}" outPercent="{$eyeLonOutPercent}" outValue="{$eyeLonOutValue}" type="BEZIER" value="{$eyeLonValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </eyeLon>
		                <eyeElevation>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Eye Elevation">
		                    	<xsl:for-each select="//stateObject[@name='eyeZoom']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="eyeZoomFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="eyeZoomValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="eyeZoomInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="eyeZoomInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="eyeZoomOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="eyeZoomOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="eyeZoomLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$eyeZoomFrame}" inPercent="{$eyeZoomInPercent}" inValue="{$eyeZoomInValue}" locked="{$eyeZoomLocked}" outPercent="{$eyeZoomOutPercent}" outValue="{$eyeZoomOutValue}" type="BEZIER" value="{$eyeZoomValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </eyeElevation>
		                <lookAtLat>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Look-at Latitude">
		                    	<xsl:for-each select="//stateObject[@name='centerLat']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="centerLatFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="centerLatValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="centerLatInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="centerLatInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="centerLatOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="centerLatOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="centerLatLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$centerLatFrame}" inPercent="{$centerLatInPercent}" inValue="{$centerLatInValue}" locked="{$centerLatLocked}" outPercent="{$centerLatOutPercent}" outValue="{$centerLatOutValue}" type="BEZIER" value="{$centerLatValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </lookAtLat>
		                <lookAtLon>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Look-at Longitude">
		                    	<xsl:for-each select="//stateObject[@name='centerLon']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="centerLonFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="centerLonValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="centerLonInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="centerLonInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="centerLonOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="centerLonOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="centerLonLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$centerLonFrame}" inPercent="{$centerLonInPercent}" inValue="{$centerLonInValue}" locked="{$centerLonLocked}" outPercent="{$centerLonOutPercent}" outValue="{$centerLonOutValue}" type="BEZIER" value="{$centerLonValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </lookAtLon>
		                <lookAtElevation>
		                    <parameter defaultValue="0.0" enabled="true" name="Render Camera - Look-at Elevation">
		                    	<xsl:for-each select="//stateObject[@name='centerZoom']//stateObject[@name='key']/restorableState">
		                    		<xsl:param name="centerZoomFrame" select="stateObject[@name='frame']"/>
		                    		<xsl:param name="centerZoomValue" select="stateObject[@name='value']"/>
		                    		<xsl:param name="centerZoomInValue" select="stateObject[@name='inValue']"/>
		                    		<xsl:param name="centerZoomInPercent" select="stateObject[@name='inPercent']"/>
		                    		<xsl:param name="centerZoomOutValue" select="stateObject[@name='outValue']"/>
		                    		<xsl:param name="centerZoomOutPercent" select="stateObject[@name='outPercent']"/>
		                    		<xsl:param name="centerZoomLocked" select="stateObject[@name='lockInOut']"/>
		                    		<parameterValue frame="{$centerZoomFrame}" inPercent="{$centerZoomInPercent}" inValue="{$centerZoomInValue}" locked="{$centerZoomLocked}" outPercent="{$centerZoomOutPercent}" outValue="{$centerZoomOutValue}" type="BEZIER" value="{$centerZoomValue}"/>
		                    	</xsl:for-each>
		                    </parameter>
		                </lookAtElevation>
            		</camera>
            	</animatableObjects>
			</animation>
		</worldWindAnimation>
	</xsl:template>

</xsl:stylesheet>