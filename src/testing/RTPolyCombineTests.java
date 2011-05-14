package testing;

import static org.junit.Assert.assertTrue;
import main.RectilinearPolygon;

import org.junit.Test;

public class RTPolyCombineTests {
	
	@Test
	public void testLeftCombine() {
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(100, 100);
		p.addPoint(200, 100);
		p.addPoint(200, 200);
		p.addPoint(100, 200);

		RectilinearPolygon q = new RectilinearPolygon();
		q.addPoint(50, 150);
		q.addPoint(100, 150);
		q.addPoint(100, 175);
		q.addPoint(50, 175);
		
		RectilinearPolygon ans = new RectilinearPolygon();
		ans.addPoint(50, 150);
		ans.addPoint(100, 150);
		ans.addPoint(100, 100);
		ans.addPoint(200, 100);
		ans.addPoint(200, 200);
		ans.addPoint(100, 200);
		ans.addPoint(100, 175);
		ans.addPoint(50, 175);
		
		assertTrue(ans.equals(p.combine(q)));	
	}
	
	@Test
	public void testSelfCombine() {
		RectilinearPolygon p = new RectilinearPolygon();
		p.addPoint(100, 100);
		p.addPoint(200, 100);
		p.addPoint(200, 200);
		p.addPoint(100, 200);

		RectilinearPolygon q = new RectilinearPolygon();
		q.addPoint(100, 100);
		q.addPoint(200, 100);
		q.addPoint(200, 200);
		q.addPoint(100, 200);
		
		assertTrue(p.combine(q).equals(p));
	}
}
