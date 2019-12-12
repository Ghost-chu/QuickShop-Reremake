package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.maxgamer.quickshop.QuickShopTest;

import static org.junit.jupiter.api.Assertions.*;

class JSONFileTest {

    private QuickShopTest plugin;
    private IFile json;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        json = new JSONFile(plugin, "messages");
        json.create();
    }

    @Test
    void get() {
        new Assertion<>(
            "There is no 'signs.item' path",
            json.get("signs.item"),
            new IsEqual<>("{0}")
        ).affirm();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unload();
    }

}