package org.maxgamer.quickshop.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class InteractUtilTest {


    private ConfigurationSection genConfig(int mode, boolean allowSneaking) {
        ConfigurationSection configurationSection = new MemoryConfiguration();
        configurationSection.set("shop.interact.interact-mode", mode);
        configurationSection.set("shop.interact.sneak-to-create", allowSneaking);
        return configurationSection;
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
