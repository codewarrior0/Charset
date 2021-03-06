/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

package pl.asie.charset.storage.barrels;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.items.ItemMinecartCharset;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemMinecartDayBarrel extends ItemMinecartCharset {
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (tintIndex == 1) {
                BarrelCacheInfo info = BarrelCacheInfo.from(stack);
                return ColorLookupHandler.INSTANCE.getColor(info.logStack, RenderUtils.AveragingMode.V_EDGES_ONLY);
            }

            return -1;
        }
    }

    public ItemMinecartDayBarrel() {
        super();
        setUnlocalizedName("charset.barrelCart");
    }

    @Override
    protected EntityMinecart createCart(GameProfile owner, ItemStack cart, World world, double x, double y, double z) {
        EntityMinecartDayBarrel minecart = new EntityMinecartDayBarrel(world, x, y, z);
        minecart.initFromStack(cart);
        return minecart;
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List list, boolean verbose) {
        super.addInformation(is, player, list, verbose);
        CharsetStorageBarrels.barrelItem.addExtraInformation(is, player, list, verbose);
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        if (is.hasTagCompound()) {
            String name = CharsetStorageBarrels.barrelItem.getItemStackDisplayName(is);
            return I18n.translateToLocalFormatted("item.charset.barrelCart.known.name", name);
        }
        return super.getItemStackDisplayName(is);
    }

    List<ItemStack> todaysCarts = null;

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        if (todaysCarts == null) {
            todaysCarts = new ArrayList<>();
            NonNullList<ItemStack> stacks = NonNullList.create();
            CharsetStorageBarrels.barrelBlock.getSubBlocks(CharsetStorageBarrels.barrelItem,
                    ModCharset.CREATIVE_TAB, stacks);
            for (ItemStack barrel : stacks) {
                TileEntityDayBarrel.Type type = TileEntityDayBarrel.getUpgrade(barrel);
                if (type == TileEntityDayBarrel.Type.NORMAL || type == TileEntityDayBarrel.Type.CREATIVE) {
                    ItemStack barrelCart = makeBarrel(barrel);
                    todaysCarts.add(barrelCart);
                }
            }
        }

        list.addAll(todaysCarts);
    }

    public ItemStack makeBarrel(ItemStack barrelItem) {
        ItemStack ret = new ItemStack(this, 1, barrelItem.getItemDamage());
        ret.setTagCompound((NBTTagCompound) barrelItem.getTagCompound().copy());
        return ret;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        TileEntityDayBarrel barrel = new TileEntityDayBarrel();
        barrel.loadFromStack(stack);
        return barrel.getPickedBlock();
    }
}
