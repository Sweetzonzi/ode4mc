package cn.solarmoon.spark_core.api.util.matcher;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

/**
 * 存储固定block或是blockTag，然后可以任意检测内容物是否匹配
 */
public class BlockMatcher {

    private final Block block;
    private final TagKey<Block> blockTag;
    private final boolean any;

    public BlockMatcher() {
        block = null;
        blockTag = null;
        this.any = false;
    }

    public BlockMatcher(boolean any) {
        block = null;
        blockTag = null;
        this.any = any;
    }

    public BlockMatcher(Block block) {
        this.block = block;
        blockTag = null;
        this.any = false;
    }

    public BlockMatcher(TagKey<Block> blockTag) {
        block = null;
        this.blockTag = blockTag;
        this.any = false;
    }

    public Optional<Block> getBlockOp() {
        return Optional.ofNullable(block);
    }

    public Optional<TagKey<Block>> getBlockTagOp() {
        return Optional.ofNullable(blockTag);
    }

    /**
     * 检测输入物是否和item或tag匹配
     * @param state 输入的要检测的BlockState
     * @return 如果matcher为空，则会匹配空的state，如果matcher不为空，则会匹配state.is方法的检测
     */
    public boolean isBlockEqual(BlockState state) {
        if (isEmpty()) return state.isAir();
        return getBlockOp().map(state::is).orElse(false) || getBlockTagOp().map(state::is).orElse(false);
    }

    /**
     * @return block的显示名或是blockTag的id翻译键名
     */
    public Component getHoverName() {
        if (isEmpty()) return Component.translatable("matcher.solarmoon_core.block_empty");
        if (isAny()) return Component.translatable("matcher.solarmoon_core.any");
        return getBlockOp().map(Block::getName)
                .orElse(getBlockTagOp().map(tag -> Component.translatable(tag.location().toLanguageKey())).orElse(Component.empty()));
    }

    public boolean isEmpty() {
        return getBlockOp().isEmpty() && getBlockTagOp().isEmpty() && !isAny();
    }

    public boolean isAny() {
        return any;
    }

    public static BlockMatcher empty() {
        return new BlockMatcher();
    }

    public static BlockMatcher any() {
        return new BlockMatcher(true);
    }

    public static BlockMatcher of(Block block) {
        return new BlockMatcher(block);
    }

    public static BlockMatcher of(TagKey<Block> blockTag) {
        return new BlockMatcher(blockTag);
    }

    @Override
    public String toString() {
        return "BlockMatcher["
                + getBlockOp().toString() + ", "
                + getBlockTagOp().toString() + ", "
                + "any:" + any
                + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BlockMatcher that = (BlockMatcher) object;
        return any == that.any && Objects.equals(block, that.block) && Objects.equals(blockTag, that.blockTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, blockTag, any);
    }
}
