uniform sampler2D tex0;
uniform sampler2D tex1;
uniform int showoutlines;

void main(void)
{
	vec4 tile = texture2D(tex0, gl_TexCoord[0].st);
	vec4 alpha = texture2D(tex1, gl_TexCoord[1].st);

	if(showoutlines == 1)
	{
		//black borders 50 percent
		if(gl_TexCoord[0].s < 0.01 || gl_TexCoord[0].s > 0.99)
			tile = mix(tile, vec4(0.0,0.0,0.0,tile.a), 0.5);
		if(gl_TexCoord[0].t < 0.01 || gl_TexCoord[0].t > 0.99)
			tile = mix(tile, vec4(0.0,0.0,0.0,tile.a), 0.5);
	}

	//alpha mask - drop frags less than 0.01
	if(alpha.a < 1.0) discard;

	//drop really really dark pixels since the alpha mask is probably missing
	if(tile.r == 0.0 && tile.g == 0.0 && tile.b == 0.0) discard;
    
	//gl_FragColor = vec4(tile.rgba);
	float gray = dot(tile.rgb, vec3(0.299, 0.587, 0.114));
	gl_FragColor = vec4(gray, gray, gray, tile.a);
}
