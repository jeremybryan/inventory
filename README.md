### Inventory API Exercise

This project provides an implementation of an inventory api that allows for the collection of the following information:

- CPU type: Intel, AMD, Apple Silicon
- Operating System: macOS, Windows, Linux
- Number of CPU cores (integer)
- Amount of memory in GB (integer)

The API defines an interface behind which alternative inventory implementations can be created using databases or other 
technical implementation approaches. The `DefaultInventory` uses a simple `Map` to store assets added to the inventory.

The API provides methods for querying the inventory allowing for search and retrieval of the following:

- Sum of memory across all Windows machines
- Count of machines with an AMD CPU
- Max memory across all macOS machines on Intel CPUs with 8 CPU cores

Additionally, convenience methods are provided for performing simple count operation for all 4 criteria and also sum, 
min, and max operations for the two integer criteria (CPU cores, memory).

#### Assumptions

##### Storage
The implementation makes uses of a `Map<String, Asset>` to collect assets into the inventory and therefore a simple 
unique identifier was generated for each Asset as part of the `Asset` creation process. This could very easily be 
adjust to make use of some other organization identification scheme. In this case, the `Asset` class (which represents 
an item in the inventory) was not assumed to have a name or otherwise unique identifier, so it was decided to generate 
an ID update Asset object creation.

##### Querying 
To facilitate the query creation process and eliminate fragile input arugment checking, it was decided to craft the 
`QueryCriteria` class to embody the scope of queries. This class utilizes a builder pattern to further facilitate ease 
of creation and improved control. The `Asset` class also makes use of this pattern.

The API provides a set of methods to allow for submitting a singular `QueryCriteria` object to drive a search or a 
`List<QueryCriteria>` objects to allow for more extensive searches. For example:

A singular query could be performed by defining a `QueryCriteria` object with the following parameters:
    `Windows OS, Intel CPU, 4 Core and 12GB of Memory`.
    
In which case the query would return a list of assets matching that one criteria.
    
A list of queries could look like this:   
    `Windows OS, Intel CPU, 4 Core and 12GB of Memory` AND
    `macOS, Apple Silicon, 8 Core and 64 GB of Memory`
    
In which case the query would return a list of assets matching both sets criteria.

##### Assets 
The `Asset` class does introduce some checking on input parameters to ensure build assets are valid. These include 
ensuring non-zero, positive values for memory and cpu while also verifying the operating system and cpu fields have 
been defined. In short, all four input parameters are required. 

For more extensive implementation scenarios, the Asset builder could be extended to decompose into more granular 
Asset classes and builders.

##### Asset Adds and Deletes
The API includes methods for adding and deleting items from the inventory. Some baseline assumptions were made here 
in terms of behavior.

For asset adds, submission of a null asset triggers an exception to be thrown as it is not assumed a clien would attempt
to store a null object.
For successful adds, the ID (or set of IDs) for the assets added to the inventory is returns to the calling code.  

For successful asset deletes the asset (or set of assets) deleted from the inventory is returned to the calling code.
This could easily be augmented to simply return a `boolean` to indicate the operation suceeded, a count to indicate how 
many objects were deleted or perhaps the IDs of the object(s) deleted.

##### Asset Types
This approach made use of enums to define the CPU and Operating System types. There are otherwise this could be 
done but this approach allowed for improved handling and safety.  This approach would accomodate adding new/different
types with minimal impact to the overall implementation.

##### Logging
Simple logging was added to provide indication of activity. Currently this is all at the `info` level. This could and 
should be adjusted when incorporated into an existing project to align with logging strategies for granularity 
and volume. 


#### Building
This project uses a standards Maven based build and also has two Dockerfiles. Dockerfile is used within the context of the GitHub Actions build 
and is used as part of a step to use the Maven build artifacts to create a container image. Dockerfile.full is will perform the build of the software 
and package as a container. 

To build a container locally using just the Dockerfile you must mount the local directory containing the jar file. Use the following:

$  docker build . -t containername:version