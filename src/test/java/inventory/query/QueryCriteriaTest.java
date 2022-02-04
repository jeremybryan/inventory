package inventory.query;

import org.junit.Test;

import static inventory.data.types.CPU.APPLE_SILLICON;
import static inventory.data.types.OperatingSystem.MACOS;
import static org.junit.Assert.*;

public class QueryCriteriaTest {

    private static final int FOUR = 4;
    private static final int TWELVE = 12;

    @Test
    public void testQueryCriteriaCreation() {
        QueryCriteria qc = QueryCriteria.builder()
                .setCore(FOUR)
                .setMemory(TWELVE)
                .setCPU(APPLE_SILLICON)
                .setOS(MACOS)
                .build();

        assertEquals(FOUR, qc.getCores().get().intValue());
        assertEquals(TWELVE, qc.getMemory().get().intValue());
        assertEquals(APPLE_SILLICON, qc.getCpu().get());
        assertEquals(MACOS, qc.getOs().get());
    }

    @Test
    public void isEmpty() {
        QueryCriteria qc = QueryCriteria.builder().build();

        assertTrue(qc.isEmpty());
    }
}