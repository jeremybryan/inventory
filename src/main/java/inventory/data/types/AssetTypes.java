package inventory.data.types;

/**
 * Data type containers to ensure consistency of these elements
 * of all assets created within the inventory.
 */
public class AssetTypes {

    public enum OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX
    }

    public enum CPU {
        AMD,
        APPLE_SILLICON,
        INTEL
    }

}
