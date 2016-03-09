package de.mwiktorin.solarstats.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.StartActivity;

public class AlarmReceiver extends BroadcastReceiver {

	public static final String SHOW_KEYBOARD = "showKeyboard";
	public static final int NOTIFICATION_ID = 1;
	private static final int EDIT_RESQUEST_CODE = 10;

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean reminderEnabled = Preferences.getInstance(context).getBoolean(context.getString(R.string.prefkey_enableReminder), false);
		if (!reminderEnabled) {
			return;
		}
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.notification);
		mBuilder.setContentTitle(context.getString(R.string.reminder_title));
		mBuilder.setContentText(context.getString(R.string.reminder_text));
		mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		mBuilder.setVibrate(new long[] { 2, 500, 500, 500 });
		mBuilder.setLights(Color.argb(255, 255, 228, 0), 1000, 2000);
		Intent startIntent = new Intent(context, StartActivity.class);
		startIntent.putExtra(SHOW_KEYBOARD, true);
		PendingIntent pIntent = PendingIntent.getActivity(context, EDIT_RESQUEST_CODE, startIntent, PendingIntent.FLAG_ONE_SHOT);
		mBuilder.addAction(R.drawable.ic_menu_edit, context.getString(R.string.reminder_edit), pIntent);
		mBuilder.addAction(R.drawable.ic_menu_recent_history, context.getString(R.string.reminder_later),
		        PendingIntent.getBroadcast(context, 0, new Intent(context, LaterAlarm.class), PendingIntent.FLAG_UPDATE_CURRENT));
		mBuilder.setContentIntent(pIntent);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		Preferences.getInstance(context).putLong(Preferences.LAST_ALARM,
		        Preferences.getInstance(context).getLong(context.getString(R.string.prefkey_reminderNextAlarm), 0));

		Utils.updateNextAlarm(context);
	}
}