package org.maxgamer.quickshop;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Use for testing related to bukkit api
 * <p>
 * If you are writing test which using runtime stuff, just extend it
 */
public abstract class TestBukkitBase {
    @BeforeClass
    public static void setUp() {
        MockBukkit.mock();
        MockBukkit.load(QuickShop.class);
    }

    @AfterClass
    public static void tearDown() {
        MockBukkit.unmock();
    }
}
