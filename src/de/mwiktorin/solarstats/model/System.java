package de.mwiktorin.solarstats.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class System implements Parcelable {

	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public final static SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

	private long id;
	private String name;
	private Date date;
	private int peak;
	private double payment;
	private int color;

	public System(long id, String name, Date date, int peak, double payment, int color) {
		this.id = id;
		this.name = name;
		this.date = date;
		this.peak = peak;
		this.payment = payment;
		this.color = color;
	}

	public System(long id, String name, String date, int peak, double payment, int color) {
		try {
			this.id = id;
			this.name = name;
			if (date != null && !date.equals("")) {
				this.date = DATE_FORMAT.parse(date);
			}
			this.peak = peak;
			this.payment = payment;
			this.color = color;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public void setDate(Date date) {
		this.date = date;
	}

	public int getPeak() {
		return peak;
	}

	public void setPeak(int peak) {
		this.peak = peak;
	}

	public double getPayment() {
		return payment;
	}

	public void setPayment(double payment) {
		this.payment = payment;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public boolean equals(Object o) {
		return ((System) o).getId() == id;
	}

	@Override
	public String toString() {
		return name;
	}

	// PARCEL:
	public System(Parcel in) {
		try {
			String[] array = new String[5];
			in.readStringArray(array);
			id = Long.parseLong(array[0]);
			name = array[1];
			date = DATE_FORMAT.parse(array[2]);
			peak = Integer.parseInt(array[3]);
			payment = Double.parseDouble(array[4]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { id + "", name, DATE_FORMAT.format(date), peak + "", payment + "" });
	}

	public static final Parcelable.Creator<System> CREATOR = new Parcelable.Creator<System>() {

		@Override
		public System createFromParcel(Parcel in) {
			return new System(in);
		}

		@Override
		public System[] newArray(int size) {
			return new System[size];
		}

	};

}
