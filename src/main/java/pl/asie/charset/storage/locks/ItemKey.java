/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.locks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.ModCharset;

import java.util.List;

public class ItemKey extends Item implements IKeyItem {
    static final boolean DEBUG_KEY_ID = false;

    public ItemKey() {
        super();
        setCreativeTab(ModCharset.CREATIVE_TAB);
        setUnlocalizedName("charset.key");
    }

    // ""security""
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("key")) {
            NBTTagCompound tag = stack.getTagCompound().copy();
            tag.removeTag("key");
            return tag;
        } else {
            return stack.getTagCompound();
        }
    }

    public String getKey(ItemStack stack) {
        return "charset:key:" + getRawKey(stack);
    }

    public String getRawKey(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("key") ? stack.getTagCompound().getString("key") : "null";
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        ItemStack result = stack.copy();
        if (result.getCount() < 1) {
            result.setCount(1);
        }
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (ItemKey.DEBUG_KEY_ID) {
            tooltip.add(getKey(stack));
        }
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        return getKey(stack).equals(lock);
    }
}
