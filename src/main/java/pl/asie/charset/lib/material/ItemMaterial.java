package pl.asie.charset.lib.material;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;

public final class ItemMaterial {
	private final String id;
	private final ItemStack stack;

	protected ItemMaterial(ItemStack stack) {
		this.id = ItemMaterialRegistry.createId(stack);
		this.stack = stack;
	}

	public Collection<String> getTypes() {
		return ItemMaterialRegistry.INSTANCE.getMaterialTypes(this);
	}

	public ItemMaterial getRelated(String relation) {
		return ItemMaterialRegistry.INSTANCE.materialRelations.get(this, relation);
	}

	public String getId() {
		return id;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public String toString() {
		return "ItemMaterial[" + id + "]";
	}
}
