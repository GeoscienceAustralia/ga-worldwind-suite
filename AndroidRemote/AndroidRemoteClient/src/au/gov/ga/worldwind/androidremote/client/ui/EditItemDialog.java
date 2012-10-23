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
package au.gov.ga.worldwind.androidremote.client.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModelListener;

/**
 * Dialog used to edit a layer item. Allows enabling/disabling a layer, changing
 * the opacity, and layer deletion.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditItemDialog extends Dialog implements ItemModelListener
{
	private final Item item;
	private final ItemModel model;
	private final ToggleButton toggle;
	private final SeekBar seek;

	public EditItemDialog(Activity activity, final Item item, final ItemModel model)
	{
		super(activity);
		this.item = item;
		this.model = model;
		setOwnerActivity(activity);
		setContentView(R.layout.edit_item_dialog);
		setTitle(item.getName());

		toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		toggle.setChecked(item.isEnabled());
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				model.setItemEnabled(item, isChecked);
			}
		});

		seek = (SeekBar) findViewById(R.id.seekBar1);
		seek.setMax(100);
		seek.setProgress((int) (item.getOpacity() * 100));
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				model.setItemOpacity(item, progress / 100f);
			}
		});

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (which == DialogInterface.BUTTON_POSITIVE)
				{
					model.removeItemAndParentsIfEmpty(item);
					dismiss();
				}
			}
		};
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener);

		ImageButton imageButton = (ImageButton) findViewById(R.id.deleteButton);
		imageButton.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialogBuilder.show();
			}
		});

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		model.addListener(this);
	}

	@Override
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		model.removeListener(this);
	}

	@Override
	public void itemEnabled(Item item, boolean enabled, boolean remote)
	{
		if (item == this.item)
		{
			toggle.setChecked(enabled);
		}
	}

	@Override
	public void itemOpacityChanged(Item item, float opacity, boolean remote)
	{
		if (item == this.item)
		{
			seek.setProgress((int) (opacity * 100));
		}
	}

	@Override
	public void itemRemoved(Item arg0, int[] arg1, boolean remote)
	{
	}

	@Override
	public void itemSelected(Item arg0, boolean remote)
	{
	}

	@Override
	public void itemsAdded(Item arg0, Item arg1, int arg2, boolean remote)
	{
	}

	@Override
	public void itemsRefreshed(Item arg0, boolean remote)
	{
	}
}
