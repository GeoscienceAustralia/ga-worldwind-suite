uniform sampler2D tex0;
uniform sampler2D tex1;
uniform int showoutlines;
uniform vec3 eyePosition;
uniform vec3 sunPosition;
uniform sampler2DShadow shadowMap;

varying vec3 normal;
varying vec3 position;
varying vec4 shadow;

void main(void)
{
	vec4 day = texture2D(tex0, gl_TexCoord[0].st * vec4(0.5, 1.0, 1.0, 1.0));
	vec4 night = texture2D(tex0, gl_TexCoord[0].st * vec4(0.5, 1.0, 1.0, 1.0) + vec4(0.5, 0.0, 0.0, 0.0));
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);

	if(showoutlines == 1)
	{
		//black borders 50 percent
		if(gl_TexCoord[0].s < 0.01 || gl_TexCoord[0].s > 0.99)
			day = mix(day, vec4(0.0,0.0,0.0,day.a), 0.5);
		if(gl_TexCoord[0].t < 0.01 || gl_TexCoord[0].t > 0.99)
			day = mix(day, vec4(0.0,0.0,0.0,day.a), 0.5);
	}

	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;

	//drop really dark pixels since the alpha mask is probably missing
	//if(day.r == 0.0 && day.g == 0.0 && day.b == 0.0) discard;

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
		
		float blue = normalize(day.rgb).b;
		float amount = clamp(blue * 5.0 - 2.0, 0.0, 1.0);
		specular *= vec4(amount);
	}
	
	day = day * vec4(2.0);
	gl_FragColor = mix(night, day, nxDir) + specular;
	//gl_FragColor = vec4(vec3(nxDir), 1.0);
	
	//gl_FragColor = vec4(fract(normalize(position)), 1.0);
	//gl_FragColor = vec4(fract(lightVector), 1.0);
	
	//gl_FragColor = vec4(vec3(shadow2DProj(shadowMap, shadow).rgb), 1.0);
}
