package testing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;

import main.RectilinearPolygon;

import org.junit.Test;

public class RLPolyTouchLineTests {

	public static RectilinearPolygon getPoly() {
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(100, 100);
		p.addPoint(200, 100);
		p.addPoint(200, 200);
		p.addPoint(100, 200);
		return p;
	}
	
	@Test
	public void rectTest() {
		
		Rectangle f = new Rectangle(20, 20, 1, 1);
		
		assertTrue(f.contains(20, 20));
		System.out.println(f.getBounds());
		assertFalse(f.contains(21, 20));
		assertFalse(f.contains(20, 21));
	}
	

	@Test
	public void testHorizOutliers() {
		RectilinearPolygon p = getPoly();

		// Way high
		assertFalse(p.touches(125, 300, 150, 300));

		// Way low
		assertFalse(p.touches(125, 50, 150, 50));

		// Way left
		assertFalse(p.touches(50, 125, 75, 125));

		// Way right
		assertFalse(p.touches(250, 125, 275, 125));
	}
	
	@Test
	public void testVerticalOutliers() {
		RectilinearPolygon p = getPoly();

		// Way high
		assertFalse(p.touches(150, 300, 150, 250));

		// Way low
		assertFalse(p.touches(125, 50, 125, 75));

		// Way left
		assertFalse(p.touches(50, 125, 50, 175));

		// Way
		assertFalse(p.touches(250, 125, 250, 175));		
	}

	@Test
	public void testCorners() {
		RectilinearPolygon p = getPoly();

		// Top left corner
		assertFalse(p.touches(99, 201, 99, 201));

		// Top right
		assertFalse(p.touches(201, 201, 201, 201));

		// Bottom left
		assertFalse(p.touches(99, 99, 99, 99));

		// Bottom right
		assertFalse(p.touches(201, 99, 201, 99));
	}

	@Test
	public void testHorizTopTouches() {
		RectilinearPolygon p = getPoly();

		// Left to Middle
		assertTrue(p.touches(50, 201, 150, 201));

		// Middle to Middle
		assertTrue(p.touches(125, 201, 150, 201));

		// Middle to right
		assertTrue(p.touches(175, 201, 250, 201));
		
		// Right to middle
		assertTrue(p.touches(250, 201, 175, 201));

	}
	
	@Test
	public void testHorizBottomTouches() {
		RectilinearPolygon p = getPoly();

		// Left to Middle
		assertTrue(p.touches(50, 99, 150, 99));

		// Middle to Middle
		assertTrue(p.touches(125, 99, 150, 99));

		// Middle to right
		assertTrue(p.touches(175, 99, 250, 99));
	}
	

	@Test
	public void testVertLeftTouches() {
		RectilinearPolygon p = getPoly();
		
		// Above to Middle
		assertTrue(p.touches(99, 250, 99, 150));
		
		// Middle to middle
		assertTrue(p.touches(99, 150, 99, 120));
		
		// Middle to below
		assertTrue(p.touches(99, 150, 99, 50));
		
	}
	
	
	@Test
	public void testVertRightTouches() {
		RectilinearPolygon p = getPoly();
		
		// Above to Middle
		assertTrue(p.touches(201, 250, 201, 150));
		
		// Middle to middle
		assertTrue(p.touches(201, 150, 201, 120));
		
		// Middle to below
		assertTrue(p.touches(201, 150, 201, 50));
	}
	
}
