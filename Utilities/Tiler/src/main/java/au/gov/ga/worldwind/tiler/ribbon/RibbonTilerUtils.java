/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.tiler.ribbon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility methods for the ribbon tiler
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RibbonTilerUtils
{
	public static void saveIntArrayToFile(int[] array, File file)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(fos);
			for (int i : array)
			{
				dos.writeInt(i);
			}
			dos.close();
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static int[] loadIntArrayFromFile(File file)
	{
		if (!file.exists())
		{
			return null;
		}

		int size = (int) (file.length() / 4l);
		try
		{
			FileInputStream fis = new FileInputStream(file);
			DataInputStream dis = new DataInputStream(fis);
			int[] array = new int[size];
			for (int i = 0; i < size; i++)
			{
				array[i] = dis.readInt();
			}
			return array;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
