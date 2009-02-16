uniform sampler2D tex0;
uniform sampler2D tex1;
uniform vec3 eyePosition;
uniform vec3 sunPosition;

varying vec3 normal;
varying vec3 position;

void main(void)
{
	vec4 tile = texture2D(tex0, gl_TexCoord[0].st);
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);

	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;

	vec4 specularColor = vec4(1.0, 1.0, 0.6, 1.0);
	vec3 norm = normalize(normal);
	
	float nxDir = max(0.0, dot(norm, sunPosition));
	//nxDir = pow(nxDir, 0.1);
	nxDir = min(1.0, nxDir * 2.0);
	
	vec4 specular = vec4(0.0);
	
	if(nxDir != 0.0)
	{
		vec3 cameraVector = normalize(eyePosition - position.xyz);
		vec3 halfVector = normalize(sunPosition + cameraVector);
		float nxHalf = max(0.0, dot(norm, halfVector));
		
		float shininess = 80.0;
		float specularPower = pow(nxHalf, shininess);
		specular = specularColor * specularPower;
		
		float amount = tile.b;
		//amount = clamp(amount * 5.0 - 2.0, 0.0, 1.0);
		specular *= vec4(amount);
	}
	
	float red = tile.r;
	gl_FragColor = mix(vec4(red, red * 0.9, red * 0.8, 0.8), vec4(0.0), nxDir) + specular;
}
