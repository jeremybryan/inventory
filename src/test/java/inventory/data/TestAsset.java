package inventory.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static inventory.data.Asset.*;
import static inventory.data.types.CPU.APPLE_SILLICON;
import static inventory.data.types.OperatingSystem.MACOS;
import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;

public class TestAsset {

    private static final int FOUR = 4;
    private static final int TWELVE = 12;
    private static final int NEG_TWELVE = -12;
    private static final int ZERO = 0;

    @Test
    public void testAssetCreation() {
        Asset asset = Asset.builder()
                .setCore(FOUR)
                .setMemory(TWELVE)
                .setCPU(APPLE_SILLICON)
                .setOS(MACOS)
                .build();

        assertEquals(valueOf(FOUR), asset.getCores());
        assertEquals(valueOf(TWELVE), asset.getMemory());
        assertEquals(APPLE_SILLICON, asset.getCPU());
        assertEquals(MACOS, asset.getOS());
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAssetCreationWithEmptyParameters() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(NON_NULL_MESSAGE);

        //Attempt to create an asset with no cpu parameter
        Asset.builder().setMemory(TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();

        //Attempt to create an asset with no memory parameter
        Asset.builder().setCore(TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();

        //Attempt to create an asset with no cpu parameter
        Asset.builder().setMemory(TWELVE).setCore(FOUR).setOS(MACOS).build();

        //Attempt to create an asset with no os parameter
        Asset.builder().setMemory(TWELVE).setCore(FOUR).setCPU(APPLE_SILLICON).build();
    }

    @Test
    public void testNegativeCPUandMemory() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(POSITIVE_ARG_MESSAGE);

        Asset.builder().setCore(NEG_TWELVE).setMemory(FOUR).setCPU(APPLE_SILLICON).setOS(MACOS).build();
        Asset.builder().setCore(FOUR).setMemory(NEG_TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();
    }

    @Test
    public void testZeroCPUandMemory() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(POSITIVE_ARG_MESSAGE);

        Asset.builder().setCore(ZERO).setMemory(FOUR).setCPU(APPLE_SILLICON).setOS(MACOS).build();
        Asset.builder().setCore(FOUR).setMemory(ZERO).setCPU(APPLE_SILLICON).setOS(MACOS).build();
    }
}