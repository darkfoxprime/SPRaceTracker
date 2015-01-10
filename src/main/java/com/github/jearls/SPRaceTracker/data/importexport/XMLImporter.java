/**
 * 
 */
package com.github.jearls.SPRaceTracker.data.importexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.persistence.Entity;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.jearls.SPRaceTracker.data.DataStore;
import com.github.jearls.SPRaceTracker.data.importexport.FieldInfo.ObjectIdentityInfo;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.StructuredObjectDataMap;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.DirectObjectData;
import com.github.jearls.SPRaceTracker.data.importexport.StructuredObjectData.ReferencedObjectData;

/**
 * @author jearls
 *
 */
public class XMLImporter extends DataObjectImporter {
    public static final long serialVersionUID = 1L;

    public class XMLImporterException extends ImporterExporterException {
        public static final long serialVersionUID = 1L;

        public XMLImporterException() {
            super();
        }

        public XMLImporterException(String message, Throwable cause,
                boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public XMLImporterException(String message, Throwable cause) {
            super(message, cause);
        }

        public XMLImporterException(String message) {
            super(message);
        }

        public XMLImporterException(Throwable cause) {
            super(cause);
        }
    }

    class NodeListIterator implements Iterable<Node>, Iterator<Node> {
        NodeList nodeList;
        int      idx, len;

        public NodeListIterator(NodeList nodeList) {
            this.nodeList = nodeList;
            idx = 0;
            len = nodeList.getLength();
        }

        public Iterator<Node> iterator() {
            return this;
        }

        boolean nodeIsIgnorable(Node node) {
            switch (node.getNodeType()) {
                case Node.COMMENT_NODE:
                    return true;
                case Node.TEXT_NODE:
                    if (node.getNodeValue().trim().length() == 0) {
                        return true;
                    }
                    break;
            }
            return false;
        }

        public boolean hasNext() {
            while ((idx < len) && (nodeIsIgnorable(nodeList.item(idx)))) {
                idx += 1;
            }
            return (idx < len);
        }

        public Node next() {
            Node nextNode;
            do {
                if (idx == len) {
                    throw new NoSuchElementException();
                }
                nextNode = nodeList.item(idx);
                idx += 1;
            } while (nodeIsIgnorable(nextNode));
            return nextNode;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    Reader          in;
    String          docName;
    XMLStreamReader xmlIn;

    public XMLImporter(DataStore dataStore, Reader in, String docName)
            throws ImporterExporterException {
        super(dataStore);
        this.docName = docName;
        this.in = in;
    }

    public XMLImporter(DataStore dataStore, InputStream in, String docName)
            throws ImporterExporterException {
        super(dataStore);
        this.docName = docName;
        this.in = new InputStreamReader(in);
    }

    public XMLImporter(DataStore dataStore, File in, String docName)
            throws ImporterExporterException {
        super(dataStore);
        this.docName = docName;
        try {
            this.in = new FileReader(in);
        } catch (FileNotFoundException e) {
            throw new ImporterExporterException(e);
        }
    }

    static class SavedObjectRelations {
        Class<?>                      objectClass    = null;
        FieldInfo                     objectField    = null;
        List<StructuredObjectDataMap> relatedObjects = null;

        SavedObjectRelations(Class<?> objectClass, FieldInfo objectField) {
            this(objectClass, objectField,
                    new LinkedList<StructuredObjectDataMap>());
        }

        public SavedObjectRelations(Class<?> objectClass,
                FieldInfo objectField,
                List<StructuredObjectDataMap> relatedObjects) {
            this.objectClass = objectClass;
            this.objectField = objectField;
            this.relatedObjects = relatedObjects;
        }
    }

    List<SavedObjectRelations> savedObjectRelationsList =
                                                                new LinkedList<SavedObjectRelations>();

    static class ObjectDataAndRelations {
        Class<?>                                      objectClass     = null;
        StructuredObjectDataMap                       objectFields    = null;
        Map<FieldInfo, List<StructuredObjectDataMap>> objectRelations = null;

        ObjectDataAndRelations(Class<?> objectClass) {
            this.objectClass = objectClass;
            objectFields = new StructuredObjectDataMap();
            objectRelations =
                    new HashMap<FieldInfo, List<StructuredObjectDataMap>>();
        }
    }

    ObjectDataAndRelations importObjectNodeData(Node objectNode,
                                                Class<?> objectClass,
                                                ClassAnalysis analysis,
                                                FieldInfo referencedFrom)
            throws ImporterExporterException {
        ObjectDataAndRelations objectData =
                new ObjectDataAndRelations(objectClass);
        for (Node fieldNode : new NodeListIterator(objectNode.getChildNodes())) {
            importFieldNode(fieldNode, objectClass, analysis, objectData,
                    referencedFrom);
        }
        return objectData;
    }

    void importFieldNode(Node fieldNode, Class<?> objectClass,
                         ClassAnalysis analysis,
                         ObjectDataAndRelations objectData,
                         FieldInfo referencedFrom)
            throws ImporterExporterException {
        String fieldNodeName = fieldNode.getNodeName();
        try {
            Field field = objectClass.getField(fieldNodeName);
            if (analysis.fieldMap.containsKey(field)) {
                FieldInfo fieldInfo =
                        new FieldInfo(analysis.fieldMap.get(field));
                fieldInfo.referencedFrom = referencedFrom;
                if (fieldInfo.relatedObject == null) {
                    String text = fieldNode.getTextContent();
                    Object fieldVal =
                            convertStringToObject(text, field.getType());
                    // System.err.println(fieldInfo + " -> " +
                    // fieldVal.toString());
                    objectData.objectFields.put(fieldInfo,
                            new DirectObjectData(fieldVal));
                } else {
                    // System.err.println(fieldInfo + " has subfields...");
                    ObjectDataAndRelations subfieldData =
                            new ObjectDataAndRelations(field.getType());
                    for (Node subfieldNode : new NodeListIterator(
                            fieldNode.getChildNodes())) {
                        // special case: there will never be
                        // related-object-lists in a subfield, so we pass in
                        // null for the objectRelations parameter
                        importFieldNode(subfieldNode, field.getType(),
                                ClassAnalysis.analyzeClass(field.getType()),
                                subfieldData, fieldInfo);
                    }
                    objectData.objectFields
                            .put(fieldInfo, new ReferencedObjectData(
                                    subfieldData.objectFields));
                }
            } else if (analysis.relationMap.containsKey(field)) {
                FieldInfo fieldInfo = analysis.relationMap.get(field);
                List<StructuredObjectDataMap> relatedObjects =
                        new LinkedList<StructuredObjectDataMap>();
                // System.err.println(fieldInfo +
                // " is a collection of related objects...");
                for (Node relatedObjectNode : new NodeListIterator(
                        fieldNode.getChildNodes())) {
                    relatedObjects
                            .add(importObjectNode(relatedObjectNode,
                                    fieldInfo.relatedObject.objectClass,
                                    fieldInfo).objectFields);
                }
                objectData.objectRelations.put(fieldInfo, relatedObjects);
            } else {
                throw new XMLImporterException("Unknown "
                        + objectClass.getSimpleName() + " field "
                        + fieldNodeName + " - analysis = " + analysis);
            }
        } catch (NoSuchFieldException e) {
            throw new XMLImporterException("Unknown "
                    + objectClass.getSimpleName() + " field " + fieldNodeName);
        }
    }

    ObjectDataAndRelations importObjectNode(Node objectNode,
                                            Class<?> objectClass,
                                            FieldInfo referencedFrom)
            throws ImporterExporterException {
        ClassAnalysis analysis = ClassAnalysis.analyzeClass(objectClass);
        String objectNodeName = objectNode.getNodeName();
        if (!objectNodeName.equals(objectClass.getSimpleName())) {
            throw new XMLImporterException("Expected "
                    + objectClass.getSimpleName() + " object node; found "
                    + objectNodeName);
        }
        ObjectDataAndRelations objectData =
                importObjectNodeData(objectNode, objectClass, analysis,
                        referencedFrom);
        ;
        // System.err.println("Class " + objectClass.getSimpleName() + " data "
        // + objectFields.objectData + " relations " +
        // objectFields.objectRelations);
        return objectData;
    }

    List<ObjectDataAndRelations> importObjectListNode(Node objectListNode)
            throws ImporterExporterException {
        String objectListName = objectListNode.getNodeName();
        if (!objectListName.endsWith("List")) {
            throw new XMLImporterException("Expected class list node; found "
                    + objectListName);
        }
        String objectClassName =
                objectListName.substring(0, objectListName.length() - 4);
        try {
            Class<?> objectClass =
                    Class.forName(dataStore.getClass().getPackage().getName()
                            + "." + objectClassName);
            if (objectClass.getAnnotation(Entity.class) == null) {
                throw new XMLImporterException(
                        "Invalid class for class list node " + objectListName);
            }
            List<ObjectDataAndRelations> objects =
                    new LinkedList<ObjectDataAndRelations>();
            for (Node objectNode : new NodeListIterator(
                    objectListNode.getChildNodes())) {
                ObjectDataAndRelations objectData =
                        importObjectNode(objectNode, objectClass, null);
                objects.add(objectData);
            }
            return objects;
        } catch (ClassNotFoundException e) {
            throw new XMLImporterException("Unknown class for class list node "
                    + objectListName);
        }
    }

    void importDocument(Element docRoot) throws ImporterExporterException {
        if (!docRoot.getNodeName().equals(docName)) {
            throw new XMLImporterException("Expected document root " + docName
                    + "; found " + docRoot.getNodeName());
        }
        List<ObjectDataAndRelations> allObjects =
                new LinkedList<ObjectDataAndRelations>();
        for (Node objectListNode : new NodeListIterator(docRoot.getChildNodes())) {
            allObjects.addAll(importObjectListNode(objectListNode));
        }

        for (ObjectDataAndRelations objectData : allObjects) {
            ObjectIdentityInfo oidInfo =
                    new ObjectIdentityInfo(objectData.objectClass);
            StructuredObjectDataMap objectIdentity =
                    new StructuredObjectDataMap();
            for (FieldInfo idInfo : oidInfo.identityFieldMap.values()) {
                objectIdentity.put(idInfo, objectData.objectFields.get(idInfo));
            }
            importDataObjectValues(objectData.objectClass, objectIdentity,
                    objectData.objectFields);
            for (Entry<FieldInfo, List<StructuredObjectDataMap>> objectRelation : objectData.objectRelations
                    .entrySet()) {
                importDataObjectRelations(objectData.objectClass,
                        objectIdentity, objectRelation.getKey(),
                        objectRelation.getValue());
            }
        }

    }

    public void importData() throws ImporterExporterException {

        try {
            Document xmlDoc =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(new InputSource(in));
            initializeImport();
            importDocument(xmlDoc.getDocumentElement());
            finalizeImport();
        } catch (SAXException e) {
            throw new XMLImporterException(e);
        } catch (IOException e) {
            throw new XMLImporterException(e);
        } catch (ParserConfigurationException e) {
            throw new XMLImporterException(e);
        }

    }
}
