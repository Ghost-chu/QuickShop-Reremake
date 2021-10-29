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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class UtilTest {

    @Test
    public void array2String() {
        String sample = "A, B, C";
        Assert.assertEquals(sample, Util.array2String(new String[]{"A", "B", "C"}));
    }

    @Test
    public void boolean2Status() {
        Assert.assertEquals(Util.boolean2Status(true), "Enabled");
        Assert.assertEquals(Util.boolean2Status(false), "Disabled");
    }

    @Test
    public void isClassAvailable() {
        Assert.assertTrue(Util.isClassAvailable(getClass().getName()));
        Assert.assertFalse(Util.isClassAvailable("random.Class"));
    }

    @Test
    public void isUUID() {
        Assert.assertTrue(Util.isUUID("b188beda-8bfb-ed66-65e5-25147a4617cf"));
        Assert.assertFalse(Util.isUUID("b188beda8bfbed6665e525147a4617cf"));
        Assert.assertFalse(Util.isUUID("?"));
    }

    @Test
    public void list2String() {
        String sample = "1, 2, 3, 4, 5";
        Assert.assertEquals(sample, Util.list2String(Arrays.asList("1", "2", "3", "4", "5")));
    }

    @Test
    public void firstUppercase() {
        Assert.assertEquals("Quickshop", Util.firstUppercase("quickshop"));
    }

    @Test
    public void mergeArgs() {
        String[] args = new String[3];
        args[0] = "yaa";
        args[1] = "hoo";
        args[2] = "woo";
        Assert.assertEquals("yaa hoo woo", Util.mergeArgs(args));
    }

    @Test
    public void testArray2String() {
        String[] array = new String[]{"aaa", "bbb", "ccc", "ddd"};
        Assert.assertEquals("aaa, bbb, ccc, ddd", Util.array2String(array));
    }

    @Test
    public void testIsClassAvailable() {
        Assert.assertTrue(Util.isClassAvailable("java.lang.String"));
        Assert.assertFalse(Util.isClassAvailable("java.lang.NotExistedClassLoL"));
    }

    @Test
    public void isMethodAvailable() {
        Assert.assertTrue(Util.isMethodAvailable(String.class.getName(), "toLowerCase"));
        Assert.assertFalse(Util.isMethodAvailable(String.class.getName(), "P90 RUSH B"));
    }

    @Test
    public void testIsUUID() {
        UUID uuid = UUID.randomUUID();
        Assert.assertTrue(Util.isUUID(uuid.toString()));
        Assert.assertTrue(Util.isUUID(Util.getNilUniqueId().toString()));
        Assert.assertFalse(Util.isUUID(uuid.toString().replace("-", "")));
    }

    @Test
    public void prettifyText() {
        Assert.assertEquals("Diamond", Util.prettifyText("DIAMOND"));
    }

    @Test
    public void testFirstUppercase() {
        Assert.assertEquals("Foobar", Util.firstUppercase("foobar"));
    }

    @Test
    public void testMergeArgs() {
    }

    @Test
    public void getNilUniqueId() {
        Assert.assertEquals(new UUID(0, 0), Util.getNilUniqueId());
    }
}
