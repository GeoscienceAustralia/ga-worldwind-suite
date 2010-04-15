uniform sampler2D tex0;
uniform sampler2D tex1;

uniform float opacity;
uniform float minElevation;
uniform float maxElevation;
uniform float minTexElevation;
uniform float maxTexElevation;
uniform float exaggeration;
uniform float bakedExaggeration;

uniform vec3 eyePosition;
uniform vec3 sunPosition;

varying vec3 position;

const float PI = 3.1415926535;

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
	//chroma = clamp(chroma, 0.0, 1.0);
	//chroma = 2.0 * chroma - chroma * chroma;
	return chroma;
}

vec3 ocean(float depth)
{
	vec3 color1 = vec3(0.0, 0.0, 0.4);
	vec3 color2 = vec3(0.0, 0.1, 0.7);
	vec3 color3 = vec3(0.7, 0.9, 1.0);
	vec3 color4 = vec3(1.0, 1.0, 0.9);
	
	// for each segment, mixer = (depth - <start>) / <size>
	if(depth < 0.2)
	{
		return mix(color1, color2, (depth - 0.0) / 0.2);
	}
	else if(depth < 0.9)
	{
		return mix(color2, color3, (depth - 0.2) / 0.7);
	}
	else
	{
		return mix(color3, color4, (depth - 0.9) / 0.1);
	}
}

void main(void)
{
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);
	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;
	
	vec4 tile = texture2D(tex0, gl_TexCoord[0].st);
	
	//read normal from tile rgb
	vec3 normalUnnormalized = tile.rgb * 2.0 - 1.0;
	//drop zero (or near zero) length normals (invalid elevation)
	if(length(normalUnnormalized) < 0.1) discard;
	
	//read elevation from tile alpha
	float elevation = minTexElevation + tile.a * (maxTexElevation - minTexElevation);
	
	//normalized normal
	vec3 normal = normalize(normalUnnormalized);
	//normalized position
	vec3 positionNormalized = normalize(position);
	//vector perpendicular to position and normal
	vec3 bivector = normalize(cross(positionNormalized, normal));
	//vector at right angles to position in direction of normal
	vec3 perpposition = normalize(cross(bivector, positionNormalized));
	//angle between position and normal
	float normalangle = acos(dot(positionNormalized, normal));
	//calculate exaggerated angle (normals are already exaggerated by bakedExaggeration)
	float exaggeratedangle = tan((exaggeration / bakedExaggeration) * atan(normalangle));
	//calculate mixer
	float mixer = clamp(exaggeratedangle * 2.0 / PI, 0.0, 1.0);
	//calculate new exaggerated normal
	normal = normalize(mix(positionNormalized, perpposition, mixer));
	
	vec3 ambiColor = vec3(0.1, 0.1, 0.1);
	vec3 diffColor = vec3(1.0, 1.0, 1.0);
	vec3 specColor = vec3(1.0, 1.0, 0.5);
	specColor /= 5.0;
	
	float nxDir = max(0.0, dot(normal, sunPosition));
	vec3 diffuse = diffColor * nxDir;
	
	float specularPower = 0.0;
	float shininess = 10;
	if(nxDir != 0.0)
	{
		vec3 cameraVector = normalize(eyePosition - position);
		vec3 halfVector = normalize(sunPosition + cameraVector);
		float nxHalf = max(0.0, dot(normal, halfVector));
		specularPower = pow(nxHalf, shininess);
	}
	
	vec3 specular = specColor * specularPower;
	vec3 light = clamp(ambiColor + diffuse, 0.0, 1.0);
	
	/*float splitPoint = 0.5;
	float percent;
	if(elevation > 0)
	{
		//percent = splitPoint + (elevation / maxElevation) * (1.0 - splitPoint);
		percent = elevation / maxElevation;
	}
	else
	{
		//percent = ((minElevation - elevation) / minElevation) * splitPoint;
		percent = (minElevation - elevation) / minElevation;
	}
	percent = clamp(percent, 0.0, 1.0);*/
	
	float percent = (elevation - minElevation) / (maxElevation - minElevation);
	percent = clamp(percent, 0.0, 1.0);
	
	//vec3 color = depthToChroma(1.0 - percent);
	vec3 color = ocean(percent);
	
	gl_FragColor = vec4(light * color + specular * color, opacity);
}
