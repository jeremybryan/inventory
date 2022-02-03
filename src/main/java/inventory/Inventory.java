package inventory;

import inventory.data.Asset;
import inventory.query.QueryCriteria;

import java.util.List;
import java.util.Optional;

public interface Inventory {

    /** Basic asset creation and deletion methods **/
    String addAsset(final Asset asset);

    List<String> addAssets(final List<Asset> assets);

    Optional<Asset> getAssetById(final String asset_id);

    List<Asset> deleteAssets(final QueryCriteria criteria);

    List<Asset> deleteAssets(final List<QueryCriteria> criteria);

    Optional<Asset> deleteAssetById(final String assetId);

    List<Asset> deleteAssetsByIds(final List<String> assetId);

    /* Since query criteria inventory search and convenience methods */
    List<Asset> getFullInventory();

    Integer totalMemory(final QueryCriteria criteria);

    Integer totalCores(final QueryCriteria criteria);

    Integer maxMemory(final QueryCriteria criteria);

    Integer maxCores(final QueryCriteria criteria);

    Integer minMemory(final QueryCriteria criteria);

    Integer minCores(final QueryCriteria criteria);

    long totalAssets(final QueryCriteria criteria);

    List<Asset> search(final QueryCriteria criteria);


    /*
     * This set of interface methods provides for submitting a list of query criteria
     * allowing for queries like
     *
     * Find me all assets that are Windows, Intel, 4 core, 28GB memory and
     * macOS, Apple Silicon, 2 core and 32 GB of memory
     *
     */
    List<Asset> search(final List<QueryCriteria> criteria);

    long totalAssets(final List<QueryCriteria> criteria);

    Integer totalMemory(final List<QueryCriteria> criteria);

    Integer totalCores(final List<QueryCriteria> criteria);

    Integer maxMemory(final List<QueryCriteria> criteria);

    Integer maxCores(final List<QueryCriteria> criteria);

    Integer minMemory(final List<QueryCriteria> criteria);

    Integer minCores(final List<QueryCriteria> criteria);
}
