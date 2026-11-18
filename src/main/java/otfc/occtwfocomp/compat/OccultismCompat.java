package otfc.occtwfocomp.compat;

import net.minecraft.world.item.Item;

/**
 * Reflective access to Occultism classes so this mod does not need Occultism on its compile classpath.
 * Occultism is a hard dependency at runtime (see fabric.mod.json).
 */
public final class OccultismCompat {
	private static final Class<?> UNREPAIRABLE_ITEM = find("com.klikli_dev.occultism.fabric.common.UnrepairableItem");

	private OccultismCompat() {
	}

	public static boolean isUnrepairable(Item item) {
		return UNREPAIRABLE_ITEM != null && UNREPAIRABLE_ITEM.isInstance(item);
	}

	private static Class<?> find(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException | LinkageError e) {
			return null;
		}
	}
}
