package inventory;

import com.sun.istack.internal.NotNull;
import inventory.data.Asset;
import inventory.query.QueryCriteria;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

/**
 * Simple class to implement an inventory api using as Hashmap as the underlying storage construct.
 *
 * This implementation adds a naive 'id' generator based upon a simple counter, robust implementation
 * the identification generator would take into account object attributes and ensure uniqueness across restarts.
 * These were not included in this exercise.
 *
 * The api provides for the following:
 * adding assets (singularly or by list)
 * deleting assets (singularly, by list of ids or inventory.data.query object)
 * finding sums, max, mins and total counts across the following:
 * CPU, Operating System, Number of Cores, Memory
 *
 * The implementation relies a QueryCriteria object to define the parameters for
 * searching the inventory.
 *
 * Empty query objects return empty results list, no search is performed in these cases.
 *
 * Look at protecting the calls...adjusting the search to return 0 on empty or null criteria
 * Define a return full inventory convenience method
 *
 */
public class DefaultInventory implements Inventory {

    // Holder of all assets
    private Map<String, Asset> inventories = new HashMap<>();

    private Logger logger = Logger.getLogger(DefaultInventory.class.getName());

    static final String NON_NULL_ARGUMENT = "Cannot add a null object to the inventory.";

    @Override
    public String addAsset(@NotNull final Asset asset) {
        notNull(asset, NON_NULL_ARGUMENT);
        logger.info("addAsset:  " + asset);
        inventories.put(asset.getAssetId(), asset);

        return asset.getAssetId();
    }

    @Override
    public List<String> addAssets(@NotNull final List<Asset> assets) {
        notNull(assets, NON_NULL_ARGUMENT);
        List<String> ids = new ArrayList<>();

        logger.info("addAssets: Adding list of assets");
        for (Asset a : assets) {
            ids.add(addAsset(a));
        }
        return ids;
    }

    @Override
    public List<Asset> deleteAssets(@NotNull final QueryCriteria criteria) {
        if (inValidCriteria(criteria)) {
            logger.info("Input criteria is null, returning empty list");
            return new ArrayList<>();
        }

        logger.info("deleteAssets matching " + criteria);
        return deleteFromInventory(criteria);
    }

    @Override
    public List<Asset> deleteAssets(@NotNull final List<QueryCriteria> criteria) {
        if (inValidCriteria(criteria)) return new ArrayList<>();

        List<Asset> assets = new ArrayList<>();
        logger.info("Deleting a list of assets matching list of criteria" );
        for (QueryCriteria c : criteria) {
            assets.addAll(deleteAssets(c));
        }

        return assets;
    }

    @Override
    public Optional<Asset> deleteAssetById(@NotNull final String asset_id) {
        Asset asset = null;

        logger.info("delete asset with id: " + asset_id);
        if (inventories.containsKey(asset_id)) {
            asset = inventories.get(asset_id);
            inventories.remove(asset_id);
        }

        return ofNullable(asset);
    }

    @Override
    public List<Asset> deleteAssetsByIds(@NotNull final List<String> asset_ids) {
        List<Asset> deletedAssets = new ArrayList<>();

        logger.info("deleteAssetsByIds: deleting a list of assets by id");
        for (String id : asset_ids) {
            Optional<Asset> current = deleteAssetById(id);
            current.ifPresent(deletedAssets::add);
        }
        return deletedAssets;
    }

    @Override
    public Optional<Asset> getAssetById(final String asset_id) {
        Asset result = null;

        logger.info("getAssetById " + asset_id);
        if (inventories.containsKey(asset_id)) {
            result = inventories.get(asset_id);
        }
        return ofNullable(result);
    }

    @Override
    public List<Asset> getFullInventory() {
        // Mimics obtaining the full inventory
        List<Map.Entry<String, Asset>> resultList = filterAssets(QueryCriteria.builder().build());

        return assetsList(resultList);
    }

    @Override
    public int getFullInventorySize() {
        // Mimics obtaining the full inventory
        List<Map.Entry<String, Asset>> resultList = filterAssets(QueryCriteria.builder().build());

        return assetsList(resultList).size();
    }

    /**
     * Convenience method to obtain the total memory for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int totalMemory() {
        logger.info("totalMemory for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getMemory).sum();
    }

    /**
     * This method will allow for finding the total memory across all assets
     * with (or without) defining criteria. For example, a default criteria
     * will simply sum up memory from all assets.
     *
     * If you wish to find the total memory for only Windows machines, define the windows criteria.
     *
     * If you wish to find total memory for Windows Machines with a Xeon process, define those criteria.
     *
     * @param criteria - defines the filter criteria to be used for calculating the sum
     * @return Integer - the total memory across all assets in the inventory meeting the defined criteria
     */
    @Override
    public int totalMemory(final QueryCriteria criteria) {
        logger.info("totalMemory for criteria: " + criteria);
        return search(criteria).stream().mapToInt(Asset::getMemory).sum();
    }

    /**
     * Convenience method to obtain the total cores for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int totalCores() {
        logger.info("totalCores for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getCores).sum();
    }

    /**
     * This method will allow for finding the total cores across all assets
     * with (or without) defining criteria. For example, a default criteria
     * will simply sum up cores from all assets.
     *
     * If you wish to find the total memory for only Windows machines, define the windows criteria.
     *
     * If you wish to find total memory for Windows Machines with a Xeon process, define those criteria.
     *
     * @param criteria - defines the filter criteria to be used for calculating the sum
     * @return Integer - the total cores across all assets in the inventory meeting the defined criteria
     */
    @Override
    public int totalCores(final QueryCriteria criteria) {
        logger.info("totalCores " + criteria);
        return search(criteria).stream()
                .mapToInt(Asset::getCores)
                .sum();
    }

    /**
     * This method can be used to find the total number of assets by any criteria
     * or combination of criteria.
     *
     * For example, to find how many assets are in the inventory with the Windows OS
     * simple pass in the criteria object with the OS field set to Windows.
     *
     * @param criteria
     * @return long - total count of assets matching the criteria
     */
    @Override
    public int totalAssets(final QueryCriteria criteria) {
        logger.info("totaling assets matching criteria: " + criteria);
        return search(criteria).size();
    }

    /**
     * Convenience method to obtain the maxMemory for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int maxMemory() {
        logger.info("maxMemory for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getMemory).max().orElse(0);
    }


    @Override
    public int maxMemory(final QueryCriteria criteria) {
        logger.info("maxMemory " + criteria);
        return search(criteria).stream().mapToInt(Asset::getMemory).max().orElse(0);
    }

    /**
     * Convenience method to obtain the maxCores for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int maxCores() {
        logger.info("maxCores for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getCores).max().orElse(0);
    }


    @Override
    public int maxCores(final QueryCriteria criteria) {
        logger.info("maxCores " + criteria);
        return search(criteria)
                .stream()
                .mapToInt(Asset::getCores)
                .max().orElse(0);
    }

    /**
     * Convenience method to obtain the minMemory for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int minMemory() {
        logger.info("minMemory for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getMemory).min().orElse(0);
    }

    @Override
    public int minMemory(final QueryCriteria criteria) {
        logger.info("minMemory " + criteria);
        return search(criteria).stream().mapToInt(Asset::getMemory).min().orElse(0);
    }

    /**
     * Convenience method to obtain the minCores for the entire inventory
     * without having to construct a query object list.
     *
     * @return int
     */
    @Override
    public int minCores() {
        logger.info("minCores for entire inventory");
        return getFullInventory().stream().mapToInt(Asset::getCores).min().orElse(0);
    }

    @Override
    public int minCores(final QueryCriteria criteria) {
        logger.info("minCores " + criteria);
        return search(criteria).stream().mapToInt(Asset::getCores).min().orElse(0);
    }

    /**
     * This takes a inventory.data.query criteria as input and builds a list of entries from the inventory the need
     * to be deleted based upon the criteria.
     *
     * The collected list is then used to reduce the inventory.
     *
     * @param criteria
     * @return list of assets as deleted from the map
     */
    private List<Asset> deleteFromInventory(final QueryCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) return new ArrayList<>();

        logger.info("deleteFromInventory " + criteria);
        List<Map.Entry<String, Asset>> deleteList = filterAssets(criteria);

        //Now remove the entries from the map
        for (Map.Entry<String, Asset> entry : deleteList) {
            //remove the item from the list
            inventories.remove(entry.getKey());
        }

        //Construct the list of assets that were deleted
        return assetsList(deleteList);
    }

    /**
     * Applies the filter criteria to the inventory and returns a list of matching map entries.
     * @param criteria
     * @return List<Map<String, Asset>> this is the set of entries matching the inventory.data.query
     */
    private List<Map.Entry<String, Asset>> filterAssets(final QueryCriteria criteria) {
        return inventories.entrySet().stream()
                .filter(includeOS(criteria))
                .filter(includeCPU(criteria))
                .filter(includeCores(criteria))
                .filter(includeMemory(criteria))
                .collect(toList());
    }

    /**
     * Takes a list of map entries and returns just the values (assets) as a list
     * @param assets
     * @return list of assets matching the query criteria
     */
    private List<Asset> assetsList(final List<Map.Entry<String, Asset>> assets) {
        return assets.stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Basic search method taking a single QueryCriteria object.
     * If the criteria object has no parameters (isEmpty) or is null, the search
     * returns an empty list.
     *
     * @param criteria
     * @return list of assets matching the query criteria
     * */
    @Override
    public List<Asset> search(@NotNull final QueryCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) return new ArrayList<>();

        logger.info("search for assets matching criteria: " + criteria);

        List<Map.Entry<String, Asset>> resultList = filterAssets(criteria);

        return assetsList(resultList);
    }

    /**
     * List the basic search method but takes a list of criteria allowing for
     * more robust searching.
     *
     * If the list is null, the method returns an empty list (found nothing).
     *
     * If any entry in the list is null or isEmpty, the rules of the search
     * method apply.
     *
     * @param criteria
     * @return list of assets matching the query criteria
     * */
    @Override
    public List<Asset> search(@NotNull final List<QueryCriteria> criteria) {
        if (criteria == null) return new ArrayList<>();

        List<Asset> result = new ArrayList<>();

        for (QueryCriteria c : criteria) {
            result.addAll(search(c));
        }

        return result;
    }

    /**
     * Determines the total number of assets matching the list of criteria
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int totalAssets(@NotNull final List<QueryCriteria> criteria) {
        int result = 0;

        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return result;
        }

        for (QueryCriteria c : criteria) {
            result += totalAssets(c);
        }

        return result;
    }

    /**
     * Determines the total amount of memory based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int totalMemory(@NotNull final List<QueryCriteria> criteria) {
        int result = 0;

        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return result;
        }

        for (QueryCriteria c : criteria) {
            result += totalMemory(c);
        }

        return result;
    }

    /**
     * Determines the total number of cores based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int totalCores(@NotNull final List<QueryCriteria> criteria) {
        int result = 0;

        // Short circuit is null
        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return result;
        }

        for (QueryCriteria c : criteria) {
            result += totalCores(c);
        }

        return result;
    }

    /**
     * Determines the max amount of memory based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int maxMemory(@NotNull final List<QueryCriteria> criteria) {
        int result = 0;

        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return 0;
        }

        for (QueryCriteria c : criteria) {
            result = max(maxMemory(c), result);
        }

        return result;
    }

    /**
     * Determines the max number of cores based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int maxCores(@NotNull final List<QueryCriteria> criteria) {
        int result = 0;

        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return 0;
        }

        for (QueryCriteria c : criteria) {
            result = max(maxCores(c), result);
        }

        return result;
    }

    /**
     * Determines the min amount of memory based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int minMemory(@NotNull final List<QueryCriteria> criteria) {
        // Set the initial value to the max integer
        int result = Integer.MAX_VALUE;

        // Short circuit is null
        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return 0;
        }

        for (QueryCriteria c : criteria) {
            result = min(minMemory(c), result);
        }

        return result;
    }

    /**
     * Determines the min number of cores based on the list criteria provided.
     * If the input list is null, a message is logged and a 0 is returned.
     *
     * @param criteria
     * @return int
     */
    @Override
    public int minCores(@NotNull final List<QueryCriteria> criteria) {
        // Set the initial value to the max integer
        int result = Integer.MAX_VALUE;

        // Short circuit is null
        if (criteria == null) {
            logger.info("Input criteria is null, returning 0.");
            return 0;
        }

        for (QueryCriteria c : criteria) {
            result = min(minCores(c), result);
        }

        return result;
    }

    private boolean inValidCriteria(final QueryCriteria criteria) {
        return criteria == null;
    }

    private boolean inValidCriteria(final List<QueryCriteria> criteria) {
        return criteria == null;
    }

    private static Predicate<Map.Entry<String, Asset>> includeOS(final QueryCriteria criteria) {
        return criteria.getOs().isPresent() ? o -> o.getValue().getOS() == criteria.getOs().get() : o -> true;
    }

    private static Predicate<Map.Entry<String, Asset>> includeCPU(final QueryCriteria criteria) {
        return criteria.getCpu().isPresent() ? o -> o.getValue().getCPU() == criteria.getCpu().get() : o -> true;
    }

    private static Predicate<Map.Entry<String, Asset>> includeMemory(final QueryCriteria criteria) {
        return criteria.getMemory().isPresent() ? o ->
                o.getValue().getMemory().intValue() == criteria.getMemory().get() : o -> true;
    }

    private static Predicate<Map.Entry<String, Asset>> includeCores(final QueryCriteria criteria) {
        return criteria.getCores().isPresent() ? o -> o.getValue().getCores().intValue() == criteria.getCores().get()
                : o -> true;
    }

}
