package net.redstone.utils.redstoneutil;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.IntStream;

public class BreakerBlock extends Block
{
    public static final DirectionProperty FACING;
    public static final BooleanProperty TRIGGERED;
    
    public BreakerBlock()
    {
        super(Settings.of(Material.STONE).strength(3.5f, 3.5f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext_1)
    {
        return this.getDefaultState().with(FACING, itemPlacementContext_1.getPlayerFacing().getOpposite());
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager$Builder_1)
    {
        stateManager$Builder_1.add(FACING, TRIGGERED);
    }
    
    @Override
    public BlockState rotate(BlockState blockState_1, BlockRotation blockRotation_1)
    {
        return blockState_1.with(FACING, blockRotation_1.rotate(blockState_1.get(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState blockState_1, BlockMirror blockMirror_1)
    {
        return blockState_1.rotate(blockMirror_1.getRotation(blockState_1.get(FACING)));
    }
    
    public void scheduledTick(BlockState blockState_1, ServerWorld world_1, BlockPos blockPos_1, Random random_1)
    {
        if (!world_1.isClient)
        {
            BlockPos blockPos = blockPos_1.offset(blockState_1.get(FACING));
            if(canBreakBlock(world_1, blockPos))
            {
                boolean invFull = false;
                Block block = world_1.getBlockState(blockPos).getBlock();
                world_1.breakBlock(blockPos, false);
                boolean has_chest_connected = false;
                for (Direction direction : Direction.Type.VERTICAL)
                {
                    Direction dir = direction;
                    BlockPos invBlockPos = blockPos_1.offset(dir);
                    BlockState invBlockState = world_1.getBlockState(blockPos_1.offset(dir));
                    Block invBlock = invBlockState.getBlock();
                    if(invBlock.hasBlockEntity())
                    {
                        BlockEntity entity = world_1.getBlockEntity(invBlockPos);
                        if ((entity instanceof ChestBlockEntity && invBlock instanceof ChestBlock) || (entity instanceof HopperBlockEntity && invBlock instanceof HopperBlock && dir == Direction.DOWN))
                        {
                            has_chest_connected = true;
                            Inventory inv = invBlock instanceof ChestBlock ? ChestBlock.getInventory((ChestBlock) invBlock, invBlockState, world_1, invBlockPos, true) : HopperBlockEntity.getInventoryAt(world_1, invBlockPos);
                            invFull = isInventoryFull(inv, dir);
                            if(!invFull)
                            {
                                getAvailableSlots(inv, dir).anyMatch((int_i) ->{
                                    ItemStack is = inv.getStack(int_i);
                                    if (is.getItem() == Items.AIR) {
                                        is = new ItemStack(block, 1);
                                        inv.setStack(int_i, is);
                                        return true;
                                    } else if (is.getCount() < is.getMaxCount()) {
                                        is.increment(1);
                                        inv.setStack(int_i, is);
                                        return true;
                                    }
                                    return false;
                                });
                            } else {
                                continue;
                            }
                        }
                        break;
                    }
                }

                if (!has_chest_connected || invFull) {
                    BlockPos top = blockPos_1.up(1);
                    world_1.spawnEntity(new ItemEntity(world_1, top.getX() + 0.5d, top.getY() + 0.5d, top.getZ() + 0.5d,
                            new ItemStack(block, 1)));
                }
            }
        }
    }

    private boolean isInventoryFull(Inventory inventory_1, Direction direction_1) {
        return getAvailableSlots(inventory_1, direction_1).allMatch((int_1) -> {
            ItemStack itemStack_1 = inventory_1.getStack(int_1);
            return itemStack_1.getCount() >= itemStack_1.getMaxCount();
        });
    }
    
    private static IntStream getAvailableSlots(Inventory inventory_1, Direction direction_1)
    {
        return inventory_1 instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory_1).getAvailableSlots(direction_1)) : IntStream.range(0, inventory_1.size());
    }
    
    private boolean canBreakBlock(World world, BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.STONE || block == Blocks.COBBLESTONE;
    }
    
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1)
    {
        boolean boolean_2 = world_1.isReceivingRedstonePower(blockPos_1) || world_1.isReceivingRedstonePower(blockPos_1.up());
        boolean boolean_3 = blockState_1.get(TRIGGERED);
        if (boolean_2 && !boolean_3)
        {
            world_1.getBlockTickScheduler().schedule(blockPos_1, this, 20);
            world_1.setBlockState(blockPos_1, blockState_1.with(TRIGGERED, true), 4);
        }
        else if (!boolean_2 && boolean_3)
        {
            world_1.setBlockState(blockPos_1, blockState_1.with(TRIGGERED, false), 4);
        }
    }
    
    static
    {
        FACING = FacingBlock.FACING;
        TRIGGERED = Properties.TRIGGERED;
    }
}
