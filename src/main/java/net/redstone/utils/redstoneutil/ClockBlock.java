package net.redstone.utils.redstoneutil;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class ClockBlock extends Block
{
    public static final BooleanProperty POWERED;
    public static final BooleanProperty LOCKED;
    
    public ClockBlock()
    {
        super(Settings.of(Material.STONE).strength(3.5f, 3.5f));
    }
    
    @Override
    public boolean emitsRedstonePower(BlockState blockState_1)
    {
        return true;
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager$Builder_1)
    {
        stateManager$Builder_1.add(POWERED, LOCKED);
    }
    
    public void scheduledTick(BlockState blockState_1, ServerWorld world_1, BlockPos blockPos_1, Random random_1)
    {
        if(!blockState_1.get(POWERED) && !blockState_1.get(LOCKED))
        {
            world_1.getBlockTickScheduler().schedule(blockPos_1, this, 4);
            world_1.setBlockState(blockPos_1, blockState_1.with(POWERED, true));
        }
        else
        {
            world_1.getBlockTickScheduler().schedule(blockPos_1, this, 20);
            world_1.setBlockState(blockPos_1, blockState_1.with(POWERED, false));
        }
    }
    
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1)
    {
        world_1.setBlockState(blockPos_1, blockState_1.with(LOCKED, world_1.isReceivingRedstonePower(blockPos_1)));
    }
    
    @Override
    public void onPlaced(World world_1, BlockPos blockPos_1, BlockState blockState_1, LivingEntity livingEntity_1, ItemStack itemStack_1)
    {
        super.onPlaced(world_1, blockPos_1, blockState_1, livingEntity_1, itemStack_1);
        world_1.getBlockTickScheduler().schedule(blockPos_1, this, 20);
    }
    
    public int getStrongRedstonePower(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, Direction direction_1)
    {
        return blockState_1.getWeakRedstonePower(blockView_1, blockPos_1, direction_1);
    }
    
    public int getWeakRedstonePower(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, Direction direction_1)
    {
        return blockState_1.get(POWERED) ? 15 : 0;
    }
    
    static {
        POWERED = Properties.POWERED;
        LOCKED = BooleanProperty.of("power_locked");
    }
}
