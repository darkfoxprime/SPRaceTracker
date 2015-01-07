package com.github.jearls.SPRaceTracker.data;

import java.io.File;
import java.util.List;

import javax.persistence.OptimisticLockException;

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

    ServerConfig             ebeanConfig;
    public EbeanServer       ebeanServer;

    /**
     * Initialize an EBean+JavaDB Data Store
     * 
     * @param dbDir
     *            The directory in which to store the database.
     * @throws DataStoreException
     *             if an error occurs while initializing the EBean data store.
     */
    public EBeanDataStore(File dbDir) throws DataStoreException {
        super();
        ebeanConfig = new ServerConfig();
        ebeanConfig.setName("EBeanDataStore");
        DataSourceConfig localDB = new DataSourceConfig();
        localDB.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
        localDB.setUsername("");
        localDB.setPassword("");
        if (dbDir.isDirectory()) {
            localDB.setUrl("jdbc:derby:" + dbDir.getAbsolutePath());
        } else {
            localDB.setUrl("jdbc:derby:" + dbDir.getAbsolutePath()
                    + ";create=true");
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

    /**
     * Save the object back to the EbeanServer.
     * 
     * @param o
     *            The object to save.
     * @throws DataStoreException
     *             if the EbeanServer generated an OptimisticLockException.
     * @see com.github.jearls.SPRaceTracker.data.DataStore#save(java.lang.Object)
     */
    @Override
    public void save(Object o) throws DataStoreException {
        try {
            this.ebeanServer.save(o);
        } catch (OptimisticLockException e) {
            throw new DataStoreException(e);
        }
    }

    /**
     * Delete the object from the EbeanServer.
     * 
     * @param o
     *            The object to delete.
     * @throws DataStoreException
     *             if the EbeanServer generated an OptimisticLockException.
     * @see com.github.jearls.SPRaceTracker.data.DataStore#delete(java.lang.Object)
     */
    @Override
    public void delete(Object o) throws DataStoreException {
        try {
            this.ebeanServer.delete(o);
        } catch (OptimisticLockException e) {
            throw new DataStoreException(e);
        }
    }

    /**
     * Fetches an object from the ebeanServer.
     * 
     * @param objectClass
     *            The object class to find and return.
     * @param ID
     *            The ID of the object to find.
     * @return The found object.
     * @throws DataStoreNotFoundException
     *             if the object was not found.
     * @see com.github.jearls.SPRaceTracker.data.DataStore#fetchByID(java.lang.Class,
     *      java.lang.Object)
     */
    @Override
    public <T> T fetchByID(Class<T> objectClass, Object ID)
            throws DataStoreNotFoundException {
        T returnVal = this.ebeanServer.find(objectClass, ID);
        if (returnVal == null) { throw new DataStoreNotFoundException(ID
                + " not found for " + objectClass.getCanonicalName()); }
        return returnVal;
    }

    /**
     * Fetches zero or more objects from the ebeanServer.
     * 
     * @param objectClass
     *            The object class to find and return.
     * @param field
     *            The field name to look for.
     * @param value
     *            The value of that field to find.
     * @return The List of zero or more objects.
     * @throws DataStoreException
     *             if an error occurred while querying the server.
     * @see com.github.jearls.SPRaceTracker.data.DataStore#fetchByField(java.lang.Class,
     *      java.lang.String, java.lang.Object)
     */
    @Override
    public <T> List<T> fetchByField(Class<T> objectClass, String field,
            Object value) throws DataStoreException {
        return this.ebeanServer.find(objectClass).where().eq(field, value)
                .findList();
    }

    /**
     * Fetches all objects of a given class from the ebeanServer.
     * 
     * @param objectClass
     *            The object class to find and return.
     * @return The List of all objects of that class.
     * @throws DataStoreException
     *             if an error occurred while querying the server.
     * @see com.github.jearls.SPRaceTracker.data.DataStore#fetchAll(java.lang.Class)
     */
    @Override
    public <T> List<T> fetchAll(Class<T> objectClass) throws DataStoreException {
        return this.ebeanServer.find(objectClass).findList();
    }
}
