package com.github.jearls.SPRaceTracker.data.importexport;

import java.util.HashMap;
import java.util.Map;

public class StructuredObjectData {
    public static class StructuredObjectDataMap extends
            HashMap<FieldInfo, StructuredObjectData> {
        public static final long serialVersionUID = 1L;

        public StructuredObjectDataMap() {
            super();
        }

        public StructuredObjectDataMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        public StructuredObjectDataMap(int initialCapacity) {
            super(initialCapacity);
        }

        public StructuredObjectDataMap(
                Map<? extends FieldInfo, ? extends StructuredObjectData> m) {
            super(m);
        }
    }

    public static class DirectObjectData extends StructuredObjectData {
        public Object fieldData;

        public DirectObjectData() {
            this.fieldData = null;
        }

        public DirectObjectData(Object fieldData) {
            this.fieldData = fieldData;
        }

        @Override
        public String toString() {
            return "DirectObjectData(" + fieldData.toString() + ")";
        }
    }

    public static class ReferencedObjectData extends StructuredObjectData {
        public StructuredObjectDataMap referencedData;

        public ReferencedObjectData() {
            this.referencedData = new StructuredObjectDataMap();
        }

        public ReferencedObjectData(StructuredObjectDataMap referencedData) {
            this.referencedData = new StructuredObjectDataMap(referencedData);
        }

        @Override
        public String toString() {
            return "ReferencedObjectData(" + referencedData.toString() + ")";
        }
    }

    public static final long serialVersionUID = 1L;
}
