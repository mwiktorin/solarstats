package de.mwiktorin.solarstats;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	
	public static final String DIAGRAMM_SELECTED_TIME_ENDYEAR = "time_endyear";
	public static final String DIAGRAMM_SELECTED_TIME_STARTYEAR = "time_startyear";
	public static final String DIAGRAMM_SELECTED_TIME_YEAR2 = "time_year2_spinner";
	public static final String DIAGRAMM_SELECTED_TIME_YEAR1 = "time_year1_spinner";
	public static final String DIAGRAMM_SELECTED_TIME_ENDDATE = "time_enddate_spinner";
	public static final String DIAGRAMM_SELECTED_TIME_STARTDATE = "time_startdate_spinner";
	public static final String DIAGRAMM_SELECTED_TIME_YEAR = "time_year_spinner_pos";
	public static final String DIAGRAMM_SELECTED_TIME_MONTH = "time_month_spinner_pos";
	public static final String DIAGRAMM_SELECTED_CATEGRORY = "category_spinner_pos";
	public static final String FIRST_START = "first_start";
	public static final String LAST_VERSION = "last_version";
	public static final String LAST_ALARM = "last_alarm";
	public static final String CURRENT_SYSTEM_ID = "current_system_id";

	private static Preferences instance;
	private SharedPreferences prefs;
	
	private Preferences(Context context) {
	    this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
	
	public static Preferences getInstance(Context context){
		if(instance == null){
			instance = new Preferences(context);
		}
		return instance;
	}
	
	public boolean getBoolean(String key, boolean defValue){
		return prefs.getBoolean(key, defValue);
	}
	
	public int getInt(String key, int defValue){
		return prefs.getInt(key, defValue);
	}
	
	public String getString(String key, String defValue){
		return prefs.getString(key, defValue);
	}
	
	public long getLong(String key, long defValue){
		return prefs.getLong(key, defValue);
	}
	
	public void putBoolean(String key, boolean value){
		prefs.edit().putBoolean(key, value).commit();
	}
	
	public void putInt(String key, int value){
		prefs.edit().putInt(key, value).commit();
	}
	
	public void putString(String key, String value){
		prefs.edit().putString(key, value).commit();
	}
	
	public void putLong(String key, long value){
		prefs.edit().putLong(key, value).commit();
	}
	
	public void deleteAllDiagrammSettings() {
		putString(DIAGRAMM_SELECTED_TIME_ENDYEAR, "");
		putString(DIAGRAMM_SELECTED_TIME_STARTYEAR, "");
		putString(DIAGRAMM_SELECTED_TIME_YEAR2, "");
		putString(DIAGRAMM_SELECTED_TIME_YEAR1, "");
		putString(DIAGRAMM_SELECTED_TIME_ENDDATE, "");
		putString(DIAGRAMM_SELECTED_TIME_STARTDATE, "");
		putInt(DIAGRAMM_SELECTED_TIME_YEAR, 0);
		putInt(DIAGRAMM_SELECTED_TIME_MONTH, 0);
		putInt(DIAGRAMM_SELECTED_CATEGRORY, 0);
	}
}
