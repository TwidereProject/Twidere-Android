package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.MatrixCursor;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;
import org.mariotaku.twidere.util.JsonSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/19.
 */
public class LoganSquareCursorFieldConverterTest {
    private final LoganSquareCursorFieldConverter converter = new LoganSquareCursorFieldConverter();

    private final Model jsonObject = new Model("a");
    private final Model[] jsonArray = {new Model("a"), new Model("b"), new Model("c")};
    private final List<Model> jsonList = Arrays.asList(jsonArray);
    private final Map<String, Model> jsonMap = Collections.singletonMap("key", new Model("value"));

    @Test
    public void testParseField() throws Exception {
        final String[] columns = {"json_object", "json_array", "json_list", "json_map"};
        MatrixCursor cursor = new MatrixCursor(columns);
        cursor.addRow(new String[]{
                JsonSerializer.serialize(jsonObject, Model.class),
                JsonSerializer.serialize(jsonArray, Model.class),
                JsonSerializer.serialize(jsonList, Model.class),
                JsonSerializer.serialize(jsonMap, Model.class)
        });
        cursor.moveToFirst();
        assertEquals(jsonObject, converter.parseField(cursor, 0, TypeUtils.parameterize(Model.class)));
        assertArrayEquals(jsonArray, (Model[]) converter.parseField(cursor, 1, TypeUtils.parameterize(Model[].class)));
        assertEquals((Object) jsonList, converter.parseField(cursor, 2, TypeUtils.parameterize(List.class, Model.class)));
        assertEquals((Object) jsonMap, converter.parseField(cursor, 3, TypeUtils.parameterize(Map.class, String.class, Model.class)));
    }

    @Test
    public void testWriteField() throws Exception {
        final ContentValues contentValues = new ContentValues();
        converter.writeField(contentValues, jsonObject, "json_object", TypeUtils.parameterize(Model.class));
        converter.writeField(contentValues, jsonArray, "json_array", TypeUtils.parameterize(Model[].class));
        converter.writeField(contentValues, jsonList, "json_list", TypeUtils.parameterize(List.class, Model.class));
        converter.writeField(contentValues, jsonMap, "json_map", TypeUtils.parameterize(Map.class, String.class, Model.class));

        assertEquals(JsonSerializer.serialize(jsonObject, Model.class), contentValues.getAsString("json_object"));
        assertEquals(JsonSerializer.serialize(jsonArray, Model.class), contentValues.getAsString("json_array"));
        assertEquals(JsonSerializer.serialize(jsonList, Model.class), contentValues.getAsString("json_list"));
        assertEquals(JsonSerializer.serialize(jsonMap, Model.class), contentValues.getAsString("json_map"));
    }

    @JsonObject
    public static class Model {
        @JsonField(name = "field")
        String field;

        public Model() {

        }

        public Model(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Model model = (Model) o;

            return !(field != null ? !field.equals(model.field) : model.field != null);

        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }
}