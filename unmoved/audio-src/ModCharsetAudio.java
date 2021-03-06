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

package pl.asie.charset.audio;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.audio.note.NoteBlockManager;
import pl.asie.charset.audio.note.PacketNoteParticle;
import pl.asie.charset.audio.recipe.RecipeTape;
import pl.asie.charset.audio.recipe.RecipeTapeReel;
import pl.asie.charset.audio.storage.DataStorageImpl;
import pl.asie.charset.audio.storage.DataStorageManager;
import pl.asie.charset.audio.storage.DataStorageStorage;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.audio.tape.ItemTapeReel;
import pl.asie.charset.lib.ModCharset;
import pl.asie.charset.lib.ModuleBase;

import java.io.IOException;

@Mod(modid = ModCharsetAudio.MODID, name = ModCharsetAudio.NAME, version = ModCharsetAudio.VERSION,
		dependencies = ModCharset.DEP_DEFAULT, updateJSON = ModCharset.UPDATE_URL)
public class ModCharsetAudio extends ModuleBase {
	public static final String MODID = "charsetaudio";
	public static final String NAME = "♫";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.audio.ProxyClient", serverSide = "pl.asie.charset.audio.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetAudio instance;

	@CapabilityInject(IDataStorage.class)
	public static Capability<IDataStorage> CAP_STORAGE;

	public static DataStorageManager storage;

	// public static AudioCableFactory audioCableFactory;
	// public static ItemPartTapeDrive partTapeDriveItem;
	public static ItemTape tapeItem;
	public static Item magneticTapeItem, tapeReelItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// audioCableFactory = (AudioCableFactory) new AudioCableFactory().setRegistryName(new ResourceLocation("charsetaudio:cable"));
		// WireManager.register(audioCableFactory);

		// partTapeDriveItem = new ItemPartTapeDrive();
		// GameRegistry.register(partTapeDriveItem.setRegistryName("tapeDrive"));

		tapeItem = new ItemTape();
		GameRegistry.register(tapeItem.setRegistryName("tape"));

		magneticTapeItem = new Item().setCreativeTab(ModCharset.CREATIVE_TAB).setUnlocalizedName("charset.tapeitem");
		tapeReelItem = new ItemTapeReel().setHasSubtypes(true).setCreativeTab(ModCharset.CREATIVE_TAB).setUnlocalizedName("charset.tapereel");
		GameRegistry.register(magneticTapeItem.setRegistryName("tapeMagnetic"));
		GameRegistry.register(tapeReelItem.setRegistryName("tapeReel"));

		// MultipartRegistry.registerPart(PartTapeDrive.class, "charsetaudio:tapedrive");

		// ModCharset.proxy.registerItemModel(partTapeDriveItem, 0, "charsetaudio:tapedrive");
		ModCharset.proxy.registerItemModel(tapeItem, 0, "charsetaudio:tape");
		ModCharset.proxy.registerItemModel(magneticTapeItem, 0, "charsetaudio:tapeMagnetic");
		for (int i = 0; i <= 128; i++) {
			String s = "charsetaudio:tapeReel#inventory";
			if (i >= 112) {
				s += "_4";
			} else if (i >= 48) {
				s += "_3";
			} else if (i >= 16) {
				s += "_2";
			} else if (i > 0) {
				s += "_1";
			}
			ModCharset.proxy.registerItemModel(tapeReelItem, i, s);
		}

		CapabilityManager.INSTANCE.register(IDataStorage.class, new DataStorageStorage(), DataStorageImpl.class);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new NoteBlockManager());

		packet.registerPacket(0x01, PacketNoteParticle.class);

		// packet.registerPacket(0x10, PacketDriveState.class);
		// packet.registerPacket(0x13, PacketDriveRecord.class);
		// packet.registerPacket(0x14, PacketDriveCounter.class);

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tapeReelItem), " i ", "ipi", " i ", 'i', "ingotIron", 'p', "paper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(magneticTapeItem, 32), "ddd", "rir", "ddd", 'd', "dyeBlack", 'r', "dustRedstone", 'i', "ingotIron"));
		// GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(partTapeDriveItem), "igi", "rRr", "ipi", 'g', "blockGlass", 'p', Blocks.PISTON, 'R', new ItemStack(tapeReelItem, 1, 0), 'r', Items.REDSTONE, 'i', "ingotIron"));

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerAudio());

		GameRegistry.addRecipe(new RecipeTapeReel());
		RecipeSorter.register("charsetaudio:tapeReel", RecipeTapeReel.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

		GameRegistry.addRecipe(new RecipeTape());
		RecipeSorter.register("charsetaudio:tape", RecipeTape.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

		proxy.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("notenoughcodecs")) {
			logger.info("NotEnoughCodecs present, MP3 and MP4 support available");
		}
	}

	@Mod.EventHandler
	public void serverStart(FMLServerStartedEvent event) {
		storage = new DataStorageManager();
		MinecraftForge.EVENT_BUS.register(storage);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		if (storage != null) {
			try {
				storage.save();
			} catch (IOException e) {

			}
			MinecraftForge.EVENT_BUS.unregister(storage);
		}
		storage = null;
	}
}
