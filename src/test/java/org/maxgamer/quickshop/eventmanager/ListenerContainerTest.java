/*
 * This file is a part of project QuickShop, the name is ListenerContainerTest.java
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

package org.maxgamer.quickshop.eventmanager;


import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Test;

public class ListenerContainerTest {

    @Test
    public void testMatches() {
        Plugin testPlugin = new JavaPlugin() {

        };
        ListenerContainer listenerContainerA = new ListenerContainer(null, "@QuickTest");
        ListenerContainer listenerContainerB = new ListenerContainer(null, "@QuickTestBad");
        Assert.assertTrue(listenerContainerA.matches(this.getClass(), testPlugin));
        Assert.assertFalse(listenerContainerB.matches(this.getClass(), testPlugin));
    }

}