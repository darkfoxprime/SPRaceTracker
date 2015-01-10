package com.github.jearls.SPRaceTracker.data.importexport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.IdentifiedBy;

public class FieldInfo {
    public static final long serialVersionUID = 1L;

    /**
     * This finds a getter for a field of a class based on a set of prefixes.
     * 
     * @param c
     *            The class
     * @param f
     *            The field
     * @param prefixes
     *            The prefixes to try.
     * @return The getter Method, or null if none found.
     */
    static Method getterForField(Class<?> c, Field f, String... prefixes) {
        Method getter = null;
        String fieldName = f.getName();
        for (String pfx : prefixes) {
            try {
                getter =
                        c.getMethod(pfx
                                + StringUtilities.toTitleCase(fieldName));
            } catch (NoSuchMethodException e) {
            }
            if (getter != null)
                break;
        }
        return getter;
    }

    /**
     * This finds a setter for a field of a class based on a set of prefixes.
     * 
     * @param c
     *            The class
     * @param f
     *            The field
     * @param prefixes
     *            The prefixes to try.
     * @return The setter Method, or null if none found.
     */
    static Method setterForField(Class<?> c, Field f, Class<?> paramType,
                                 String... prefixes) {
        Method setter = null;
        String fieldName = f.getName();
        for (String pfx : prefixes) {
            try {
                setter =
                        c.getMethod(
                                pfx + StringUtilities.toTitleCase(fieldName),
                                paramType);
            } catch (NoSuchMethodException e) {
            }
            if (setter != null)
                break;
        }
        return setter;
    }

    /**
     * This class holds the identification information about a related object.
     * 
     * @author jearls
     */
    public static class ObjectIdentityInfo {
        Class<?>              objectClass;
        FieldInfo             referencedFrom;
        List<Field>           identityFieldOrder;
        Map<Field, FieldInfo> identityFieldMap;

        public ObjectIdentityInfo(ObjectIdentityInfo other) {
            this.objectClass = other.objectClass;
            this.referencedFrom = other.referencedFrom;
            this.identityFieldOrder =
                    new LinkedList<Field>(other.identityFieldOrder);
            this.identityFieldMap = new HashMap<Field, FieldInfo>();
            for (Entry<Field, FieldInfo> entry : other.identityFieldMap
                    .entrySet()) {
                this.identityFieldMap.put(entry.getKey(),
                        new FieldInfo(entry.getValue()));
            }
        }

        /**
         * Creates a ObjectIdentityInfo object for a given class, with no
         * referencing field.
         * 
         * @param objectClass
         *            The related object class.
         */
        public ObjectIdentityInfo(Class<?> objectClass) {
            this(objectClass, null);
        }

        /**
         * Creates a ObjectIdentityInfo
         * 
         * @param objectClass
         * @param referencedFrom
         */
        public ObjectIdentityInfo(Class<?> objectClass, FieldInfo referencedFrom) {
            this.objectClass = objectClass;
            this.referencedFrom = referencedFrom;
            IdentifiedBy identity =
                    objectClass.getAnnotation(IdentifiedBy.class);
            identityFieldOrder = new LinkedList<Field>();
            identityFieldMap = new HashMap<Field, FieldInfo>();
            for (String identityFieldName : identity.value()) {
                try {
                    Field identityField =
                            objectClass.getField(identityFieldName);
                    identityFieldOrder.add(identityField);
                    identityFieldMap.put(identityField, new FieldInfo(
                            identityField, referencedFrom));
                } catch (ReflectiveOperationException e) {
                    // FIXME: Need to handle this better, rather than just
                    // ignoring the columns that
                    // fail. Need to analyze why they would fail.
                }
            }
        }

        @Override
        public String toString() {
            return "ObjectIdentityInfo("
                    + objectClass.getSimpleName()
                    + ","
                    + ((referencedFrom == null) ? "null" : referencedFrom.field
                            .getDeclaringClass().getSimpleName()
                            + "."
                            + referencedFrom.field.getName())
                    + "){identityFieldOrder=" + identityFieldOrder
                    + ";identityFieldMap=" + identityFieldMap + "}";
        }
    }

    /**
     * The type of field as it pertains to class analysis.
     * 
     * @author jearls
     */
    public enum FieldType {
        /**
         * An ignorable field: one marked as <b>static</b> or <b>final</b>, or
         * one annotated with <b>@Id</b> or <b>@Transient</b>.
         */
        IGNORABLE,
        /**
         * An "owned" relation field: one annotated with <b>@OneToMany</b> or
         * <b>@ManyToMany</b> with a "mappedBy" parameter.
         */
        OWNED_RELATION,
        /**
         * An "owning" relation field: one annotated with <b>@ManyToMany</b>
         * with<u>out</u> a "mappedBy" parameter. (The "owning" side of a @OneToMany
         * relation is not annotated and appears as a regular FIELD).
         */
        OWNING_RELATION,
        /**
         * Any other type of field.
         */
        FIELD;

        /**
         * Determines the type of field. A field is IGNORABLE if it is final,
         * static, or annotated by @Id or @Transient. A field is a
         * OWNING_RELATION if it is annotated by @OneToMany or @ManyToMany.
         * Otherwise, a field is a FIELD.
         * 
         * @param f
         *            The field to check.
         * @return the FieldType of the field.
         */
        public static FieldType determineFieldType(Field f) {
            if ((f.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                return FieldType.IGNORABLE;
            }
            for (Annotation a : f.getAnnotations()) {
                if (a.annotationType() == Id.class) {
                    return FieldType.IGNORABLE;
                } else if (a.annotationType() == Transient.class) {
                    return FieldType.IGNORABLE;
                } else if (a.annotationType() == OneToMany.class) {
                    return FieldType.OWNED_RELATION;
                } else if (a.annotationType() == ManyToMany.class) {
                    try {
                        String mappedBy =
                                (String) a.annotationType()
                                        .getMethod("mappedBy").invoke(a);
                        if (mappedBy.length() > 0) {
                            return FieldType.OWNED_RELATION;
                        } else {
                            return FieldType.OWNING_RELATION;
                        }
                    } catch (ReflectiveOperationException e) {
                        return FieldType.OWNING_RELATION;
                    }
                }
            }
            return FieldType.FIELD;
        }
    };

    /**
     * FieldInfo holds the information about a field: the CSV header names, the
     * getter/setter methods, and any referenced fields from another object.
     * 
     * @author jearls
     */
    public Field              field;
    public FieldType          fieldType;
    public FieldInfo          referencedFrom;
    public Method             getter;
    public Method             setter;
    public Method             adder;         // for ToMany fields
    public ObjectIdentityInfo relatedObject;

    public FieldInfo(FieldInfo other) {
        this.field = other.field;
        this.fieldType = other.fieldType;
        this.referencedFrom = other.referencedFrom;
        this.getter = other.getter;
        this.setter = other.setter;
        this.adder = other.adder;
        if (other.relatedObject == null) {
            this.relatedObject = null;
        } else {
            this.relatedObject = new ObjectIdentityInfo(other.relatedObject);
        }
    }

    /**
     * Create the FieldInfo instance about a specific field.
     * 
     * @param f
     *            The field to analyze.
     */
    public FieldInfo(Field f) {
        this(f, null);
    }

    public FieldInfo(Field f, FieldInfo referencedFrom) {
        field = f;
        fieldType = FieldType.determineFieldType(f);
        this.referencedFrom = referencedFrom;
        Class<?> realDataType = f.getType();
        Class<?> dataType = realDataType;
        if (fieldType == FieldType.OWNING_RELATION
                || fieldType == FieldType.OWNED_RELATION) {
            // A OneToMany or ManyToMany relation will be a collection
            // class.
            // Verify that, just to make sure:
            if (Collection.class.isAssignableFrom(dataType)) {
                // Grab the parameterized type of collection as the field's
                // data type.
                dataType =
                        (Class<?>) (((ParameterizedType) f.getGenericType())
                                .getActualTypeArguments()[0]);
            }
        }
        getter = getterForField(f.getDeclaringClass(), f, "get", "is");
        setter = setterForField(f.getDeclaringClass(), f, realDataType, "set");
        adder = setterForField(f.getDeclaringClass(), f, dataType, "add");
        if (dataType.getAnnotation(IdentifiedBy.class) == null) {
            relatedObject = null;
        } else {
            relatedObject = new ObjectIdentityInfo(dataType, this);
        }
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldInfo) {
            return this.hashCode() == obj.hashCode();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "FieldInfo"
                + "("
                + field.getDeclaringClass().getSimpleName()
                + "."
                + field.getName()
                + ","
                + ((referencedFrom == null) ? "null" : referencedFrom.field
                        .getDeclaringClass().getSimpleName()
                        + "."
                        + referencedFrom.field.getName())

                + ")"
        /*
         * + "{" + "type=" + fieldType + ";" + "getter=" +
         * getter.getDeclaringClass().getSimpleName() + "." + getter.getName() +
         * ";setter=" + setter.getDeclaringClass().getSimpleName() + "." +
         * setter.getName() + ";relatedObject=" + ((relatedObject == null) ?
         * "null" : relatedObject) + "}"
         */
        ;
    }

}
