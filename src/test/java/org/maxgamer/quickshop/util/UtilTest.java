/*
 * This file is a part of project QuickShop, the name is UtilTest.java
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class UtilTest {

    @Test
    void array2String() {
        String sample = "A, B, C";
        Assertions.assertEquals(sample, Util.array2String(new String[]{"A", "B", "C"}));
    }

    @Test
    void boolean2Status() {
        Assertions.assertEquals(Util.boolean2Status(true), "Enabled");
        Assertions.assertEquals(Util.boolean2Status(false), "Disabled");
    }

    @Test
    void isClassAvailable() {
        Assertions.assertTrue(Util.isClassAvailable(getClass().getName()));
        Assertions.assertFalse(Util.isClassAvailable("random.Class"));
    }

    @Test
    void isUUID() {
        Assertions.assertTrue(Util.isUUID("b188beda-8bfb-ed66-65e5-25147a4617cf"));
        Assertions.assertFalse(Util.isUUID("b188beda8bfbed6665e525147a4617cf"));
        Assertions.assertFalse(Util.isUUID("?"));
    }

    @Test
    void list2String() {
        String sample = "1, 2, 3, 4, 5";
        Assertions.assertEquals(sample, Util.list2String(Arrays.asList("1", "2", "3", "4", "5")));
    }

    @Test
    void firstUppercase() {
        Assertions.assertEquals("QuickShop", Util.firstUppercase("quickShop"));
    }

    @Test
    void mergeArgs() {
        String[] args = new String[3];
        args[0] = "yaa";
        args[1] = "hoo";
        args[2] = "woo";
        Assertions.assertEquals("yaa hoo woo", Util.mergeArgs(args));
    }
}
