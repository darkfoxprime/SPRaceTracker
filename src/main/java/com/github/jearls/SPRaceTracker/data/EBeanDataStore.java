/**
 * 
 */
package com.github.jearls.SPRaceTracker.data;

import java.io.File;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;

/**
 * @author jearls
 *
 */
public class EBeanDataStore extends DataStore {
    public static final long serialVersionUID = 1L;

    ServerConfig ebeanConfig;
    public EbeanServer ebeanServer;

    /**
     * Initialize an EBean+JavaDB Data Store
     * @param dbDir The directory in which to store the database.
     * @throws DataStoreException if an error occurs while initializing the EBean data store.
     */
    public EBeanDataStore(File dbDir) throws DataStoreException {
        super(dbDir);
        ebeanConfig = new ServerConfig();
        ebeanConfig.setName("EBeanDataStore");
        DataSourceConfig localDB = new DataSourceConfig();
        localDB.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
        localDB.setUsername("");
        localDB.setPassword("");
        if (dbDir.isDirectory()) {
            localDB.setUrl("jdbc:derby:" + dbDir.getAbsolutePath());
        } else {
            localDB.setUrl("jdbc:derby:" + dbDir.getAbsolutePath() + ";create=true");
            ebeanConfig.setDdlGenerate(true);
            ebeanConfig.setDdlRun(true);
        }
        ebeanConfig.setDataSourceConfig(localDB);
        ebeanConfig.setDefaultServer(true);
        
        try {
            ebeanServer = EbeanServerFactory.create(ebeanConfig);
        } catch (Exception e) {
            throw new DataStoreException(e);
        }
    }
}
