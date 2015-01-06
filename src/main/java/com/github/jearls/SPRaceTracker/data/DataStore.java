/**
 * 
 */
package com.github.jearls.SPRaceTracker.data;

import java.io.File;

/**
 * This class manages the data store that the actual data objects use.  Specific subclasses will be built for the specific types of datastores.
 * @author jearls
 */
public abstract class DataStore {
    public static final long serialVersionUID = 1L;
    
    public DataStore(File dbDir) throws DataStoreException {}
}
