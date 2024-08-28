package cn.solarmoon.solarmoon_core.api.util.matcher;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * 可以输入Item或ItemTag的类，这样可以选择性进行获取物的单个或通用辨别
 */
public class ItemMatcher {

    private final Item item;
    private final TagKey<Item> itemTag;
    private final boolean any;

    /**
     * 纯空的获取所需物
     */
    public ItemMatcher() {
        this.item = null;
        this.itemTag = null;
        this.any = false;
    }

    /**
     * 任意物品都能满足的的获取所需物
     */
    public ItemMatcher(boolean any) {
        this.item = null;
        this.itemTag = null;
        this.any = any;
    }

    public ItemMatcher(Item item) {
        this.item = item;
        this.itemTag = null;
        this.any = false;
    }

    public ItemMatcher(ItemStack item) {
        this.item = item.getItem();
        this.itemTag = null;
        this.any = false;
    }

    public ItemMatcher(TagKey<Item> itemTag) {
        this.item = null;
        this.itemTag = itemTag;
        this.any = false;
    }

    public Optional<Item> getItemOp() {
        return Optional.ofNullable(item);
    }

    public Optional<TagKey<Item>> getItemTagOp() {
        return Optional.ofNullable(itemTag);
    }

    /**
     * 检测输入物是否和item或tag匹配
     * @param stack 输入的要检测的itemStack
     * @return 如果matcher为空，则会匹配空的stack，如果matcher不为空，则会匹配stack.is方法的检测
     */
    public boolean isItemEqual(ItemStack stack) {
        if (isAny()) return true;
        if (isEmpty()) return stack.isEmpty();
        return getItemOp().map(stack::is).orElse(false) || getItemTagOp().map(stack::is).orElse(false);
    }

    /**
     * @return item的显示名或是itemTag的id翻译键名（没有any）
     */
    public Component getHoverName() {
        if (isEmpty()) return Component.translatable("matcher.solarmoon_core.item_empty");
        if (isAny()) return Component.translatable("matcher.solarmoon_core.any");
        return getItemOp().map(item1 -> item1.getDefaultInstance().getHoverName())
                .orElse(getItemTagOp().map(tag -> Component.translatable(tag.location().toLanguageKey())).orElse(Component.empty()));
    }

    public boolean isEmpty() {
        return getItemOp().isEmpty() && getItemTagOp().isEmpty() && !isAny();
    }

    public boolean isAny() {
        return any;
    }

    public static ItemMatcher empty() {
        return new ItemMatcher();
    }

    public static ItemMatcher any() {
        return new ItemMatcher(true);
    }

    public static ItemMatcher of(Item item) {
        return new ItemMatcher(item);
    }

    public static ItemMatcher of(ItemStack stack) {
        return new ItemMatcher(stack);
    }

    public static ItemMatcher of(TagKey<Item> itemTag) {
        return new ItemMatcher(itemTag);
    }

    @Override
    public String toString() {
        return "ItemMatcher["
                + getItemOp().toString() + ", "
                + getItemTagOp().toString() + ", "
                + "any:" + any
                + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ItemMatcher that = (ItemMatcher) object;
        return any == that.any && Objects.equals(item, that.item) && Objects.equals(itemTag, that.itemTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, itemTag, any);
    }
}
