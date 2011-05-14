package testing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import main.RectilinearPolygon;

import org.junit.Test;

public class RLPolyTouchPolyTests {

	public static RectilinearPolygon getPoly() {
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(100, 100);
		p.addPoint(200, 100);
		p.addPoint(200, 200);
		p.addPoint(100, 200);
		return p;
	}

	
	@Test
	public void testLeftTouch() {
		RectilinearPolygon base = getPoly();
		
		// Above to Middle
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(50, 150);
		p.addPoint(100, 150);
		p.addPoint(100, 250);
		p.addPoint(50, 250);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to Middle
		p.reset();
		p.addPoint(50, 150);
		p.addPoint(100, 150);
		p.addPoint(100, 175);
		p.addPoint(50, 175);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to bottom
		p.reset();
		p.addPoint(50, 50);
		p.addPoint(100, 50);
		p.addPoint(100, 175);
		p.addPoint(50, 175);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
	}
	
	@Test
	public void testRightTouch() {
		RectilinearPolygon base = getPoly();
		
		
		// Above to Middle
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(200, 150);
		p.addPoint(250, 150);
		p.addPoint(250, 250);
		p.addPoint(200, 250);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to Middle
		p.reset();
		p.addPoint(200, 150);
		p.addPoint(250, 150);
		p.addPoint(250, 175);
		p.addPoint(200, 175);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to bottom
		p.reset();
		p.addPoint(200, 50);
		p.addPoint(250, 50);
		p.addPoint(250, 175);
		p.addPoint(200, 175);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));

	}
	
	@Test
	public void testTopTouch() {
		RectilinearPolygon base = getPoly();
		
		// Left to Middle
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(50, 200);
		p.addPoint(150, 200);
		p.addPoint(150, 300);
		p.addPoint(50, 300);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to Middle
		p.reset();
		p.addPoint(125, 200);
		p.addPoint(175, 200);
		p.addPoint(175, 300);
		p.addPoint(125, 300);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to right
		p.reset();
		p.addPoint(175, 200);
		p.addPoint(250, 200);
		p.addPoint(250, 300);
		p.addPoint(175, 300);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));

	}
	
	@Test
	public void testBottomTouch() {
		RectilinearPolygon base = getPoly();
		
		// Left to Middle
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(50, 50);
		p.addPoint(150, 50);
		p.addPoint(150, 100);
		p.addPoint(50, 100);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to Middle
		p.reset();
		p.addPoint(125, 50);
		p.addPoint(175, 50);
		p.addPoint(175, 100);
		p.addPoint(125, 100);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
		
		// Middle to right
		p.reset();
		p.addPoint(175, 50);
		p.addPoint(250, 50);
		p.addPoint(250, 100);
		p.addPoint(175, 100);
		
		assertTrue(p.touches(base));
		assertTrue(base.touches(p));
	}
	
	@Test
	public void testCornerNoTouch() {
		RectilinearPolygon base = getPoly();

		// Top Left
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(50, 200);
		p.addPoint(100, 200);
		p.addPoint(100, 300);
		p.addPoint(50, 300);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));

		// Top right
		p.reset();
		p.addPoint(200, 200);
		p.addPoint(300, 200);
		p.addPoint(300, 300);
		p.addPoint(200, 300);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));
		
		// Bottom Left
		p.reset();
		p.addPoint(50, 50);
		p.addPoint(100, 50);
		p.addPoint(100, 100);
		p.addPoint(50, 100);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));
		
		// Bottom right
		p.reset();
		p.addPoint(200, 50);
		p.addPoint(300, 50);
		p.addPoint(300, 100);
		p.addPoint(200, 100);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));
		
		
	}
	
	@Test
	public void testOutliersNoTouch() {
		RectilinearPolygon base = getPoly();

		// Way Left
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(50, 75);
		p.addPoint(75, 75);
		p.addPoint(75, 300);
		p.addPoint(50, 300);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));
		
		// Way above
		p.reset();
		p.addPoint(50, 220);
		p.addPoint(200, 220);
		p.addPoint(200, 300);
		p.addPoint(50, 300);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));
		
		// Way below
		p.reset();
		p.addPoint(50, 70);
		p.addPoint(200, 70);
		p.addPoint(200, 90);
		p.addPoint(50, 90);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));

		// Way right
		p.reset();
		p.addPoint(250, 75);
		p.addPoint(275, 75);
		p.addPoint(275, 300);
		p.addPoint(250, 300);
		
		assertFalse(p.touches(base));
		assertFalse(base.touches(p));


	}
	
}
