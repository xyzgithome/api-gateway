package com.wym.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {
    public static void properties2Object(final Properties ps, final Object object, String prefix) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            try {
                String mn = method.getName();
                if (!mn.startsWith("set")) {
                    continue;
                }

                String tmp = mn.substring(4);
                String first = mn.substring(3, 4);
                String key = prefix + first.toLowerCase() + tmp;
                String property = ps.getProperty(key);
                if (Objects.isNull(property)) {
                    continue;
                }

                Class<?>[] pt = method.getParameterTypes();
                if (ArrayUtils.isEmpty(pt)) {
                    continue;
                }

                String cn = pt[0].getSimpleName();
                Object arg;
                switch (cn) {
                    case "int":
                    case "Integer":
                        arg = Integer.parseInt(property);
                        break;
                    case "long":
                    case "Long":
                        arg = Long.parseLong(property);
                        break;
                    case "double":
                    case "Double":
                        arg = Double.parseDouble(property);
                        break;
                    case "boolean":
                    case "Boolean":
                        arg = Boolean.parseBoolean(property);
                        break;
                    case "float":
                    case "Float":
                        arg = Float.parseFloat(property);
                        break;
                    case "String":
                        arg = property;
                        break;
                    default:
                        continue;
                }
                method.invoke(object, arg);
            } catch (Throwable t) {
                log.error("parse property error, t:", t);
            }
        }
    }
}
