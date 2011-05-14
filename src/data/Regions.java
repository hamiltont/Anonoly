package data;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Regions {
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

		throw new IllegalStateException(
				"None of the regions contained the given location");
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

}
