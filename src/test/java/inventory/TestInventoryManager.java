package inventory;

import inventory.data.Asset;
import inventory.data.types.CPU;
import inventory.data.types.OperatingSystem;
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
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isEqualCollection;
import static org.junit.Assert.*;

public class TestInventoryManager {

    private Inventory inventory;

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;     
    private static final int FOUR = 4;    
    private static final int EIGHT = 8;   
    private static final int TWELVE = 12;
    private static final int TWENTYFOUR = 24;  
    private static final int THIRTYTWO = 32;
    private static final int FOURTY_EIGHT = 48;
    private static final int SIXTYFOUR = 64;
    private static final int ONEHUNDREDTWENTYEIGHT = 128;

    // Set of mac assets
    private Asset macAMD1232 = getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO);
    private Asset macINTEL1232 = getAsset(OperatingSystem.MACOS, CPU.INTEL, TWELVE, THIRTYTWO);
    private Asset macXEON1232 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWELVE, THIRTYTWO);
    private Asset macXEON48 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT);

    // Set of windows assets
    private Asset winAMD1232 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWELVE, THIRTYTWO);
    private Asset winINTEL1232 = getAsset(OperatingSystem.WINDOWS, CPU.INTEL, TWELVE, THIRTYTWO);
    private Asset winXEON1232 = getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWELVE, THIRTYTWO);
    private Asset winXEON24128 = getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR, ONEHUNDREDTWENTYEIGHT);
    private Asset winAMD24128 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR, ONEHUNDREDTWENTYEIGHT);

    // Set of linux assets
    private Asset linuxAMD1232 = getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO);
    private Asset linuxINTEL1232 = getAsset(OperatingSystem.LINUX, CPU.INTEL, TWELVE, THIRTYTWO);
    private Asset linuxXEON1232 = getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, TWELVE, THIRTYTWO);
    private Asset linuxXEON4128 = getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, FOUR, ONEHUNDREDTWENTYEIGHT);


    @Before
    public void setup() {
        inventory = new DefaultInventory();
    }

    @Test
    public void addAsset() {
        Asset macAsset = getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO);
        Asset winAsset = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWELVE, THIRTYTWO);
        Asset linAsset = getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO);

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

        // Since we defined no criteria, this should return nothing...no criteria was matched.
        assertEquals(ZERO, results.size());
    }


    @Test
    public void testQueryFindsOneWinAssetInventory() {
        //load inventory
        load32G12CoreInventory();

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWELVE)
                .setMemory(THIRTYTWO)
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD).build();

        List<Asset> results = inventory.search(query);
        assertNotNull(results);
        assertEquals(ONE, results.size());

        Asset resultAsset = results.get(ZERO);
        assertEquals(resultAsset.getOS(), OperatingSystem.WINDOWS);
        assertEquals(resultAsset.getCPU(), CPU.AMD);
    }

    @Test
    public void testQueryForCoreMemoryAndCPU() {
        //load inventory
        load32G12CoreInventory();

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWELVE)
                .setMemory(THIRTYTWO)
                .setCPU(CPU.AMD).build();

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
                .setCPU(CPU.AMD).build();

        List<Asset> results = inventory.search(query);

        assertEquals(results.size(), FOUR);
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
                .setMemory(EIGHT).build();

        List<Asset> results = inventory.search(query);

        assertEquals(results.size(), ONE);
        assertEquals(results.get(ZERO), macXEON48);
    }

    @Test
    public void testQueryForCoresOnly() {
        //load inventory
        load32G12CoreInventory();

        //Adding 2 additional assets beyond the base inventory
        inventory.addAsset(winXEON24128);
        inventory.addAsset(winAMD24128);

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWENTYFOUR).build();

        List<Asset> results = inventory.search(query);

        assertEquals(TWO, results.size());
        assertTrue(isEqualCollection(
                new ArrayList<>(asList(winXEON24128, winAMD24128)), results));

    }

    @Test
    public void testSumOfCoresAllAssets() {
        //total cores - 48
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core

        int sum = inventory.totalCores();

        assertEquals(FOURTY_EIGHT, sum);
    }

    @Test
    public void testSumOfCoreAll24Assets() {
        //total cores - 48
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core
        inventory.addAsset(winXEON24128); //12 core


        QueryCriteria query = QueryCriteria.builder().
                setCore(TWENTYFOUR).build();

        int sum = inventory.totalCores(query);

        assertEquals(FOURTY_EIGHT, sum);
    }

    @Test
    public void testSumOfCoresAllWindowsAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS).build();

        int sum = inventory.totalCores(query);

        assertEquals(FOURTY_EIGHT, sum);
    }

    @Test
    public void testSumOfCoresAllWindowsAmdAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD).build();

        int sum = inventory.totalCores(query);

        assertEquals(TWENTYFOUR, sum);
    }

    @Test
    public void testSumOfCoresAllAmdAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setCPU(CPU.AMD).build();

        int sum = inventory.totalCores(query);

        assertEquals(FOURTY_EIGHT, sum);
    }

    @Test
    public void testSumOfCoresAll128MemoryAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();

        int sum = inventory.totalCores(query);

        assertEquals(112, sum);
    }

    @Test
    public void testSumOfAllMemory() {
        //total cores - 192
        inventory.addAsset(winAMD24128);   //24 core
        inventory.addAsset(macAMD1232);    //12 core
        inventory.addAsset(linuxXEON1232); //12 core

        int sum = inventory.totalMemory();

        assertEquals(192, sum);
    }

    @Test
    public void testSumOfMemoryAllMacAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder().
                setOS(OperatingSystem.MACOS).build();
        int sum = inventory.totalMemory(query);

        assertEquals(THIRTYTWO, sum);
    }

    @Test
    public void testSumOfMemoryAllMacXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON).build();

        int sum = inventory.totalMemory(query);

        assertEquals(EIGHT, sum);
    }

    @Test
    public void testSumOfMemoryAllXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);


        QueryCriteria query = QueryCriteria.builder()
                .setCPU(CPU.APPLE_SILLICON).build();

        int sum = inventory.totalMemory(query);

        assertEquals(40, sum);
    }

    @Test
    public void testSumOfMemoryAll4CoreAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);


        QueryCriteria query = QueryCriteria.builder()
                .setCore(FOUR).build();

        int sum = inventory.totalMemory(query);

        assertEquals(136, sum);
    }

    @Test
    public void testSumOfMemoryAll32MemoryAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);


        QueryCriteria query = QueryCriteria.builder()
                .setMemory(THIRTYTWO).build();

        Integer sum = inventory.totalMemory(query);

        assertEquals(SIXTYFOUR, sum.intValue());
    }

    @Test
    public void testMinCores() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);

        int sum = inventory.minCores();

        assertEquals(FOUR, sum);
    }

    @Test
    public void testMinCoresForLinuxXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));


        QueryCriteria query = QueryCriteria.builder()
            .setOS(OperatingSystem.LINUX)
            .setCPU(CPU.APPLE_SILLICON)
            .build();

        int sum = inventory.minCores(query);

        assertEquals(FOUR, sum);
    }

    @Test
    public void testMaxCores() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        int sum = inventory.maxCores();

        assertEquals(SIXTYFOUR, sum);
    }

    @Test
    public void testMaxCoresForLinuxXeonAssets() {
        inventory.addAsset(winAMD24128);
        inventory.addAsset(macAMD1232);
        inventory.addAsset(macXEON48);
        inventory.addAsset(linuxXEON1232);
        inventory.addAsset(linuxXEON4128);
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR, ONEHUNDREDTWENTYEIGHT));


        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.LINUX)
                .setCPU(CPU.APPLE_SILLICON)
                .build();

        int sum = inventory.maxCores(query);

        assertEquals(SIXTYFOUR, sum);
    }

    @Test
    public void testMinMemory() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

       int sum = inventory.minMemory();

        assertEquals(EIGHT, sum);
    }

    @Test
    public void testMinMemoryForMacAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS).build();

        int sum = inventory.minMemory(query);

        assertEquals(EIGHT, sum);
    }

    @Test
    public void testMinMemoryForLinuxXeonAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.LINUX)
                .setCPU(CPU.APPLE_SILLICON).build();

        int sum = inventory.minMemory(query);

        assertEquals(ONEHUNDREDTWENTYEIGHT, sum);
    }

    @Test
    public void testMaxMemory() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        int sum = inventory.maxMemory();

        assertEquals(ONEHUNDREDTWENTYEIGHT, sum);
    }

    @Test
    public void testMaxMemoryForMacAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS).build();

        int sum = inventory.maxMemory(query);

        assertEquals(THIRTYTWO, sum);
    }

    @Test
    public void testMaxMemoryForLinuxXeonAssets() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.LINUX)
                .setCPU(CPU.APPLE_SILLICON).build();

        int sum = inventory.maxMemory(query);

        assertEquals(ONEHUNDREDTWENTYEIGHT, sum);
    }


    @Test
    public void testCountOfAll() {
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        int totalAssets = inventory.getFullInventorySize();

        assertEquals(6, totalAssets);
    }

    @Test
    public void testCountAllOfOneCPU() {
        //We are testing for APPLE_SILLICON ... the inventory load adds 3 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, 64,
                ONEHUNDREDTWENTYEIGHT));


        QueryCriteria query = QueryCriteria.builder()
                .setCPU(CPU.APPLE_SILLICON).build();

        int totalAssets = inventory.totalAssets(query);

        assertEquals(6, totalAssets);
    }

    @Test
    public void testCountOfAllOneMemory() {
        //We are testing for 32GB ... the inventory load adds 9 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setMemory(THIRTYTWO).build();

        int totalAssets = inventory.totalAssets(query);

        assertEquals(11, totalAssets);
    }

    @Test
    public void testCountAllOfOneCoreSize() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setCore(TWENTYFOUR).build();

        int totalAssets = inventory.totalAssets(query);

        assertEquals(TWO, totalAssets);
    }

    @Test
    public void testTotalAssetsWinAMD32Core128Memory() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));

        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setCore(TWENTYFOUR)
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();

        int totalAssets = inventory.totalAssets(query);

        assertEquals(ONE, totalAssets);
    }

    @Test
    public void testTotalAssetsWinAMD32Core128MemoryFindNone() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, EIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.AMD, TWELVE, THIRTYTWO));
        inventory.addAsset(getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT));
        inventory.addAsset(getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, SIXTYFOUR,
                ONEHUNDREDTWENTYEIGHT));


        QueryCriteria query = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setCore(THIRTYTWO)
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();

        int totalAssets = inventory.totalAssets(query);

        assertEquals(ZERO, totalAssets);
    }

    @Test
    public void testMultipleCriteria() {
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                ONEHUNDREDTWENTYEIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setCore(THIRTYTWO)
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setCore(TWENTYFOUR)
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();


        //We are looking for the following:
        // All assets that are WINDOWS, AMD, 32 CORE, 128 GB RAM
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE and 128 GB
        // We expect 2
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        long totalAssets = inventory.totalAssets(criteria);
        assertEquals(TWO, totalAssets);

        List<Asset> results = inventory.search(criteria);
        assertTrue(CollectionUtils.isEqualCollection(results, new ArrayList<>(asList(asset1, asset2))));
    }

    @Test
    public void testMemoryMultipleCriteria() {
        //Tests max and min memory for multiple criteria
        //We are testing for 24 Core ... the inventory load adds 0 assets that match
        load32G12CoreInventory();

        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setCore(THIRTYTWO).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setCore(TWENTYFOUR).build();


        //We are looking for the min and max memory across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE
        // We expect the min memory is 8 and the max memory is 128
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        int min_result = inventory.minMemory(criteria);
        assertEquals(EIGHT, min_result);

        int max_result = inventory.maxMemory(criteria);
        assertEquals(ONEHUNDREDTWENTYEIGHT, max_result);
    }

    @Test
    public void testCoreMultipleCriteria() {
        //Tests max and min core for multiple criteria
        load32G12CoreInventory();

        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setMemory(EIGHT).build();


        //We are looking for the min and max memory across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE
        // We expect the min memory is 8 and the max memory is 128
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        int min_result = inventory.minCores(criteria);
        assertEquals(TWENTYFOUR, min_result);

        int max_result = inventory.maxCores(criteria);
        assertEquals(THIRTYTWO, max_result);
    }

    @Test
    public void testTotalCoreMultipleCriteria() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset3 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT)
                .setCore(THIRTYTWO).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setMemory(EIGHT)
                .setCore(TWENTYFOUR).build();


        //We are looking for total cores across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        //We expect the total cores to equal 104
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        int result = inventory.totalCores(criteria);
        assertEquals(104, result);
    }

    @Test
    public void testTotalMemoryMultipleCriteria() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset3 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT)
                .setCore(THIRTYTWO).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setMemory(EIGHT)
                .setCore(TWENTYFOUR).build();


        //We are looking for total cores across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        //We expect the total memory to equal 128 + 24 = 152
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        int result = inventory.totalMemory(criteria);
        assertEquals(152, result);
    }

    @Test
    public void testTotalAssetsMultipleCriteria() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset3 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        inventory.addAsset(asset1);
        inventory.addAsset(asset2);
        inventory.addAsset(asset3);
        inventory.addAsset(asset4);

        // Criteria 1
        QueryCriteria query1 = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS)
                .setCPU(CPU.AMD)
                .setMemory(ONEHUNDREDTWENTYEIGHT)
                .setCore(THIRTYTWO).build();

        // Criteria 2
        QueryCriteria query2 = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS)
                .setCPU(CPU.APPLE_SILLICON)
                .setMemory(EIGHT)
                .setCore(TWENTYFOUR).build();


        //We are looking for total assets across machines matching the following
        // criteria:
        // All assets that are WINDOWS, AMD, 32 CORE, 128GB memory,
        // AND
        // All assets that are MACOS, APPLE_SILLICON, 24 CORE, 8GB Memory
        // We have 4 machines matching this criteria
        List<QueryCriteria> criteria = new ArrayList<>(asList(query1, query2));

        int result = inventory.totalAssets(criteria);
        assertEquals(FOUR, result);
    }

    @Test
    public void testAddAssets() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, THIRTYTWO,
                ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset3 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, TWENTYFOUR,
                EIGHT);

        String assetId = inventory.addAsset(asset1);
        assertNotNull(assetId);

        String assetId2 = inventory.addAsset(asset2);
        assertNotEquals(assetId, assetId2);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4));
        List<String> asset_ids = inventory.addAssets(assets);

        assertEquals(TWO, asset_ids.size());
        assertNotEquals(asset_ids.get(ZERO), asset_ids.get(ONE));
    }

    @Test
    public void testDeleteAssetWithNull() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWO, ONEHUNDREDTWENTYEIGHT);

        List<Asset> assets = new ArrayList<>(singletonList(asset1));

        // Add the assets
        List<String> asset_ids = inventory.addAssets(assets);

        // Determine size of inventory
        int size = inventory.getFullInventorySize();
        assertEquals(ONE, size);

        //Delete using the criteria
        List<Asset> deletedAsset = inventory.deleteAssets((QueryCriteria) null);

        assertEquals(ZERO, deletedAsset.size());

        //Delete using the criteria
        List<Asset> deletedAssetList = inventory.deleteAssets((List<QueryCriteria>) null);

        assertEquals(ZERO, deletedAssetList.size());
    }

    @Test
    public void testDeleteAssetWithCriteria() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWO, ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.INTEL, TWELVE, EIGHT);
        Asset asset3 = getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, TWENTYFOUR, SIXTYFOUR);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, THIRTYTWO);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4, asset1, asset2));

        // Add the assets
        List<String> asset_ids = inventory.addAssets(assets);

        // Determine size of inventory
        int size = inventory.getFullInventorySize();
        assertEquals(FOUR, size);

        //Query to delete the newest asset
        QueryCriteria toDeleteQuery = QueryCriteria.builder()
                .setOS(OperatingSystem.LINUX)
                .setCPU(CPU.APPLE_SILLICON)
                .setCore(TWENTYFOUR)
                .setMemory(SIXTYFOUR).build();

        //Search to find it
        List<Asset> assetToDelete = inventory.search(toDeleteQuery);

        //Delete using the criteria
        List<Asset> deletedAsset = inventory.deleteAssets(toDeleteQuery);

        assertEquals(ONE, deletedAsset.size());
        assertEquals(assetToDelete.get(ZERO), deletedAsset.get(ZERO));

        //Verify size is now 3
        int currentSize = inventory.getFullInventorySize();
        assertEquals(3, currentSize);

        //Delete based on multiple criteria

        //Query to delete the newest asset
        QueryCriteria macQuery = QueryCriteria.builder()
                .setOS(OperatingSystem.MACOS).build();

        QueryCriteria winQuery = QueryCriteria.builder()
                .setOS(OperatingSystem.WINDOWS).build();

        List<QueryCriteria> queryList = new ArrayList<>(asList(macQuery, winQuery));

        // Find the set of items we plan to delete
        List<Asset> setToDelete = inventory.search(queryList);
        assertEquals(3, setToDelete.size());

        List<Asset> deletedAssetList = inventory.deleteAssets(queryList);
        assertEquals(3, deletedAssetList.size());

        assertTrue(CollectionUtils.isEqualCollection(setToDelete, deletedAssetList));
        long finalSize = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(ZERO, finalSize);
    }

    @Test
    public void testDeleteListAssetById() {
        Asset asset1 = getAsset(OperatingSystem.WINDOWS, CPU.AMD, TWO, ONEHUNDREDTWENTYEIGHT);
        Asset asset2 = getAsset(OperatingSystem.MACOS, CPU.INTEL, TWELVE, EIGHT);
        Asset asset3 = getAsset(OperatingSystem.LINUX, CPU.APPLE_SILLICON, TWENTYFOUR, SIXTYFOUR);
        Asset asset4 = getAsset(OperatingSystem.MACOS, CPU.APPLE_SILLICON, FOUR, THIRTYTWO);

        List<Asset> assets = new ArrayList<>(asList(asset3, asset4, asset1, asset2));

        //Add the assets
        List<String> asset_ids = inventory.addAssets(assets);

        int size = inventory.getFullInventorySize()                  ;
        assertEquals(FOUR, size);

        // Grab object by id
        Optional<Asset> foundAsset = inventory.getAssetById(asset_ids.get(ZERO));
        assertNotNull(foundAsset.orElse(null));

        //Delete by id
        Optional<Asset> deleteAsset1 = inventory.deleteAssetById(asset_ids.get(ZERO));
        assertTrue(deleteAsset1.isPresent());
        assertEquals(foundAsset, deleteAsset1);

        //Verify the size
        long new_size = inventory.getFullInventorySize();
        assertEquals(3, new_size);

        // Dropping this id
        asset_ids.remove(ZERO);

        // Delete from the inventory based on the remaining ids in the list
        List<Asset> deletedAssetList = inventory.deleteAssetsByIds(asset_ids);
        assertEquals(3, deletedAssetList.size());

        long final_size = inventory.totalAssets(QueryCriteria.builder().build());
        assertEquals(ZERO, final_size);
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

    private static Asset getAsset(final OperatingSystem os, final CPU cpu, final Integer core,
                           final Integer memory) {
        return Asset.builder()
                .setOS(os)
                .setCPU(cpu)
                .setCore(core)
                .setMemory(memory)
                .build();
    }
}