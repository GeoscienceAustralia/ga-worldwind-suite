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

vec3 lordhowe(float depth)
{
vec3 color2 = vec3(0.490196078431373, 0.490196078431373, 0.498039215686275);
vec3 color3 = vec3(0.474509803921569, 0.447058823529412, 0.501960784313725);
vec3 color4 = vec3(0.454901960784314, 0.4, 0.505882352941176);
vec3 color5 = vec3(0.427450980392157, 0.345098039215686, 0.509803921568627);
vec3 color6 = vec3(0.403921568627451, 0.298039215686275, 0.513725490196078);
vec3 color7 = vec3(0.380392156862745, 0.250980392156863, 0.517647058823529);
vec3 color8 = vec3(0.337254901960784, 0.243137254901961, 0.572549019607843);
vec3 color9 = vec3(0.286274509803922, 0.243137254901961, 0.627450980392157);
vec3 color10 = vec3(0.243137254901961, 0.243137254901961, 0.686274509803922);
vec3 color11 = vec3(0.356862745098039, 0.447058823529412, 0.741176470588235);
vec3 color12 = vec3(0.454901960784314, 0.631372549019608, 0.796078431372549);
vec3 color13 = vec3(0.564705882352941, 0.831372549019608, 0.850980392156863);
vec3 color14 = vec3(0.537254901960784, 0.792156862745098, 0.764705882352941);
vec3 color15 = vec3(0.494117647058824, 0.72156862745098, 0.658823529411765);
vec3 color16 = vec3(0.450980392156863, 0.650980392156863, 0.552941176470588);
vec3 color17 = vec3(0.403921568627451, 0.576470588235294, 0.435294117647059);
vec3 color18 = vec3(0.345098039215686, 0.482352941176471, 0.294117647058824);
vec3 color19 = vec3(0.584313725490196, 0.662745098039216, 0.317647058823529);
vec3 color20 = vec3(0.941176470588235, 0.917647058823529, 0.352941176470588);
vec3 color21 = vec3(0.854901960784314, 0.607843137254902, 0.388235294117647);
vec3 color22 = vec3(0.803921568627451, 0.52156862745098, 0.435294117647059);
vec3 color23 = vec3(0.803921568627451, 0.52156862745098, 0.435294117647059);
vec3 color24 = vec3(0.803921568627451, 0.784313725490196, 0.494117647058824);
vec3 color25 = vec3(0.831372549019608, 0.909803921568627, 0.76078431372549);
vec3 color26 = vec3(0.976470588235294, 0.972549019607843, 0.972549019607843);
vec3 color27 = vec3(0.976470588235294, 0.976470588235294, 0.976470588235294);
vec3 color28 = vec3(0.984313725490196, 0.984313725490196, 0.984313725490196);
vec3 color29 = vec3(0.980392156862745, 0.996078431372549, 0.992156862745098);
vec3 color30 = vec3(0.180392156862745, 0.180392156862745, 0.180392156862745);
vec3 color31 = vec3(0.180392156862745, 0.180392156862745, 0.180392156862745);

if(depth < -135.0) { return color2; }
if(depth < -130.0) { return mix(color2, color3, (depth - (-135.0)) / 5.0); }
if(depth < -125.0) { return mix(color3, color4, (depth - (-130.0)) / 5.0); }
if(depth < -120.0) { return mix(color4, color5, (depth - (-125.0)) / 5.0); }
if(depth < -115.0) { return mix(color5, color6, (depth - (-120.0)) / 5.0); }
if(depth < -110.0) { return mix(color6, color7, (depth - (-115.0)) / 5.0); }
if(depth < -105.0) { return mix(color7, color8, (depth - (-110.0)) / 5.0); }
if(depth < -100.0) { return mix(color8, color9, (depth - (-105.0)) / 5.0); }
if(depth < -95.0) { return mix(color9, color10, (depth - (-100.0)) / 5.0); }
if(depth < -90.0) { return mix(color10, color11, (depth - (-95.0)) / 5.0); }
if(depth < -85.0) { return mix(color11, color12, (depth - (-90.0)) / 5.0); }
if(depth < -80.0) { return mix(color12, color13, (depth - (-85.0)) / 5.0); }
if(depth < -75.0) { return mix(color13, color14, (depth - (-80.0)) / 5.0); }
if(depth < -70.0) { return mix(color14, color15, (depth - (-75.0)) / 5.0); }
if(depth < -65.0) { return mix(color15, color16, (depth - (-70.0)) / 5.0); }
if(depth < -60.0) { return mix(color16, color17, (depth - (-65.0)) / 5.0); }
if(depth < -55.0) { return mix(color17, color18, (depth - (-60.0)) / 5.0); }
if(depth < -50.0) { return mix(color18, color19, (depth - (-55.0)) / 5.0); }
if(depth < -45.0) { return mix(color19, color20, (depth - (-50.0)) / 5.0); }
if(depth < -40.0) { return mix(color20, color21, (depth - (-45.0)) / 5.0); }
if(depth < -37.0) { return mix(color21, color22, (depth - (-40.0)) / 3.0); }
if(depth < -35.0) { return mix(color22, color23, (depth - (-37.0)) / 2.0); }
if(depth < -30.0) { return mix(color23, color24, (depth - (-35.0)) / 5.0); }
if(depth < -25.0) { return mix(color24, color25, (depth - (-30.0)) / 5.0); }
if(depth < -20.0) { return mix(color25, color26, (depth - (-25.0)) / 5.0); }
if(depth < -15.0) { return mix(color26, color27, (depth - (-20.0)) / 5.0); }
if(depth < -10.0) { return mix(color27, color28, (depth - (-15.0)) / 5.0); }
if(depth < -5.0) { return mix(color28, color29, (depth - (-10.0)) / 5.0); }
if(depth < 0.0) { return mix(color29, color30, (depth - (-5.0)) / 5.0); }
if(depth < 800.0) { return mix(color30, color31, (depth - (0.0)) / 800.0); }
return color31;
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
	//vec3 color = ocean(percent);
	vec3 color = lordhowe(elevation);
	
	gl_FragColor = vec4(light * color + specular * color, opacity);
}
