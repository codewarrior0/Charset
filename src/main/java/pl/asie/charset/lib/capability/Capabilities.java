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

package pl.asie.charset.lib.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.capability.audio.DefaultAudioReceiver;
import pl.asie.charset.lib.capability.audio.DefaultAudioSource;
import pl.asie.charset.lib.capability.inventory.DefaultItemInsertionHandler;
import pl.asie.charset.lib.capability.lib.DefaultAxisRotatable;
import pl.asie.charset.lib.capability.lib.DefaultDebuggable;
import pl.asie.charset.lib.capability.lib.DefaultMovable;
import pl.asie.charset.lib.capability.pipe.DefaultPipeView;
import pl.asie.charset.lib.capability.providers.CapabilityWrapperFluidStacks;
import pl.asie.charset.lib.capability.providers.CapabilityWrapperInsertionToItemHandler;
import pl.asie.charset.lib.capability.providers.CapabilityWrapperInventory;
import pl.asie.charset.lib.capability.redstone.DefaultBundledEmitter;
import pl.asie.charset.lib.capability.redstone.DefaultBundledEmitterStorage;
import pl.asie.charset.lib.capability.redstone.DefaultRedstoneEmitter;
import pl.asie.charset.lib.capability.redstone.DefaultRedstoneEmitterStorage;
import pl.asie.charset.lib.capability.redstone.DummyRedstoneReceiver;

public class Capabilities {
	@CapabilityInject(IAudioSource.class)
	public static Capability<IAudioSource> AUDIO_SOURCE;
	@CapabilityInject(IAudioReceiver.class)
	public static Capability<IAudioReceiver> AUDIO_RECEIVER;

	@CapabilityInject(IAxisRotatable.class)
	public static Capability<IAxisRotatable> AXIS_ROTATABLE;
	@CapabilityInject(IDebuggable.class)
	public static Capability<IDebuggable> DEBUGGABLE;
	@CapabilityInject(IMovable.class)
	public static Capability<IMovable> MOVABLE;

	@CapabilityInject(IItemInsertionHandler.class)
	public static Capability<IItemInsertionHandler> ITEM_INSERTION_HANDLER;
	@CapabilityInject(IPipeView.class)
	public static Capability<IPipeView> PIPE_VIEW;

	@CapabilityInject(IBundledEmitter.class)
	public static Capability<IBundledEmitter> BUNDLED_EMITTER;
	@CapabilityInject(IBundledReceiver.class)
	public static Capability<IBundledReceiver> BUNDLED_RECEIVER;
	@CapabilityInject(IRedstoneEmitter.class)
	public static Capability<IRedstoneEmitter> REDSTONE_EMITTER;
	@CapabilityInject(IRedstoneReceiver.class)
	public static Capability<IRedstoneReceiver> REDSTONE_RECEIVER;

	public static void init() {
		CapabilityManager.INSTANCE.register(IAudioSource.class, new NullCapabilityStorage<IAudioSource>(), DefaultAudioSource::new);
		CapabilityManager.INSTANCE.register(IAudioReceiver.class, new NullCapabilityStorage<IAudioReceiver>(), DefaultAudioReceiver::new);

		CapabilityManager.INSTANCE.register(IAxisRotatable.class, new NullCapabilityStorage<IAxisRotatable>(), DefaultAxisRotatable::new);
		CapabilityManager.INSTANCE.register(IDebuggable.class, new NullCapabilityStorage<IDebuggable>(), DefaultDebuggable::new);
		CapabilityManager.INSTANCE.register(IMovable.class, new NullCapabilityStorage<IMovable>(), DefaultMovable::new);

		CapabilityManager.INSTANCE.register(IItemInsertionHandler.class, new NullCapabilityStorage<IItemInsertionHandler>(), DefaultItemInsertionHandler::new);
		CapabilityManager.INSTANCE.register(IPipeView.class, new NullCapabilityStorage<IPipeView>(), DefaultPipeView::new);

		CapabilityManager.INSTANCE.register(IBundledEmitter.class, new DefaultBundledEmitterStorage(), DefaultBundledEmitter::new);
		CapabilityManager.INSTANCE.register(IRedstoneEmitter.class, new DefaultRedstoneEmitterStorage(), DefaultRedstoneEmitter::new);
		CapabilityManager.INSTANCE.register(IBundledReceiver.class, new NullCapabilityStorage<IBundledReceiver>(), DummyRedstoneReceiver::new);
		CapabilityManager.INSTANCE.register(IRedstoneReceiver.class, new NullCapabilityStorage<IRedstoneReceiver>(), DummyRedstoneReceiver::new);

		if (Loader.isModLoaded("mcmultipart")) {
			initMultiplePants();
		}
 	}

	@Optional.Method(modid = "mcmultipart")
	private static void initMultiplePants() {
		// TODO 1.11
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new AudioReceiverWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new BundledEmitterWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new RedstoneEmitterWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new BundledReceiverWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new RedstoneReceiverWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new DebuggableWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new ItemInsertionHandlerWrapper());
//		CapabilityWrapperRegistry.registerCapabilityWrapper(new PipeViewWrapper());
	}

	public static void registerVanillaWrappers() {
		CapabilityHelper.registerWrapper(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new CapabilityWrapperInventory());
		CapabilityHelper.registerWrapper(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, new CapabilityWrapperFluidStacks());
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, new CapabilityWrapperInsertionToItemHandler());
	}
}
