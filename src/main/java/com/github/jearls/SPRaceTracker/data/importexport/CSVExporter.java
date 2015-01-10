package com.github.jearls.SPRaceTracker.data.importexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import com.github.jearls.SPRaceTracker.data.importexport.FieldInfo.ObjectIdentityInfo;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.DirectObjectData;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.ReferencedObjectData;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.StructuredObjectDataMap;

/**
 * This exports the data store into a zip file containing multiple CSV files,
 * one per data class.
 * 
 * @author jearls
 */
public class CSVExporter extends DataObjectExporter {
    public static final long serialVersionUID = 1L;

    final ZipOutputStream    zipOut;

    final CSVPrinter         csvOut;

    List<FieldInfo>          fieldOrder       = null; // created by
                                                      // startExporting()

    /**
     * Create the CSVExporter going to the given output stream.
     */
    public CSVExporter(OutputStream out) throws IOException {
        zipOut = new ZipOutputStream(out);
        csvOut =
                new CSVPrinter(new OutputStreamWriter(zipOut),
                        CSVFormat.EXCEL.withQuoteMode(QuoteMode.NON_NUMERIC));
    }

    /**
     * Create the XMLExporter going to the given file.
     */
    public CSVExporter(File out) throws IOException {
        this(new FileOutputStream(out));
    }

    /**
     * Initializes the export. Currently does nothing.
     * 
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#initializeExport()
     */
    @Override
    void initializeExport() throws ImporterExporterException {
    }

    static class SavedRelations {
        StructuredObjectDataMap       objectData;
        FieldInfo                     fieldInfo;
        List<StructuredObjectDataMap> relatedObjects;

        public SavedRelations() {
        }

        public SavedRelations(StructuredObjectDataMap objectData,
                FieldInfo fieldInfo,
                List<StructuredObjectDataMap> relatedObjects) {
            this.objectData = objectData;
            this.fieldInfo = fieldInfo;
            this.relatedObjects = relatedObjects;
        }
    }

    List<SavedRelations> savedObjectRelations =
                                                      new LinkedList<SavedRelations>();

    /**
     * @param relations
     */
    void exportSavedRelations(SavedRelations relations) throws IOException {
        zipOut.putNextEntry(new ZipEntry(relations.fieldInfo.field
                .getDeclaringClass().getSimpleName()
                + "."
                + relations.fieldInfo.field.getName() + ".csv"));
        ObjectIdentityInfo ownerIdentity =
                new ObjectIdentityInfo(
                        relations.fieldInfo.field.getDeclaringClass());
        List<FieldInfo> fieldOrder = new LinkedList<FieldInfo>();
        fieldOrder.addAll(ownerIdentity.identityFieldMap.values());
        fieldOrder.addAll(relations.fieldInfo.relatedObject.identityFieldMap
                .values());
        outputFieldHeaders(fieldOrder);
        for (StructuredObjectDataMap relatedObject : relations.relatedObjects) {
            // make a copy of the related data so we can add our own fields in
            relatedObject = new StructuredObjectDataMap(relatedObject);
            relatedObject.putAll(relations.objectData);
            outputStructuredObjectMap(fieldOrder, relatedObject);
        }
    }

    /**
     * Finalizes the export. Before closing out the zip file, this needs to
     * write all the relation tables that were being saved during the data
     * object exports.
     * 
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#finalizeExport()
     */
    @Override
    void finalizeExport() throws ImporterExporterException {
        try {
            for (SavedRelations relations : savedObjectRelations) {
                exportSavedRelations(relations);
            }
            zipOut.flush();
            zipOut.close();
        } catch (IOException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Internal function to generate a field order list from a structured field
     * map.
     */
    List<FieldInfo> flattenFieldList(Map<Field, FieldInfo> classFields) {
        List<FieldInfo> fieldList = new ArrayList<FieldInfo>();
        for (final FieldInfo fieldInfo : classFields.values()) {
            if (fieldInfo.relatedObject != null) {
                fieldList
                        .addAll(flattenFieldList(fieldInfo.relatedObject.identityFieldMap));
            } else {
                fieldList.add(fieldInfo);
            }
        }
        return fieldList;
    }

    /**
     * Starts exporting a given class. This creates the zipfile entry for the
     * class.
     * 
     * @param dataClass
     * @param analysis
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#startExporting(java.lang.Class,
     *      com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter.ClassAnalysis)
     */
    @Override
    public void startExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            zipOut.putNextEntry(new ZipEntry(dataClass.getSimpleName() + ".csv"));
            // generate the field order for the export
            fieldOrder = flattenFieldList(analysis.fieldMap);
            outputFieldHeaders(fieldOrder);
        } catch (IOException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Finishes exporting a given class. This closes out the zipfile entry.
     * 
     * @param dataClass
     * @param analysis
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#finishExporting(java.lang.Class,
     *      com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter.ClassAnalysis)
     */
    @Override
    public void finishExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            zipOut.closeEntry();
        } catch (IOException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Internal method to print a header line for a given list of fields.
     */
    void outputFieldHeaders(List<FieldInfo> fieldOrder) throws IOException {
        List<String> headers = new LinkedList<String>();
        for (FieldInfo fieldInfo : fieldOrder) {
            String header = fieldInfo.field.getName();
            for (FieldInfo ref = fieldInfo.referencedFrom; ref != null; ref =
                    ref.referencedFrom) {
                header = ref.field.getName() + ": " + header;
            }
            headers.add(header);
        }
        csvOut.printRecord(headers);
        csvOut.flush();
    }

    /**
     * Internal method to find the [deep] object data from a
     * StructuredObjectData tree given the fieldInfo of the leaf.
     */
    Object findFieldInfoData(FieldInfo fieldInfo,
                             StructuredObjectDataMap structuredData) {
        Deque<FieldInfo> fieldInfoStack = new LinkedList<FieldInfo>();
        while (!structuredData.containsKey(fieldInfo)) {
            fieldInfoStack.push(fieldInfo);
            fieldInfo = fieldInfo.referencedFrom;
        }
        while (!fieldInfoStack.isEmpty()) {
            structuredData =
                    ((ReferencedObjectData) structuredData.get(fieldInfo)).referencedData;
            fieldInfo = fieldInfoStack.pop();
        }
        return ((DirectObjectData) structuredData.get(fieldInfo)).fieldData;
    }

    /**
     * Internal function to export an object map.
     */
    void outputStructuredObjectMap(List<FieldInfo> fieldOrder,
                                   StructuredObjectDataMap objectFields)
            throws IOException {
        List<Object> objectData = new LinkedList<Object>();
        for (FieldInfo fieldInfo : fieldOrder) {
            objectData.add(findFieldInfoData(fieldInfo, objectFields));
        }
        csvOut.printRecord(objectData);
        csvOut.flush();
    }

    /**
     * Exports the object values and relations for a given data class. In this
     * case, it creates one zipfile entry for the object values themselves, and
     * one additional zipfile entry for each relation.
     * 
     * @param objectFields
     * @param objectRelations
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#exportObjectValues(java.util.Map,
     *      java.util.Map)
     */
    @Override
    public void exportObjectValues(Class<?> dataClass,
                                   StructuredObjectDataMap objectFields,
                                   Map<FieldInfo, List<StructuredObjectDataMap>> objectRelations)
            throws ImporterExporterException {
        try {
            outputStructuredObjectMap(fieldOrder, objectFields);
            if (objectRelations.size() > 0) {
                for (Entry<FieldInfo, List<StructuredObjectDataMap>> objectRelation : objectRelations
                        .entrySet()) {
                    savedObjectRelations
                            .add(new SavedRelations(objectFields,
                                    objectRelation.getKey(), objectRelation
                                            .getValue()));
                }
            }
        } catch (IOException e) {
            throw new ImporterExporterException(e);
        }
    }
}
