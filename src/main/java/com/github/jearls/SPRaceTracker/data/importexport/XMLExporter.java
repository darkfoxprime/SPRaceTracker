package com.github.jearls.SPRaceTracker.data.importexport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.StructuredObjectDataMap;

/**
 * @author jearls
 *
 */
public class XMLExporter extends DataObjectExporter {
    public static final long serialVersionUID = 1L;

    final Writer             out;
    final XMLStreamWriter    xmlOut;

    final String             docName;

    /**
     * Create the XMLExporter going to the given output stream.
     */
    public XMLExporter(OutputStream out, String docName)
            throws ImporterExporterException {
        try {
            this.out = new OutputStreamWriter(out);
            this.xmlOut =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            this.out);
            this.docName = docName;
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Create the XMLExporter going to the given file.
     */
    public XMLExporter(File out, String docName)
            throws ImporterExporterException {
        try {
            this.out = new FileWriter(out);
            this.xmlOut =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            this.out);
            this.docName = docName;
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        } catch (IOException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Create the XMLExporter going to the given writer.
     */
    public XMLExporter(Writer out, String docName)
            throws ImporterExporterException {
        try {
            this.out = out;
            this.xmlOut =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            this.out);
            this.docName = docName;
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Initializes the export. This creates the XMLStreamWriter instance and
     * starts the XML document.
     * 
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#initializeExport()
     */
    @Override
    void initializeExport() throws ImporterExporterException {
        try {
            xmlOut.writeStartDocument("1.0");
            xmlOut.writeStartElement(this.docName);
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#finalizeExport()
     */
    @Override
    void finalizeExport() throws ImporterExporterException {
        try {
            xmlOut.writeEndElement();
            xmlOut.flush();
            xmlOut.close();
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * @param dataClass
     * @param analysis
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#startExporting(java.lang.Class,
     *      com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter.ClassAnalysis)
     */
    @Override
    public void startExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            xmlOut.writeStartElement(dataClass.getSimpleName() + "List");
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * @param dataClass
     * @param analysis
     * @see com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter#finishExporting(java.lang.Class,
     *      com.github.jearls.SPRaceTracker.data.importexport.DataObjectExporter.ClassAnalysis)
     */
    @Override
    public void finishExporting(Class<?> dataClass, ClassAnalysis analysis)
            throws ImporterExporterException {
        try {
            xmlOut.writeEndElement();
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }

    /**
     * Internal function to export an object map.
     * 
     * @param objectFields
     */
    void exportStructuredObjectMap(StructuredObjectDataMap objectFields) {
        for (Entry<FieldInfo, StructuredObjectData> objectField : objectFields
                .entrySet()) {
            try {
                StructuredObjectData objectData = objectField.getValue();
                xmlOut.writeStartElement(objectField.getKey().field.getName());
                if (objectData instanceof StructuredObjectData.ReferencedObjectData) {
                    exportStructuredObjectMap(((StructuredObjectData.ReferencedObjectData) objectData).referencedData);
                } else if (objectData instanceof StructuredObjectData.DirectObjectData) {
                    xmlOut.writeCharacters(((StructuredObjectData.DirectObjectData) objectData).fieldData
                            .toString());
                }
                xmlOut.writeEndElement();
            } catch (XMLStreamException e) {
            }
        }
    }

    /**
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
            xmlOut.writeStartElement(dataClass.getSimpleName());
            exportStructuredObjectMap(objectFields);
            for (Entry<FieldInfo, List<StructuredObjectDataMap>> relatedField : objectRelations
                    .entrySet()) {
                final FieldInfo fieldInfo = relatedField.getKey();
                final List<StructuredObjectDataMap> relatedObjects =
                        relatedField.getValue();
                xmlOut.writeStartElement(fieldInfo.field.getName());
                for (StructuredObjectDataMap relatedObjectFields : relatedObjects) {
                    xmlOut.writeStartElement(fieldInfo.relatedObject.objectClass
                            .getSimpleName());
                    exportStructuredObjectMap(relatedObjectFields);
                    xmlOut.writeEndElement();
                }
                xmlOut.writeEndElement();
            }
            xmlOut.writeEndElement();
        } catch (XMLStreamException e) {
            throw new ImporterExporterException(e);
        }
    }
}
