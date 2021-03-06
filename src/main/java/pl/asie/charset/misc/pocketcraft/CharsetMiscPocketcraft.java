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

package pl.asie.charset.misc.pocketcraft;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "misc.pocketcraft",
		description = "Adds a Pocket Crafting Table"
)
public class CharsetMiscPocketcraft {
	public static final int GUI_ID = 0x100;

	@CharsetModule.Instance
	public static CharsetMiscPocketcraft instance;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static ItemPocketTable pocketTable;
	public static String pocketActions = "xcbf";

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RegistryUtils.register(pocketTable = new ItemPocketTable(), "pocketTable");
		RegistryUtils.registerModel(pocketTable, 0, "charset:pocket_crafting_table");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(pocketTable),
				" c", "s ", 's', "stickWood", 'c', "workbench"));

		packet.registerPacket(0x01, PacketPTAction.class);
		GuiHandlerCharset.INSTANCE.register(GUI_ID, Side.SERVER, (r) -> new ContainerPocketTable(r.player));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		GuiHandlerCharset.INSTANCE.register(GUI_ID, Side.CLIENT, (r) -> new GuiPocketTable(new ContainerPocketTable(r.player)));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
