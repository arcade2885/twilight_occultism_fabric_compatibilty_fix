package otfc.occtwfocomp.mixin;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;

import otfc.occtwfocomp.compat.OccultismCompat;

/**
 * Fixes the crash:
 * <pre>
 * java.lang.IncompatibleClassChangeError: Conflicting default methods:
 *   io/github/fabricators_of_create/porting_lib/core/util/MutableDataComponentHolder.set
 *   com/klikli_dev/occultism/fabric/common/extensions/IItemStackExtension.set
 * </pre>
 *
 * <p>Twilight Forest bundles Porting Lib, whose {@code MutableDataComponentHolder} mixin interface adds
 * supplier-based component helpers ({@code set(Supplier, T)}, {@code remove(Supplier)}, ...) as
 * <b>default</b> methods to {@link ItemStack}. Occultism's {@code IItemStackExtension} mixin interface
 * adds default methods with the exact same names and erased signatures. Vanilla {@code ItemStack} does
 * not declare these supplier-based overloads itself, so when game code (e.g. Occultism's
 * {@code ItemNBTUtil#setBoundSpiritName}) calls {@code ItemStack.set(Supplier, Object)} the JVM finds two
 * unrelated, equally specific default methods and refuses to link the call site.</p>
 *
 * <p>Declaring the methods directly on {@link ItemStack} makes them win over both interface defaults
 * (JVM method resolution always prefers a class method over interface default methods), so the call
 * links normally. The implementations simply forward to the vanilla component-type overloads, which is
 * exactly what both mods' defaults do.</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@SuppressWarnings("unchecked")
	public <T> T set(Supplier<? extends DataComponentType<? super T>> type, T value) {
		return ((ItemStack) (Object) this).set((DataComponentType<T>) (DataComponentType<?>) type.get(), value);
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
		return ((ItemStack) (Object) this).remove((DataComponentType<T>) (DataComponentType<?>) type.get());
	}

	@SuppressWarnings("unchecked")
	public <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> operator) {
		return ((ItemStack) (Object) this).update((DataComponentType<T>) (DataComponentType<?>) type.get(), defaultValue, operator);
	}

	@SuppressWarnings("unchecked")
	public <T, U> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, U other, BiFunction<T, U, T> function) {
		return ((ItemStack) (Object) this).update((DataComponentType<T>) (DataComponentType<?>) type.get(), defaultValue, other, function);
	}

	/**
	 * Both Occultism's {@code IItemStackExtension} and Porting Lib's {@code ItemStackInjection} add a
	 * default {@code isRepairable()Z} to {@link ItemStack}. Occultism's spirit grindstone calls it
	 * through {@link ItemStack}, so it has to exist on the class as well.
	 *
	 * <p>Uses Occultism's semantics (damageable, except for its {@code UnrepairableItem} marker).</p>
	 */
	public boolean isRepairable() {
		ItemStack stack = (ItemStack) (Object) this;
		return stack.isDamageableItem() && !OccultismCompat.isUnrepairable(stack.getItem());
	}
}
