package pl.asie.charset.pipes.modcompat.commoncapabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.pipes.pipe.TilePipe;
import pl.asie.charset.pipes.shifter.TileShifter;

@CharsetModule(
	name = "commoncapabilities:pipes.slotlessExtraction",
	dependencies = {"pipes", "mod:commoncapabilities"},
	isModCompat = true
)
public class ShifterExtractionHandlerSlotlessItems implements TileShifter.ExtractionHandler<ISlotlessItemHandler> {
	@CapabilityInject(ISlotlessItemHandler.class)
	public static Capability<ISlotlessItemHandler> CAP;

	@Mod.EventHandler
	public void register(FMLInitializationEvent event) {
		TileShifter.registerExtractionHandler(this);
	}

	@Override
	public Capability<ISlotlessItemHandler> getCapability() {
		return CAP;
	}

	@Override
	public TileShifter.ExtractionType getExtractionType() {
		return TileShifter.ExtractionType.ITEMS;
	}

	@Override
	public EnumActionResult extract(ISlotlessItemHandler handler, TilePipe output, TileShifter shifter, EnumFacing direction) {
		if (output.isLikelyToFailInsertingItem(direction))
			return EnumActionResult.FAIL;

		IItemInsertionHandler outHandler = CapabilityHelper.get(Capabilities.ITEM_INSERTION_HANDLER, output, direction.getOpposite());
		if (outHandler != null) {
			ItemStack[] filters = shifter.getFilters();
			int filterCount = 0;

			for (ItemStack filter : filters) {
				if (!filter.isEmpty()) {
					filterCount++;
					int matchFlags = filter.getHasSubtypes() ? (ItemMatch.STACKSIZE | ItemMatch.DAMAGE) : ItemMatch.STACKSIZE;
					ItemStack stack = handler.extractItem(filter, matchFlags, true);
					if (!stack.isEmpty()) {
						if (outHandler.insertItem(stack, false).isEmpty()) {
							ItemStack newStack = handler.extractItem(filter, matchFlags, false);
							return EnumActionResult.SUCCESS;
						}
					}
				}
			}

			if (filterCount == 0) {
				ItemStack stack = handler.extractItem(1, true);
				if (!stack.isEmpty()) {
					if (outHandler.insertItem(stack, false).isEmpty()) {
						ItemStack newStack = handler.extractItem(1, false);
						return EnumActionResult.SUCCESS;
					}
				}
			}
		}

		return EnumActionResult.FAIL;
	}
}
