/*
 * This file is a part of project QuickShop, the name is InteractUtilTest.java
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

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.sections.FlatFileSection;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class InteractUtilTest {


    private FlatFileSection genConfig(int mode, boolean allowSneaking) {
        Yaml configurationSection = LightningBuilder.fromFile(new File("unit-test.yml")).createYaml();
        configurationSection.set("shop.interact.interact-mode", mode);
        configurationSection.set("shop.interact.sneak-to-create", allowSneaking);
        return configurationSection.getSection("");
    }

    @Test
    public void testInteractBoolean() {
        //ONLY
        InteractUtil.init(genConfig(0, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(0, false));
        Assert.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        //BOTH
        InteractUtil.init(genConfig(1, false));
        Assert.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(1, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        //REVERSED
        InteractUtil.init(genConfig(2, false));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(2, true));
        Assert.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assert.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));

    }
}
