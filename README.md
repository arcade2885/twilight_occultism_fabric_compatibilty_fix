# Occultism x Twilight Forest Compatibility

A tiny Fabric 1.21.1 compatibility mod that fixes the crash:

```
java.lang.IncompatibleClassChangeError: Conflicting default methods:
  io/github/fabricators_of_create/porting_lib/core/util/MutableDataComponentHolder.set
  com/klikli_dev/occultism/fabric/common/extensions/IItemStackExtension.set
    at net.minecraft.class_1799.set(class_1799.java)
    at com.klikli_dev.occultism.util.ItemNBTUtil.setBoundSpiritName(ItemNBTUtil.java:74)
    ...
```

## Cause

- Twilight Forest bundles **Porting Lib**, which adds `MutableDataComponentHolder` to `ItemStack`
  through a mixin. That interface declares supplier-based convenience defaults such as
  `set(Supplier<? extends DataComponentType<? super T>>, T)`, `remove(Supplier<...>)` and
  `update(Supplier<...>, ...)`.
- **Occultism** adds its own `IItemStackExtension` interface to `ItemStack` with default methods
  using the exact same names and erased signatures.
- Vanilla `ItemStack` only has the `DataComponentType`-based overloads, not the supplier-based ones.
  When Occultism calls `itemStack.set(supplier, value)`, the JVM finds two unrelated, equally
  specific default methods and throws `IncompatibleClassChangeError` instead of picking one.

## Fix

`ItemStackMixin` adds the conflicting methods directly to `ItemStack`. A class-level method always
beats interface default methods during JVM method resolution, so the call sites link again.
The supplier-based methods simply forward to the vanilla `DataComponentType` overloads, which is
exactly what both mods' default methods did.

The same analysis also found a second latent conflict on `ItemStack`: `isRepairable()` (used by
Occultism's spirit grindstone). It is provided as a class-level method too, keeping Occultism's
`UnrepairableItem` behaviour.

## Requirements

- Minecraft 1.21.1, Fabric Loader, Fabric API
- Occultism (Fabric)
- The Twilight Forest (Fabric, includes Porting Lib)

## Build

```
gradlew build
```

The finished mod jar is placed in `build/libs/`.
