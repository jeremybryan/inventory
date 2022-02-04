package inventory.query;

import inventory.data.types.CPU;
import inventory.data.types.OperatingSystem;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * This class provides a mechanism for specifying query/filtering parameters
 * to submit to the inventory. Here are use the builder to create query objects
 * to define the search/filter to be applied to the inventory.
 *
 * Criteria can be specified for all or none of the parameters.
 *
 */
public class QueryCriteria {

    private final OperatingSystem os;
    private final CPU cpu;
    private final Integer cores;
    private final Integer memory;


    public Optional<OperatingSystem> getOs() {
        return ofNullable(os);
    }

    public Optional<CPU> getCpu() {
        return ofNullable(cpu);
    }

    public Optional<Integer> getCores() {
        return ofNullable(cores);
    }

    public Optional<Integer> getMemory() {
        return ofNullable(memory);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("Operating System: ")
                .append(os)
                .append(" CPU: ")
                .append(cpu)
                .append(" Cores: ")
                .append(cores)
                .append(" Memory (GB): ")
                .append(memory)
                .toString();
    }

    public boolean isEmpty() {
        if (os == null && cpu == null && cores == null && memory == null) {
            return true;
        }
        return false;
    }

    private QueryCriteria(final Builder builder) {
        os = builder.os;
        cpu = builder.cpu;
        cores = builder.cores;
        memory = builder.memory;
    }

    public static QueryCriteria.Builder builder() {
        return new QueryCriteria.Builder();
    }

    public static class Builder {

        private OperatingSystem os;
        private CPU cpu;
        private Integer cores;
        private Integer memory;

        private Builder() {}

        // Setter methods
        public Builder setOS(OperatingSystem os)
        {
            this.os = os;
            return this;
        }
        public Builder setCPU(CPU cpu)
        {
            this.cpu = cpu;
            return this;
        }
        public Builder setCore(Integer cores)
        {
            this.cores = cores;
            return this;
        }
        public Builder setMemory(Integer memory)
        {
            this.memory = memory;
            return this;
        }

        public QueryCriteria build()
        {
            return new QueryCriteria(this);
        }

    }

}
