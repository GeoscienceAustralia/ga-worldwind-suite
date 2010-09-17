uniform sampler2D tex0;
uniform sampler2D tex1;

uniform float opacity;
uniform float exaggeration;
uniform float bakedExaggeration;

uniform vec3 eyePosition;
uniform vec3 sunPosition;

varying vec3 position;

const float PI = 3.1415926535;

float distancePercent(float value, float distanceTo)
{
	float diff = 0.0;
	if (value < distanceTo)
	{
		diff = (distanceTo - value) / distanceTo;
	}
	else if (value > distanceTo)
	{
		diff = (value - distanceTo) / (1.0 - distanceTo);
	}
	return clamp(diff, 0.0, 1.0);
}

mat3 projection(vec3 from, vec3 to)
{
	vec3 v = cross(from, to);
	float e = dot(from, to);
	float f = abs(e);
	mat3 mtx;
	
    float h = 1.0 / (1.0 + e);
    float hvx = h * v[0];
    float hvz = h * v[2];
    float hvxy = hvx * v[1];
    float hvxz = hvx * v[2];
    float hvyz = hvz * v[1];
    
    mtx[0][0] = e + hvx * v[0];
    mtx[0][1] = hvxy - v[2];
    mtx[0][2] = hvxz + v[1];

    mtx[1][0] = hvxy + v[2];
    mtx[1][1] = e + h * v[1] * v[1];
    mtx[1][2] = hvyz - v[0];

    mtx[2][0] = hvxz - v[1];
    mtx[2][1] = hvyz + v[0];
    mtx[2][2] = e + hvz * v[2];
	
	return mtx;
}

void main(void)
{
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);
	//alpha mask - drop frags less than 1
	if(alpha.a < 1.0) discard;
	
	vec4 tile = texture2D(tex0, gl_TexCoord[0].st);
	
	//read normal from tile rgb
	vec3 normalUnnormalized = tile.rgb * 2.0 - 1.0;
	//drop zero (or near zero) length normals (invalid elevation)
	if(length(normalUnnormalized) < 0.1) discard;
	
	bool unitZNormal = false;
	
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
	
	if(unitZNormal)
	{
		//reproject normal with respect to unit-z
		vec3 unitz = vec3(0.0, 0.0, 1.0);
		mat3 matrix = projection(positionNormalized, unitz);
		perpposition = perpposition * matrix;
		positionNormalized = unitz;
	}
	
	//calculate new exaggerated normal
	normal = normalize(mix(positionNormalized, perpposition, mixer));
	
	float diffuse = 1.0;
	float ambient = 0.1;
	vec3 specColor = vec3(1.0, 1.0, 0.5);
	float shininess = 100;
	
	float nxDir = max(0.0, dot(normal, sunPosition));
	float light = max(nxDir * diffuse, ambient);
	
	float specularPower = 0.0;
	if(nxDir != 0.0)
	{
		vec3 cameraVector = normalize(eyePosition - position);
		vec3 halfVector = normalize(sunPosition + cameraVector);
		float nxHalf = max(0.0, dot(normal, halfVector));
		specularPower = pow(nxHalf, shininess);
	}
	vec3 specular = specColor * specularPower;
	
	bool colorToAlpha = false;
	
	if(colorToAlpha)
	{
		float cta = 1.0;
		float percent = distancePercent(light, cta);
		if(percent > 0.0)
		{
			light = (light - cta) / percent + cta;
		}
		
		//vec3 rgb = vec3(light) * percent + specular;
		//float a = min(percent + specularPower, 1.0);
		vec3 rgb = vec3(light * percent);
		float a = percent;
		
		gl_FragColor = vec4(rgb, a * opacity);
	}
	else
	{
		gl_FragColor = vec4(light + specular, opacity);
	}
}
