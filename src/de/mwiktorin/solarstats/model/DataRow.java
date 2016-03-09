package de.mwiktorin.solarstats.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;


public class DataRow implements Parcelable{
	
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public final static SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

	private Date date;
	private int value;
	private long id;

	public DataRow(Date date, int value, long id) {
		init(date, value, id);
	}
	
	/**
	 * @param date Date in format "yyyy-MM-dd"
	 */
	public DataRow(String date, int value, long id){
		try {
	        init(DATE_FORMAT.parse(date), value, id);
        } catch (ParseException e) {
	        e.printStackTrace();
        }
	}

	private void init(Date date, int value, long id) {
		this.date = date;
		this.value = value;
		this.id = id;
	}

	/**
	 * @return date in format "dd.MM.yyyy"
	 */
	public String getStringDate() {
		return DATE_FORMAT_STRING.format(date);
	}
	
	/**
	 * @return date in format "yyyy-MM-dd"
	 */
	public String getDatabaseDate() {
		return DATE_FORMAT.format(date);
	}
	
	public Date getDate() {
	    return date;
    }
	
	public int getValue() {
		return value;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * @param date Date in format "yyyy-MM-dd"
	 */
	public void setDate(String date){
		try {
	        this.date = DATE_FORMAT.parse(date);
        } catch (ParseException e) {
	        e.printStackTrace();
        }
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public long getId() {
	    return id;
    }
	
	public void setId(long id) {
	    this.id = id;
    }
	
	public boolean isBefore(DataRow row){
		return date.before(row.date);
	}
	
	/**
	 * !! NOTE: this checks only the date field !!
	 */
	@Override
	public boolean equals(Object o) {
	    DataRow row = (DataRow) o;
	    if(row == null)
	    	return false;
	    return this.date.equals(row.date);
	}
	
	@Override
	public String toString() {
	    return DATE_FORMAT_STRING.format(date) + ": " + value + " id: " + id;
	}
	
	//PARCEL:
	public DataRow(Parcel in){
		long[] array = new long[3];
		in.readLongArray(array);
		date = new Date(array[0]);
		value = (int) array[1];
		id = array[2];
	}

	@Override
    public int describeContents() {
	    return 0;
    }

	@Override
    public void writeToParcel(Parcel dest, int flags) {
		dest.writeLongArray(new long[] { date.getTime(), value, id });
    }
	
	public static final Parcelable.Creator<DataRow> CREATOR = new Parcelable.Creator<DataRow>(){

		@Override
        public DataRow createFromParcel(Parcel in) {
	        return new DataRow(in);
        }

		@Override
        public DataRow[] newArray(int size) {
	        return new DataRow[size];
        }
		
	};

}
