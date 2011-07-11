package turnerha.data;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import turnerha.region.Regions;

public class DataLoader {

	/**
	 * Defines a start / end year to filter by. Both values are inclusive, e.g.
	 * if start is 2001, then year 2001 is included in the results
	 * 
	 */
	public static class YearFilter {
		public int startYear;
		public int endYear;
	}

	/**
	 * Defines a start / end month to filter by. Both values are inclusive, e.g.
	 * if start is Jan, then month Jan is included in the results. The values
	 * should be retrieved from {@link java.util.Calendar}
	 * 
	 */
	public static class MonthFilter {
		public int startMonth;
		public int endMonth;
	}

	/**
	 * Defines a start / end day to filter by. Both values are inclusive, e.g.
	 * if start is Monday, then day Monday is included in the results. The
	 * values should be retrieved from {@link java.util.Calendar}
	 * 
	 */
	public static class DayFilter {
		public int startDay;
		public int endDay;
	}

	/**
	 * Defines a start / end hour to filter by. Both values are inclusive, e.g.
	 * if start is 11, then hour 11 is included in the results. These values are
	 * used for the {@link java.util.Calendar#HOUR_OF_DAY}, and should therefore
	 * be in [0,1,...,22,23]
	 * 
	 */
	public static class HourFilter {
		public int startHour;
		public int endHour;
	}

	private GregorianCalendar mEndOfCurrentTimeSlice = null;
	private HourFilter mHourFilter;
	private GregorianCalendar mRangeStart;
	private GregorianCalendar mRangeEnd;
	private BufferedReader mInputFile;
	private long mSliceLength;

	private GregorianCalendar mTempCalendar = new GregorianCalendar();

	private static final String dataFileName = "sorted_data_by_mysql.tsv";
	private static int xScaleFactor = 104; // (822600-817400)/50
	private static final int xBase = 817400;
	private static final double xRange = 822600 - 817400;
	private static int yScaleFactor = 70; // (441400-437900)/50
	private static final int yBase = 437900;
	private static final double yRange = 441400 - 437900;

	/**
	 * Note that the passed {@link HourFilter} is substantially different than
	 * the year, month, or day filters. The year, month, or day filters set a
	 * start and end range, which filters everything not in that range. The hour
	 * filter, on the other hand, determines the hours that are allowed for each
	 * day within the y/m/d range. For example, you cannot define 1PM on May 1st
	 * 2011 to 7PM on May 7th 2011, as that is not how the start and end hours
	 * work. You can specify the date range May 1 to May 7, and for every day in
	 * that range the hours 1PM to 7PM will be allowed through the filters
	 * 
	 * @param sliceSize
	 *            Defines the length that each slice of time should be divided
	 *            into, with seconds as the unit. The first slice will start on
	 *            the hour of the hourfilter, and will end at the start of the
	 *            hour plus this amount of time
	 * 
	 * @param yf
	 * @param mf
	 * @param df
	 * @param hf
	 */
	@SuppressWarnings("hiding")
	public DataLoader(long sliceSize, YearFilter yf, MonthFilter mf,
			DayFilter df, HourFilter hf, int desiredWidth, int desiredHeight) {
		mSliceLength = sliceSize;

		yScaleFactor = (int) Math.ceil(yRange / (double) desiredHeight);
		xScaleFactor = (int) Math.ceil(xRange / (double) desiredWidth);

		try {
			mInputFile = new BufferedReader(new FileReader(dataFileName));
			// mInputFile.readLine(); // toss out the header
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("There was no information in the file!");
			e.printStackTrace();
			return;
		}

		GregorianCalendar temp = new GregorianCalendar();

		YearFilter yearFilter = null;
		if (yf != null)
			yearFilter = yf;
		else {
			yearFilter = new YearFilter();
			yearFilter.startYear = temp.getMinimum(Calendar.YEAR);
			yearFilter.endYear = temp.getLeastMaximum(Calendar.YEAR);
		}

		MonthFilter monthFilter = null;
		if (mf != null)
			monthFilter = mf;
		else {
			monthFilter = new MonthFilter();
			monthFilter.startMonth = temp.getMinimum(Calendar.MONTH);
			monthFilter.endMonth = temp.getMaximum(Calendar.MONTH);
		}

		DayFilter dayFilter = null;
		if (df != null)
			dayFilter = df;
		else {
			dayFilter = new DayFilter();
			dayFilter.startDay = temp.getMinimum(Calendar.DAY_OF_MONTH);
			dayFilter.endDay = temp.getMaximum(Calendar.DAY_OF_MONTH);
		}

		mRangeStart = new GregorianCalendar(0, 0, 0, 0, 0, 0);
		mRangeStart.set(Calendar.YEAR, yearFilter.startYear);
		mRangeStart.set(Calendar.MONTH, monthFilter.startMonth);
		mRangeStart.set(Calendar.DAY_OF_MONTH, dayFilter.startDay);

		mRangeEnd = new GregorianCalendar(0, 0, 0, 0, 0, 0);
		mRangeEnd.set(Calendar.YEAR, yearFilter.endYear);
		mRangeEnd.set(Calendar.MONTH, monthFilter.endMonth);
		mRangeEnd.set(Calendar.DAY_OF_MONTH, dayFilter.endDay);
		mRangeEnd.getTimeInMillis();

		if (hf != null)
			mHourFilter = hf;
		else {
			mHourFilter = new HourFilter();
			mHourFilter.startHour = temp.getMinimum(Calendar.HOUR_OF_DAY);
			mHourFilter.endHour = temp.getMaximum(Calendar.HOUR_OF_DAY);
		}

		advanceInputFileToFirstLine();

	}

	/**
	 * Skips the input buffer ahead by large ranges.
	 */
	private void advanceInputFileToFirstLine() {
		int approxCharsPerLine = 60;
		int numberOfLines = 100000;

		while (true) {
			try {
				mInputFile.mark((approxCharsPerLine + 15) * numberOfLines);
				mInputFile.skip((approxCharsPerLine) * numberOfLines);
				mInputFile.readLine(); // Toss out the (potentially incomplete)
				// line
				String line = mInputFile.readLine();
				if (line == null)
					return;

				long time = Long.parseLong(line.split("\t")[0]) * 1000;
				mTempCalendar.setTimeInMillis(time);
				if (passesDayMonthYearTimeFilters(time)) {
					mInputFile.reset();
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private boolean passesDayMonthYearTimeFilters(long timeStamp) {
		mTempCalendar.setTimeInMillis(timeStamp);

		if (mTempCalendar.compareTo(mRangeStart) < 0
				|| mTempCalendar.compareTo(mRangeEnd) > 0)
			return false;

		return true;
	}

	/**
	 * Reads data from the file, adding pixels as we go
	 * 
	 * @param r
	 * @return -1 if the process is complete (due to either an error, the EOF,
	 *         or the end of data in our filter window), or the number of data
	 *         readings added (not the number of unique users seen)
	 */
	public int addPixels(Regions r) {
		String nextLine = null;
		int valueCt = 0;

		while (true) {
			// Read in next line
			try {
				mInputFile.mark(200);
				nextLine = mInputFile.readLine();
				if (nextLine == null) {
					mInputFile.close();
					return -1;
				}
			} catch (IOException e) {
				e.printStackTrace();
				valueCt = 0;
				return -1;
			}

			// Check current time slice
			String[] values = nextLine.split("\t");
			long timeStamp = Long.parseLong(values[0]) * 1000;
			if (isInCurrentTimeSlice(timeStamp) == false) {
				try {
					mInputFile.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
				updateCurrentTimeSlice();
				return valueCt;
			}

			// Check filters
			if (passesDayMonthYearTimeFilters(timeStamp) == false) {
				// Are we before or after
				mTempCalendar.setTimeInMillis(timeStamp);
				if (mTempCalendar.after(mRangeEnd)) {
					return -1;
				} else
					continue;
			}

			if (passesHourFilter(timeStamp) == false)
				continue;

			// Add point :D
			double x = Double.parseDouble(values[2]) - xBase;
			double y = Double.parseDouble(values[3].trim()) - yBase;

			int xScaled = (int) (x / xScaleFactor);
			int yScaled = (int) (y / yScaleFactor);
			r.addDataReading(new Point(xScaled, yScaled), values[1]);

			valueCt++;
		}

	}

	public long getCurrentTimesliceStart() {

		final long curEnd = mEndOfCurrentTimeSlice.getTimeInMillis();
		final long start = curEnd - mSliceLength * 1000;
		return start;
	}

	public long getCurrentTimesliceEnd() {
		return mEndOfCurrentTimeSlice.getTimeInMillis();
	}

	private void updateCurrentTimeSlice() {
		long updated = mEndOfCurrentTimeSlice.getTimeInMillis();
		updated += mSliceLength * 1000;

		mEndOfCurrentTimeSlice.setTimeInMillis(updated);
	}

	private boolean isInCurrentTimeSlice(long timeStamp) {
		mTempCalendar.setTimeInMillis(timeStamp);
		if (mEndOfCurrentTimeSlice == null) {
			mEndOfCurrentTimeSlice = new GregorianCalendar();
			mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
			mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
			mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
			mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
			updateCurrentTimeSlice();
			return true;
		}

		if (mTempCalendar.before(mEndOfCurrentTimeSlice)
				|| mTempCalendar.equals(mEndOfCurrentTimeSlice))
			return true;
		return false;
	}

	/**
	 * Checks if the passed timestamp passes the hour filter
	 * 
	 * @param timeStamp
	 * @return
	 */
	private boolean passesHourFilter(long timeStamp) {

		mTempCalendar.setTimeInMillis(timeStamp);

		int hour = mTempCalendar.get(Calendar.HOUR_OF_DAY);
		if (hour < mHourFilter.startHour || hour > mHourFilter.endHour)
			return false;

		if (hour == mHourFilter.endHour) {
			int min = mTempCalendar.get(Calendar.MINUTE);
			int sec = mTempCalendar.get(Calendar.SECOND);
			int mill = mTempCalendar.get(Calendar.MILLISECOND);
			if (min != 0 || sec != 0 || mill != 0)
				return false;
		}

		return true;
	}
}
