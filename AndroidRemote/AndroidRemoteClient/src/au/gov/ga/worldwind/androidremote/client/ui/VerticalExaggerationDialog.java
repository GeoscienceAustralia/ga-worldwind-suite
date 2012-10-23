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
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.ve.VerticalExaggerationMessage;

/**
 * Dialog that provides the user with the ability to change the vertical
 * exaggeration of the server's model. The exaggeration is kept in sync with the
 * server; if the server side value changes, so does the slider.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class VerticalExaggerationDialog extends Dialog implements CommunicatorListener
{
	private final Communicator communicator;
	private final SeekBar seek;
	private boolean settingFromMessage = false;

	public VerticalExaggerationDialog(Activity activity, final float exaggeration, final Communicator communicator)
	{
		super(activity);
		this.communicator = communicator;
		setOwnerActivity(activity);
		setContentView(R.layout.vertical_exaggeration_dialog);
		setTitle(R.string.vertical_exaggeration);

		final TextView text = (TextView) findViewById(R.id.veText);
		setText(text, exaggeration);

		seek = (SeekBar) findViewById(R.id.veBar);
		seek.setMax(2000);
		seek.setProgress(exaggerationToSlider(exaggeration));
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
				float exaggeration = sliderToExaggeration(progress);
				setText(text, exaggeration);
				if (!settingFromMessage)
				{
					communicator.sendMessage(new VerticalExaggerationMessage(exaggeration));
				}
			}
		});

		Button ve1 = (Button) findViewById(R.id.veButton1);
		Button ve2 = (Button) findViewById(R.id.veButton2);
		Button ve5 = (Button) findViewById(R.id.veButton5);
		Button ve10 = (Button) findViewById(R.id.veButton10);
		ve1.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				seek.setProgress(exaggerationToSlider(1));
			}
		});
		ve2.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				seek.setProgress(exaggerationToSlider(2));
			}
		});
		ve5.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				seek.setProgress(exaggerationToSlider(5));
			}
		});
		ve10.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				seek.setProgress(exaggerationToSlider(10) + 1);
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

	private void setText(TextView text, float exaggeration)
	{
		int dp = exaggeration < 1 ? 2 : exaggeration < 10 ? 1 : 0;
		text.setText(String.format("%." + dp + "f", exaggeration) + "x");
	}

	private static int exaggerationToSlider(float exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100d - y) / 100d);
		return (int) Math.round(x * 1000d);
	}

	private static float sliderToExaggeration(int slider)
	{
		double x = slider / 1000d;
		double y = (Math.pow(10d, x + 2) - 100d) / 99d;
		return (float) y;
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		communicator.addListener(this);
	}

	@Override
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		communicator.removeListener(this);
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof VerticalExaggerationMessage)
		{
			settingFromMessage = true;
			VerticalExaggerationMessage vem = (VerticalExaggerationMessage) message;
			seek.setProgress(exaggerationToSlider(vem.exaggeration));
			settingFromMessage = false;
		}
	}

	@Override
	public void stateChanged(State state)
	{
	}
}
