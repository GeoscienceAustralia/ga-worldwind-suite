uniform sampler2D tex0;
uniform sampler2D tex1;

uniform float opacity;
uniform float scale;
uniform float bias;

uniform vec3 sunPosition;

varying vec3 position;

const float PI = 3.1415926535;

void main(void)
{
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);
	
	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;
	
	vec3 positionNormalized = normalize(position);
	vec3 sunNormalized = normalize(sunPosition);
	float sunAngle = acos(dot(positionNormalized, sunNormalized));
	
	//sunposition: S = sunPosition
	//point on plane: P = position;
	//plane's normal: N = positionNormalized;
	//nearest point on plane to sunposition: NPP = S - ((S - P).N)N
	//plane's east vector: PEV = (0,1,0).N
	//plane's north vector: PNV = N.PEV
	//heading angle = angle between PNV and NPP
	vec3 nearestPoint = sunPosition - dot((sunPosition - position), positionNormalized) * positionNormalized;
	vec3 eastVector = normalize(cross(vec3(0.0, 1.0, 0.0), positionNormalized));
	vec3 northVector = normalize(cross(positionNormalized, eastVector));
	vec3 nearestPointVector = normalize(nearestPoint - position);
	float angleFromNorth = acos(dot(northVector, nearestPointVector));
	float angleFromEast = acos(dot(eastVector, nearestPointVector));
	bool leftQuads = angleFromEast > (PI / 2.0);
	
	float pieight = PI * 0.125;
	float horizon1 = 0.0;
	float horizon2 = 0.0;
	float mixer = mod(angleFromNorth, pieight) / pieight;
	if(angleFromNorth == PI)
	{
		mixer = 1.0;
	}
	
	//  2  3  4  5  6
	//  1           7
	//  0           8
	// 15           9
	// 14 13 12 11 10
	

	//debug horizon textures, using bias as modifier:
	/*angleFromNorth = abs(bias) * PI;
	if(bias < -0.5)
	{
		angleFromEast = abs(bias) * PI;
	}
	else if(bias < 0.0)
	{
		angleFromEast = (0.5 + abs(bias)) * PI;
	}
	else if(bias < 0.5)
	{
		angleFromEast = (0.5 - bias) * PI;
	}
	else
	{
		angleFromEast = (bias - 0.5) * PI;
	}
	
	leftQuads = angleFromEast > (PI * 0.5);
	
	mixer = mod(angleFromNorth, pieight) / pieight;
	if(angleFromNorth == PI)
	{
		mixer = 1.0;
	}*/
	
	
	vec2 texCoord = gl_TexCoord[0].st * 0.5;
	vec2 off0 = vec2(0.0, 0.0);
	vec2 off1 = vec2(0.5, 0.0);
	vec2 off2 = vec2(0.0, 0.5);
	vec2 off3 = vec2(0.5, 0.5);
	
	if(leftQuads)
	{
		if(angleFromNorth > pieight * 7.0)
		{
			//13-12 (a1, a0)
			horizon1 = texture2D(tex0, texCoord + off1).a;
			horizon2 = texture2D(tex0, texCoord + off0).a;
		}
		else if(angleFromNorth > pieight * 6.0)
		{
			//14-13 (a2, a1)
			horizon1 = texture2D(tex0, texCoord + off2).a;
			horizon2 = texture2D(tex0, texCoord + off1).a;
		}
		else if(angleFromNorth > pieight * 5.0)
		{
			//15-14 (a3, a2)
			horizon1 = texture2D(tex0, texCoord + off3).a;
			horizon2 = texture2D(tex0, texCoord + off2).a;
		}
		else if(angleFromNorth > pieight * 4.0)
		{
			//0-15 (r0, a3)
			horizon1 = texture2D(tex0, texCoord + off0).r;
			horizon2 = texture2D(tex0, texCoord + off3).a;
		}
		else if(angleFromNorth > pieight * 3.0)
		{
			//1-0 (r1, r0)
			horizon1 = texture2D(tex0, texCoord + off1).r;
			horizon2 = texture2D(tex0, texCoord + off0).r;
		}
		else if(angleFromNorth > pieight * 2.0)
		{
			//2-1 (r2, r1)
			horizon1 = texture2D(tex0, texCoord + off2).r;
			horizon2 = texture2D(tex0, texCoord + off1).r;
		}
		else if(angleFromNorth > pieight)
		{
			//3-2 (r3, r2)
			horizon1 = texture2D(tex0, texCoord + off3).r;
			horizon2 = texture2D(tex0, texCoord + off2).r;
		}
		else
		{
			//4-3 (g0, r3)
			horizon1 = texture2D(tex0, texCoord + off0).g;
			horizon2 = texture2D(tex0, texCoord + off3).r;
		}
	}
	else
	{
		if(angleFromNorth > pieight * 7.0)
		{
			//11-12 (b3, a0)
			horizon1 = texture2D(tex0, texCoord + off3).b;
			horizon2 = texture2D(tex0, texCoord + off0).a;
		}
		else if(angleFromNorth > pieight * 6.0)
		{
			//10-11 (b2, b3)
			horizon1 = texture2D(tex0, texCoord + off2).b;
			horizon2 = texture2D(tex0, texCoord + off3).b;
		}
		else if(angleFromNorth > pieight * 5.0)
		{
			//9-10 (b1, b2)
			horizon1 = texture2D(tex0, texCoord + off1).b;
			horizon2 = texture2D(tex0, texCoord + off2).b;
		}
		else if(angleFromNorth > pieight * 4.0)
		{
			//8-9 (b0, b1)
			horizon1 = texture2D(tex0, texCoord + off0).b;
			horizon2 = texture2D(tex0, texCoord + off1).b;
		}
		else if(angleFromNorth > pieight * 3.0)
		{
			//7-8 (g3, b0)
			horizon1 = texture2D(tex0, texCoord + off3).g;
			horizon2 = texture2D(tex0, texCoord + off0).b;
		}
		else if(angleFromNorth > pieight * 2.0)
		{
			//6-7 (g2, g3)
			horizon1 = texture2D(tex0, texCoord + off2).g;
			horizon2 = texture2D(tex0, texCoord + off3).g;
		}
		else if(angleFromNorth > pieight)
		{
			//5-6 (g1, g2)
			horizon1 = texture2D(tex0, texCoord + off1).g;
			horizon2 = texture2D(tex0, texCoord + off2).g;
		}
		else
		{
			//4-5 (g0, g1)
			horizon1 = texture2D(tex0, texCoord + off0).g;
			horizon2 = texture2D(tex0, texCoord + off1).g;
		}
	}
	
	float horizon = mix(horizon1, horizon2, mixer);
	float horizonAngle = horizon * PI;
	horizonAngle = horizonAngle * scale + bias;
	
	bool inShadow = sunAngle > horizonAngle;
	gl_FragColor = vec4(0.0, 0.0, 0.0, inShadow ? opacity : 0.0);
	
	//debug sun position:
	/*if(sunAngle < 0.1)
	{
		gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}*/
}
