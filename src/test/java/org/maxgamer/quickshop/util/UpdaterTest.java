package org.maxgamer.quickshop.util;


import org.junit.Assert;
import org.junit.Test;

public class UpdaterTest {

    @Test
    public void TestUpdateVer() {
        Assert.assertTrue(Updater.hasUpdate("3", "4"));
        Assert.assertFalse(Updater.hasUpdate("3.2.3", "3.2.3"));
        Assert.assertTrue(Updater.hasUpdate("3.2.0", "3.2.3"));
        Assert.assertTrue(Updater.hasUpdate("3.2.3", "3.2.3.1"));
        Assert.assertTrue(Updater.hasUpdate("3.2.3", "3.2.3.0"));
        Assert.assertTrue(Updater.hasUpdate("3.2.3.0", "3.2.3.1"));
        Assert.assertFalse(Updater.hasUpdate("3.2.3.1.3", "3.2.3.1"));
    }
}
