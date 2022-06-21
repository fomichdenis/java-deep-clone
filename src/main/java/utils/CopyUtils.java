package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class CopyUtils {

    public static <T> T deepCopy(T object) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new Copier().deepCopy(object);
    }

    private static class Copier {

        private final Map<Object, Object> oldToNewObjects = new HashMap<>();

        private <T> T deepCopy(T object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            if (object == null) {
                return null;
            }
            if (oldToNewObjects.get(object) != null) {
                return (T) oldToNewObjects.get(object);
            }
            if (object instanceof Enum || isReturnTheSameObject(object.getClass())) {
                return object;
            }

            Object clone = getObjectFromCacheOrCreateNew(object);

            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                if(field.get(object) == null /*|| Modifier.isFinal(field.getModifiers())*/) {
                    continue;
                }

                if(field.getType().isPrimitive()
                        || isReturnTheSameObject(field.getType())) {
                    field.set(clone, field.get(object));
                }
                else {
                    Object childObj = field.get(object);
                    if (childObj instanceof Collection) {
                        field.set(clone, getCopiedCollection((Collection<Object>) childObj));
                    }
                    else if (childObj instanceof Map) {
                        field.set(clone, getCopiedMap((Map<Object, Object>) childObj));
                    }
                    else {
                        field.set(clone, deepCopy(childObj));
                    }
                }
            }
            return (T) clone;
        }

        private Collection<Object> getCopiedCollection(Collection collectionChildObj) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Collection<Object> newCollection = (Collection<Object>) getObjectFromCacheOrCreateNew(collectionChildObj);

            for (Object o : collectionChildObj) {
                newCollection.add(deepCopy(o));
            }
            return newCollection;

        }

        private Map<Object, Object> getCopiedMap(Map<Object, Object> mapChildObj) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Map<Object, Object> newMap = (Map<Object, Object>) getObjectFromCacheOrCreateNew(mapChildObj);

            for (Map.Entry<Object, Object> o : mapChildObj.entrySet()) {
                newMap.put(deepCopy(o.getKey()), deepCopy(o.getValue()));
            }
            return newMap;

        }

        private Object getObjectFromCacheOrCreateNew(Object object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            if (oldToNewObjects.get(object) != null) {
                return oldToNewObjects.get(object);
            }
            /*List<Class<?>> parameterTypes = new ArrayList<>();
            List<Object> parameterValues = new ArrayList<>();
            for (Field field : object.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    parameterTypes.add(field.getType());
                    parameterValues.add(field.get(object));
                }
            }
            Object clone = object.getClass().getDeclaredConstructor(parameterTypes.toArray(Class<?>[]::new)).newInstance(parameterValues.toArray(Object[]::new));*/
            Object clone = object.getClass().getDeclaredConstructor().newInstance();
            oldToNewObjects.put(object, clone);
            return clone;
        }

        private boolean isReturnTheSameObject(Class clazz) {
            return clazz.equals(String.class)
                    || (clazz.getSuperclass() != null && clazz.getSuperclass().equals(Number.class))
                    || clazz.equals(Boolean.class);
        }

    }

}
