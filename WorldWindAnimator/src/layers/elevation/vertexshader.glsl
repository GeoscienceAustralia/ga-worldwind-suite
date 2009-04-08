uniform float equatorialRadius;
uniform float eccentricitySquared;
uniform vec3 referenceCenter;
uniform bool flatWorld;

varying float elevation;

vec3 cartesianToGeodetic(vec3 cart)
{
	float ra2 = 1.0 / (equatorialRadius * equatorialRadius);
	
	float X = cart.z;
	float Y = cart.x;
	float Z = cart.y;
	float e2 = eccentricitySquared;
	float e4 = e2 * e2;
	
	float XXpYY = X * X + Y * Y;
	float sqrtXXpYY = sqrt(XXpYY);
	float p = XXpYY * ra2;
	float q = Z * Z * (1.0 - e2) * ra2;
	float r = 1.0 / 6.0 * (p + q - e4);
	float s = e4 * p * q / (4.0 * r * r * r);
	float t = pow(1.0 + s + sqrt(s * (2.0 + s)), 1.0 / 3.0);
	float u = r * (1.0 + t + 1.0 / t);
	float v = sqrt(u * u + e4 * q);
	float w = e2 * (u + v - q) / (2.0 * v);
	float k = sqrt(u + v + w * w) - w;
	float D = k * sqrtXXpYY / (k + e2);
	float lon = 2.0 * atan(Y, X + sqrtXXpYY);
	float sqrtDDpZZ = sqrt(D * D + Z * Z);
	float lat = 2.0 * atan(Z, D + sqrtDDpZZ);
	float elevation = (k + e2 - 1.0) * sqrtDDpZZ / k;
	
	return vec3(lon, lat, elevation);
}

void main(void)
{
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
	gl_TexCoord[1] = gl_TextureMatrix[0] * gl_MultiTexCoord1;
	
	if(flatWorld)
		elevation = gl_Vertex.z + referenceCenter.z;
	else
		elevation = cartesianToGeodetic(gl_Vertex.xyz + referenceCenter).z;
}
