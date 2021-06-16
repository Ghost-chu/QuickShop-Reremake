/*
 * This file is a part of project QuickShop, the name is MsgUtilTest.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

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

    @Test
    public void fillArgs() {
        String sample = "I like apple and orange";
        Assertions.assertEquals(sample, MsgUtil.fillArgs("I like {0} and {1}", "apple", "orange"));
    }

    @Test
    public void getSubString() {
        String sample = "cra";
        Assertions.assertEquals(sample, MsgUtil.getSubString("Minecraft", "Mine", "ft"));
    }

    @Test
    public void isJson() {
        Assertions.assertTrue(MsgUtil.isJson("{}"));
        Assertions.assertTrue(MsgUtil.isJson("[]"));
        Assertions.assertFalse(MsgUtil.isJson("{]"));
    }

}
