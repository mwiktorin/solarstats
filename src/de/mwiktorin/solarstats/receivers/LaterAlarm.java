package de.mwiktorin.solarstats.receivers;

import java.util.Calendar;

import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaterAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() + 3600 * 1000);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
//		System.out.println("next alarm: " + cal.getTime().toString());
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);
		Preferences.getInstance(context).putLong(context.getString(R.string.prefkey_reminderNextAlarm), cal.getTimeInMillis());
	}
}