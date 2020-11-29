package org.maxgamer.quickshop.util;

import org.junit.Assert;
import org.junit.Test;

public class MsgUtilTest {

    @Test
    public void testNullLevelEnchantment() {
        try {
            Integer integer = null;
            RomanNumber.toRoman(integer);
        } catch (Exception e) {
            Assert.fail("Failed to test null number input");
        }
    }

}
