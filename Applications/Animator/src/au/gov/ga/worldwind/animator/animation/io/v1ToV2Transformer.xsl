<?xml version="1.0" encoding="UTF-8" standalone="no" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<worldWindAnimation version="2.0">
			<animation frameCount="<xsl:value-of select="//restorableState[@name='frameCount']"/>" zoomRequired="<xsl:value-of select="//restorableState[@name='scaledZoom']"/>">
			
			</animation>
		</worldWindAnimation>
	</xsl:template>

</xsl:stylesheet>