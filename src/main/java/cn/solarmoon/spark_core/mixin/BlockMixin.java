package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.api.blockstate.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour {

    @Shadow public abstract BlockState defaultBlockState();

    @Shadow private BlockState defaultBlockState;

    private Block block = (Block)(Object)this;

    public BlockMixin(Properties p_60452_) {
        super(p_60452_);
    }

    @Inject(method = "registerDefaultState", at = @At("TAIL"))
    public void registerDefaultState(BlockState state, CallbackInfo ci) {
        if (block instanceof IHorizontalFacingState) {
            state = state.setValue(IHorizontalFacingState.getFACING(), Direction.NORTH);
        }
        if (block instanceof IWaterLoggedState) {
            state = state.setValue(IWaterLoggedState.getWATERLOGGED(), false);
        }
        if (block instanceof ILitState litBlock) {
            state = state.setValue(ILitState.getLIT(), litBlock.defaultLitValue());
        }
        if (block instanceof INoLimitAgeState) {
            state = state.setValue(INoLimitAgeState.getAGE(), 0);
        }
        if (block instanceof IBedPartState) {
            state = state.setValue(IBedPartState.getPART(), BedPart.FOOT);
        }
        if (block instanceof IMultilayerState) {
            state = state.setValue(IMultilayerState.getLAYER(), 0);
        }
        this.defaultBlockState = state;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;createBlockStateDefinition(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V"))
    private StateDefinition.Builder<Block, BlockState> modifyBuilder(StateDefinition.Builder<Block, BlockState> builder) {
        if (block instanceof IHorizontalFacingState) {
            builder.add(IHorizontalFacingState.getFACING());
        }
        if (block instanceof IWaterLoggedState) {
            builder.add(IWaterLoggedState.getWATERLOGGED());
        }
        if (block instanceof ILitState) {
            builder.add(ILitState.getLIT());
        }
        if (block instanceof INoLimitAgeState) {
            builder.add(INoLimitAgeState.getAGE());
        }
        if (block instanceof IBedPartState) {
            builder.add(IBedPartState.getPART());
        }
        if (block instanceof IMultilayerState) {
            builder.add(IMultilayerState.getLAYER());
        }
        return builder;
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = this.defaultBlockState();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        Level level = context.getLevel();
        if (block instanceof IHorizontalFacingState) {
            state = state.setValue(IHorizontalFacingState.getFACING(), context.getHorizontalDirection().getOpposite());
        }
        if (block instanceof IWaterLoggedState) {
            FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
            state = state.setValue(IWaterLoggedState.getWATERLOGGED(), fluid.getType() == Fluids.WATER);
        }
        if (block instanceof IBedPartState) {
            state = level.getBlockState(blockpos1).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockpos1) ? state.setValue(IHorizontalFacingState.getFACING(), direction) : null;
        }
        cir.setReturnValue(state);
    }

    /**
     * @return 更新水流、双方块两部分绑定
     */
    @Override
    public BlockState updateShape(BlockState stateIn, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (block instanceof IWaterLoggedState) {
            if (stateIn.getValue(IWaterLoggedState.getWATERLOGGED())) {
                level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
        if (block instanceof IBedPartState) {
            if (direction == IBedPartState.getNeighbourDirection(stateIn.getValue(IBedPartState.getPART()), stateIn.getValue(IHorizontalFacingState.getFACING()))) {
                return facingState.is(block) && facingState.getValue(IBedPartState.getPART()) != stateIn.getValue(IBedPartState.getPART()) ? stateIn : Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(stateIn, direction, facingState, level, currentPos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (block instanceof IWaterLoggedState) {
            return state.getValue(IWaterLoggedState.getWATERLOGGED()) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }
        return super.getFluidState(state);
    }

    /**
     * 设置转向
     */
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (block instanceof IHorizontalFacingState) {
            return super.rotate(state, rot).setValue(IHorizontalFacingState.getFACING(), rot.rotate(state.getValue(IHorizontalFacingState.getFACING())));
        }
        return super.rotate(state, rot);
    }

    /**
     * 设置镜像
     */
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        if (block instanceof IHorizontalFacingState) {
            return super.mirror(state, mirror).rotate(mirror.getRotation(state.getValue(IHorizontalFacingState.getFACING())));
        }
        return super.mirror(state, mirror);
    }

    /**
     * 创造模式下一起破坏bedPartBlock的连接方块
     */
    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
        if (block instanceof IBedPartState) {
            if (!level.isClientSide && player.isCreative()) {
                BedPart bedpart = state.getValue(IBedPartState.getPART());
                if (bedpart == BedPart.FOOT) {
                    BlockPos blockpos = pos.relative(IBedPartState.getNeighbourDirection(bedpart, state.getValue(IHorizontalFacingState.getFACING())));
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (blockstate.is(block) && blockstate.getValue(IBedPartState.getPART()) == BedPart.HEAD) {
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                        level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                    }
                }
            }
        }
    }

    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack, CallbackInfo ci) {
        if (block instanceof IBedPartState) {
            BlockPos facingPos = pos.relative(state.getValue(IHorizontalFacingState.getFACING()));
            level.setBlock(facingPos, state.setValue(IBedPartState.getPART(), BedPart.HEAD), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

}
