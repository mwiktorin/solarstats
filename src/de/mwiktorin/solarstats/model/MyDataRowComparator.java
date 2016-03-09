package de.mwiktorin.solarstats.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;


public class MyDataRowComparator implements Comparator<DataRow> {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	
	@Override
    public int compare(DataRow lhs, DataRow rhs) {
	    if(lhs.getStringDate().equals(rhs.getStringDate())) {
	    	return 0;
	    }
	    try {
	        if(DATE_FORMAT.parse(lhs.getStringDate()).before(DATE_FORMAT.parse(rhs.getStringDate()))){
	        	return -1;
	        } else {
	        	return 1;
	        }
        } catch (ParseException e) {
	        e.printStackTrace();
	        return 0;
        }
    }

}
