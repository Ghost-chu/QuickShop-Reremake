/*
 * This file is a part of project QuickShop, the name is BKCommonLibPerformance.java
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

package org.maxgamer.quickshop.util.wrapper.performance;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BKCommonLibPerformance implements PerformanceUtil {
    @Override
    public String getName() {
        return "BKCommonLib";
    }

    @Override
    public BlockState getState(Block block) {
        return BlockUtil.getState(block);
    }

    @Override
    public <T extends BlockState> T getState(Block block, Class<T> clazz) {
        return BlockUtil.getState(block, clazz);
    }

    @Override
    public Block getAttached(Block block) {
        return BlockUtil.getAttachedBlock(block);
    }

}
