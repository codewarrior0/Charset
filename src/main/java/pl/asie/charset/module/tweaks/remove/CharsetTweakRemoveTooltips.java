package pl.asie.charset.module.tweaks.remove;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
        name = "tweak.remove.itemTooltips",
        profile = ModuleProfile.STABLE,
        isDefault = false
)
public class CharsetTweakRemoveTooltips {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        event.getToolTip().clear();
    }
}
