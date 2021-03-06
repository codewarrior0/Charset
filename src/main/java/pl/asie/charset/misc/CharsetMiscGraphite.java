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

package pl.asie.charset.misc;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "misc.graphite",
		description= "Adds a black dye replacement created from charcoal"
)
public class CharsetMiscGraphite {
	private Item graphite;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		graphite = new Item().setUnlocalizedName("charset.graphite");
		RegistryUtils.register(graphite, "graphite");

		RegistryUtils.registerModel(graphite, 0, "charset:graphite");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		OreDictionary.registerOre("dyeBlack", graphite);
		GameRegistry.addShapelessRecipe(new ItemStack(graphite, 2, 0), new ItemStack(Items.COAL, 1, 1));
	}
}
