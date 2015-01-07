package com.github.jearls.SPRaceTracker.data;

import java.util.List;

/**
 * This class manages the data store that the actual data objects use. Specific
 * subclasses will be built for the specific types of datastores.
 * 
 * @author jearls
 */
public abstract class DataStore {
    public static final long serialVersionUID = 1L;

    /**
     * Saves an object back to the DataStore, plus any other objects which
     * cascade from it.
     * 
     * @param o
     *            The object to save.
     * @throws DataStoreException
     *             if an error occurred while saving.
     */
    public abstract void save(Object o) throws DataStoreException;

    /**
     * Removes an object from the DataStore. Might remove other objects based on
     * the cascades.
     * 
     * @param o
     *            The object to remove.
     * @throws DataStoreException
     *             if an error occurred while deleting.
     */
    public abstract void delete(Object o) throws DataStoreException;

    /**
     * Fetches an object from the data store by its ID.
     * 
     * @param objectClass
     *            The object class to fetch and return.
     * @param ID
     *            The ID of the object to fetch.
     * @return The found object.
     * @throws DataStoreNotFoundException
     *             if no object exists in the data store with that ID.
     * @throws DataStoreException
     *             if an error occurred while fetching the object.
     */
    public abstract <T> T fetchByID(Class<T> objectClass, Object ID)
            throws DataStoreNotFoundException, DataStoreException;

    /**
     * Fetches zero or more objects from the data store who have a field with a
     * specific value.
     * 
     * @param objectClass
     *            The object class to fetch and return.
     * @param field
     *            The field name to search within the objects.
     * @param value
     *            The value to look for in that field.
     * @return A List of zero or more objects of the given class that match the
     *         query.
     * @throws DataStoreException
     *             if an error occurred while fetching the objects.
     */
    public abstract <T> List<T> fetchByField(Class<T> objectClass,
            String field, Object value) throws DataStoreException;

    /**
     * Fetches all objects from the data store of a given class.
     * 
     * @param objectClass
     *            The object class to fetch and return.
     * @return A List of the objects.
     * @throws DataStoreException
     *             if an error occurred while fetching the objects.
     */
    public abstract <T> List<T> fetchAll(Class<T> objectClass)
            throws DataStoreException;
}
