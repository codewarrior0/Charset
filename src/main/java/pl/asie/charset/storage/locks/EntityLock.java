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

import com.google.common.base.Predicate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pl.asie.charset.api.storage.IKeyItem;

public class EntityLock extends EntityHanging implements IEntityAdditionalSpawnData {
    static final DataParameter<Integer> COLOR_0 = EntityDataManager.createKey(EntityLock.class, DataSerializers.VARINT);
    static final DataParameter<Integer> COLOR_1 = EntityDataManager.createKey(EntityLock.class, DataSerializers.VARINT);

    private static final DataParameter<EnumFacing> HANGING_ROTATION = EntityDataManager.createKey(EntityLock.class, DataSerializers.FACING);
    private static final Predicate<Entity> IS_HANGING_ENTITY = new Predicate<Entity>() {
        public boolean apply(Entity entity) {
            return entity instanceof EntityHanging;
        }
    };

    private String lockKey = null;
    private String prefixedLockKey = null;
    protected int[] colors = new int[] { -1, -1 };
    private TileEntity tileCached;

    public EntityLock(World worldIn) {
        super(worldIn);
    }

    public EntityLock(World worldIn, ItemStack stack, BlockPos pos, EnumFacing facing) {
        super(worldIn, pos);
        this.setColors(stack.getTagCompound());
        this.setLockKey(((ItemLock) stack.getItem()).getRawKey(stack));
        this.updateFacingWithBoundingBox(facing);
    }

    private void setColors(NBTTagCompound compound) {
        if (compound != null) {
            colors[0] = compound.hasKey("color0") ? compound.getInteger("color0") : -1;
            colors[1] = compound.hasKey("color1") ? compound.getInteger("color1") : -1;
        } else {
            colors[0] = -1;
            colors[1] = -1;
        }
    }

    private void setLockKey(String s) {
        this.lockKey = s;
        this.prefixedLockKey = s != null ? "charset:key:" + s : null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setLockKey(compound.hasKey("key") ? compound.getString("key") : null);
        setColors(compound);
    }

    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            return I18n.translateToLocal("item.charset.lock.name");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (lockKey != null) {
            compound.setString("key", lockKey);
        }
        if (colors[0] != -1) {
            compound.setInteger("color0", colors[0]);
        }
        if (colors[1] != -1) {
            compound.setInteger("color1", colors[1]);
        }
    }

    public TileEntity getAttachedTile() {
        if (tileCached == null || tileCached.isInvalid()) {
            BlockPos pos = this.hangingPosition.offset(this.facingDirection.getOpposite());
            tileCached = world.getTileEntity(pos);
        }

        return tileCached;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setEntityInvulnerable(true);
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer && entityIn.isSneaking()) {
            if (!this.world.isRemote) {
                ItemStack stack = ((EntityPlayer) entityIn).getHeldItemMainhand();
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemKey) || !(((ItemKey) stack.getItem()).canUnlock(prefixedLockKey, stack))) {
                    stack = ((EntityPlayer) entityIn).getHeldItemOffhand();
                    if (stack.isEmpty() || !(stack.getItem() instanceof ItemKey) || !(((ItemKey) stack.getItem()).canUnlock(prefixedLockKey, stack))) {
                        return super.hitByEntity(entityIn);
                    }
                }

                if (!this.isDead) {
                    this.setDead();
                    this.onBroken(entityIn);
                }

                return true;
            } else {
                return super.hitByEntity(entityIn);
            }
        }

        return super.hitByEntity(entityIn);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote && lockKey != null && lockKey.length() > 0) {
            if (getAttachedTile() instanceof ILockableContainer) {
                ILockableContainer container = (ILockableContainer) tileCached;
                if (!container.isLocked() || !prefixedLockKey.equals(container.getLockCode().getLock())) {
                    container.setLockCode(new LockCode(prefixedLockKey));
                }
            }
        }
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (!world.isRemote && hand == EnumHand.MAIN_HAND && lockKey != null) {
            ItemStack stack = player.getHeldItem(hand);

            boolean canUnlock = false;
            if (!stack.isEmpty() && stack.getItem() instanceof IKeyItem) {
                IKeyItem key = (IKeyItem) stack.getItem();
                canUnlock = key.canUnlock(prefixedLockKey, stack);
            }

            if (!canUnlock) {
                stack = player.getHeldItemOffhand();
                if (!stack.isEmpty() && stack.getItem() instanceof IKeyItem) {
                    IKeyItem key = (IKeyItem) stack.getItem();
                    canUnlock = key.canUnlock(prefixedLockKey, stack);
                }
            }

            if (getAttachedTile() instanceof ILockableContainer) {
                ILockableContainer container = (ILockableContainer) tileCached;
                if (canUnlock) {
                    unlockContainer();
                }

                BlockPos pos = this.hangingPosition.offset(this.facingDirection.getOpposite());
                IBlockState state = world.getBlockState(pos);

                state.getBlock().onBlockActivated(world, pos, state, player, hand, this.facingDirection,
                        0.5F + this.facingDirection.getFrontOffsetX() * 0.5F,
                        0.5F + this.facingDirection.getFrontOffsetY() * 0.5F,
                        0.5F + this.facingDirection.getFrontOffsetZ() * 0.5F
                );

                if (canUnlock) {
                    container.setLockCode(new LockCode(prefixedLockKey));
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            }
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public float getCollisionBorderSize()
    {
        return 0.0F;
    }

    @Override
    public boolean onValidSurface() {
        if (getAttachedTile() instanceof ILockableContainer) {
            ILockableContainer container = (ILockableContainer) tileCached;
            if (container.isLocked() && prefixedLockKey.equals(container.getLockCode().getLock())) {
                return true;
            }
        }

        if (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
            return false;
        } else {
            BlockPos pos = this.hangingPosition.offset(this.facingDirection.getOpposite());
            TileEntity tile = world.getTileEntity(pos);

            if (!(tile instanceof ILockableContainer)) {
                return false;
            }

            return this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), IS_HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    public int getWidthPixels() {
        return 8;
    }

    @Override
    public int getHeightPixels() {
        return 8;
    }

    private void unlockContainer() {
        if (getAttachedTile() instanceof ILockableContainer) {
            unlockContainer((ILockableContainer) tileCached);
        }

        if (tileCached instanceof TileEntityChest) {
            // working around vanilla bugs as usual, this time large chests
            // seem to enjoy syncing codes but only when InventoryLargeChest
            // is instantiated, so we have to undo it ourselves.
            ((TileEntityChest) tileCached).checkForAdjacentChests();
            if (((TileEntityChest) tileCached).adjacentChestXNeg != null) {
                unlockContainer(((TileEntityChest) tileCached).adjacentChestXNeg);
            }
            if (((TileEntityChest) tileCached).adjacentChestXPos != null) {
                unlockContainer(((TileEntityChest) tileCached).adjacentChestXPos);
            }
            if (((TileEntityChest) tileCached).adjacentChestZNeg != null) {
                unlockContainer(((TileEntityChest) tileCached).adjacentChestZNeg);
            }
            if (((TileEntityChest) tileCached).adjacentChestZPos != null) {
                unlockContainer(((TileEntityChest) tileCached).adjacentChestZPos);
            }
        }
    }


    private void unlockContainer(ILockableContainer container) {
        if (container.isLocked()) {
            if (lockKey != null) {
                if (!prefixedLockKey.equals(container.getLockCode().getLock())) {
                    return;
                }
            }

            container.setLockCode(null);
        }
    }

    private ItemStack createItemStack(Item item) {
        ItemStack lock = new ItemStack(item);
        lock.setTagCompound(new NBTTagCompound());
        if (lockKey != null) {
            lock.getTagCompound().setString("key", lockKey);
        }
        if (colors[0] != -1) {
            lock.getTagCompound().setInteger("color0", colors[0]);
        }
        if (colors[1] != -1) {
            lock.getTagCompound().setInteger("color1", colors[1]);
        }
        return lock;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return createItemStack(CharsetStorageLocks.keyItem);
    }

    @Override
    public void onBroken(Entity brokenEntity) {
        unlockContainer();
        this.entityDropItem(createItemStack(CharsetStorageLocks.lockItem), 0.0F);
    }

    @Override
    public void playPlaceSound() {
        // TODO
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeByte(facingDirection.ordinal());
        buffer.writeInt(colors[0]);
        buffer.writeInt(colors[1]);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.updateFacingWithBoundingBox(EnumFacing.getFront(buffer.readUnsignedByte()));
        colors[0] = buffer.readInt();
        colors[1] = buffer.readInt();
    }
}
