package net.redstone.utils.redstoneutil;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class DetectorBlock extends Block
{
    public static final DirectionProperty FACING;
    public static final BooleanProperty POWERED, LOCKED, TRIGGER;
    
    public DetectorBlock()
    {
        super(Settings.of(Material.STONE).strength(3.5f, 3.5f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }
    
    @Override
    public boolean emitsRedstonePower(BlockState blockState_1)
    {
        return true;
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext_1)
    {
        return this.getDefaultState().with(FACING, itemPlacementContext_1.getPlayerLookDirection().getOpposite());
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager$Builder_1)
    {
        stateManager$Builder_1.add(FACING, POWERED, TRIGGER, LOCKED);
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
        world_1.setBlockState(blockPos_1, blockState_1.with(POWERED, blockState_1.get(TRIGGER) && !blockState_1.get(LOCKED)));
        world_1.getBlockTickScheduler().schedule(blockPos_1, this, 2);
    }
    
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1)
    {
        BlockState facing_block = world_1.getBlockState(blockPos_1.offset(blockState_1.get(FACING)));
        BlockState back_block = world_1.getBlockState(blockPos_1.offset(blockState_1.get(FACING).getOpposite()));
        BlockState neighbor = world_1.getBlockState(blockPos_2);
        
        if(back_block != neighbor)
            world_1.setBlockState(blockPos_1, blockState_1.with(TRIGGER, facing_block.getBlock() != Blocks.AIR).with(LOCKED, isReceivingRedstonePower(world_1, blockState_1, blockPos_1)));
    }
    
    public int getStrongRedstonePower(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, Direction direction_1)
    {
        return blockState_1.getWeakRedstonePower(blockView_1, blockPos_1, direction_1);
    }
    
    public int getWeakRedstonePower(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, Direction direction_1)
    {
        return (blockState_1.get(POWERED) && blockState_1.get(FACING) == direction_1) ? 15 : 0;
    }
    
    @Override
    public void onPlaced(World world_1, BlockPos blockPos_1, BlockState blockState_1, LivingEntity livingEntity_1, ItemStack itemStack_1)
    {
        world_1.getBlockTickScheduler().schedule(blockPos_1, this, 2);
    }
    
    private boolean isReceivingRedstonePower(World world, BlockState state, BlockPos pos)
    {
        for (Direction dir : Direction.Type.HORIZONTAL)
            if(dir != state.get(FACING).getOpposite() && world.getEmittedRedstonePower(pos.offset(dir), dir) > 0)
                return true;
            
        for (Direction dir : Direction.Type.VERTICAL)
            if(dir != state.get(FACING).getOpposite() && world.getEmittedRedstonePower(pos.offset(dir), dir) > 0)
                return true;
            
        return false;
    }
    
    static
    {
        FACING = FacingBlock.FACING;
        POWERED = Properties.POWERED;
        LOCKED = BooleanProperty.of("power_locked");
        TRIGGER = BooleanProperty.of("trigger");
    }
}
