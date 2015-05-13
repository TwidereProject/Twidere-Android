package org.mariotaku.simplerestapi;

import android.support.annotation.NonNull;
import android.util.Pair;

import org.apache.commons.lang3.NotImplementedException;
import org.mariotaku.simplerestapi.http.ValueMap;
import org.mariotaku.simplerestapi.http.mime.BaseTypedData;
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

    private ArrayList<Pair<String, String>> queriesCache, formsCache, headersCache;
    private ArrayList<Pair<String, TypedData>> partsCache;
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

    private static String[] getValueMapKeys(String[] annotationValue, ValueMap valueMap) {
        return annotationValue != null && annotationValue.length > 0 ? annotationValue : valueMap.keys();
    }

    private static void checkMethod(RestMethod restMethod, Body body, HashMap<Form, Object> forms, HashMap<Part, Object> parts, FileValue file) {
        if (restMethod == null)
            throw new NotImplementedException("Method must has annotation annotated with @RestMethod");
        if (restMethod.hasBody() && body == null) {
            throw new IllegalArgumentException("@Body required for method " + restMethod.value());
        } else if (!restMethod.hasBody() && body != null) {
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

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.annotationType())) {
                return (T) annotation;
            }
        }
        return null;
    }

    @NonNull
    public Map<String, Object> getExtras() {
        if (extrasCache != null) return extrasCache;
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<Extra, Object> entry : extras.entrySet()) {
            final Extra extra = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : getValueMapKeys(extra.value(), valueMap)) {
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

    @NonNull
    public List<Pair<String, String>> getForms() {
        if (formsCache != null) return formsCache;
        final ArrayList<Pair<String, String>> list = new ArrayList<>();
        for (Map.Entry<Form, Object> entry : forms.entrySet()) {
            final Form form = entry.getKey();
            final Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : getValueMapKeys(form.value(), valueMap)) {
                    if (valueMap.has(key)) {
                        list.add(Pair.create(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else {
                final char delimiter = form.arrayDelimiter();
                String valueString = Utils.toString(value, delimiter);
                for (String key : form.value()) {
                    list.add(Pair.create(key, valueString));
                }
            }
        }
        return formsCache = list;
    }

    @NonNull
    public List<Pair<String, TypedData>> getParts() {
        if (partsCache != null) return partsCache;
        final ArrayList<Pair<String, TypedData>> list = new ArrayList<>();
        for (Map.Entry<Part, Object> entry : parts.entrySet()) {
            final Part part = entry.getKey();
            final String[] names = part.value();
            final Object value = entry.getValue();
            if (value instanceof TypedData) {
                list.add(Pair.create(names[0], (TypedData) value));
            } else if (value != null) {
                list.add(Pair.create(names[0], BaseTypedData.wrap(value)));
            }
        }
        return partsCache = list;
    }

    @NonNull
    public List<Pair<String, String>> getHeaders() {
        if (headersCache != null) return headersCache;
        final ArrayList<Pair<String, String>> list = new ArrayList<>();
        for (Map.Entry<Header, Object> entry : headers.entrySet()) {
            final Header header = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : getValueMapKeys(header.value(), valueMap)) {
                    if (valueMap.has(key)) {
                        list.add(Pair.create(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else if (value != null) {
                for (String key : header.value()) {
                    list.add(Pair.create(key, String.valueOf(value)));
                }
            }
        }
        return headersCache = list;
    }

    public RestMethod getMethod() {
        return method;
    }

    @NonNull
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

    @NonNull
    public List<Pair<String, String>> getQueries() {
        if (queriesCache != null) return queriesCache;
        final ArrayList<Pair<String, String>> list = new ArrayList<>();
        for (Map.Entry<Query, Object> entry : queries.entrySet()) {
            final Query form = entry.getKey();
            final Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof ValueMap) {
                final ValueMap valueMap = (ValueMap) value;
                for (String key : getValueMapKeys(form.value(), valueMap)) {
                    if (valueMap.has(key)) {
                        list.add(Pair.create(key, String.valueOf(valueMap.get(key))));
                    }
                }
            } else {
                final char delimiter = form.arrayDelimiter();
                String valueString = Utils.toString(value, delimiter);
                for (String key : form.value()) {
                    list.add(Pair.create(key, valueString));
                }
            }
        }
        return queriesCache = list;
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

    public RequestInfo toRequestInfo() {
        return new RequestInfo(getMethod().value(), getPath(), getQueries(), getForms(),
                getHeaders(), getParts(), getFile(), getBody(), getExtras());
    }

    public RequestInfo toRequestInfo(RequestInfo.Factory factory) {
        return factory.create(this);
    }

    public Body getBody() {
        return body;
    }

    public FileValue getFile() {
        return file;
    }
}