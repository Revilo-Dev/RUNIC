package net.revilodev.runic.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.revilodev.runic.screen.custom.ArtisansWorkbenchMenu;


public class ArtisansWorkbench extends HorizontalDirectionalBlock {

    public static final MapCodec<ArtisansWorkbench> CODEC =
            simpleCodec(ArtisansWorkbench::new);
    private static final VoxelShape Z_AXIS_SHAPE = Shapes.or(
            box(3.0D, 0.0D, 1.0D, 13.0D, 3.0D, 15.0D),
            box(5.0D, 3.0D, 3.0D, 11.0D, 9.0D, 13.0D),
            box(4.0D, 3.0D, 2.0D, 12.0D, 4.0D, 14.0D),
            box(4.0D, 9.0D, 2.0D, 12.0D, 10.0D, 15.0D),
            box(3.0D, 10.0D, 0.0D, 13.0D, 15.0D, 15.0D),
            box(4.0D, 10.0D, 15.0D, 12.0D, 15.0D, 16.0D)
    );
    private static final VoxelShape X_AXIS_SHAPE = Shapes.or(
            box(1.0D, 0.0D, 3.0D, 15.0D, 3.0D, 13.0D),
            box(3.0D, 3.0D, 5.0D, 13.0D, 9.0D, 11.0D),
            box(2.0D, 3.0D, 4.0D, 14.0D, 4.0D, 12.0D),
            box(2.0D, 9.0D, 4.0D, 15.0D, 10.0D, 12.0D),
            box(0.0D, 10.0D, 3.0D, 15.0D, 15.0D, 13.0D),
            box(15.0D, 10.0D, 4.0D, 16.0D, 15.0D, 12.0D)
    );

    public ArtisansWorkbench(Properties props) {
        super(props);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    // uses without item
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, ply) -> ArtisansWorkbenchMenu.server(id, inv, level, pos),
                    Component.translatable("block.runic.artisans_workbench")
            );
            sp.openMenu(provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING).getOpposite();
        return facing.getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING);
    }
}
