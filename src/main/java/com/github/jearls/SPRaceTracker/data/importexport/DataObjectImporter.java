package com.github.jearls.SPRaceTracker.data.importexport;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jearls.SPRaceTracker.data.DataStore;
import com.github.jearls.SPRaceTracker.data.DataStoreException;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.ReferencedObjectData;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.DirectObjectData;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.StructuredObjectDataMap;

/**
 * This is the base DataObjectImporter class. Instead of this class driving the
 * subclasses like in the DataObjectExporter, the subclasses must drive the
 * import process, first calling this class to initialize the import, then by
 * processing their import data to identify the data classes and load the object
 * data, and calling importObjectData to load the object data into the database.
 * At the end of the import, the relational data should be imported by calling
 * importObjectRelations, and then the import finalized.
 * 
 * XXX Assumption: All imports and exports are based on strings. This might not
 * be a valid assumption.
 * 
 * @author jearls
 */
public abstract class DataObjectImporter {
    public static final long serialVersionUID = 1L;

    final DataStore          dataStore;

    static Object convertStringToObject(String value, Class<?> dataType)
            throws ImporterExporterException {
        PropertyEditor editor = PropertyEditorManager.findEditor(dataType);
        if (editor != null) {
            editor.setAsText(value);
            return editor.getValue();
        }
        throw new ImporterExporterException("Unable to convert string to "
                + dataType.getName());
    }

    public DataObjectImporter(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    class ToOneRelation {
        Object                  dataObject;
        FieldInfo               fieldInfo;
        StructuredObjectDataMap relatedIdentity;

        ToOneRelation(Object dataObject, FieldInfo fieldInfo,
                StructuredObjectDataMap relatedIdentity) {
            this.dataObject = dataObject;
            this.fieldInfo = fieldInfo;
            this.relatedIdentity = relatedIdentity;
        }

        @Override
        public String toString() {
            return "ToOneRelation(" + "("
                    + dataObject.getClass().getSimpleName() + ")" + dataObject
                    + "," + fieldInfo + "," + relatedIdentity + ")";
        }
    }

    class ToManyRelation {
        Class<?>                      dataClass;
        StructuredObjectDataMap       objectIdentity;
        FieldInfo                     fieldInfo;
        List<StructuredObjectDataMap> relatedIdentities;

        ToManyRelation(Class<?> dataClass,
                StructuredObjectDataMap objectIdentity, FieldInfo fieldInfo,
                List<StructuredObjectDataMap> relatedIdentity) {
            this.dataClass = dataClass;
            this.objectIdentity = objectIdentity;
            this.fieldInfo = fieldInfo;
            this.relatedIdentities =
                    new LinkedList<StructuredObjectDataMap>(relatedIdentity);
        }

        @Override
        public String toString() {
            return "ToManyRelation(" + dataClass.getSimpleName() + ","
                    + objectIdentity + "," + fieldInfo + ","
                    + relatedIdentities + ")";
        }
    }

    List<ToOneRelation>  relatedToOneList  = new LinkedList<ToOneRelation>();
    List<ToManyRelation> relatedToManyList = new LinkedList<ToManyRelation>();

    void initializeImport() throws ImporterExporterException {
    }

    void finalizeImport() throws ImporterExporterException {
        try {
            if (relatedToOneList.size() > 0) {
                System.err.println("Need to finish *ToOne relations:");
                while (!relatedToOneList.isEmpty()) {
                    int startSize = relatedToOneList.size();
                    System.err.println("Starting size = " + startSize);
                    Iterator<ToOneRelation> relatedToOneIter =
                            relatedToOneList.iterator();
                    while (relatedToOneIter.hasNext()) {
                        ToOneRelation relation = relatedToOneIter.next();
                        Object relatedObject =
                                queryDataStoreForObjectIdentity(
                                        relation.fieldInfo.field.getType(),
                                        relation.relatedIdentity);
                        if (relatedObject != null) {
                            relatedToOneIter.remove();
                            relation.fieldInfo.setter.invoke(
                                    relation.dataObject, relatedObject);
                            dataStore.save(relatedObject);
                        }
                    }
                    if (relatedToOneList.size() == startSize) {
                        System.err.println("");
                        throw new ImporterExporterException(
                                "Cyclic key relation found!  Check database!");
                    }
                }
            }
            if (relatedToManyList.size() > 0) {
                System.err.println("Need to finish *ToMany relations:");
                for (ToManyRelation relation : relatedToManyList) {
                    System.err.println(relation);
                    Object dataObject =
                            queryDataStoreForObjectIdentity(relation.dataClass,
                                    relation.objectIdentity);
                    if (dataObject != null) {
                        for (StructuredObjectDataMap relatedIdentity : relation.relatedIdentities) {
                            Object relatedObject =
                                    queryDataStoreForObjectIdentity(
                                            relation.fieldInfo.relatedObject.objectClass,
                                            relatedIdentity);
                            Collection<?> relationCollection =
                                    (Collection<?>) relation.fieldInfo.getter
                                            .invoke(dataObject);
                            if (relationCollection.contains(relatedObject)) {
                                System.err.println("Skipping already-existing "
                                        + relatedObject + " in "
                                        + relation.dataClass.getSimpleName()
                                        + "."
                                        + relation.fieldInfo.field.getName());
                            } else {
                                relation.fieldInfo.adder.invoke(dataObject,
                                        relatedObject);
                                dataStore.save(dataObject);
                            }
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new ImporterExporterException(e);
        } catch (DataStoreException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Imports the abstract representation of a data object's values.
     * 
     * @param objectFields
     *            The data fields of the object, as a mapping of FieldInfo -&gt;
     *            StructuredObjectData.
     */
    void importDataObjectValues(Class<?> dataClass,
                                StructuredObjectDataMap objectIdentity,
                                StructuredObjectDataMap objectFields)
            throws ImporterExporterException {
        // first, check if we already exist in the data store.
        // to do this, we get our ObjectIdentityInfo and generate a data store
        // query based on the identityFieldMap and the objectFields.
        if (queryDataStoreForObjectIdentity(dataClass, objectIdentity) == null) {
            try {
                System.err.println("Importing " + dataClass.getSimpleName()
                        + " from " + objectFields);
                System.err.println("* create new instance of "
                        + dataClass.getSimpleName());
                Object dataObject = dataClass.getConstructor().newInstance();
                for (Entry<FieldInfo, StructuredObjectData> fieldData : objectFields
                        .entrySet()) {
                    if (fieldData.getValue() instanceof DirectObjectData) {
                        System.err
                                .println("* Call "
                                        + fieldData.getKey().setter.getName()
                                        + "("
                                        + (((DirectObjectData) fieldData
                                                .getValue()).fieldData)
                                                .toString() + ")");
                        fieldData.getKey().setter
                                .invoke(dataObject,
                                        ((DirectObjectData) fieldData
                                                .getValue()).fieldData);
                    } else {
                        System.err.println("** cannot deal with "
                                + fieldData.getKey());
                        relatedToOneList.add(new ToOneRelation(dataObject,
                                fieldData.getKey(),
                                new StructuredObjectDataMap(
                                        ((ReferencedObjectData) fieldData
                                                .getValue()).referencedData)));
                    }
                }
                dataStore.save(dataObject);
            } catch (ReflectiveOperationException e) {
                throw new ImporterExporterException(e);
            } catch (DataStoreException e) {
                throw new ImporterExporterException(e);
            }
        } else {
            System.err.println("Skipping already-existing "
                    + dataClass.getSimpleName() + " " + objectIdentity);
        }
    }

    /**
     * Queries the data store for an object based on a structured identity map.
     * First, this scans the identity map for any ReferencedObjectData fields.
     * Those are recursively queried. If we got a result from the query, then
     * the ReferencedObjectData identity is replaced by the ID field of the
     * object found and we make the data store query against this ID. If exactly
     * one object was returned from the data store query, we return it;
     * otherwise, we return null.
     * 
     * @param objectIdentity
     * @return
     */
    Object queryDataStoreForObjectIdentity(Class<?> dataClass,
                                           StructuredObjectDataMap objectIdentity) {
        Map<String, Object> queryFields = new HashMap<String, Object>();
        Object returnValue = null;
        for (Entry<FieldInfo, StructuredObjectData> idEntry : objectIdentity
                .entrySet()) {
            if (idEntry.getValue() instanceof ReferencedObjectData) {
                Object result =
                        queryDataStoreForObjectIdentity(
                                idEntry.getKey().field.getType(),
                                ((ReferencedObjectData) idEntry.getValue()).referencedData);
                if (result == null) {
                    return null;
                }
                queryFields.put(idEntry.getKey().field.getName(), result);
            } else {
                queryFields.put(idEntry.getKey().field.getName(),
                        ((DirectObjectData) idEntry.getValue()).fieldData);
            }
        }
        try {
            List<?> results = dataStore.fetchByFields(dataClass, queryFields);
            if (results.size() == 1) {
                returnValue = results.get(0);
            }
        } catch (DataStoreException e) {
        }
        // System.err.println("queryDataStoreForObjectIdentity("
        // + dataClass.getSimpleName() + "," + objectIdentity + "["
        // + queryFields + "]" + ") -> " + returnValue);
        return returnValue;
    }

    /**
     * Imports the abstract representation of a data object's relation.
     * 
     * @param objectRelations
     *            The list of related objects that are "owned" by this object
     *            (meaning they are in a @ManyToMany relationship with no
     *            mappedBy attribute)
     */
    void importDataObjectRelations(Class<?> dataClass,
                                   StructuredObjectDataMap objectIdentity,
                                   FieldInfo relatingField,
                                   List<StructuredObjectDataMap> relatedObjects)
            throws ImporterExporterException {
        relatedToManyList.add(new ToManyRelation(dataClass, objectIdentity,
                relatingField, relatedObjects));
    }
}
