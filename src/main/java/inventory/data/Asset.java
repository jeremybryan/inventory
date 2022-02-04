package inventory.data;

import inventory.data.types.AssetTypes;

import java.util.UUID;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Represents an inventory asset including a unique asset id along with
 * Operating System, CPU, cores and memory.
 *
 * All 4 attributes are required.
 */
public class Asset {

    private final String assetId;
    private final AssetTypes.OperatingSystem os;
    private final AssetTypes.CPU cpu;
    private final Integer cores;
    private final Integer memory;

    static final String POSITIVE_ARG_MESSAGE =
            "Asset requires non-zero, positive arguments for cores and memory";

    static final String NON_NULL_MESSAGE = "Asset cannot be created with null arguments";

    public String getAssetId() { return assetId; }

    public AssetTypes.CPU getCPU() {
        return cpu;
    }

    public Integer getCores() {
        return cores;
    }

    public Integer getMemory() {
        return memory;
    }

    public AssetTypes.OperatingSystem getOS() { return os; }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("Asset ID: " )
                .append(assetId)
                .append(" Operating System: ")
                .append(os)
                .append(" CPU: ")
                .append(cpu)
                .append(" Cores: ")
                .append(cores)
                .append(" Memory (GB): ")
                .append(memory)
                .toString();
    }

    private Integer isPositive(final Integer input) {
        notNull(input, NON_NULL_MESSAGE);
        if (input <= 0)
            throw new IllegalArgumentException(POSITIVE_ARG_MESSAGE);

        return input;
    }

    private Asset(final Asset.Builder builder) {
        // Naive asset Id
        assetId = UUID.randomUUID().toString();
        os = notNull(builder.os);
        cpu = notNull(builder.cpu);
        cores = isPositive(builder.cores);
        memory = isPositive(builder.memory);
    }

    public static Asset.Builder builder() {
        return new Asset.Builder();
    }

    public static final class Builder {

        private AssetTypes.OperatingSystem os;
        private AssetTypes.CPU cpu;
        private Integer cores;
        private Integer memory;

        private Builder() {}

        // Setter methods
        public Asset.Builder setOS(AssetTypes.OperatingSystem os)
        {
            this.os = os;
            return this;
        }

        public Asset.Builder setCPU(AssetTypes.CPU cpu)
        {
            this.cpu = cpu;
            return this;
        }

        public Asset.Builder setCore(Integer cores)
        {
            this.cores = cores;
            return this;
        }

        public Asset.Builder setMemory(Integer memory)
        {
            this.memory = memory;
            return this;
        }

        public Asset build()
        {
            return new Asset(this);
        }

    }

}
