package com.github.jearls.SPRaceTracker.data;

import java.lang.reflect.InvocationTargetException;
import javax.persistence.Id;

/**
 * This is a version tracker to keep track of which version of each object type
 * is stored in the data store. Each object type will be responsible for being
 * able to update itself when an older version is found (e.g. when the
 * application is updated).
 * 
 * @author jearls
 *
 */
public class ObjectVersion {
    public static final long serialVersionUID = 1L;

    @Id
    private String           name;
    private long             version;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(long version) {
        this.version = version;
    }

    public static void checkAndUpdate(DataStore store, Class<?> objectClass)
            throws DataStoreException {
        long myVersion;
        try {
            myVersion =
                    objectClass.getField("serialVersionUID").getLong(
                            objectClass);
        } catch (Exception e) {
            myVersion = 1L;
        }
        ObjectVersion v;
        try {
            v = store.fetchByID(ObjectVersion.class, objectClass.getName());
        } catch (DataStoreNotFoundException e) {
            v = new ObjectVersion();
            v.setName(objectClass.getName());
            v.setVersion(1L);
            store.save(v);
        }
        if (v.getVersion() != myVersion) {
            try {
                objectClass.getMethod("updateDataStore", DataStore.class,
                        Long.TYPE).invoke(objectClass, store, v.getVersion());
                v.setVersion(myVersion);
                store.save(v);
            } catch (IllegalAccessException e) {
                throw new DataStoreException("Unable to update "
                        + objectClass.getName());
            } catch (NoSuchMethodException e) {
                throw new DataStoreException("Unable to update "
                        + objectClass.getName());
            } catch (InvocationTargetException e) {
                throw new DataStoreException("Unable to update "
                        + objectClass.getName());
            }
        }
    }
}
