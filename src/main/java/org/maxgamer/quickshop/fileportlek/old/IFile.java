///*
// * This file is a part of project QuickShop, the name is IFile.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.maxgamer.quickshop.fileportlek.old;
//
//import org.bukkit.Location;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.configuration.InvalidConfigurationException;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.maxgamer.quickshop.mock.MckFileConfiguration;
//
//import java.io.InputStream;
//import java.util.List;
//import java.util.Optional;
//
//public interface IFile {
//
//    /**
//     * Creates yml file on the path
//     */
//    void create();
//
//    @NotNull InputStream getInputStream();
//
//    /**
//     * Reloads file
//     */
//    void reload();
//
//    /**
//     * Saves file
//     */
//    void save();
//
//    /**
//     * @return the string of the file
//     */
//    @NotNull String saveToString();
//
//    /**
//     * Loads from the data
//     *
//     * @param data the data
//     * @throws InvalidConfigurationException if config is invalid
//     */
//    void loadFromString(@NotNull String data) throws InvalidConfigurationException;
//
//    /**
//     * Gets the object
//     *
//     * @param path object path to get
//     * @return if path does not exist returns null
//     */
//    @Nullable Object get(@NotNull String path);
//
//    /**
//     * Gets the object with fallback
//     *
//     * @param path     object path to get
//     * @param fallback fallback object to get if path does not exist
//     * @return if path does not exist returns fallback object
//     */
//    @Nullable Object get(@NotNull String path, @Nullable Object fallback);
//
//    /**
//     * Gets or sets the object
//     *
//     * @param path     object path to get
//     * @param fallback fallback object to get if path does not exist
//     * @param <T>      object type
//     * @return if path does not exist returns and set the path with fallback object
//     */
//    @NotNull <T> T getOrSet(@NotNull String path, @NotNull T fallback);
//
//    /**
//     * Gets location
//     *
//     * @param path location path to get
//     * @return {@link Location}
//     */
//    @NotNull Location getLocation(@NotNull String path);
//
//    /**
//     * Sets location into the path
//     *
//     * @param path     location path to set
//     * @param location {@link Location}
//     */
//    void setLocation(@NotNull String path, @NotNull Location location);
//
//    /**
//     * Pattern:
//     *
//     * <p>path: (material): 'DIAMOND' [amount]: 1 [data]: 0 [display-name]: 'Diamond Sword'
//     * [enchants]: DAMAGE_ALL: 10 FIRE_ASPECT: 2 [lore]: - 'Lore 1' - 'Lore 2'
//     *
//     * @param path itemstack path to get
//     * @return {@link ItemStack}
//     */
//    @NotNull ItemStack getCustomItemStack(@NotNull String path);
//
//    /**
//     * Sets custom itemstack to path
//     *
//     * @param path      itemstack path to set
//     * @param itemStack {@link ItemStack}
//     */
//    void setCustomItemStack(@NotNull String path, @NotNull ItemStack itemStack);
//
//    /**
//     * Gets string
//     *
//     * @param path string path to get
//     * @return {@link String}
//     */
//    @NotNull Optional<String> getString(@NotNull String path);
//
//    /**
//     * Sets object to path
//     *
//     * @param path   object path to set
//     * @param object {@link Object}
//     */
//    void set(@NotNull String path, @Nullable Object object);
//
//    /**
//     * Gets string list
//     *
//     * @param path string list path to get
//     * @return string list
//     */
//    @NotNull List<String> getStringList(@NotNull String path);
//
//    /**
//     * Gets int
//     *
//     * @param path integer path to get
//     * @return {@link Integer}
//     */
//    int getInt(@NotNull String path);
//
//    /**
//     * Gets double
//     *
//     * @param path double path to get
//     * @return {@link Double}
//     */
//    double getDouble(@NotNull String path);
//
//    /**
//     * Gets long
//     *
//     * @param path long path to get
//     * @return {@link Integer}
//     */
//    long getLong(@NotNull String path);
//
//    /**
//     * Gets byte
//     *
//     * @param path byte path to get
//     * @return {@link Byte}
//     */
//    byte getByte(@NotNull String path);
//
//    /**
//     * Gets short
//     *
//     * @param path short path to get
//     * @return {@link Short}
//     */
//    short getShort(@NotNull String path);
//
//    /**
//     * Gets boolean
//     *
//     * @param path boolean path to get
//     * @return {@link Boolean}
//     */
//    boolean getBoolean(@NotNull String path);
//
//    /**
//     * Creates configuration section into path
//     *
//     * @param path configuration section path to create
//     * @return {@link ConfigurationSection}
//     */
//    @NotNull ConfigurationSection createSection(@NotNull String path);
//
//    /**
//     * Gets configuration section from path
//     *
//     * @param path configurations section path to get
//     * @return if there isn't section returns {@link MckFileConfiguration}
//     */
//    @NotNull ConfigurationSection getSection(@NotNull String path);
//
//    /**
//     * Gets configuration section from path if there is no section It will create
//     *
//     * @param path configurations section path to get
//     * @return {@link ConfigurationSection}
//     */
//    @NotNull ConfigurationSection getOrCreateSection(@NotNull String path);
//
//}
