package inventory.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static inventory.data.Asset.Builder.newInstance;
import static inventory.data.Asset.NON_NULL_MESSAGE;
import static inventory.data.Asset.POSITIVE_ARG_MESSAGE;
import static inventory.data.types.AssetTypes.CPU.APPLE_SILLICON;
import static inventory.data.types.AssetTypes.OperatingSystem.MACOS;
import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;

public class TestAsset {

    private static final int FOUR = 4;
    private static final int TWELVE = 12;
    private static final int NEG_TWELVE = -12;
    private static final int ZERO = 0;

    @Test
    public void testAssetCreation() {
        Asset asset = newInstance()
                .setCore(FOUR)
                .setMemory(TWELVE)
                .setCPU(APPLE_SILLICON)
                .setOS(MACOS)
                .build();

        assertEquals(valueOf(4), asset.getCores());
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
        newInstance().setMemory(TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();

        //Attempt to create an asset with no memory parameter
        newInstance().setCore(TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();

        //Attempt to create an asset with no cpu parameter
        newInstance().setMemory(TWELVE).setCore(FOUR).setOS(MACOS).build();

        //Attempt to create an asset with no os parameter
        newInstance().setMemory(TWELVE).setCore(FOUR).setCPU(APPLE_SILLICON).build();
    }

    @Test
    public void testNegativeCPUandMemory() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(POSITIVE_ARG_MESSAGE);

        newInstance().setCore(NEG_TWELVE).setMemory(FOUR).setCPU(APPLE_SILLICON).setOS(MACOS).build();
        newInstance().setCore(FOUR).setMemory(NEG_TWELVE).setCPU(APPLE_SILLICON).setOS(MACOS).build();
    }

    @Test
    public void testZeroCPUandMemory() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(POSITIVE_ARG_MESSAGE);

        newInstance().setCore(ZERO).setMemory(FOUR).setCPU(APPLE_SILLICON).setOS(MACOS).build();
        newInstance().setCore(FOUR).setMemory(ZERO).setCPU(APPLE_SILLICON).setOS(MACOS).build();
    }
}