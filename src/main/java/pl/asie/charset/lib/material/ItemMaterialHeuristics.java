package pl.asie.charset.lib.material;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

import java.util.Collection;
import java.util.function.Consumer;

public final class ItemMaterialHeuristics {
    private static boolean initialized = false;
    private static ItemMaterialRegistry reg;

    private ItemMaterialHeuristics() {

    }

    private static void addResultingBlock(ItemMaterial base, ItemStack result, String source, String target) {
        if (!result.isEmpty()) {
            result.setCount(1);
            ItemMaterial slabMaterial = reg.getOrCreateMaterial(result);
            if (reg.registerType(slabMaterial, target)) {
                reg.registerRelation(base, slabMaterial, target, source);
                for (String s : base.getTypes()) {
                    if (!s.equals(source)) {
                        reg.registerType(slabMaterial, s);
                    }
                }
            }
        }
    }

    private static void findSlab(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("slab") != null)
            return;

        ItemStack slab = RecipeUtils.getCraftingResult(null, 3, 3,
                null, null, null,
                null, null, null,
                base.getStack(), base.getStack(), base.getStack());
        addResultingBlock(base, slab, "block", "slab");
    }

    private static void findStair(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("stairs") != null)
            return;

        ItemStack slab = RecipeUtils.getCraftingResult(null, 3, 3,
                null, null, base.getStack(),
                null, base.getStack(), base.getStack(),
                base.getStack(), base.getStack(), base.getStack());
        addResultingBlock(base, slab, "block", "stairs");
    }

    private static void initLogMaterial(ItemStack log) {
        // We look for the plank first to ensure only valid logs
        // get registered.
        ItemStack plank = RecipeUtils.getCraftingResult(null, 3, 3, log);
        if (!plank.isEmpty() && ItemUtils.isOreType(plank, "plankWood")) {
            ItemMaterial logMaterial = reg.getOrCreateMaterial(log);
            if (reg.registerTypes(logMaterial, "log", "wood", "block")) {
                plank.setCount(1);
                ItemMaterial plankMaterial = reg.getOrCreateMaterial(plank);
                if (reg.registerTypes(plankMaterial, "plank", "wood", "block")) {
                    reg.registerRelation(logMaterial, plankMaterial, "plank", "log");

                    ItemStack stick = RecipeUtils.getCraftingResult(null, 2, 2,
                            plank, null,
                            plank, null);
                    if (stick.isEmpty()) {
                        stick = new ItemStack(Items.STICK);
                    } else {
                        stick.setCount(1);
                    }

                    ItemMaterial stickMaterial = reg.getOrCreateMaterial(stick);
                    if (reg.registerTypes(stickMaterial, "stick", "wood", "item")) {
                        reg.registerRelation(plankMaterial, stickMaterial, "stick", "plank");
                        reg.registerRelation(stickMaterial, logMaterial, "log");
                    }
                }
            }
        }
    }

    private static boolean containsOreDict(ItemStack stack, String entry) {
        return ArrayUtils.contains(OreDictionary.getOreIDs(stack), OreDictionary.getOreID(entry));
    }

    private static int indexOfUpper(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                return i;
            }
        }

        return -1;
    }

    // TODO: Tie ores to ingots
    private static void initIngotLikeMaterial(String oreName, ItemStack stack) {
        int splitPoint = indexOfUpper(oreName, 0);
        if (splitPoint < 0) return;

        String prefix = oreName.substring(0, splitPoint);
        String suffixU = oreName.substring(splitPoint);
        String suffix = suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1);
        ItemMaterial ingotMat = reg.getOrCreateMaterial(stack);

        reg.registerTypes(ingotMat, prefix, suffix, "item");

        // Try crafting a nugget
        if (prefix.equals("ingot")) {
            ItemStack nugget = RecipeUtils.getCraftingResult(null, 1, 1,
                    stack);
            if (!nugget.isEmpty() && containsOreDict(nugget, "nugget" + suffixU)) {
                ItemMaterial nuggetMat = reg.getOrCreateMaterial(nugget);
                reg.registerTypes(nuggetMat, "nugget", suffix, "item");
                reg.registerRelation(ingotMat, nuggetMat, "nugget", prefix);
            }
        }

        // Try crafting a block
        ItemStack block = RecipeUtils.getCraftingResult(null, 3, 3,
                stack, stack, stack,
                stack, stack, stack,
                stack, stack, stack);
        if (!block.isEmpty() && containsOreDict(block, "block" + suffixU)) {
            ItemMaterial blockMat = reg.getOrCreateMaterial(block);
            reg.registerTypes(blockMat, suffix, "block");
            reg.registerRelation(ingotMat, blockMat, "block", prefix);
        }
    }

    private static void initOreMaterial(String oreName) {
        String prefix = "ore";
        String suffixU = oreName.substring(prefix.length());
        String suffix = suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1);

        // Create ore materials for each ore
        supplyExpandedStacks(OreDictionary.getOres(oreName), (stack -> reg.registerTypes(reg.getOrCreateMaterial(stack), prefix, suffix, "block")));
    }

    private static void initStoneMaterial(String oreName, ItemStack stack) {
        if (oreName.endsWith("Polished")) return;
        String prefix = "stone";
        String suffixU = oreName.substring(prefix.length());
        String suffix = suffixU.length() > 0 ? suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1) : "";

        ItemMaterial stoneMat = reg.getOrCreateMaterial(stack);
        reg.registerTypes(stoneMat, "stone", suffix, "block");

        // Try crafting a brick
        ItemStack block = RecipeUtils.getCraftingResult(null, 2, 2,
                stack, stack,
                stack, stack);
        if (!block.isEmpty()) {
            ItemMaterial brickMat = reg.getOrCreateMaterial(block);
            reg.registerTypes(brickMat, "stone", "brick", suffix, "block");
            reg.registerRelation(stoneMat, brickMat, "brick", "parent");
        }
    }

    private static void initCobblestoneMaterial(String oreName, ItemStack stack) {
        ItemMaterial cobbleMat = reg.getOrCreateMaterial(stack);
        reg.registerTypes(cobbleMat, "cobblestone", "block");

        ItemStack stoneStack = FurnaceRecipes.instance().getSmeltingResult(stack);
        ItemMaterial stoneMaterial = reg.getMaterialIfPresent(stoneStack);
        if (stoneMaterial == null) {
            ModCharset.logger.warn("Found OreDict cobblestone which does not give OreDict stone -> " + cobbleMat.toString());
        } else {
            reg.registerRelation(cobbleMat, stoneMaterial, "stone", "cobblestone");
        }
    }

    private static void supplyExpandedStacks(Collection<ItemStack> stacks, Consumer<ItemStack> stackConsumer) {
        for (ItemStack log : stacks) {
            try {
                if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    for (int i = 0; i < (log.getItem() instanceof ItemBlock ? 16 : 128); i++) {
                        ItemStack stack = new ItemStack(log.getItem(), 1, i);
                        stackConsumer.accept(stack);
                    }
                } else {
                    stackConsumer.accept(log.copy());
                }
            } catch (Exception e) {

            }
        }
    }

    public static void init() {
        if (initialized)
            return;

        ProgressManager.ProgressBar bar = ProgressManager.push("Material scanning", 6);

        reg = ItemMaterialRegistry.INSTANCE;
        initialized = true;

        bar.step("Wood");
        supplyExpandedStacks(OreDictionary.getOres("logWood", false), ItemMaterialHeuristics::initLogMaterial);

        bar.step("Ores");
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("ore")) {
                initOreMaterial(oreName);
            }
        }

        bar.step("Ingots/Dusts/Gems");
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("ingot") || oreName.startsWith("dust") || oreName.startsWith("gem")) {
                supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initIngotLikeMaterial(oreName, s)));
            }
        }

        bar.step("Stones");
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("stone")) {
                supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initStoneMaterial(oreName, s)));
            }
        }

        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("cobblestone")) {
                supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initCobblestoneMaterial(oreName, s)));
            }
        }

        bar.step("Misc");
        reg.registerTypes(reg.getOrCreateMaterial(new ItemStack(Blocks.BEDROCK)), "block", "bedrock");

        bar.step("Slabs/Stairs");
        for (ItemMaterial material : reg.getMaterialsByType("block")) {
            findSlab(material);
            findStair(material);
        }

        ProgressManager.pop(bar);
    }
}
