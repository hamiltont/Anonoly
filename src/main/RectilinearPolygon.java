package main;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class RectilinearPolygon extends Polygon {

	@Override
	public void addPoint(int x, int y) {
		super.addPoint(x, y); // TODO make this enforce RL property
	}

	public boolean touches(RectilinearPolygon other) {
		PathIterator pi = other.getPathIterator(null);

		double[] current = new double[2];
		int[] first = new int[2];
		int[] last = new int[2];
		while (pi.isDone() == false) {
			int type = pi.currentSegment(current);
			switch (type) {
			case PathIterator.SEG_LINETO:
				if (touches(last[0], last[1], getInt(current[0]),
						getInt(current[1])))
					return true;
				break;
			case PathIterator.SEG_CLOSE:
				if (touches(last[0], last[1], first[0], first[1]))
					return true;
				break;
			case PathIterator.SEG_MOVETO:
				first[0] = getInt(current[0]);
				first[1] = getInt(current[1]);
				break;
			default:
				throw new IllegalArgumentException(
						"The passed RectilinearPolygon is not rectilinear!");
			}

			last[0] = getInt(current[0]);
			last[1] = getInt(current[1]);

			pi.next();
		}

		return false;
	}

	// Combines two polys into one
	public RectilinearPolygon combine(RectilinearPolygon other) {
		Area a = new Area(other);
		Area b = new Area(this);

		if (!a.isPolygonal() || !b.isPolygonal())
			throw new IllegalArgumentException(
					"One of the Rectilinear polys was not rectilinear");

		b.add(a);

		if (!a.isPolygonal())
			throw new IllegalStateException("Something unknown went wrong");

		PathIterator pi = b.getPathIterator(null);
		RectilinearPolygon p = new RectilinearPolygon();
		double[] current = new double[2];
		while (pi.isDone() == false) {
			int type = pi.currentSegment(current);
			switch (type) {
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_MOVETO:
				// These are backwards for Area for some reason??
				p.addPoint(getInt(current[0]), getInt(current[1]));
				break;
			case PathIterator.SEG_CLOSE:
				break;
			default:
				throw new IllegalArgumentException(
						"The passed RectilinearPolygon is not rectilinear!");
			}
			pi.next();
		}

		return p;
	}

	private static int getInt(double d) {
		if (d - (Math.floor(d)) != 0)
			throw new IllegalArgumentException("Double " + d
					+ " could not be converted to an int");
		return (int) Math.floor(d);
	}

	// Does not count 'corners'. This line is assummed to 1) be the complete
	// straight line e.g. the next LINE_TO would be at a 90 degree angle to the
	// end point of this line segment, and 2) includes the corner point (which
	// is subsequently ignored internally, and only the interior line segment
	// is considered). Assumes everything is in quadrant 1, including this
	// entire rectipoly e.g all numbers are positive.
	public boolean touches(int x1, int y1, int x2, int y2) {
		if ((x1 != x2) && (y1 != y2))
			throw new IllegalArgumentException(
					"The passed arguments are not a vertical or horizontal line segment");

		Rectangle bounds = getBounds();

		// We are a point
		if (x1 == x2 && y1 == y2) {
			System.err.println("A point was passed to #touches. [" + x1 + ", "
					+ y1 + "]");

			if (contains(x1 + 1, y1)) {
				lastTouch.x = x1 + 1;
				lastTouch.y = y1;
				return true;
			} else if (contains(x1 - 1, y1)) {
				lastTouch.x = x1 - 1;
				lastTouch.y = y1;
				return true;
			} else if (contains(x1, y1 + 1)) {
				lastTouch.x = x1;
				lastTouch.y = y1 + 1;
				return true;
			} else if (contains(x1, y1 - 1)) {
				lastTouch.x = x1;
				lastTouch.y = y1 - 1;
				return true;
			}

		}

		// We are a horizontal segment
		if (y1 == y2) {
			int leftx = Math.min(x1, x2);
			int rightx = Math.max(x1, x2);

			// [ shape ] *------*
			if (bounds.x + bounds.width < leftx)
				return false;

			// *------* [ shape ]
			if (bounds.x > rightx)
				return false;

			// *------*
			// [ shape ]
			if ((bounds.y - 1) > y1)
				return false;

			// [ shape ]
			// *------*
			if ((bounds.y + bounds.height + 1) < y1)
				return false;

			// We have checked all simple simple cases, now to check complex
			// interactions where the line segment is contained within the
			// bounding boxes vertically. Simplest method is to check all pixels
			// above and below the interior of the line segment
			for (int x = leftx + 1; x != rightx - 1; x++)
				if (contains(x, y1 + 1)) {
					lastTouch.x = x;
					lastTouch.y = y1 + 1;
					return true;
				} else if (contains(x, y1 - 1)) {
					lastTouch.x = x;
					lastTouch.y = y1 - 1;
					return true;
				}

		} else {
			// We are a vertical line segment

			int topy = Math.min(y1, y2);
			int bottomy = Math.max(y1, y2);

			// [ shape ] |
			if ((bounds.x + bounds.width + 1) < x1)
				return false;

			// | [ shape ]
			if ((bounds.x - 1) > x1)
				return false;

			// . . |
			// [ shape ]
			if ((bounds.y - 1) > bottomy)
				return false;

			// [ shape ]
			// . . |
			if ((bounds.y + bounds.height + 1) < topy)
				return false;

			// We have checked all simple simple cases, now to check complex
			// interactions where the line segment is contained within the
			// bounding boxes vertically. Simplest method is to check all pixels
			// above and below the interior of the line segment
			for (int y = topy + 1; y != bottomy - 1; y++)
				if (contains(x1 + 1, y)) {
					lastTouch.x = x1 + 1;
					lastTouch.y = y;
					return true;
				} else if (contains(x1 - 1, y)) {
					lastTouch.x = x1 - 1;
					lastTouch.y = y;
					return true;
				}
		}

		return false;
	}

	Point lastTouch = new Point();

	public Point getLastTouchedPoint() {
		return lastTouch;
	}

	// Asks this poly to generate a new Poly from within itself, giving up the
	// area in the process. Used for splitting poly's into smaller subcomponents
	public RectilinearPolygon cut(int maximum) {
		double[] temp = new double[2];
		getPathIterator(null).currentSegment(temp);
		int[] first = new int[2];
		first[0] = getInt(temp[0]);
		first[1] = getInt(temp[1]);

		if (maximum >= getPixelSize())
			throw new IllegalArgumentException(
					"Cut should never be called with more than the available area");

		List<Point> consumable = getConsumablePoints(maximum, new Point(
				first[0], first[1]));

		Area builder = new Area();
		GeneralPath gp = new GeneralPath();
		boolean firstt = true;
		for (Point p : consumable) {
			if (firstt) {
				gp.moveTo(p.getX(), p.getY());
				firstt = false;
			}
			gp.lineTo(p.getX(), p.getY());
		}
		builder.add(new Area(gp));

		PathIterator pi = builder.getPathIterator(null);
		RectilinearPolygon p = new RectilinearPolygon();
		double[] current = new double[2];
		while (pi.isDone() == false) {
			int type = pi.currentSegment(current);
			switch (type) {
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_MOVETO:
				p.addPoint(getInt(current[0]), getInt(current[1]));
				break;
			case PathIterator.SEG_CLOSE:
				break;
			default:
				throw new IllegalArgumentException(
						"The passed RectilinearPolygon is not rectilinear!");
			}
			pi.next();
		}

		return p;
	}

	// Given a 'start' point that the requester knows is a touched point between
	// us and them, this request will return all of the points that can be
	// safely consumed by the requestor. Consider this a 'best effort' to get to
	// the maximum number
	// Can return an empty list!
	public List<Point> getConsumablePoints(int maximum, Point startPoint) {
		if (maximum <= 0)
			throw new IllegalArgumentException("Maximum is " + maximum);

		List<Point> consumablePoints = new ArrayList<Point>(maximum);

		if (isConsumable(startPoint.x, startPoint.y, consumablePoints))
			// TODO - better understanding of this section
			consumablePoints.add(startPoint);
		else {
			System.err.println("Returning an empty consumablePoint set");
			return consumablePoints;
		}

		int i = 0;

		do {
			Point cur = null;
			try {
				cur = consumablePoints.get(i);
			} catch (IndexOutOfBoundsException ide) {
				// We have run out of consumable points. We should never arrive
				// here - before someone calls this method they should check if
				// we don't have enough pixels to meet the maximum number
				// requested and still exist. if we don't, they should just
				// combine with us. If we do, then every single pass of this
				// loop should realistically add at least one traversal option
				// to the list (I may be wrong about this statement, I'm tired
				// as crap).
				ide.printStackTrace();
				throw new IllegalStateException("Oh dear god why");
			}
			if (isConsumable(cur.x, cur.y - 1, consumablePoints))
				consumablePoints.add(new Point(cur.x, cur.y - 1));
			if (isConsumable(cur.x + 1, cur.y, consumablePoints))
				consumablePoints.add(new Point(cur.x + 1, cur.y));
			if (isConsumable(cur.x, cur.y + 1, consumablePoints))
				consumablePoints.add(new Point(cur.x, cur.y + 1));
			if (isConsumable(cur.x - 1, cur.y, consumablePoints))
				consumablePoints.add(new Point(cur.x - 1, cur.y));
			++i;
		} while (consumablePoints.size() < maximum);

		while (consumablePoints.size() != maximum)
			consumablePoints.remove(maximum);

		return consumablePoints;
	}

	private boolean isConsumable(int x, int y, List<Point> toBeConsumed) {
		// First, ensure that we 'own' this point to mark it consumable
		if (false == contains(x, y))
			return false;

		// Second, ensure we are not already planning to consume it
		if (toBeConsumed.contains(new Point(x, y)))
			return false;

		// We do own it, so now we need to check how many of it's neighboring
		// pixels would be 'available' if this pixel was consumed. If 1 pixel
		// would be available, then we are trimming a branch down. If 2 would be
		// available, we cannot be sure that we are not splitting a polygon in
		// two, so we leave that pixel alone. If 3 would be available, we cannot
		// be sure we are not splitting a polygon. If 4 are available, something
		// is wrong, because someone had to decide that they wanted to consume
		// this pixel. If 0 are available, we are consuming the last pixel in
		// this poly
		int available = 0;

		// Check if north is available
		boolean northAvail = false;
		if (contains(x, y + 1))
			if (false == toBeConsumed.contains(new Point(x, y + 1))) {
				northAvail = true;
				++available;
			}

		// Check if east is available
		boolean eastAvail = false;
		if (contains(x + 1, y))
			if (false == toBeConsumed.contains(new Point(x + 1, y))) {
				eastAvail = true;
				++available;
			}

		// Check if south is available
		boolean southAvail = false;
		if (contains(x, y - 1))
			if (false == toBeConsumed.contains(new Point(x, y - 1))) {
				southAvail = true;
				++available;
			}

		// Check if west is available
		boolean westAvail = false;
		if (contains(x - 1, y))
			if (false == toBeConsumed.contains(new Point(x - 1, y))) {
				westAvail = true;
				++available;
			}

		if (available == 4)
			throw new IllegalStateException("Why are 4 available?!");

		// Check if there will still be linkings between the remaining elements
		if (available == 3) {
			if (!northAvail) {
				// Check bottom-left and bottom-right
				if (false == contains(x - 1, y - 1)
						|| toBeConsumed.contains(new Point(x - 1, y - 1)))
					return false;
				if (false == contains(x + 1, y - 1)
						|| toBeConsumed.contains(new Point(x + 1, y - 1)))
					return false;

				return true;
			} else if (!eastAvail) {
				// Check top-left and bottom-left
				if (false == contains(x - 1, y + 1)
						|| toBeConsumed.contains(new Point(x - 1, y + 1)))
					return false;
				if (false == contains(x - 1, y - 1)
						|| toBeConsumed.contains(new Point(x - 1, y - 1)))
					return false;

				return true;
			} else if (!southAvail) {
				// Check top-left and top-right
				if (false == contains(x - 1, y + 1)
						|| toBeConsumed.contains(new Point(x - 1, y + 1)))
					return false;
				if (false == contains(x + 1, y + 1)
						|| toBeConsumed.contains(new Point(x + 1, y + 1)))
					return false;

				return true;
			} else if (!westAvail) {
				// Check top-right and bottom-right
				if (false == contains(x + 1, y + 1)
						|| toBeConsumed.contains(new Point(x + 1, y + 1)))
					return false;
				if (false == contains(x + 1, y - 1)
						|| toBeConsumed.contains(new Point(x + 1, y - 1)))
					return false;

				return true;
			} else
				throw new IllegalStateException(
						"The available count was incorrect");
		}

		if (available == 2) {
			if (northAvail && southAvail)
				return false;
			if (!northAvail && !southAvail)
				return false;
			return true;
		}

		if (available == 1)
			return true;

		if (available == 0 && toBeConsumed.size() != getPixelSize() - 1)
			throw new IllegalStateException(
					"Why have we reached the maximum? tbc:"
							+ toBeConsumed.size() + ", avail:" + getPixelSize());

		throw new IllegalAccessError("How did we get here?!");
	}

	// Returns true if a point is either contained inside the path for this
	// shape, or happens to fall on the path line for this shape
	@Override
	public boolean contains(int x, int y) {
		// First check the inside
		if (super.contains(x, y) == true)
			return true;

		// Then check the bordering path
		PathIterator pi = getPathIterator(null);
		double[] temp = new double[2];
		int[] current = new int[2];
		int[] first = new int[2];
		int[] prev = new int[2];
		while (pi.isDone() == false) {
			int type = pi.currentSegment(temp);
			current[0] = getInt(temp[0]);
			current[1] = getInt(temp[1]);
			switch (type) {
			case PathIterator.SEG_LINETO:
				// Is this a horizontal line?
				if (current[1] == prev[1]) {
					if (y != prev[1])
						break;

					int minx = Math.min(current[0], prev[0]);
					int maxx = Math.max(current[0], prev[0]);
					if (x <= maxx && x >= minx)
						return true;

				} else if (current[0] == prev[0]) {
					// This is a vertical line
					if (x != prev[0])
						break;

					int miny = Math.min(current[1], prev[1]);
					int maxy = Math.max(current[1], prev[1]);
					if (y <= maxy && y >= miny)
						return true;
				} else
					throw new IllegalStateException(
							"Movement is neither horizontal or vertical");
				break;
			case PathIterator.SEG_MOVETO:
				first[0] = getInt(current[0]);
				first[1] = getInt(current[1]);
				break;
			case PathIterator.SEG_CLOSE:
				// The closing line is horizontal
				if (first[1] == prev[1]) {
					if (y != prev[1])
						break;

					int minx = Math.min(first[0], prev[0]);
					int maxx = Math.max(first[0], prev[0]);
					if (x <= maxx && x >= minx)
						return true;
				} else if (first[0] == prev[0]) {
					// The closing line is vertival
					if (x != prev[0])
						break;

					int miny = Math.min(first[1], prev[1]);
					int maxy = Math.max(first[1], prev[1]);
					if (y <= maxy && y >= miny)
						return true;

				} else
					throw new IllegalStateException(
							"Close movement is neither horizontal or vertical");

				break;
			default:
				throw new IllegalArgumentException(
						"The passed RectilinearPolygon is not rectilinear!");
			}

			prev[0] = current[0];
			prev[1] = current[1];

			pi.next();
		}

		return false;
	}

	// TODO - if it matters, you can well known string search algo's to
	// drastically improve the performance here
	@Override
	public boolean equals(Object other) {
		if (false == other instanceof RectilinearPolygon)
			return false;

		RectilinearPolygon op = (RectilinearPolygon) other;

		if (op.npoints != npoints)
			return false;

		int sum = 0;
		for (int i = 0; i < npoints; i++) {
			sum += xpoints[i];
			sum += ypoints[i];
			sum -= op.xpoints[i];
			sum -= op.ypoints[i];
		}
		if (sum != 0)
			return false;

		// Search same direction
		for (int offset = 0; offset < npoints; offset++) {

			boolean found = true;
			for (int i = 0; i < npoints; i++) {
				int x = op.xpoints[i];
				int y = op.ypoints[i];

				int x2 = xpoints[offset + i % npoints];
				int y2 = ypoints[offset + i % npoints];

				if (x != x2 || y != y2) {
					found = false;
					break;
				}
			}

			if (found)
				return true;
		}

		// Search different directions
		for (int offset = 0; offset < npoints; offset++) {

			boolean found = true;
			for (int i = 0; i < npoints; i++) {
				int x = op.xpoints[i];
				int y = op.ypoints[i];

				int index = (npoints - (offset + i) % npoints) - 1;
				int x2 = xpoints[index];
				int y2 = ypoints[index];

				if (x != x2 || y != y2) {
					found = false;
					break;
				}
			}

			if (found)
				return true;
		}

		return false;
	}

	public int getPixelSize() {
		Rectangle b = getBounds();
		int area = 0;
		for (int x = b.x; x < b.x + b.width; x++)
			for (int y = b.y; y < b.y + b.height; y++)
				if (contains(x, y))
					++area;
		return area;
	}

	@Override
	public int hashCode() {
		return getPathIterator(null).hashCode();
	}

	// Given a list of points that are all touching, this builds the poly that
	// represents them
	// TODO incomplete
	public static RectilinearPolygon build(List<Point> param) {
		Set<Point> points = new HashSet<Point>(param);

		RectilinearPolygon rp = new RectilinearPolygon();

		List<Point> keepers = new ArrayList<Point>();

		// You could do this all with a single object, this just seems cleaner
		// for only a little fuss
		Point left = new Point();
		Point right = new Point();
		Point above = new Point();
		Point below = new Point();
		Point tl = new Point();
		Point tr = new Point();
		Point bl = new Point();
		Point br = new Point();

		// After this, keepers will contain all edge and corner points
		for (Point p : points) {

			left.x = p.x - 1;
			left.y = p.y;
			if (!points.contains(left)) {
				keepers.add(p);
				continue;
			}

			right.x = p.x + 1;
			right.y = p.y;
			if (!points.contains(right)) {
				keepers.add(p);
				continue;
			}

			above.x = p.x;
			above.y = p.y + 1;
			if (!points.contains(above)) {
				keepers.add(p);
				continue;
			}

			below.x = p.x;
			below.y = p.y - 1;
			if (!points.contains(below)) {
				keepers.add(p);
				continue;
			}

			tl.x = p.x - 1;
			tl.y = p.y + 1;
			if (!points.contains(tl)) {
				keepers.add(p);
				continue;
			}

			tr.x = p.x + 1;
			tr.y = p.y + 1;
			if (!points.contains(tr)) {
				keepers.add(p);
				continue;
			}

			bl.x = p.x - 1;
			bl.y = p.y - 1;
			if (!points.contains(bl)) {
				keepers.add(p);
				continue;
			}

			br.x = p.x + 1;
			br.y = p.y - 1;
			if (!points.contains(br)) {
				keepers.add(p);
				continue;
			}

		}

		RectilinearPolygon poly = new RectilinearPolygon();
		Point start = new Point();
		c(keepers.get(0), start);
		poly.addPoint(start.x, start.y);
		Point prev = new Point();
		c(start, prev);
		Point cur = new Point();
		c(prev, cur);
		do {
			// Go up, make line from prev
			while (keepers.contains(cur))
				cur.y = cur.y + 1;
			cur.y = cur.y - 1;
			if (cur.equals(start))
				break;
			if (cur.equals(prev) == false)
				poly.addPoint(cur.x, cur.y);
			c(cur, prev);

			// Go left, make line from prev to cur
			while (keepers.contains(cur))
				cur.x = cur.x - 1;
			cur.x = cur.x + 1;
			if (cur.equals(start))
				break;
			if (cur.equals(prev) == false) {
				poly.addPoint(cur.x, cur.y);
				c(cur, prev);
				continue;
			}
			c(cur, prev);

			// Go down
			while (keepers.contains(cur))
				cur.y = cur.y - 1;
			cur.y = cur.y + 1;
			if (cur.equals(start))
				break;
			if (cur.equals(prev) == false)
				poly.addPoint(cur.x, cur.y);
			c(cur, prev);

			// Go

		} while (cur.equals(start) == false);

		return null;
	}
	
	private static void c(Point src, Point dest) {
		dest.x = src.x;
		dest.y = src.y;
	}

	// returns 0 if p is internal, 1 if p is a corner, 2 if p is an edge
	private static int getType(Point p, Set<Point> others) {
		boolean top = others.contains(new Point(p.x, p.y + 1));
		boolean bot = others.contains(new Point(p.x, p.y - 1));
		boolean left = others.contains(new Point(p.x - 1, p.y));
		boolean rit = others.contains(new Point(p.x + 1, p.y));
		boolean tl = others.contains(new Point(p.x - 1, p.y + 1));
		boolean tr = others.contains(new Point(p.x + 1, p.y + 1));
		boolean bl = others.contains(new Point(p.x - 1, p.y - 1));
		boolean br = others.contains(new Point(p.x + 1, p.y - 1));

		if (top && bot && left && rit && tl && tr && bl && br)
			return 0;

		if ((left && tl && top) || (top && tr && rit) || (rit && br && bot)
				|| (bot && bl && left))
			return 1;

		return -1;
	}
}
