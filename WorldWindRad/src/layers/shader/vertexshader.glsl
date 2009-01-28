varying vec3 normal;
varying vec3 position;
varying vec4 shadow;

void main(void)
{
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
	gl_TexCoord[1] = gl_TextureMatrix[0] * gl_MultiTexCoord1;
	
	//normal = normalize(gl_NormalMatrix * gl_Normal);
	normal = gl_Normal;
	position = gl_Vertex.xyz;
	shadow = gl_TextureMatrix[0] * gl_ModelViewMatrix * gl_Vertex;
}
