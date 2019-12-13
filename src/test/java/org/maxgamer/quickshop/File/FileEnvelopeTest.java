package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;
import org.maxgamer.quickshop.QuickShopTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileEnvelopeTest {

    private final List<String> test = new ArrayList<>();
    private QuickShopTest plugin;
    private IFile json;
    private IFile yaml;

    @BeforeEach
    void start() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        json = new JSONFile(plugin, "messages");
        json.create();
        yaml = new YAMLFile(plugin, "messages");
        yaml.create();
        test.add("test");
        json.set("test.list", test);
        yaml.set("test.list", test);
        json.set("test.int", 13);
        yaml.set("test.int", 13);
        json.set("test.boolean", true);
        yaml.set("test.boolean", true);
        json.set("test.short", (short) 1);
        yaml.set("test.short", (short) 1);
        json.set("test.long", (long) 1);
        yaml.set("test.long", (long) 1);
        json.set("test.byte", (byte) 1);
        yaml.set("test.byte", (byte) 1);
        json.set("test.double", (double) 1);
        yaml.set("test.double", (double) 1);
    }

    @Test
    void get() {
        assertEquals(
            "{0}",
            json.get("signs.item")
        );
        assertEquals(
            "{0}",
            yaml.get("signs.item")
        );
    }

    @Test
    void getString() {
        final Optional<String> optionalS = json.getString("shop-staff-cleared");
        final Optional<String> optionalSS = yaml.getString("shop-staff-cleared");

        if (!optionalS.isPresent() || !optionalSS.isPresent()) {
            throw new IllegalArgumentException("There no path called 'shop-staff-cleared'");
        }

        assertEquals(
            "&aSuccessfully removed all staff for your shop.",
            optionalS.get()
        );
        assertEquals(
            "&aSuccessfully removed all staff for your shop.",
            optionalSS.get()
        );
    }

    @Test
    void set() {
        json.set("test.test", "test");
        yaml.set("test.test", "test");
        assertEquals(
            "{0}",
            json.get("signs.item")
        );
        assertEquals(
            "{0}",
            yaml.get("signs.item")
        );
    }

    @Test
    void create() {
        json.create();
        yaml.create();
    }

    @Test
    void save() {
        json.save();
        yaml.save();
    }

    @Test
    void getWithFallback() {
        assertEquals(
            "&aSuccessfully removed all staff for your shop.",
            json.get("shop-staff-cleared", "test")
        );
        assertEquals(
            "&aSuccessfully removed all staff for your shop.",
            yaml.get("shop-staff-cleared", "test")
        );
    }

    @Test
    void getOrSet() {
        assertEquals(
            "test",
            json.getOrSet("test.test123", "test")
        );
        assertEquals(
            "test",
            yaml.getOrSet("test.test123", "test")
        );
        assertEquals(
            "test",
            json.getOrSet("test.test123", "test")
        );
        assertEquals(
            "test",
            yaml.getOrSet("test.test123", "test")
        );
    }

    @Test
    void getStringList() {
        assertEquals(
            test,
            json.getStringList("test.list")
        );
        assertEquals(
            test,
            yaml.getStringList("test.list")
        );
    }

    @Test
    void getInt() {
        assertEquals(
            13,
            json.getInt("test.int")
        );
        assertEquals(
            13,
            yaml.getInt("test.int")
        );
    }

    @Test
    void getDouble() {
        assertEquals(
            1,
            json.getDouble("test.short")
        );
        assertEquals(
            1,
            yaml.getDouble("test.short")
        );
    }

    @Test
    void getLong() {
        assertEquals(
            1,
            json.getLong("test.short")
        );
        assertEquals(
            1,
            yaml.getLong("test.short")
        );
    }

    @Test
    void getByte() {
        assertEquals(
            (byte) 1,
            json.getByte("test.short")
        );
        assertEquals(
            (byte) 1,
            yaml.getByte("test.short")
        );
    }

    @Test
    void getShort() {
        assertEquals(
            (short) 1,
            json.getShort("test.short")
        );
        assertEquals(
            (short) 1,
            yaml.getShort("test.short")
        );
    }

    @Test
    void getBoolean() {
        assertTrue(
            json.getBoolean("test.boolean")
        );
        assertTrue(
            yaml.getBoolean("test.boolean")
        );
    }

    @Test
    void createSection() {
        json.createSection("test.section");
        yaml.createSection("test.section");
    }

    @Test
    void getSection() {
        assertTrue(
            () -> !(json.getSection("test.section") instanceof MckFileConfiguration)
        );
        assertTrue(
            () -> !(yaml.getSection("test.section") instanceof MckFileConfiguration)
        );
    }

    @Test
    void getOrCreateSection() {
        assertTrue(
            () -> !(json.getOrCreateSection("test.section") instanceof MckFileConfiguration)
        );
        assertTrue(
            () -> !(yaml.getOrCreateSection("test.section") instanceof MckFileConfiguration)
        );
    }

    @AfterEach
    void stop() {
        json.save();
        yaml.save();
        MockBukkit.unload();
    }

}
