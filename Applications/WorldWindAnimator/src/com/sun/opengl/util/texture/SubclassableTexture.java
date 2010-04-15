package com.sun.opengl.util.texture;

import javax.media.opengl.GLException;

public class SubclassableTexture extends Texture
{
	public SubclassableTexture(TextureData data) throws GLException
	{
		super(data);
	}
	
	public SubclassableTexture(int target) throws GLException
	{
		super(target);
	}
}
