/*
 * This file is a part of project QuickShop, the name is MojangAPITest.java
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

package org.maxgamer.quickshop.util.mojangapi;

import org.junit.Assert;
import org.junit.Test;

public class MojangAPITest {
  @Test
  public void testMojangMetaApi() {
    MojangAPI api = new MojangAPI();
    Assert.assertTrue(api.getMetaAPI("1.16.5").get().isPresent());
    Assert.assertFalse(api.getMetaAPI("1.16.5").get().get().isEmpty());
  }

  @Test
  public void testMojangAssetsApi() {
    MojangAPI api = new MojangAPI();
    Assert.assertTrue(api.getAssetsAPI("1.16.5").isAvailable());
    Assert.assertTrue(
        api.getAssetsAPI("1.16.5").getGameAssetsFile().isPresent());
    Assert.assertFalse(api.getAssetsAPI("1.16.5")
                           .getGameAssetsFile()
                           .get()
                           .getContent()
                           .isEmpty());
    Assert.assertFalse(
        api.getAssetsAPI("1.16.5").getGameAssetsFile().get().getId().isEmpty());
    Assert.assertFalse(api.getAssetsAPI("1.16.5")
                           .getGameAssetsFile()
                           .get()
                           .getSha1()
                           .isEmpty());
  }
}
