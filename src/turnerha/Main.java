package turnerha;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import turnerha.data.DataLoader;
import turnerha.data.DataLoader.DayFilter;
import turnerha.data.DataLoader.HourFilter;
import turnerha.data.DataLoader.MonthFilter;
import turnerha.data.DataLoader.YearFilter;
import turnerha.polygon.RectilinearPixelPoly;
import turnerha.region.Region;
import turnerha.region.Regions;

public class Main {

	public static final int K = 10;
	public static final int regionXsize = 500;
	public static final int regionYsize = 500;

	public static final Logger log = Logger.getLogger(Main.class
			.getCanonicalName());
	static List<Point> randomReadings = new ArrayList<Point>();
	static YearFilter yf = new YearFilter();
	static MonthFilter mf = new MonthFilter();
	static DayFilter df = new DayFilter();
	static HourFilter hf = new HourFilter();
	static long sliceUsed = 15 * 60;

	/**
	 * This is a multiplier used on the desired K value before checking if a
	 * region should be split. A region should be split if it's K value is
	 * greater than or equal to the safety-margin-adjusted desired k value. To
	 * avoid a large number of privacy invasions (e.g. regions with a K between
	 * 1...desired K - 1) this should be at least 2. Splitting a region into
	 * equal parts when that region does not have at least 2x the number of
	 * minimally required readings implies that (if the number of incoming
	 * readings stay fairly constant) the next cycle will cause a privacy
	 * invasion in at least one of those readings
	 */
	private static double SAFETY_MARGIN = 3.0;

	static {
		yf.startYear = 2003;
		mf.startMonth = 9;
		df.startDay = 24;

		yf.endYear = 2003;
		mf.endMonth = 10;
		df.endDay = 31;

		hf.startHour = 12;
		hf.endHour = 18;
	}

	public static void main(String[] args) {

		
		
		Regions r = new Regions(new Dimension(regionXsize, regionYsize));
		r.resetUniqueUsersSeen();
		r.resetRegionUsage();

		// Random rand = new Random();
		// rand.setSeed(10);
		// for (int i = 0; i < 100; i++)
		// randomReadings.add(new Point(rand.nextInt(50), rand.nextInt(50)));

		// for (int x = 0; x < 3; x++)
		// for (int y = 0; y < 3; y++)
		// randomReadings.add(new Point(x, y));

		FileWriter outFile = getOutFileWriter();
		int totalRegionCount = 0;

		DataLoader loader = new DataLoader(sliceUsed, yf, mf, df, hf,
				regionXsize, regionYsize);
		// DataLoader loader = new DataLoader(sliceUsed, null, null, null, null,
		// regionXsize, regionYsize);

		log.info("Generated Random Data Reading Locations");

		int cycle = 0;
		while (true) {

			log.info("Cycle is " + cycle++);
			printImage(r, cycle);

			// Add data readings
			r.resetUniqueUsersSeen();
			r.resetDataReadingCounts();
			int result = loader.addPixels(r);
			if (result == -1) {
				log.severe("Done reading data file");
				break;
			} else if (result == 0) {
				log.info("No data points entered");
				continue;
			}
			log.info("Added data reading locations");

			// Order and reset usage
			r.orderRegions(OptimialityRanking);
			r.resetRegionUsage();
			log.info("Ordered regions and reset usage data");

			log.info("Running algorithm");
			runAlgorithm(r);

			// Write out results
			totalRegionCount += r.getRegions().size();
			try {
				outFile.write(Long.toString(loader.getCurrentTimesliceStart()));
				outFile.write(",");
				outFile.write(Long.toString(loader.getCurrentTimesliceEnd()));
				outFile.write(",");
				StringBuilder b = new StringBuilder();
				for (Region reg : r.getRegions()) {
					b.append(reg.getUniqueUsersCount()).append('|').append(
							reg.getDataReadingCount()).append('|');
					b.append(
							((RectilinearPixelPoly) reg.getPolyImpl())
									.getArea()).append('>');
				}
				b.deleteCharAt(b.length() - 1);
				b.append('\n');

				outFile.write(b.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.severe("Total Region Count: " + totalRegionCount);
		// Finish up the output file
		try {
			outFile.close();

			RandomAccessFile f = new RandomAccessFile(new File(getFileName()),
					"rw");
			while (f.readLine().equals("# Total Region Count:") == false)
				;
			f.writeBytes("# ");
			f.writeBytes(Integer.toString(totalRegionCount));
			f.writeChar('\n');
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getFileName() {
		String filename = "k-distributions/" + K + "in";
		filename += sliceUsed;
		filename += "sec";
		filename += "-" + regionXsize + "x" + regionYsize;
		filename += ".csv";
		return filename;
	}

	@SuppressWarnings("deprecation")
	private static FileWriter getOutFileWriter() {

		String filename = getFileName();

		try {
			FileWriter fw = new FileWriter(filename);
			fw.write("# From " + df.startDay + "/" + mf.startMonth + "/"
					+ yf.startYear + " to " + df.endDay + "/" + mf.endMonth
					+ "/" + yf.endYear);
			fw.write("\n# And " + hf.startHour + ":00 to " + hf.endHour
					+ ":00\n");
			GregorianCalendar time = new GregorianCalendar();
			time.setTimeInMillis(System.currentTimeMillis());
			fw.write("# Executed at " + time.getTime().toLocaleString() + "\n");
			fw.write("# Using " + regionXsize + "x" + regionYsize + "\n");
			fw.write("# Desired K: " + K + "\n# \n");
			fw
					.write("# Format: \n# 	Time start, Time end, Regions\n"
							+ "# Region Format:\n# 	unique users | reading count"
							+ " | area > <Next Region>\n# \n# Total Region Count:\n# ##################\n\n");
			return fw;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unused")
	private static void moveReadings(Random rand, List<Point> randomReadings2) {
		for (Point p : randomReadings2)
			if (rand.nextBoolean()) {
				// Move east west
				if (rand.nextBoolean())
					p.x = Math.min(p.x + rand.nextInt(5), 49);
				else
					p.x = Math.max(p.x - rand.nextInt(5), 0);
			} else {
				// Move east west
				if (rand.nextBoolean())
					p.y = Math.min(p.y + rand.nextInt(5), 49);
				else
					p.y = Math.max(p.y - rand.nextInt(5), 0);
			}
	}

	private static void printImage(Regions r, int cycle) {
		BufferedImage bi = r.getImage();
		StringBuilder name = new StringBuilder("images/cycle");
		if (cycle < 10)
			name.append("00").append(cycle).append(".png");
		else if (cycle > 9 && cycle < 100)
			name.append("0").append(cycle).append(".png");
		else
			name.append(cycle).append(".png");

		try {
			File f = new File(name.toString());
			ImageIO.write(bi, "png", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAlgorithm(Regions regions) {
		List<Region> regionsList = regions.getRegions();

		// We use a copy of the original list so that Java does not complain
		// about concurrent modification exceptions (our calls to regions inside
		// of this loop can end up modifying the list aka a split will modify
		// the list)
		List<Region> regionsCopy = new ArrayList<Region>(regionsList);

		for (Region region : regionsCopy) {

			if (region.getIsUsed())
				continue;

			RectilinearPixelPoly regionPolygon = (RectilinearPixelPoly) region
					.getPolyImpl();

			log.info("Next region: " + region);

			int count = region.getUniqueUsersCount();
			if (count == K) {
				log.info("Needs no attention");
				region.setIsUsed(true);
				continue;
			}

			// We are not large enough, time to merge!
			if (count < K) {
				log.info("Needs to merge");
				List<Region> neighbors = regions.findNeighborsOf(region);

				// Remove all used neighbors
				Iterator<Region> it = neighbors.iterator();
				while (it.hasNext())
					if (it.next().getIsUsed())
						it.remove();

				log.fine("Found " + neighbors.size() + " neighbors");
				Collections.sort(neighbors, OptimialityRanking);
				log.fine("Sorted neighbors");

				if (log.isLoggable(Level.FINEST))
					for (Region p : neighbors)
						log.finest("\t" + p);

				int desiredChange = getDesiredChange(K - count, regionPolygon
						.getArea());
				log.fine("Needs to grow by " + desiredChange);

				for (Region neighbor : neighbors) {
					log.finest("Considering Neighbor " + neighbor);
					RectilinearPixelPoly neighborPoly = (RectilinearPixelPoly) neighbor
							.getPolyImpl();

					if (neighbor.getUniqueUsersCount() + count <= K)
						desiredChange = neighborPoly.getArea();

					// If desired change is greater than or equal to the area
					// available, we can rest assured that the consumeArea
					// method will not fail
					if (desiredChange >= neighborPoly.getArea()) {
						Collection<Point> points = neighborPoly.consumeArea(
								desiredChange, null);
						regionPolygon.merge(points);
						desiredChange -= points.size();

						log.fine("Got " + points.size()
								+ " from neighbor, don't need more");
					} else {
						// If desired change is less than the area available, we
						// have to be sure the consumeArea method does not fail
						int consumedSoFar = 0;
						int origDesiredChange = desiredChange;
						do {

							Point start = regionPolygon
									.getStartPoint(neighborPoly);
							Collection<Point> consumable = null;
							if (start == null) {
								log
										.info("Unable to find a start point, so consuming entire polygon");
								log.info("This gives us "
										+ neighborPoly.getArea()
										+ " pixels when we only wanted "
										+ desiredChange);
								consumable = neighborPoly.consumeArea(
										neighborPoly.getArea(), null);
							} else
								consumable = neighborPoly.consumeArea(
										desiredChange, start);
							consumedSoFar += consumable.size();
							regionPolygon.merge(consumable);
							desiredChange -= consumable.size();

						} while (consumedSoFar < origDesiredChange);

						log.fine("Got " + consumedSoFar
								+ " from neighbor, still needs "
								+ desiredChange);
					}

					// Mark the region we used as used
					neighbor.setIsUsed(true);

					// If we fully eat a poly we can shrink below our expected
					// change
					if (desiredChange <= 0)
						break;

				}

				region.setIsUsed(true);

				continue;
			}

			// We are too large, time to shrink!
			if (count >= ((double) K * SAFETY_MARGIN)) {
				log.info("Needs to shrink");
				int partitions = getNumberOfPartitions(count);
				RectilinearPixelPoly poly = (RectilinearPixelPoly) region
						.getPolyImpl();
				int areaPerPartition = poly.getArea() / partitions;
				log
						.fine("Needs to be cut into " + partitions
								+ " partitions with " + areaPerPartition
								+ " area each");
				if (poly.getArea() == 0) {
					log.severe("Not sure why, but this poly has an area of 0");
					areaPerPartition = 1;
				}
				if (areaPerPartition == 0) {
					log
							.severe("There were so many data readings that we could have fractions of pixels and still meet k-anonymity. For now we are using the finest granularity possible (e.g. 1 pixel) and having k values higher than desired. ");
					areaPerPartition = 1;
				}
				while (partitions != 1) {
					poly.split(areaPerPartition);
					--partitions;
				}

				region.setIsUsed(true);
			}

		} // End r.hasNext

	}

	/**
	 * Given a value of for data readings larger than the desired K, this will
	 * return the number of equally-sized partitions that the region should be
	 * separated into to promote all divisions having close to K in the next
	 * iteration.
	 * 
	 * @param realK
	 * @return
	 */
	public static int getNumberOfPartitions(int realK) {
		if (realK < K)
			throw new IllegalStateException();

		double percentOfDesiredK = (double) realK / (double) (K * 3);
		int floor = (int) Math.floor(percentOfDesiredK);
		return floor;
	}

	public static int getDesiredChange(int differenceFromK, int currentArea) {
		// For now it's just a simple scaling
		if (differenceFromK > 0) {
			double diffPerct = (double) differenceFromK / (double) K;
			diffPerct += 1;

			double finalArea = (double) currentArea * diffPerct;
			double change = finalArea - currentArea;
			return (int) change;
		}

		if (differenceFromK < 0) {
			int total = -1 * differenceFromK + K;
			double diffPerct = (double) K / (double) total;

			double finalArea = (double) currentArea * (diffPerct);
			double change = currentArea - finalArea;
			return Math.max((int) change, 0);
		}

		throw new IllegalStateException();
	}

	static Comparator<Region> OptimialityRanking = new Comparator<Region>() {

		@Override
		public int compare(Region o1, Region o2) {
			int rc1 = o1.getUniqueUsersCount();
			int rc2 = o2.getUniqueUsersCount();

			if (rc1 < K && rc2 > K)
				return -1;
			else if (rc2 < K && rc1 > K)
				return 1;
			else if (rc2 == rc1)
				return 0;
			else if (rc1 < K && rc2 == K)
				return -1;
			else if (rc1 > K && rc2 == K)
				return -1;
			else if (rc2 < K && rc1 == K)
				return 1;
			else if (rc2 > K && rc1 == K)
				return 1;

			// At this point they are either both >K or both <K, but not equal

			// Are they <K
			if (rc1 < K) {
				if (rc1 < rc2)
					return -1;
				else
					return 1;
			}

			// Are they >K
			if (rc1 > K) {
				if (rc1 < rc2)
					return 1;
				else
					return -1;
			}

			throw new IllegalStateException();
		}
	};

}
