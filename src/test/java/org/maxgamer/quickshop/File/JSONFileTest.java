package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.maxgamer.quickshop.QuickShopTest;

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
    void testAll() {
        if (plugin == null) {
            return;
        }

        new Assertion<>(
            "There is no 'shop-staff-cleared' path",
            json.get("shop-staff-cleared"),
            new IsEqual<>("&aSuccessfully removed all staff for your shop.")
        ).affirm();

        new Assertion<>(
            "The value of the path is not a String",
            json.get("shop-staff-cleared"),
            new IsInstanceOf(String.class)
        ).affirm();

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
