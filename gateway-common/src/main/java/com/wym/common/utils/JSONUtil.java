package com.wym.common.utils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.text.SimpleDateFormat;

public class JSONUtil {
    public static final String CODE = "code";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String MESSAGE = "message";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory jasonFactory = mapper.getFactory();

    static {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        mapper.addMixIn(Object.class, ExcludeFilter.class);
        mapper.setFilterProvider(new SimpleFilterProvider()
                .addFilter("excludeFilter",SimpleBeanPropertyFilter.serializeAllExcept("class")));
    }


    public static String toJSONString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("object format to json error:" + obj, e);
        }
    }

    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }


    @JsonFilter("excludeFilter")
    public static class ExcludeFilter {

    }
}

