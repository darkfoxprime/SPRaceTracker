package com.github.jearls.SPRaceTracker.data.importexport;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * This is just a collection of convenience functions for use with strings and
 * conversions.
 * 
 * @author jearls
 */
public abstract class StringUtilities {
    public static final long serialVersionUID = 1L;

    /**
     * Convert a string to a target object type. This properly handles
     * primitives, String, Color, and Font. Other types would need to be
     * registered via {@link PropertyEditorManager.registerEditor}.
     * 
     * @param targetType
     * @param text
     * @return
     */
    public static Object convertStringToType(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    /**
     * This converts a string to TitleCase (first character uppercase, the rest
     * left alone).
     */
    public static String toTitleCase(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * This converts a CamelCase string to multiple words, adding a space in
     * each lowercase->uppercase transition.
     */
    public static String toMultiWord(String text) {
        return text.replaceAll("(\\p{javaLowerCase})(\\p{javaUpperCase})",
                "$1 $2");
    }

}
