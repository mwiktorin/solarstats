package de.mwiktorin.solarstats.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.mwiktorin.solarstats.Utils;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Utils.updateNextAlarm(context);
	}
}