package inventory;

import inventory.data.Asset;
import inventory.query.QueryCriteria;

import java.util.List;
import java.util.Optional;

public interface Inventory {

    /** Basic asset creation and deletion methods **/

    // Must return the unique ID of the asset added
    String addAsset(final Asset asset);

    // Contract is to return the list unique IDs of the asset added
    List<String> addAssets(final List<Asset> assets);

    Optional<Asset> getAssetById(final String asset_id);

    // Must return list of assets or empty list
    List<Asset> deleteAssets(final QueryCriteria criteria);

    // Must return list of assets or empty list
    List<Asset> deleteAssets(final List<QueryCriteria> criteria);

    // Must return asset that was deleted
    Optional<Asset> deleteAssetById(final String assetId);

    // Must return list of assets or empty list
    List<Asset> deleteAssetsByIds(final List<String> assetId);

    /* Since query criteria inventory search and convenience methods */

    // Must return list of assets or empty list (if inventory is empty)
    List<Asset> getFullInventory();

    // Must return list of assets or empty list (if inventory is empty)
    int getFullInventorySize();

    int totalMemory();

    int totalMemory(final QueryCriteria criteria);

    int totalCores();

    int totalCores(final QueryCriteria criteria);

    int maxMemory();

    int maxMemory(final QueryCriteria criteria);

    int maxCores();

    int maxCores(final QueryCriteria criteria);

    int minMemory();

    int minMemory(final QueryCriteria criteria);

    int minCores();

    int minCores(final QueryCriteria criteria);

    int totalAssets(final QueryCriteria criteria);

    // Must return list of assets matching query or empty list
    List<Asset> search(final QueryCriteria criteria);


    /*
     * This set of interface methods provides for submitting a list of query criteria
     * allowing for queries like
     *
     * Find me all assets that are Windows, Intel, 4 core, 28GB memory and
     * macOS, Apple Silicon, 2 core and 32 GB of memory
     *
     */

    // Must return list of assets matching query or empty list
    List<Asset> search(final List<QueryCriteria> criteria);

    int totalAssets(final List<QueryCriteria> criteria);

    int totalMemory(final List<QueryCriteria> criteria);

    int totalCores(final List<QueryCriteria> criteria);

    int maxMemory(final List<QueryCriteria> criteria);

    int maxCores(final List<QueryCriteria> criteria);

    int minMemory(final List<QueryCriteria> criteria);

    int minCores(final List<QueryCriteria> criteria);
}
