package de.mwiktorin.solarstats.model;

import java.util.Date;

public class DateCount {

	private Date date;
	private int count;

	public DateCount(Date date) {
		this.date = date;
		count = 1;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public void incrementCount(){
		this.count++;
	}
	
	public void incrementCount(int n){
		this.count += n;
	}

	/**
	 * NOTE: Only checks the date field!
	 */
	@Override
	public boolean equals(Object o) {
		return ((DateCount) o).getDate().equals(this.date);
	}
	
	@Override
	public String toString() {
	    return "Date: " + date.toString() + " count: " + count;
	}

}
