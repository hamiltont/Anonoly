package turnerha.region;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import turnerha.polygon.RectilinearPixelPoly;

public class Environment {
	List<Region> mRegions = new ArrayList<Region>();

	Rectangle mBorder;

	public Environment(Dimension totalArea) {
		Set<Point> startingPoints = new HashSet<Point>(totalArea.width
				* totalArea.height);
		for (int x = 0; x < totalArea.width; x++)
			for (int y = 0; y < totalArea.height; y++)
				startingPoints.add(new Point(x, y));

		RectilinearPixelPoly firstPoly = new RectilinearPixelPoly(
				startingPoints);
		Region r = new Region(firstPoly, this);
		addRegion(r);

		mBorder = new Rectangle(totalArea.width - 1, totalArea.height - 1);
	}

	/**
	 * Returns a rectangle that encloses this region. The perimeter of this
	 * rectangle will constitute the 'edge' of the region (and therefore every
	 * pixel on the perimeter of this region should be also contained in a
	 * border of one of the sub-regions)
	 * 
	 * @return
	 */
	public Rectangle getBorderRectangle() {
		return mBorder;
	}

	protected void removeRegion(Region poly) {
		mRegions.remove(poly);
	}

	protected void addRegion(Region r) {
		mRegions.add(r);
	}

	/**
	 * Regions are marked as 'used' or 'not used' while the Anonoly algorithm is
	 * executing. This call resets all regions to 'not used' for the beginning
	 * of the algorithm, and as regions are merged or split they become 'used'.
	 * The algorithm is complete when all regions have been 'used'
	 */
	public void resetRegionUsage() {
		for (Region p : mRegions)
			p.setIsUsed(false);
	}

	/**
	 * Regions contain the number of data readings they receive each timeslice
	 * for reporting purposes. This call resets those counts to zero
	 */
	public void resetDataReadingCounts() {
		for (Region p : mRegions)
			p.resetDataReadingCount();
	}

	/**
	 * Regions contain the number of unique users they have seen (if each
	 * incoming data reading is from a different user, then this will equal the
	 * data reading counts). This unique user count is useful for Anonoly
	 * execution e.g. determining the k-value and for reporting purposes
	 */
	public void resetUniqueUsersSeen() {
		for (Region p : mRegions)
			p.resetUniqueUsersSeen();
	}

	/**
	 * Returns the sum of the unique user counts from each region. This is
	 * useful for determining if it is possible to meet a specific K value e.g.
	 * if there are more than K unique users entering data, then K may be
	 * possible, but if there are fewer than K unique users entering data then K
	 * is definitely not possible
	 */
	public int getUniqueUserCount() {
		int count = 0;
		for (Region p : mRegions)
			count += p.getUniqueUsersCount();
		return count;
	}

	public void addDataReading(Point location, String userID) {
		for (Region p : mRegions)
			if (((RectilinearPixelPoly) p.getPolyImpl()).contains(location)) {
				p.addDataReadingFrom(userID);
				return;
			}

		(new IllegalStateException(
				"None of the regions contained the given location: " + location))
				.printStackTrace();
	}

	public List<Region> getRegions() {
		return mRegions;
	}

	public void orderRegions(Comparator<Region> comp) {
		Collections.sort(mRegions, comp);
	}

	public void printRegionOrdering() {
		System.out.println("Regions were ranked: ");
		System.out.print("\t");
		for (Region p : mRegions)
			System.out.print("" + p.getUniqueUsersCount() + ", ");
		System.out.println();
	}

	public List<Region> findNeighborsOf(Region center) {
		List<Region> result = new ArrayList<Region>();
		for (Region p : mRegions)
			if (p.touches(center))
				result.add(p);

		return result;
	}

	Color[] colors = null;

	public BufferedImage getDebugBorderImage(RectilinearPixelPoly... regions) {
		BufferedImage bi = new BufferedImage(mBorder.width + 1,
				mBorder.height + 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		if (colors == null)
			generateColors();
		int color = 0;

		for (RectilinearPixelPoly poly : regions) {
			g.setColor(colors[color++ % colors.length]);
			for (Point p : poly.getBorder())
				g.drawLine(p.x, p.y, p.x, p.y);

			// g.setColor(Color.black);
			// for (Point p : poly.getBorder())
			// g.drawLine(p.x, p.y, p.x, p.y);
		}

		return bi;
	}

	public BufferedImage getDebugImage(RectilinearPixelPoly... regions) {

		BufferedImage bi = new BufferedImage(mBorder.width + 1,
				mBorder.height + 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		if (colors == null)
			generateColors();
		int color = 0;

		for (RectilinearPixelPoly poly : regions) {
			g.setColor(colors[color++ % colors.length]);
			for (Point p : poly.mPoints)
				g.drawLine(p.x, p.y, p.x, p.y);
		}

		return bi;
	}

	public BufferedImage getImage() {
		BufferedImage bi = new BufferedImage(mBorder.width + 1,
				mBorder.height + 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		if (colors == null)
			generateColors();
		int color = 0;

		for (Region poly : mRegions) {
			g.setColor(colors[color++ % colors.length]);
			for (Point p : ((RectilinearPixelPoly) poly.getPolyImpl()).mPoints)
				g.drawLine(p.x, p.y, p.x, p.y);
		}

		return bi;
	}

	/**
	 * Returns an image of this environment where there are black pixels on the
	 * borders, and regions are either green, red, or blue, depending upon if their
	 * k-value is at, above, or below the desired value.
	 * 
	 * @return
	 */
	public BufferedImage getKvalueImage(int k) {
		BufferedImage bi = new BufferedImage(mBorder.width + 1,
				mBorder.height + 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();


		for (Region poly : mRegions) {
			RectilinearPixelPoly rp = (RectilinearPixelPoly) poly.getPolyImpl();
			if (poly.getUniqueUsersCount() < k)
				g.setColor(Color.BLUE);
			else if (poly.getUniqueUsersCount() > k)
				g.setColor(Color.RED);
			else
				g.setColor(Color.GREEN);
			
			for (Point p : rp.mPoints)
				g.drawLine(p.x, p.y, p.x, p.y);
			
			g.setColor(Color.BLACK);
			for (Point p : rp.getBorder())
				g.drawLine(p.x, p.y, p.x, p.y);
		}

		return bi;
	}

	private void generateColors() {
		colors = new Color[15];
		colors[0] = new Color(51, 102, 255); // med-dark blue
		colors[1] = new Color(204, 51, 255); // purple
		colors[2] = new Color(255, 51, 204); // hot pink
		colors[3] = new Color(102, 51, 255); // darkish blue
		colors[4] = new Color(255, 255, 255); // white
		colors[5] = new Color(51, 204, 255); // light blue
		colors[6] = new Color(255, 51, 102); // hot red
		colors[7] = new Color(51, 255, 204); // teal
		colors[8] = new Color(184, 138, 0); // wood
		colors[9] = new Color(245, 184, 0); // light orange
		colors[10] = new Color(204, 255, 51); // lime green
		colors[11] = new Color(0, 0, 255); // pure green
		colors[12] = new Color(150, 39, 113); // dark purple
		colors[13] = new Color(255, 255, 0); // pure yellow
		colors[14] = new Color(0, 255, 255); // pure teal
	}
}
