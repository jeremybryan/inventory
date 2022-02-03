package inventory;

import inventory.data.Asset;
import inventory.data.types.AssetTypes;
import inventory.query.QueryCriteria;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static inventory.DefaultInventory.NON_NULL_ARGUMENT;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEqualCollection;
import static org.junit.Assert.*;

public class TestInventoryManager {

    private Inventory inventory;

    private static final int TWELVE_CORE = 12;
    private static final int FOUR_CORE = 4;
    private static final int TWENTYFOUR_CORE = 24;
    private static final int TWO_CORE = 2;

    private static final int THIRTYTWO_GB = 32;
    private static final int SIXTYFOUR_GB = 64;
    private static final int EIGHT_GB = 8;
    private static final int ONEHUNDREDTWENTYEIGHT_GB = 128;

    // Set of mac assets
    private Asset macAMD1232 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);
    private Asset macINTEL1232 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.INTEL, TWELVE_CORE, THIRTYTWO_GB);
    private Asset macXEON1232 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, TWELVE_CORE, THIRTYTWO_GB);
    private Asset macXEON48 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB);

    // Set of windows assets
    private Asset winAMD1232 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);
    private Asset winINTEL1232 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.INTEL, TWELVE_CORE, THIRTYTWO_GB);
    private Asset winXEON1232 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWELVE_CORE, THIRTYTWO_GB);
    private Asset winXEON24128 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE, ONEHUNDREDTWENTYEIGHT_GB);
    private Asset winAMD24128 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE, ONEHUNDREDTWENTYEIGHT_GB);

    // Set of linux assets
    private Asset linuxAMD1232 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);
    private Asset linuxINTEL1232 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.INTEL, TWELVE_CORE, THIRTYTWO_GB);
    private Asset linuxXEON1232 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, TWELVE_CORE, THIRTYTWO_GB);
    private Asset linuxXEON4128 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, ONEHUNDREDTWENTYEIGHT_GB);


    @Before
    public void setup() {
        inventory = new DefaultInventory();
    }

    @Test
    public void addAsset() {
        Asset macAsset = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);
        Asset winAsset = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);
        Asset linAsset = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB);

        inventory.addAsset(macAsset);
        inventory.addAsset(winAsset);
        inventory.addAsset(linAsset);

        //Total asset list
        List<Asset> assets = new ArrayList<>(asList(macAsset, winAsset, linAsset));

        assertTrue(isEqualCollection(assets, inventory.getFullInventory()));
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void addNullAsset() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(NON_NULL_ARGUMENT);

        inventory.addAsset(null);
    }

    @Test
    public void testSearchForEmptyCriteria() {
        //load inventory
        load32G12CoreInventory();

        QueryCriteria query = QueryCriteria.builder().build();
        List<Asset> results = inventory.search(query);

        // Since we defined no criteria, this should return the entire inventory of 9 assets
        assertEquals(9, results.size());
    }


    @Test
    public void testQueryFindsOneWinAssetInventory() {
        //load inventory
        load32G12CoreInventory();

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWELVE_CORE)
                .setMemory(THIRTYTWO_GB)
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD).build();

        List<Asset> results = inventory.search(query);
        assertNotNull(results);
        assertEquals(1, results.size());

        Asset resultAsset = results.get(0);
        assertEquals(resultAsset.getOS(), AssetTypes.OperatingSystem.WINDOWS);
        assertEquals(resultAsset.getCPU(), AssetTypes.CPU.AMD);
    }

    @Test
    public void testQueryForCoreMemoryAndCPU() {
        //load inventory
        load32G12CoreInventory();

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWELVE_CORE)
                .setMemory(THIRTYTWO_GB)
                .setCPU(AssetTypes.CPU.AMD).build();

        List<Asset> results = inventory.search(query);
        assertNotNull(results);
        assertEquals(3, results.size());

        //We should see 3 results...one for each OS type
        assertTrue(isEqualCollection(
                new ArrayList<>(asList(macAMD1232, winAMD1232, linuxAMD1232)), results));
    }

    @Test
    public void testQueryForCPUOnly() {
        //load inventory
        load32G12CoreInventory();

        //Adding 2 additional assets beyond the base inventory
        inventory.addAsset(winXEON24128);
        inventory.addAsset(winAMD24128);

        QueryCriteria query = QueryCriteria.builder()
                .setCPU(AssetTypes.CPU.AMD).build();

        List<Asset> results = inventory.search(query);

        assertEquals(results.size(), 4);
        assertTrue(isEqualCollection(
                new ArrayList<>(asList(macAMD1232, winAMD1232, linuxAMD1232, winAMD24128)), results));
    }

    @Test
    public void testQueryForMemoryOnly() {
        //load inventory
        load32G12CoreInventory();

        //Adding 2 additional assets beyond the base inventory
        inventory.addAsset(winXEON24128);
        inventory.addAsset(macXEON48);

        QueryCriteria query = QueryCriteria.builder()
                .setMemory(EIGHT_GB).build();

        List<Asset> results = inventory.search(query);

        assertEquals(results.size(), 1);
        assertEquals(results.get(0), macXEON48);
    }

    @Test
    public void testQueryForCoresOnly() {
        //load inventory
        load32G12CoreInventory();

        //Adding 2 additional assets beyond the base inventory
        inventory.addAsset(winXEON24128);
        inventory.addAsset(winAMD24128);

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWENTYFOUR_CORE).build();

        List<Asset> results = inventory.search(query);

        assertEquals(2, results.size());
        assertTrue(isEqualCollection(
                new ArrayList<>(asList(winXEON24128, winAMD24128)), results));

    }

    @Test
    public void testSumOfCoresAllAssets() {
        //total cores - 48
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core


        QueryCriteria query = QueryCriteria.builder().build();
        Integer sum = inventory.totalCores(query);

        assertEquals(48, sum.intValue());
    }

    @Test
    public void testSumOfCoreAll24Assets() {
        //total cores - 48
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core
        inventory.addAsset(winXEON24128); //12 core


        QueryCriteria query = QueryCriteria.builder().
                setCore(24).build();

        Integer sum = inventory.totalCores(query);

        assertEquals(48, sum.intValue());
    }

    @Test
    public void testSumOfCoresAllWindowsAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS).build();

        Integer sum = inventory.totalCores(query);

        assertEquals(48, sum.intValue());
    }

    @Test
    public void testSumOfCoresAllWindowsAmdAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD).build();

        Integer sum = inventory.totalCores(query);

        assertEquals(24, sum.intValue());
    }

    @Test
    public void testSumOfCoresAllAmdAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setCPU(AssetTypes.CPU.AMD).build();

        Integer sum = inventory.totalCores(query);

        assertEquals(48, sum.intValue());
    }

    @Test
    public void testSumOfCoresAll128MemoryAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setMemory(128).build();

        Integer sum = inventory.totalCores(query);

        assertEquals(112, sum.intValue());
    }

    @Test
    public void testSumOfAllMemory() {
        //total cores - 192
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core


        QueryCriteria query = QueryCriteria.builder().build();
        Integer sum = inventory.totalMemory(query);

        assertEquals(192, sum.intValue());
    }

    @Test
    public void testSumOfMemoryAllMacAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder().
                setOS(AssetTypes.OperatingSystem.MACOS).build();
        Integer sum = inventory.totalMemory(query);

        assertEquals(32, sum.intValue());
    }

    @Test
    public void testSumOfMemoryAllMacXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON).build();

        Integer sum = inventory.totalMemory(query);

        assertEquals(8, sum.intValue());
    }

    @Test
    public void testSumOfMemoryAllXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder()
                .setCPU(AssetTypes.CPU.APPLE_SILLICON).build();

        Integer sum = inventory.totalMemory(query);

        assertEquals(40, sum.intValue());
    }

    @Test
    public void testSumOfMemoryAll4CoreAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);


        QueryCriteria query = QueryCriteria.builder()
                .setCore(4).build();

        Integer sum = inventory.totalMemory(query);

        assertEquals(136, sum.intValue());
    }

    @Test
    public void testSumOfMemoryAll32MemoryAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);


        QueryCriteria query = QueryCriteria.builder()
                .setMemory(32).build();

        Integer sum = inventory.totalMemory(query);

        assertEquals(64, sum.intValue());
    }

    @Test
    public void testMinCores() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);


        QueryCriteria query = QueryCriteria.builder().build();

        Integer sum = inventory.minCores(query);

        assertEquals(4, sum.intValue());
    }

    @Test
    public void testMinCoresForLinuxXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder()
            .setOS(AssetTypes.OperatingSystem.LINUX)
            .setCPU(AssetTypes.CPU.APPLE_SILLICON)
            .build();

        Integer sum = inventory.minCores(query);

        assertEquals(4, sum.intValue());
    }

    @Test
    public void testMaxCores() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder().build();

        Integer sum = inventory.maxCores(query);

        assertEquals(64, sum.intValue());
    }

    @Test
    public void testMaxCoresForLinuxXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64, ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.LINUX)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .build();

        Integer sum = inventory.maxCores(query);

        assertEquals(64, sum.intValue());
    }

    @Test
    public void testMinMemory() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder().build();

        Integer sum = inventory.minMemory(query);

        assertEquals(8, sum.intValue());
    }

    @Test
    public void testMinMemoryForMacAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS).build();

        Integer sum = inventory.minMemory(query);

        assertEquals(8, sum.intValue());
    }

    @Test
    public void testMinMemoryForLinuxXeonAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.LINUX)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON).build();

        Integer sum = inventory.minMemory(query);

        assertEquals(ONEHUNDREDTWENTYEIGHT_GB, sum.intValue());
    }

    @Test
    public void testMaxMemory() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder().build();

        Integer sum = inventory.maxMemory(query);

        assertEquals(128, sum.intValue());
    }

    @Test
    public void testMaxMemoryForMacAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS).build();

        Integer sum = inventory.maxMemory(query);

        assertEquals(32, sum.intValue());
    }

    @Test
    public void testMaxMemoryForLinuxXeonAssets() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.LINUX)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON).build();

        Integer sum = inventory.maxMemory(query);

        assertEquals(128, sum.intValue());
    }


    @Test
    public void testCountOfAll() {
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder().build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(6, totalAssets);
    }

    @Test
    public void testCountAllOfOneCPU() {
        //We are testing for APPLE_SILLICON ... the inventory load adds 3 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder()
                .setCPU(AssetTypes.CPU.APPLE_SILLICON).build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(6, totalAssets);
    }

    @Test
    public void testCountOfAllOneMemory() {
        //We are testing for 32GB ... the inventory load adds 9 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setMemory(32).build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(11, totalAssets);
    }

    @Test
    public void testCountAllOfOneCoreSize() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setCore(24).build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(2, totalAssets);
    }

    @Test
    public void testTotalAssetsWinAMD32Core128Memory() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setCore(24)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB).build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(1, totalAssets);
    }

    @Test
    public void testTotalAssetsWinAMD32Core128MemoryFindNone() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, EIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.AMD, TWELVE_CORE, THIRTYTWO_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWENTYFOUR_CORE,
                ONEHUNDREDTWENTYEIGHT_GB));
        inventory.addAsset(getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT_GB));


        QueryCriteria query = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setCore(32)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB).build();

        long totalAssets = inventory.totalAssets(query);

        assertEquals(0, totalAssets);
    }

    @Test
    public void testMultipleCriteria() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                ONEHUNDREDTWENTYEIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setCore(32)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setCore(24)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB).build();


        //We are looking for the following:
        // All assets that are WINDOWS, AMD, 32 CORE, 128 GB RAM
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE and 128 GB
        // We expect 2
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        long totalAssets = inventory.totalAssets(criteria);
        assertEquals(2, totalAssets);

        List<Asset> results = inventory.search(criteria);
        assertTrue(CollectionUtils.isEqualCollection(results, new ArrayList<>(asList(asset1, asset2))));
    }

    @Test
    public void testMemoryMultipleCriteria() {
        //Tests max and min memory for multiple criteria
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setCore(32).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setCore(24).build();


        //We are looking for the min and max memory across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE
        // We expect the min memory is 8 and the max memory is 128
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        Integer min_result = inventory.minMemory(criteria);
        assertEquals(EIGHT_GB, min_result.intValue());

        Integer max_result = inventory.maxMemory(criteria);
        assertEquals(ONEHUNDREDTWENTYEIGHT_GB, max_result.intValue());
    }

    @Test
    public void testCoreMultipleCriteria() {
        //Tests max and min core for multiple criteria
        load32G12CoreInventory();

        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setMemory(EIGHT_GB).build();


        //We are looking for the min and max memory across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE
        // We expect the min memory is 8 and the max memory is 128
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        Integer min_result = inventory.minCores(criteria);
        assertEquals(TWENTYFOUR_CORE, min_result.intValue());

        Integer max_result = inventory.maxCores(criteria);
        assertEquals(THIRTYTWO_GB, max_result.intValue());
    }

    @Test
    public void testTotalCoreMultipleCriteria() {
        //Tests total core for multiple criteria
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB)
                .setCore(THIRTYTWO_GB).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setMemory(EIGHT_GB)
                .setCore(TWENTYFOUR_CORE).build();


        //We are looking for total cores across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        //We expect the total cores to equal 104
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        Integer result = inventory.totalCores(criteria);
        assertEquals(104, result.intValue());
    }

    @Test
    public void testTotalMemoryMultipleCriteria() {
        //Tests total memory for multiple criteria
        //Tests total core for multiple criteria
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB)
                .setCore(THIRTYTWO_GB).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setMemory(EIGHT_GB)
                .setCore(TWENTYFOUR_CORE).build();


        //We are looking for total cores across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        //We expect the total memory to equal 128 + 24 = 152
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        Integer result = inventory.totalMemory(criteria);
        assertEquals(152, result.intValue());
    }

    @Test
    public void testTotalAssetsMultipleCriteria() {
        //Tests total assets for multiple criteria
        //Tests total memory for multiple criteria
        //Tests total core for multiple criteria
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS)
                .setCPU(AssetTypes.CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT_GB)
                .setCore(THIRTYTWO_GB).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setMemory(EIGHT_GB)
                .setCore(TWENTYFOUR_CORE).build();


        //We are looking for total assets across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        // We have 4 machines matching this criteria
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        long result = inventory.totalAssets(criteria);
        assertEquals(4, result);
    }

    @Test
    public void testAddAssets() {
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, 32,
                ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, 24,
                EIGHT_GB);

        String assetId = inventory.addAsset(asset1);
        assertNotNull(assetId);

        String assetId2 = inventory.addAsset(asset2);
        assertNotEquals(assetId, assetId2);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4));
        List<String> asset_ids = inventory.addAssets(assets);

        assertEquals(2, asset_ids.size());
        assertNotEquals(asset_ids.get(0), asset_ids.get(1));
    }

    @Test
    public void testDeleteAssetWithCriteria() {
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWO_CORE, ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.INTEL, TWELVE_CORE, EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE, SIXTYFOUR_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, THIRTYTWO_GB);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4, asset1, asset2));

        // Add the assets
        List<String> asset_ids = inventory.addAssets(assets);

        // Determine size of inventory
        long size = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(4, size);

        //Query to delete the newest asset
        QueryCriteria toDeleteQuery = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.LINUX)
                .setCPU(AssetTypes.CPU.APPLE_SILLICON)
                .setCore(TWENTYFOUR_CORE)
                .setMemory(SIXTYFOUR_GB).build();

        //Search to find it
        List<Asset> assetToDelete = inventory.search(toDeleteQuery);

        //Delete using the criteria
        List<Asset> deletedAsset = inventory.deleteAssets(toDeleteQuery);

        assertEquals(1, deletedAsset.size());
        assertEquals(assetToDelete.get(0), deletedAsset.get(0));

        //Verify size is now 3
        long currentSize = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(3, currentSize);

        //Delete based on multiple criteria

        //Query to delete the newest asset
        QueryCriteria macQuery = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.MACOS).build();

        QueryCriteria winQuery = QueryCriteria.builder()
                .setOS(AssetTypes.OperatingSystem.WINDOWS).build();

        List<QueryCriteria> queryList = new ArrayList<>(asList(macQuery, winQuery));

        // Find the set of items we plan to delete
        List<Asset> setToDelete = inventory.search(queryList);
        assertEquals(3, setToDelete.size());

        List<Asset> deletedAssetList = inventory.deleteAssets(queryList);
        assertEquals(3, deletedAssetList.size());

        assertTrue(CollectionUtils.isEqualCollection(setToDelete, deletedAssetList));
        long finalSize = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(0, finalSize);
    }

    @Test
    public void testDeleteListAssetById() {
        Asset asset1 = getAsset(AssetTypes.OperatingSystem.WINDOWS, AssetTypes.CPU.AMD, TWO_CORE, ONEHUNDREDTWENTYEIGHT_GB);
        Asset asset2 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.INTEL, TWELVE_CORE, EIGHT_GB);
        Asset asset3 = getAsset(AssetTypes.OperatingSystem.LINUX, AssetTypes.CPU.APPLE_SILLICON, TWENTYFOUR_CORE, SIXTYFOUR_GB);
        Asset asset4 = getAsset(AssetTypes.OperatingSystem.MACOS, AssetTypes.CPU.APPLE_SILLICON, FOUR_CORE, THIRTYTWO_GB);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4, asset1, asset2));

        //Add the assets
        List<String> asset_ids = inventory.addAssets(assets);

        long size = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(4, size);

        // Grab object by id
        Optional<Asset> foundAsset = inventory.getAssetById(asset_ids.get(0));
        assertNotNull(foundAsset.get());

        //Delete by id
        Optional<Asset> deleteAsset1 = inventory.deleteAssetById(asset_ids.get(0));
        assertTrue(deleteAsset1.isPresent());
        assertEquals(foundAsset, deleteAsset1);

        //Verify the size
        long new_size = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(3, new_size);

        // Dropping this id
        asset_ids.remove(0);

        // Delete from the inventory based on the remaining ids in the list
        List<Asset> deletedAssetList = inventory.deleteAssetsByIds(asset_ids);
        assertEquals(3, deletedAssetList.size());

        long final_size = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(0, final_size);
    }

    /**
     * Creates an inventory with 1 of each type of os and process where each as 32GB of memory and 12 cores
     */
    private void load32G12CoreInventory() {
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macINTEL1232);
        inventory.addAsset(macXEON1232);

        inventory.addAsset(winAMD1232);
        inventory.addAsset(winINTEL1232);
        inventory.addAsset(winXEON1232);

        inventory.addAsset(linuxAMD1232);
        inventory.addAsset(linuxINTEL1232);
        inventory.addAsset(linuxXEON1232);
    }

    private static Asset getAsset(final AssetTypes.OperatingSystem os, final AssetTypes.CPU cpu, final Integer core,
                           final Integer memory) {
        return Asset.builder()
                .setOS(os)
                .setCPU(cpu)
                .setCore(core)
                .setMemory(memory)
                .build();
    }
}