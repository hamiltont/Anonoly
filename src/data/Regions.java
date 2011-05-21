package data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Regions {
	// TODO create Region class, separate the polygon representation (e.g.
	// RectilinearPixelPoly) from the data that makes this a 'region' e.g. the
	// input count, etc. This will make it easy to change the underlying polygon
	// representation
	List<RectilinearPixelPoly> mRegions = new ArrayList<RectilinearPixelPoly>();

	Rectangle mBorder;

	public Regions(Dimension totalArea) {
		List<Point> startingPoints = new ArrayList<Point>(totalArea.width
				* totalArea.height);
		for (int x = 0; x < totalArea.width; x++)
			for (int y = 0; y < totalArea.height; y++)
				startingPoints.add(new Point(x, y));

		createRegion(startingPoints);

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

	protected void removeRegion(RectilinearPixelPoly poly) {
		mRegions.remove(poly);
	}

	protected void createRegion(Collection<Point> points) {
		RectilinearPixelPoly p = new RectilinearPixelPoly(points, this);
		mRegions.add(p);
	}

	public void resetRegionUsage() {
		for (RectilinearPixelPoly p : mRegions)
			p.setIsUsed(false);
	}

	public void resetDataReadingCount() {
		for (RectilinearPixelPoly p : mRegions)
			p.resetDataReadingCount();
	}

	public void addDataReading(Point location) {
		for (RectilinearPixelPoly p : mRegions)
			if (p.contains(location)) {
				p.addDataReading();
				return;
			}

		(new IllegalStateException(
				"None of the regions contained the given location"))
				.printStackTrace();
	}

	public List<RectilinearPixelPoly> getRegions() {
		return mRegions;
	}

	public void orderRegions(Comparator<RectilinearPixelPoly> comp) {
		Collections.sort(mRegions, comp);

		System.out.println("Regions were ranked: ");
		System.out.print("\t");
		for (RectilinearPixelPoly p : mRegions)
			System.out.print("" + p.getDataReadingCount() + ", ");
		System.out.println();
	}

	public List<RectilinearPixelPoly> findNeighbors(RectilinearPixelPoly center) {
		List<RectilinearPixelPoly> result = new ArrayList<RectilinearPixelPoly>();
		for (RectilinearPixelPoly p : mRegions)
			if (p.touches(center))
				result.add(p);

		return result;
	}

	Color[] colors = null;

	public BufferedImage getImage() {
		BufferedImage bi = new BufferedImage(mBorder.width + 1,
				mBorder.height + 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		if (colors == null)
			generateColors();
		int color = 0;

		for (RectilinearPixelPoly poly : mRegions) {
			g.setColor(colors[color++ % colors.length]);
			for (Point p : poly.mPoints)
				g.drawLine(p.x, p.y, p.x, p.y);

			// g.setColor(Color.black);
			// for (Point p : poly.getBorder())
			// g.drawLine(p.x, p.y, p.x, p.y);
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
