package main;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import data.Regions;

public class DataLoader {

	public enum TimeSlice {
		Hour, Half_Hour, Quarter_Hour
	}

	/**
	 * Defines a start / end year to filter by. Both values are inclusive, e.g.
	 * if start is 2001, then year 2001 is included in the results
	 * 
	 */
	public class YearFilter {
		int startYear;
		int endYear;
	}

	/**
	 * Defines a start / end month to filter by. Both values are inclusive, e.g.
	 * if start is Jan, then month Jan is included in the results. The values
	 * should be retrieved from {@link java.util.Calendar}
	 * 
	 */
	public class MonthFilter {
		int startMonth;
		int endMonth;
	}

	/**
	 * Defines a start / end day to filter by. Both values are inclusive, e.g.
	 * if start is Monday, then day Monday is included in the results. The
	 * values should be retrieved from {@link java.util.Calendar}
	 * 
	 */
	public class DayFilter {
		int startDay;
		int endDay;
	}

	/**
	 * Defines a start / end hour to filter by. Both values are inclusive, e.g.
	 * if start is 11, then hour 11 is included in the results. These values are
	 * used for the {@link java.util.Calendar#HOUR_OF_DAY}, and should therefore
	 * be in [0,1,...,22,23]
	 * 
	 */
	public class HourFilter {
		int startHour;
		int endHour;
	}

	private TimeSlice mSliceSize;
	private GregorianCalendar mEndOfCurrentTimeSlice = null;
	private YearFilter mYearFilter;
	private MonthFilter mMonthFilter;
	private DayFilter mDayFilter;
	private HourFilter mHourFilter;
	private BufferedReader mInputFile;

	private GregorianCalendar mTempCalendar = new GregorianCalendar();
	
	private static final String dataFileName = "sorted_data_by_mysql.tsv";
	private static final int xScaleFactor = 104; // (822600-817400)/50
	private static final int xBase = 817400;
	private static final int yScaleFactor = 70; // (441400-437900)/50
	private static final int yBase = 437900;

	@SuppressWarnings("hiding")
	public DataLoader(TimeSlice sliceSlize, YearFilter yf,
			MonthFilter mf, DayFilter df, HourFilter hf) {
		mSliceSize = sliceSlize;

		try {
			mInputFile = new BufferedReader(new FileReader(dataFileName));
			//mInputFile.readLine(); // toss out the header
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("There was no information in the file!");
			e.printStackTrace();
			return;
		}

		GregorianCalendar temp = new GregorianCalendar();

		if (yf != null)
			mYearFilter = yf;
		else {
			mYearFilter = new YearFilter();
			mYearFilter.startYear = temp.getMinimum(Calendar.YEAR);
			mYearFilter.endYear = temp.getMaximum(Calendar.YEAR);
		}

		if (mf != null)
			mMonthFilter = mf;
		else {
			mMonthFilter = new MonthFilter();
			mMonthFilter.startMonth = temp.getMinimum(Calendar.MONTH);
			mMonthFilter.endMonth = temp.getMaximum(Calendar.MONTH);
		}

		if (df != null)
			mDayFilter = df;
		else {
			mDayFilter = new DayFilter();
			mDayFilter.startDay = temp.getMinimum(Calendar.DAY_OF_MONTH);
			mDayFilter.endDay = temp.getMaximum(Calendar.DAY_OF_MONTH);
		}

		if (hf != null)
			mHourFilter = hf;
		else {
			mHourFilter = new HourFilter();
			mHourFilter.startHour = temp.getMinimum(Calendar.HOUR_OF_DAY);
			mHourFilter.endHour = temp.getMaximum(Calendar.HOUR_OF_DAY);
		}

	}

	/**
	 * Reads data from the file, adding pixels as we go
	 * 
	 * @param r
	 * @return 0 if pixels were read and added, 1 if the end of the file was
	 *         reached (whether or not pixels were actually added is
	 *         unreported), -1 if there was an error (such as an IO exception)
	 */
	public int addPixels(Regions r) {
		String nextLine = null;

		while (true) {
			// Read in next line
			try {
				mInputFile.mark(200);
				nextLine = mInputFile.readLine();
				if (nextLine == null) {
					mInputFile.close();
					return 1;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			// Check filters
			String[] values = nextLine.split("\t");
			long timeStamp = Long.parseLong(values[0]) * 1000;
			if (passesTimeFilters(timeStamp) == false)
				continue;

			// Check current time slice
			if (isInCurrentTimeSlice(timeStamp) == false) {
				try {
					mInputFile.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
				updateCurrentTimeSlice();
				return 0;
			}
			
			// Add point :D
			double x = Double.parseDouble(values[2]) - xBase;
			double y = Double.parseDouble(values[3].trim()) - yBase;
			
			int xScaled = (int) (x / xScaleFactor);
			int yScaled = (int) (y / yScaleFactor);
			r.addDataReading(new Point(xScaled, yScaled));
			
		}

	}

	private void updateCurrentTimeSlice() {
		switch (mSliceSize) {
		case Hour:
			mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
			break;
		case Half_Hour:
			mEndOfCurrentTimeSlice.add(Calendar.MINUTE, 30);
			break;
		case Quarter_Hour:
			mEndOfCurrentTimeSlice.add(Calendar.MINUTE, 15);
			break;
		}
	}

	private boolean isInCurrentTimeSlice(long timeStamp) {
		mTempCalendar.setTimeInMillis(timeStamp);
		if (mEndOfCurrentTimeSlice == null) {
			switch (mSliceSize) {
			case Hour:
				mEndOfCurrentTimeSlice = new GregorianCalendar();
				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
				mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
				mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
				return true;
			case Half_Hour:
				mEndOfCurrentTimeSlice = new GregorianCalendar();
				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
				int min = mEndOfCurrentTimeSlice.get(Calendar.MINUTE);
				if (min < 30)
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 30);
				else {
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
					mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
				}
				return true;
			case Quarter_Hour:
				mEndOfCurrentTimeSlice = new GregorianCalendar();
				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
				min = mEndOfCurrentTimeSlice.get(Calendar.MINUTE);
				if (min < 15)
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 15);
				else if (min < 30)
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 30);
				else if (min < 45)
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 45);
				else {
					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
					mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
				}
				return true;
			}
		}

		if (mTempCalendar.before(mEndOfCurrentTimeSlice)
				|| mTempCalendar.equals(mEndOfCurrentTimeSlice))
			return true;
		return false;
	}

	/**
	 * Checks if the passed timestamp passes all filters
	 * 
	 * @param timeStamp
	 * @return
	 */
	private boolean passesTimeFilters(long timeStamp) {

		mTempCalendar.setTimeInMillis(timeStamp);

		int year = mTempCalendar.get(Calendar.YEAR);
		if (year < mYearFilter.startYear || year > mYearFilter.endYear)
			return false;

		int month = mTempCalendar.get(Calendar.MONTH);
		if (month < mMonthFilter.startMonth || month > mMonthFilter.endMonth)
			return false;

		int day = mTempCalendar.get(Calendar.DAY_OF_MONTH);
		if (day < mDayFilter.startDay || day > mDayFilter.endDay)
			return false;

		int hour = mTempCalendar.get(Calendar.HOUR_OF_DAY);
		if (hour < mHourFilter.startHour || hour > mHourFilter.endHour)
			return false;

		return true;
	}
}
