package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.QuickShopTest;

class JSONFileTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockBukkit.load(QuickShopTest.class);
    }

    @Test
    void reload() {

    }

    @Test
    void save() {

    }

    @AfterEach
    void tearDown() {
        MockBukkit.unload();
    }
}