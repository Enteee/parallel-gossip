package ch.duckpond.parallel.gossip.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimeVectorTest {

	private TimeVector tv0;
	private TimeVector tv1;
	private TimeVector tv2;
	private TimeVector tv3;

	@Before
	public final void beforeTest() {
		tv0 = new TimeVector(4);
		tv1 = new TimeVector(4);
		tv1.increment(0);
		tv1.increment(2);
		tv1.increment(2);
		tv1.increment(2);
		tv2 = new TimeVector(4);
		tv2.increment(1);
		tv2.increment(1);
		tv2.increment(3);
		tv2.increment(3);
		tv2.increment(3);
		tv2.increment(3);
		tv3 = new TimeVector(4);
		tv3.increment(0);
		tv3.increment(0);
		tv3.increment(1);
		tv3.increment(1);
		tv3.increment(1);
		tv3.increment(2);
		tv3.increment(2);
		tv3.increment(2);
		tv3.increment(2);
		tv3.increment(3);
		tv3.increment(3);
		tv3.increment(3);
		tv3.increment(3);
		tv3.increment(3);
	}

	@Test
	public final void testMax() {
		TimeVector tvResultMax = new TimeVector(4);
		tvResultMax.increment(0);
		tvResultMax.increment(2);
		tvResultMax.increment(2);
		tvResultMax.increment(2);
		tvResultMax.increment(1);
		tvResultMax.increment(1);
		tvResultMax.increment(3);
		tvResultMax.increment(3);
		tvResultMax.increment(3);
		tvResultMax.increment(3);
		tv1.max(tv2);
		Assert.assertArrayEquals(tvResultMax.toArray(), tv1.toArray());
	}

	@Test
	public final void testIsSame() {
		Assert.assertFalse(tv1.isSame(tv0));
		Assert.assertTrue(tv1.isSame(tv1));
		Assert.assertFalse(tv1.isSame(tv2));
	}

	@Test
	public final void testIsLessOrEqual() {
		Assert.assertTrue(tv0.isLessOrEqual(tv0));
		Assert.assertTrue(tv0.isLessOrEqual(tv1));
		Assert.assertTrue(tv0.isLessOrEqual(tv2));
		Assert.assertFalse(tv1.isLessOrEqual(tv0));
		Assert.assertTrue(tv1.isLessOrEqual(tv1));
		Assert.assertFalse(tv1.isLessOrEqual(tv2));
		Assert.assertFalse(tv2.isLessOrEqual(tv0));
		Assert.assertFalse(tv2.isLessOrEqual(tv1));
		Assert.assertTrue(tv2.isLessOrEqual(tv2));
	}

	@Test
	public final void testIsLess() {
		Assert.assertFalse(tv0.isLess(tv0));
		Assert.assertTrue(tv0.isLess(tv1));
		Assert.assertTrue(tv0.isLess(tv2));
		Assert.assertFalse(tv1.isLess(tv0));
		Assert.assertFalse(tv1.isLess(tv1));
		Assert.assertFalse(tv1.isLess(tv2));
		Assert.assertFalse(tv2.isLess(tv0));
		Assert.assertFalse(tv2.isLess(tv1));
		Assert.assertFalse(tv2.isLess(tv2));
	}

	@Test
	public final void testCompare() {
		Assert.assertTrue(tv0.compareTo(tv0) == 0);
		Assert.assertTrue(tv0.compareTo(tv1) < 0);
		Assert.assertTrue(tv0.compareTo(tv2) < 0);
		Assert.assertTrue(tv0.compareTo(tv3) < 0);
		Assert.assertTrue(tv1.compareTo(tv0) > 0);
		Assert.assertTrue(tv1.compareTo(tv1) == 0);
		Assert.assertTrue(tv1.compareTo(tv2) > 0);
		Assert.assertTrue(tv1.compareTo(tv3) < 0);
		Assert.assertTrue(tv2.compareTo(tv0) > 0);
		Assert.assertTrue(tv2.compareTo(tv1) < 0);
		Assert.assertTrue(tv2.compareTo(tv2) == 0);
		Assert.assertTrue(tv2.compareTo(tv3) < 0);
		Assert.assertTrue(tv3.compareTo(tv0) > 0);
		Assert.assertTrue(tv3.compareTo(tv1) > 0);
		Assert.assertTrue(tv3.compareTo(tv2) > 0);
		Assert.assertTrue(tv3.compareTo(tv3) == 0);
	}
}
