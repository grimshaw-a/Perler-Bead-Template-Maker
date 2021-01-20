import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.*;
import java.util.*;

public class ConvertImageToTemplateTest {

	@Test
	public void testProcessImage() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		Bead white = new Bead("White", "01", new Color(241, 241, 241));
		Bead black = new Bead("Black", "18", new Color(46, 47, 50));
		ArrayList<Bead> beads = new ArrayList<>();
		beads.add(black);
		beads.add(white);
		beads.add(yellow);
		beads.add(orange);
		ImageToBeadConverter converter = new ImageToBeadConverter();
		BeadMatrix bm = converter.processImage("allBlack.jpg", beads);
		assertEquals(bm.beadGrid[0][0].getColor(), new Color(46, 47, 50));
		int beadCount = bm.colorList.get("Black");
		assertEquals(beadCount, 16);
	}
	
	@Test
	public void testAddBeadAsPossibleMatch() {
		ColorMatch cm1 = new ColorMatch("01");
		ColorMatch cm2 = new ColorMatch("02");
		ColorMatch cm3 = new ColorMatch("03");
		Map<String,ColorMatch> map = new HashMap<>();
		map.put("01", cm1);
		map.put("02", cm2);
		map.put("03", cm3);
		cm1.addBeadAsPossibleMatch(map, "01", 0);
		cm1.addBeadAsPossibleMatch(map, "01", 1);
		assertEquals(cm1.addBeadAsPossibleMatch(map, "01", 2), true);
		assertEquals(cm2.addBeadAsPossibleMatch(map, "02", 0), false);
	}
	
	@Test
	public void testIsMatch() {
		ColorMatch cm1 = new ColorMatch();
		ColorMatch cm2 = new ColorMatch();
		cm2.addToChannel(cm2, 0);
		ColorMatch cm3 = new ColorMatch();
		cm2.addToChannel(cm3, 0);
		cm2.addToChannel(cm3, 1);
		ColorMatch cm4 = new ColorMatch();
		cm2.addToChannel(cm4, 0);
		cm2.addToChannel(cm4, 1);
		cm2.addToChannel(cm4, 2);
		assertEquals(cm2.isMatch(cm1), false);
		assertEquals(cm2.isMatch(cm2), false);
		assertEquals(cm2.isMatch(cm3), false);
		assertEquals(cm2.isMatch(cm4), true);
	}
	
	@Test
	public void testGetName() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		assertEquals(yellow.getName(), "Yellow");
	}
	
	@Test
	public void testGetCode() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		assertEquals(yellow.getCode(), "03");
	}
	
	@Test
	public void testGetColor() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		assertEquals(yellow.getColor().getRed(), 236);
		assertEquals(yellow.getColor().getGreen(), 216);
	}
	
	@Test
	public void testGetX() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0), 5, 8);
		assertEquals(yellow.getX(), 8);
	}
	
	@Test
	public void testGetY() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0), 5, 8);
		assertEquals(yellow.getY(), 5);
	}
	 
	@Test
	public void testCreateCodeMap() {
		ColorLibrary cl = new ColorLibrary();
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		ArrayList<Bead> list = new ArrayList<>();
		list.add(yellow);
		list.add(orange);
		Map<String,String> returnValue = cl.createCodeMap(list);
		assertEquals(returnValue.get("Yellow"), "03");
		assertEquals(returnValue.get("Orange"), "04");
	}
	
	@Test
	public void testCompare1() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		TonalValueComparator comparator = new TonalValueComparator();
		assertEquals(comparator.compare(yellow, orange), 1);
		assertEquals(comparator.compare(orange, yellow), -1);
		assertEquals(comparator.compare(yellow, yellow), 0);
	}
	
	@Test
	public void testCompare2() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		RedChannelComparator comparator = new RedChannelComparator();
		assertEquals(comparator.compare(yellow, orange), -1);
		assertEquals(comparator.compare(orange, yellow), 1);
		assertEquals(comparator.compare(yellow, yellow), 0);
	}
	
	@Test
	public void testCompare3() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		GreenChannelComparator comparator = new GreenChannelComparator();
		assertEquals(comparator.compare(yellow, orange), 1);
		assertEquals(comparator.compare(orange, yellow), -1);
		assertEquals(comparator.compare(yellow, yellow), 0);
	}
	
	@Test
	public void testCompare4() {
		Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
		Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
		BlueChannelComparator comparator = new BlueChannelComparator();
		assertEquals(comparator.compare(yellow, orange), -1);
		assertEquals(comparator.compare(orange, yellow), 1);
		assertEquals(comparator.compare(yellow, yellow), 0);
	}
}