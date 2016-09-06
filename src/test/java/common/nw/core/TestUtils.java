package common.nw.core;

import common.nw.core.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.DuplicateFormatFlagsException;

/**
 * testclass for {@link common.nw.core.utils.Utils}
 */
public class TestUtils {

	@Test
	public void testFlagCombine() {
		Assert.assertTrue(Utils.doFlagCombine(0, 124));
		Assert.assertTrue(Utils.doFlagCombine(0, 0));

		Assert.assertTrue(Utils.doFlagCombine(0b010, 0b111));
		Assert.assertTrue(Utils.doFlagCombine(0b111, 0b111));

		Assert.assertFalse(Utils.doFlagCombine(0b1000, 0b111));
		Assert.assertFalse(Utils.doFlagCombine(0b101, 0b0));
	}

	@Test
	public void testParseIntToString() {
		Assert.assertEquals("3", Utils.parseIntWithMinLength(3, 1));
		Assert.assertEquals("0003", Utils.parseIntWithMinLength(3, 4));
	}

	@Test(expected = DuplicateFormatFlagsException.class)
	public void testParseIntToStringExeption() {
		Utils.parseIntWithMinLength(3, 0);
	}

	@Test
	public void testDaysSinceUpdate() {
		Assert.assertEquals(0, Utils.getDaysSinceUpdate(new Date(System.currentTimeMillis())));
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Assert.assertEquals(1, Utils.getDaysSinceUpdate(c.getTime()));
	}
}
