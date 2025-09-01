package com.AIT.Optimanage.Support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiExportService {

    private final RequestMappingHandlerMapping handlerMapping;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ApiExportService(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    public Map<String, Object> generatePostmanCollection(String baseUrl) {
        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
        record Route(String method, String path, RequestMappingInfo info, HandlerMethod handler) {}

        List<Route> routes = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : map.entrySet()) {
            RequestMappingInfo info = e.getKey();
            Set<String> methods = info.getMethodsCondition().getMethods().stream().map(Enum::name).collect(Collectors.toSet());
            Set<String> paths = info.getPathPatternsCondition() != null
                    ? info.getPathPatternsCondition().getPatterns().stream().map(PathPattern::getPatternString).collect(Collectors.toSet())
                    : info.getPatternsCondition().getPatterns();

            for (String p : paths) {
                if ("/error".equals(p)) continue;
                if (methods.isEmpty()) {
                    routes.add(new Route("GET", p, info, e.getValue()));
                } else {
                    for (String m : methods) {
                        routes.add(new Route(m, p, info, e.getValue()));
                    }
                }
            }
        }

        routes = routes.stream().sorted(Comparator.comparing(Route::path).thenComparing(Route::method)).collect(Collectors.toList());

        Map<String, List<Route>> byFolder = new TreeMap<>();
        for (Route r : routes) {
            String p = r.path();
            String folder = "/".equals(p) ? "root" : p.replaceFirst("^/", "");
            int idx = folder.indexOf('/');
            folder = idx > 0 ? folder.substring(0, idx) : folder;
            byFolder.computeIfAbsent(folder, k -> new ArrayList<>()).add(r);
        }

        Map<String, Object> collection = new LinkedHashMap<>();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Optimanage API");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        List<Map<String, Object>> variables = new ArrayList<>();
        Map<String, Object> baseVar = new LinkedHashMap<>();
        baseVar.put("key", "base_url");
        baseVar.put("value", baseUrl);
        variables.add(baseVar);

        Map<String, Object> tokenVar = new LinkedHashMap<>();
        tokenVar.put("key", "token");
        tokenVar.put("value", "");
        variables.add(tokenVar);
        collection.put("variable", variables);

        List<Map<String, Object>> folders = new ArrayList<>();
        for (Map.Entry<String, List<Route>> entry : byFolder.entrySet()) {
            String folderName = entry.getKey();
            List<Route> folderRoutes = entry.getValue();

            List<Map<String, Object>> requests = new ArrayList<>();
            for (Route r : folderRoutes) {
                Map<String, Object> reqItem = new LinkedHashMap<>();
                reqItem.put("name", r.method() + " " + r.path());

                Map<String, Object> req = new LinkedHashMap<>();
                req.put("method", r.method());

                List<Map<String, Object>> headers = new ArrayList<>();
                Map<String, Object> headerAuth = new LinkedHashMap<>();
                headerAuth.put("key", "Authorization");
                headerAuth.put("value", "Bearer {{token}}");
                headerAuth.put("type", "text");
                headers.add(headerAuth);
                if ("POST".equalsIgnoreCase(r.method()) || "PUT".equalsIgnoreCase(r.method()) || "PATCH".equalsIgnoreCase(r.method())) {
                    Map<String, Object> headerCt = new LinkedHashMap<>();
                    headerCt.put("key", "Content-Type");
                    headerCt.put("value", MediaType.APPLICATION_JSON_VALUE);
                    headerCt.put("type", "text");
                    headers.add(headerCt);
                }
                req.put("header", headers);

                Map<String, Object> url = new LinkedHashMap<>();
                url.put("raw", "{{base_url}}" + r.path());
                url.put("host", List.of("{{base_url}}"));
                url.put("path", List.of(r.path()));
                req.put("url", url);

                if ("POST".equalsIgnoreCase(r.method()) || "PUT".equalsIgnoreCase(r.method()) || "PATCH".equalsIgnoreCase(r.method())) {
                    String sample = buildSampleBodyJson(r.method(), r.path(), r.info(), r.handler());
                    if (sample != null) {
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mode", "raw");
                        body.put("raw", sample);
                        Map<String, Object> options = new LinkedHashMap<>();
                        Map<String, Object> rawOpt = new LinkedHashMap<>();
                        rawOpt.put("language", "json");
                        options.put("raw", rawOpt);
                        body.put("options", options);
                        req.put("body", body);
                    }
                }

                reqItem.put("request", req);
                requests.add(reqItem);
            }

            Map<String, Object> folder = new LinkedHashMap<>();
            folder.put("name", folderName);
            folder.put("item", requests);
            folders.add(folder);
        }
        collection.put("item", folders);
        return collection;
    }

    private String buildSampleBodyJson(String httpMethod, String path, RequestMappingInfo info, HandlerMethod handler) {
        try {
            Class<?> dtoClass = resolveRequestBodyType(handler);
            if (dtoClass == null) return null;
            Map<String, Object> payload = buildSampleForClass(dtoClass, 0, new HashSet<>(), httpMethod, path);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{\n  \"example\": \"value\"\n}";
        }
    }

    private Class<?> resolveRequestBodyType(HandlerMethod handler) {
        var parameters = handler.getMethod().getParameters();
        for (var p : parameters) {
            boolean hasRequestBody = Arrays.stream(p.getAnnotations()).anyMatch(a -> a.annotationType().getName().endsWith("RequestBody"));
            if (hasRequestBody) return p.getType();
        }
        return null;
    }

    private Map<String, Object> buildSampleForClass(Class<?> dtoClass, int depth, Set<Class<?>> seen, String httpMethod, String path) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (dtoClass == null) return m;
        if (seen.contains(dtoClass) || depth > 2) return m;
        seen.add(dtoClass);
        for (var f : dtoClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(JsonIgnore.class)) continue;
            JsonProperty jp = f.getAnnotation(JsonProperty.class);
            if (jp != null && jp.access() == JsonProperty.Access.READ_ONLY) continue;
            f.setAccessible(true);
            String name = f.getName();
            Class<?> type = f.getType();
            if ("POST".equalsIgnoreCase(httpMethod) && name.equalsIgnoreCase("id") && !path.contains("{")) continue;
            Object sample = sampleValueForField(name, type, f.getAnnotations(), depth, seen, httpMethod, path, f);
            m.put(name, sample);
        }
        return m;
    }

    private Object sampleValueForField(String name, Class<?> type, Annotation[] anns, int depth, Set<Class<?>> seen, String httpMethod, String path, java.lang.reflect.Field field) {
        for (var a : anns) {
            String an = a.annotationType().getName();
            if (an.endsWith("Email")) return "user@empresa.com";
        }
        String lower = name.toLowerCase();
        if (lower.equals("nome")) return "Fulano";
        if (lower.equals("sobrenome")) return "Silva";
        if (lower.contains("senha") && lower.contains("nova")) return "SenhaNova123!";
        if (lower.contains("senha")) return "Senha123!";
        if (lower.contains("password")) return "Password123!";
        if (lower.contains("code")) return "123456";
        if (lower.contains("token")) return "<refresh-token>";
        if (lower.contains("cpf")) return "123.456.789-09";
        if (lower.contains("cnpj")) return "12.345.678/0001-90";
        if (lower.contains("cep") || lower.contains("zipcode")) return "01001-000";
        if (lower.contains("telefone") || lower.contains("celular") || lower.contains("phone")) return "(11) 91234-5678";
        if (lower.contains("logradouro")) return "Av. Paulista";
        if (lower.equals("numero") || lower.equals("número")) return "1000";
        if (lower.contains("complemento")) return "Conj. 101";
        if (lower.contains("bairro")) return "Bela Vista";
        if (lower.contains("cidade") || lower.contains("municipio")) return "São Paulo";
        if (lower.equals("estado") || lower.equals("uf")) return "SP";
        if (lower.contains("razao") && lower.contains("social")) return "Empresa Exemplo LTDA";
        if (lower.contains("nomefantasia") || lower.contains("fantasia")) return "Exemplo";
        if (lower.contains("preco") || lower.contains("valor") || lower.contains("price") || lower.contains("amount")) return new BigDecimal("99.90");
        if (lower.contains("quantidade") || lower.equals("qty")) return 1;
        if (lower.contains("ativo") || lower.startsWith("is") || type == boolean.class || type == Boolean.class) return true;
        if (lower.contains("permiteorcamento")) return true;
        if (lower.contains("role")) return "ADMIN";
        if (lower.contains("email")) return "user@empresa.com";

        if (type == boolean.class || type == Boolean.class) return true;
        if (type == int.class || type == Integer.class) return 1;
        if (type == long.class || type == Long.class) return 1L;
        if (type == double.class || type == Double.class) return 1.0d;
        if (type == float.class || type == Float.class) return 1.0f;
        if (type == short.class || type == Short.class) return (short) 1;
        if (type == byte.class || type == Byte.class) return (byte) 1;

        if (Number.class.isAssignableFrom(type)) return 1;
        if (BigDecimal.class.isAssignableFrom(type)) return new BigDecimal("99.90");

        if (type == LocalDate.class) return LocalDate.of(2025, 1, 1).toString();
        if (type == LocalDateTime.class) return LocalDateTime.of(2025, 1, 1, 12, 0).toString();
        if (type == Instant.class) return Instant.parse("2025-01-01T12:00:00Z").toString();
        if (java.time.temporal.Temporal.class.isAssignableFrom(type) || lower.contains("date")) return "2025-01-01";

        if (type == UUID.class) return UUID.fromString("123e4567-e89b-12d3-a456-426614174000").toString();

        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants != null && constants.length > 0) return constants[0].toString();
        }

        if (Collection.class.isAssignableFrom(type)) {
            java.lang.reflect.Type generic = field.getGenericType();
            Object elem = "string";
            if (generic instanceof ParameterizedType pt) {
                var args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] instanceof Class<?> gc) {
                    elem = sampleValueForField(name + "Item", gc, new Annotation[0], depth + 1, seen, httpMethod, path, field);
                }
            }
            List<Object> list = new ArrayList<>();
            list.add(elem);
            return list;
        }

        if (type.isArray()) {
            Class<?> ct = type.getComponentType();
            Object elem = sampleValueForField(name + "Item", ct, new Annotation[0], depth + 1, seen, httpMethod, path, field);
            return List.of(elem);
        }

        if (Map.class.isAssignableFrom(type)) {
            Map<String, Object> mv = new LinkedHashMap<>();
            mv.put("key", "value");
            return mv;
        }

        if (!type.getName().startsWith("java.")) {
            return buildSampleForClass(type, depth + 1, seen, httpMethod, path);
        }
        return "string";
    }
}

