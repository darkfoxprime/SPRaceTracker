package com.github.jearls.SPRaceTracker.data.importexport;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jearls.SPRaceTracker.data.DataStore;
import com.github.jearls.SPRaceTracker.data.DataStoreException;
import com.github.jearls.SPRaceTracker.data.importexport.FieldInfo.FieldType;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.StructuredObjectDataMap;

/**
 * This is the base DataObjectExporter class. It analyzes the data store,
 * extracts or creates object data. Subclasses must be defined to do the actual
 * import/export work.
 * 
 * XXX Assumption: All imports and exports are based on strings. This might not
 * be a valid assumption.
 * 
 * @author jearls
 */
public abstract class DataObjectExporter {
    public static final long serialVersionUID = 1L;

    /**
     * Exports the DataStore data for all classes related, directly or
     * indirectly, to the listed dataClasses.
     * 
     * @param store
     *            The DataStore that holds the data to export.
     * @param dataClasses
     *            The classes to be exported.
     * @throws ImporterExporterException
     */
    public void exportData(DataStore store, Class<?>... dataClasses)
            throws ImporterExporterException {
        initializeExport();
        for (Entry<Class<?>, ClassAnalysis> classInfo : ClassAnalysis
                .analyzeRelatedClasses(dataClasses).entrySet()) {
            final Class<?> dataClass = classInfo.getKey();
            final ClassAnalysis analysis = classInfo.getValue();
            exportDataClass(store, dataClass, analysis);
        }
        finalizeExport();
    }

    /**
     * <p>
     * Exports the DataStore data for the listed classes.
     * </p>
     * <p>
     * <b><i>IMPORTANT!</i></b> It is the caller's responsibility to ensure that
     * any other DataStore classes referenced directly or as part of a
     * ManyToMany relation are included in the export, or the exported data will
     * not be importable.
     * </p>
     * 
     * @param store
     *            The DataStore that holds the data to export.
     * @param dataClasses
     *            The classes to be exported.
     * @throws ImporterExporterException
     */
    public void exportDataClasses(DataStore store, Class<?>... dataClasses)
            throws ImporterExporterException {
        initializeExport();
        for (Class<?> dataClass : dataClasses) {
            final ClassAnalysis analysis =
                    ClassAnalysis.analyzeClass(dataClass);
            exportDataClass(store, dataClass, analysis);
        }
        finalizeExport();
    }

    /**
     * Fetches the data objects for a single data class and exports those.
     * 
     * @param store
     *            The DataStore from which to fetch the data objects.
     * @param dataClass
     *            The class of objects to fetch.
     * @param analysis
     *            The ClassAnalysis of the data class.
     * @throws ImporterExporterException
     */
    void exportDataClass(DataStore store, Class<?> dataClass,
                         ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            final List<?> objects = store.fetchAll(dataClass);
            startExporting(dataClass, analysis);
            for (Object dataObject : objects) {
                exportDataObject(dataObject, analysis);
            }
            finishExporting(dataClass, analysis);
        } catch (DataStoreException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * <p>
     * Exports one or more data objects.
     * </p>
     * <p>
     * <p>
     * <b><i>IMPORTANT!</i></b> It is the caller's responsibility to ensure that
     * any other data objects referenced directly or as part of a ManyToMany
     * relation are included in the export, or the exported data will not be
     * importable.
     * </p>
     * 
     * @param dataObjects
     * @throws ImporterExporterException
     */
    public void exportDataObjects(Object... dataObjects)
            throws ImporterExporterException {
        initializeExport();
        Map<Class<?>, ClassAnalysis> classAnalyses =
                new HashMap<Class<?>, ClassAnalysis>();
        Map<Class<?>, List<Object>> classObjects =
                new HashMap<Class<?>, List<Object>>();
        for (Object dataObject : dataObjects) {
            if (!classAnalyses.containsKey(dataObject.getClass())) {
                classObjects.put(dataObject.getClass(),
                        new LinkedList<Object>());
                classAnalyses.put(dataObject.getClass(),
                        ClassAnalysis.analyzeClass(dataObject.getClass()));
            }
            classObjects.get(dataObject.getClass()).add(dataObject);
        }
        for (Class<?> dataClass : classAnalyses.keySet()) {
            final ClassAnalysis analysis = classAnalyses.get(dataClass);
            startExporting(dataClass, analysis);
            for (Object dataObject : classObjects.get(dataClass)) {
                exportDataObject(dataObject, analysis);
            }
            finishExporting(dataClass, analysis);
        }
        finalizeExport();
    }

    /**
     * Fetches the object data described by a set of fieldInfos from a data
     * object and stores it in the passed-in map of StructuredObjectData. Data
     * in referenced objects are stored into ReferencedObjectData objects within
     * the map.
     * 
     * @param fieldInfos
     *            The fieldInfos describing the fields to fetch from the data
     *            object.
     * @param objectData
     *            The object from which to fetch the data.
     * @param objectData
     *            The map of FieldInfo to StructuredObjectData that will hold
     *            the fetched object data.
     * @throws ReflectiveOperationException
     */
    static void fetchObjectData(Collection<FieldInfo> fieldInfos,
                                Object dataObject,
                                StructuredObjectDataMap objectData)
            throws ReflectiveOperationException {
        for (final FieldInfo info : fieldInfos) {
            Object fieldData = info.getter.invoke(dataObject);
            if (info.relatedObject == null) {
                objectData.put(info, new StructuredObjectData.DirectObjectData(
                        fieldData));
            } else {
                StructuredObjectData.ReferencedObjectData refData =
                        new StructuredObjectData.ReferencedObjectData();
                objectData.put(info, refData);
                fetchObjectData(info.relatedObject.identityFieldMap.values(),
                        fieldData, refData.referencedData);
            }
        }
    }

    /**
     * Used by exportDataClass and exportDataObjects to export a single data
     * object.
     * 
     * @param objectData
     *            The data object to be exported.
     * @param analysis
     *            The ClassAnalysis for the data object's class.
     * @throws ImporterExporterException
     */
    public void exportDataObject(Object dataObject, ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            // generate an easier representation of the object's fields.
            final StructuredObjectDataMap objectFields =
                    new StructuredObjectDataMap();
            fetchObjectData(analysis.fieldMap.values(), dataObject,
                    objectFields);
            // generate an easier representation of the identities of
            // the object's relations.
            // this is stored as a mapping of FieldInfo (for the
            // object's field) to a list of the identity data for the
            // objects listed by that field.
            final Map<FieldInfo, List<StructuredObjectDataMap>> objectRelations =
                    new HashMap<FieldInfo, List<StructuredObjectDataMap>>();
            for (final FieldInfo relatedFieldInfo : analysis.relationMap
                    .values()) {
                if (relatedFieldInfo.fieldType == FieldType.OWNING_RELATION) {
                    final Collection<?> relatedObjects =
                            (Collection<?>) relatedFieldInfo.getter
                                    .invoke(dataObject);
                    final List<StructuredObjectDataMap> relatedObjectData =
                            new LinkedList<StructuredObjectDataMap>();
                    for (final Object relatedObject : relatedObjects) {

                        final StructuredObjectDataMap relatedObjectIdentity =
                                new StructuredObjectDataMap();
                        fetchObjectData(
                                relatedFieldInfo.relatedObject.identityFieldMap
                                        .values(),
                                relatedObject, relatedObjectIdentity);
                        relatedObjectData.add(relatedObjectIdentity);
                    }
                    objectRelations.put(relatedFieldInfo, relatedObjectData);
                }
            }
            exportObjectValues(dataObject.getClass(), objectFields,
                    objectRelations);
        } catch (ReflectiveOperationException e) {
            // FIXME: we're just skipping objects when we can't access a
            // field??
        }
    }

    /**
     * Does whatever is needed to initialize the exported data.
     */
    abstract void initializeExport() throws ImporterExporterException;

    /**
     * Does whatever is needed to finalize the exported data.
     */
    abstract void finalizeExport() throws ImporterExporterException;

    /**
     * Starts exporting a data class.
     * 
     * @param dataClass
     *            The class to begin exporting.
     * @param analysis
     *            The analysis of the data class.
     */
    abstract void startExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException;

    /**
     * Finishes exporting a data class.
     * 
     * @param dataClass
     *            The class to finish exporting.
     * @param analysis
     *            The analysis of the data class.
     */
    abstract void finishExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException;

    /**
     * Exports the abstract representation of a data object:
     * 
     * @param objectFields
     *            The data fields of the object, as a mapping of FieldInfo -&gt;
     *            StructuredObjectData.
     * @param objectRelations
     *            The list of related objects that are "owned" by this object
     *            (meaning they are in a @ManyToMany relationship with no
     *            mappedBy attribute)
     */
    abstract void exportObjectValues(Class<?> dataClass,
                                     StructuredObjectDataMap objectFields,
                                     Map<FieldInfo, List<StructuredObjectDataMap>> objectRelations)
            throws ImporterExporterException;

}
