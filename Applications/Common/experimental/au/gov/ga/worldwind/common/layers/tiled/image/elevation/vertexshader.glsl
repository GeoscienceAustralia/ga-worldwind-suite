uniform mat4 oldModelViewInverse;

varying vec3 position;

void main(void)
{
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
	gl_TexCoord[1] = gl_TextureMatrix[0] * gl_MultiTexCoord1;
	
	//calculate reference center using the old (untranslated) modelview matrix
	vec3 refCenter = (oldModelViewInverse * gl_ModelViewMatrix)[3].xyz;
	position = gl_Vertex.xyz + refCenter;
}
