package main;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import main.DataLoader.DayFilter;
import main.DataLoader.HourFilter;
import main.DataLoader.MonthFilter;
import main.DataLoader.YearFilter;

import data.RectilinearPixelPoly;
import data.Regions;

public class Main {

	public static final Logger log = Logger.getLogger(Main.class
			.getCanonicalName());

	public static final int K = 1500;
	static List<Point> randomReadings = new ArrayList<Point>();

	static YearFilter yf = new YearFilter();
	static MonthFilter mf = new MonthFilter();
	static DayFilter df = new DayFilter();
	static HourFilter hf = new HourFilter();

	static {
		yf.startYear = 2003;
		yf.endYear = 2003;
		mf.startMonth = 9;
		mf.endMonth = 10;
		df.startDay = 24;
		df.endDay = 31;
		hf.startHour = 11;
		hf.endHour = 17;
	}

	public static void main(String[] args) {

		Regions r = new Regions(new Dimension(50, 50));
		r.resetDataReadingCount();
		r.resetRegionUsage();

		// Random rand = new Random();
		// rand.setSeed(10);
		// for (int i = 0; i < 100; i++)
		// randomReadings.add(new Point(rand.nextInt(50), rand.nextInt(50)));

		// for (int x = 0; x < 3; x++)
		// for (int y = 0; y < 3; y++)
		// randomReadings.add(new Point(x, y));

		DataLoader loader = new DataLoader(DataLoader.TimeSlice.Day, yf, mf,
				df, hf);
		log.info("Generated Random Data Reading Locations");

		int cycle = 0;
		while (true) {

			log.info("Cycle is " + cycle++);
			printImage(r, cycle);

			// Add data readings
			r.resetDataReadingCount();
			// moveReadings(rand, randomReadings);
			// for (Point p : randomReadings)
			// r.addDataReading(p);
			loader.addPixels(r);

			log.info("Added data reading locations");

			// Order and reset usage
			r.orderRegions(OptimialityRanking);
			r.resetRegionUsage();
			log.info("Ordered regions and reset usage data");

			log.info("Running algorithm");
			runAlgorithm(r);

		}
	}

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

	public static void runAlgorithm(Regions reg) {
		List<RectilinearPixelPoly> regions = reg.getRegions();

		// We use a copy of the original list so that Java does not complain
		// about concurrent modification exceptions (our calls to regions inside
		// of this loop can end up modifying the list aka a split will modify
		// the list)
		List<RectilinearPixelPoly> regionsCopy = new ArrayList<RectilinearPixelPoly>(
				regions);

		for (RectilinearPixelPoly poly : regionsCopy) {

			if (poly.getIsUsed())
				continue;

			log.info("Next region: " + poly);

			int count = poly.getDataReadingCount();
			if (count == K) {
				log.info("Needs no attention");
				poly.setIsUsed(true);
				continue;
			}

			// We are not large enough, time to merge!
			if (count < K) {
				log.info("Needs to merge");
				List<RectilinearPixelPoly> neighbors = reg.findNeighbors(poly);

				// Remove all used neighbors
				Iterator<RectilinearPixelPoly> it = neighbors.iterator();
				while (it.hasNext())
					if (it.next().getIsUsed())
						it.remove();

				log.fine("Found " + neighbors.size() + " neighbors");
				Collections.sort(neighbors, OptimialityRanking);
				log.fine("Sorted neighbors");

				if (log.isLoggable(Level.FINEST))
					for (RectilinearPixelPoly p : neighbors)
						log.finest("\t" + p);

				int desiredChange = getDesiredChange(K - count, poly.getArea());
				log.fine("Needs to grow by " + desiredChange);

				for (RectilinearPixelPoly resource : neighbors) {
					log.finest("Considering Neighbor " + resource);

					if (resource.getDataReadingCount() + count <= K)
						desiredChange = resource.getArea();

					// If desired change is greater than or equal to the area
					// available, we can rest assured that the consumeArea
					// method will not fail
					if (desiredChange >= resource.getArea()) {
						Collection<Point> points = resource.consumeArea(
								desiredChange, null);
						poly.merge(points);
						desiredChange -= points.size();

						log.fine("Got " + points.size()
								+ " from neighbor, don't need more");
					} else {
						// If desired change is less than the area available, we
						// have to be sure the consumeArea method does not fail
						int consumedSoFar = 0;
						int origDesiredChange = desiredChange;
						do {

							Point start = poly.getStartPoint(resource);
							Collection<Point> consumable = null;
							if (start == null) {
								log
										.info("Unable to find a start point, so consuming entire polygon");
								log.info("This gives us " + resource.getArea()
										+ " pixels when we only wanted "
										+ desiredChange);
								consumable = resource.consumeArea(resource
										.getArea(), null);
							} else
								consumable = resource.consumeArea(
										desiredChange, start);
							consumedSoFar += consumable.size();
							poly.merge(consumable);
							desiredChange -= consumable.size();

						} while (consumedSoFar < origDesiredChange);

						log.fine("Got " + consumedSoFar
								+ " from neighbor, still needs "
								+ desiredChange);
					}

					// Mark the region we used as used
					resource.setIsUsed(true);

					// If we fully eat a poly we can shrink below our expected
					// change
					if (desiredChange <= 0)
						break;

				}

				poly.setIsUsed(true);

				continue;
			}

			// We are too large, time to shrink!
			// The K*2 is critical - you don't want to split if you are going to
			// cause a privacy invasion!
			if (count >= (K * 2)) {
				log.info("Needs to shrink");
				int partitions = getNumberOfPartitions(count);
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

				poly.setIsUsed(true);
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
	// TODO I can avoid dropping below K so rapidly by reducing the
	// aggressiveness of this approach, e.g. divide the result by 1.5 before
	// returning. Create a parameter for this!
	public static int getNumberOfPartitions(int realK) {
		if (realK < K)
			throw new IllegalStateException();

		double percentOfDesiredK = (double) realK / (double) K;
		int floor = (int) Math.floor(percentOfDesiredK / 1.1);
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

	static Comparator<RectilinearPixelPoly> OptimialityRanking = new Comparator<RectilinearPixelPoly>() {

		@Override
		public int compare(RectilinearPixelPoly o1, RectilinearPixelPoly o2) {
			int rc1 = o1.getDataReadingCount();
			int rc2 = o2.getDataReadingCount();

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
