uniform sampler2D tex0;
uniform sampler2D tex1;
uniform float opacity;
uniform float minElevation;
uniform float maxElevation;

varying float elevation;

vec3 depthToChroma(float depth)
{
	float r = 2.0 - depth * 4.0;
	float b = depth * 4.0 - 2.0;
	float g = depth * 4.0;
	if(g >= 2.0)
	{
		g = 4.0 - g;
	}
	
	vec3 chroma = vec3(r, g, b);
	chroma = clamp(chroma, 0.0, 1.0);
	chroma = 2.0 * chroma - chroma * chroma;
	return chroma;
}

void main(void)
{
	vec4 tile = texture2D(tex0, gl_TexCoord[0].st);
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);
	
	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;
	
	float splitPoint = 0.5;
	
	float percent;
	//float percent = (elevation - minElevation) / (maxElevation - minElevation);
	
	if(elevation >= 0)
	{
		//percent = splitPoint + (elevation / maxElevation) * (1.0 - splitPoint);
		percent = elevation / maxElevation;
	}
	else
	{
		//percent = ((minElevation - elevation) / minElevation) * splitPoint;
		percent = (minElevation - elevation) / minElevation;
	}
	
	percent = clamp(percent, 0.0, 1.0);
	vec3 chroma = depthToChroma(1.0 - percent);
	gl_FragColor = vec4(chroma, opacity);
}
