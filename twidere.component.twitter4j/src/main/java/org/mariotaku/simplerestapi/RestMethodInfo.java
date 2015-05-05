package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.KeyValuePair;
import org.mariotaku.simplerestapi.http.ValueMap;
import org.mariotaku.simplerestapi.http.mime.BaseTypedData;
import org.mariotaku.simplerestapi.http.mime.FormTypedBody;
import org.mariotaku.simplerestapi.http.mime.MultipartTypedBody;
import org.mariotaku.simplerestapi.http.mime.TypedData;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Extra;
import org.mariotaku.simplerestapi.param.File;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Header;
import org.mariotaku.simplerestapi.param.Part;
import org.mariotaku.simplerestapi.param.Path;
import org.mariotaku.simplerestapi.param.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/2/6.
 */
public final class RestMethodInfo {

    private final RestMethod method;
    private final String path;
    private final Body body;

    private final HashMap<Path, Object> paths;
    private final HashMap<Query, Object> queries;
    private final HashMap<Header, Object> headers;
    private final HashMap<Form, Object> forms;
    private final HashMap<Part, Object> parts;
    private final HashMap<Extra, Object> extras;
    private final FileValue file;

    private ArrayList<KeyValuePair> queriesCache, formsCache, headersCache;
    private ArrayList<TypedData> partsCache;
    private Map<String, Object> extrasCache;
    private TypedData bodyCache;

    RestMethodInfo(final RestMethod method, String path, final Body body, final HashMap<Path, Object> paths, final HashMap<Query, Object> queries,
                   final HashMap<Header, Object> headers, final HashMap<Form, Object> forms, final HashMap<Part, Object> parts,
                   final FileValue file, HashMap<Extra, Object> extras) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.paths = paths;
        this.queries = queries;
        this.headers = headers;
        this.forms = forms;
        this.parts = parts;
        this.extras = extras;
        this.file = file;
    }

    public TypedData getBody() {
        if (bodyCache != null) return bodyCache;
        if (body == null) return null;
        switch (body.value()) {
            case FORM: {
                bodyCache = new FormTypedBody(getForms());
                break;
            }
            case MULTIPART: {
                bodyCache = new MultipartTypedBody(getParts());
                break;
            }
            case FILE: {
                bodyCache = file.body();
                break;
            }
        }
        return bodyCache;
    }

    public Map<String, Object> getExtras() {
        if (extrasCache != null) return extrasCache;
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<Extra, Object> entry : extras.entrySet()) {
            final Extra extra = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : extra.value()) {
                    if (valueMap.has(key)) {
                        map.put(key, valueMap.get(key));
                    }
                }
            } else if (value != null) {
                for (String key : extra.value()) {
                    map.put(key, value);
                }
            }
        }
        return extrasCache = map;
    }

    public List<KeyValuePair> getForms() {
        if (formsCache != null) return formsCache;
        final ArrayList<KeyValuePair> list = new ArrayList<>();
        for (Map.Entry<Form, Object> entry : forms.entrySet()) {
            final Form form = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : form.value()) {
                    if (valueMap.has(key)) {
                        list.add(new KeyValuePair(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else if (value != null) {
                for (String key : form.value()) {
                    list.add(new KeyValuePair(key, String.valueOf(value)));
                }
            }
        }
        return formsCache = list;
    }

    public List<TypedData> getParts() {
        if (partsCache != null) return partsCache;
        final ArrayList<TypedData> list = new ArrayList<>();
        for (Map.Entry<Part, Object> entry : parts.entrySet()) {
            final Part form = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof TypedData) {
                list.add((TypedData) value);
            } else if (value != null) {
                list.add(BaseTypedData.wrap(value));
            }
        }
        return partsCache = list;
    }

    public List<KeyValuePair> getHeaders() {
        if (headersCache != null) return headersCache;
        final ArrayList<KeyValuePair> list = new ArrayList<>();
        for (Map.Entry<Header, Object> entry : headers.entrySet()) {
            final Header form = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : form.value()) {
                    if (valueMap.has(key)) {
                        list.add(new KeyValuePair(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else if (value != null) {
                for (String key : form.value()) {
                    list.add(new KeyValuePair(key, String.valueOf(value)));
                }
            }
        }
        return headersCache = list;
    }

    public RestMethod getMethod() {
        return method;
    }

    public String getPath() {
        StringBuilder sb = new StringBuilder();
        int start, end, prevEnd = -1;
        while ((start = path.indexOf('{', prevEnd)) != -1 && (end = path.indexOf('}', start)) != -1) {
            sb.append(path.substring(prevEnd + 1, start));
            final String key = path.substring(start + 1, end);
            final String replacement = findPathReplacement(key);
            if (replacement == null)
                throw new IllegalArgumentException("Path key {" + key + "} not bound");
            sb.append(replacement);
            prevEnd = end;
        }
        sb.append(path.substring(prevEnd + 1));
        return sb.toString();
    }

    public List<KeyValuePair> getQueries() {
        if (queriesCache != null) return queriesCache;
        final ArrayList<KeyValuePair> list = new ArrayList<>();
        for (Map.Entry<Query, Object> entry : queries.entrySet()) {
            final Query form = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : form.value()) {
                    if (valueMap.has(key)) {
                        list.add(new KeyValuePair(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else if (value != null) {
                for (String key : form.value()) {
                    list.add(new KeyValuePair(key, String.valueOf(value)));
                }
            }
        }
        return queriesCache = list;
    }

    static RestMethodInfo get(Method method, Object[] args) {
        RestMethod restMethod = null;
        String pathFormat = null;
        for (Annotation annotation : method.getAnnotations()) {
            final Class<?> annotationType = annotation.annotationType();
            restMethod = annotationType.getAnnotation(RestMethod.class);
            if (restMethod != null) {
                try {
                    pathFormat = (String) annotationType.getMethod("value").invoke(annotation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        final Body body = method.getAnnotation(Body.class);
        final HashMap<Path, Object> paths = new HashMap<>();
        final HashMap<Query, Object> queries = new HashMap<>();
        final HashMap<Header, Object> headers = new HashMap<>();
        final HashMap<Form, Object> forms = new HashMap<>();
        final HashMap<Part, Object> parts = new HashMap<>();
        final HashMap<Extra, Object> extras = new HashMap<>();
        FileValue file = null;
        final Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0, j = annotations.length; i < j; i++) {
            final Path path = getAnnotation(annotations[i], Path.class);
            if (path != null) {
                paths.put(path, args[i]);
            }
            final Query query = getAnnotation(annotations[i], Query.class);
            if (query != null) {
                queries.put(query, args[i]);
            }
            final Header header = getAnnotation(annotations[i], Header.class);
            if (header != null) {
                headers.put(header, args[i]);
            }
            final Form form = getAnnotation(annotations[i], Form.class);
            if (form != null) {
                forms.put(form, args[i]);
            }
            final Part part = getAnnotation(annotations[i], Part.class);
            if (part != null) {
                parts.put(part, args[i]);
            }
            final File paramFile = getAnnotation(annotations[i], File.class);
            if (paramFile != null) {
                if (file == null) {
                    file = new FileValue(paramFile, args[i]);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            final Extra extra = getAnnotation(annotations[i], Extra.class);
            if (extra != null) {
                extras.put(extra, args[i]);
            }
        }
        checkMethod(restMethod, body, forms, parts, file);
        return new RestMethodInfo(restMethod, pathFormat, body, paths, queries, headers, forms, parts, file, extras);
    }

    private static void checkMethod(RestMethod restMethod, Body body, HashMap<Form, Object> forms, HashMap<Part, Object> parts, FileValue file) {
        if (restMethod == null)
            throw new NullPointerException("Method must has annotation annotated with @RestMethod");
        if (!restMethod.hasBody() && body != null) {
            throw new IllegalArgumentException(restMethod.value() + " does not allow body");
        }
        if (body == null) return;
        switch (body.value()) {
            case FILE: {
                if (file == null) {
                    throw new NullPointerException("@File annotation is required");
                }
                if (!forms.isEmpty() || !parts.isEmpty()) {
                    throw new IllegalArgumentException("Only arguments with @File annotation allowed");
                }
                break;
            }
            case MULTIPART: {
                if (!forms.isEmpty() || file != null) {
                    throw new IllegalArgumentException("Only arguments with @Part annotation allowed");
                }
                break;
            }
            case FORM: {
                if (file != null || !parts.isEmpty()) {
                    throw new IllegalArgumentException("Only arguments with @Form annotation allowed");
                }
                break;
            }
        }
    }

    private String findPathReplacement(String key) {
        for (Map.Entry<Path, Object> entry : paths.entrySet()) {
            final Path path = entry.getKey();
            if (key.equals(path.value())) {
                if (path.encoded()) {
                    return String.valueOf(entry.getValue());
                } else {
                    return Utils.encode(String.valueOf(entry.getValue()), "UTF-8");
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.annotationType())) {
                return (T) annotation;
            }
        }
        return null;
    }
}