package utils;

import exception.ObjectCannotBeClonedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CopyUtils {

    /**
     * Deeply copy an object creating new instances of all nested references recursively.
     * All referenced objects MUST have a constructor without args to be created.
     * This method doesn't copy object with Number superclass (except AtomicInteger and AtomicLong)
     * but set a value from original object.
     * The method works via reflections so the warning will be thrown if any of referenced object is instance
     * of a class that shouldn't be accessed via reflection.
     * The method uses recursion so its depth is indirectly limited.
     * @param object object to be copied
     * @return copy of object with copies of all nested referenced objects
     * @throws ObjectCannotBeClonedException if any object can't be copied
     * @throws IllegalAccessException if any Field of any object is enforcing Java language access
     *                                   control and the underlying constructor is inaccessible
    */
    public static <T> T deepClone(T object) throws ObjectCannotBeClonedException, IllegalAccessException {
        return new Copier(false).deepCopy(object);
    }

    /**
     * Deeply copy an object creating new instances of all nested references recursively.
     * All referenced objects MUST have a constructor without args to be created.
     * This method doesn't copy object with Number superclass (except AtomicInteger and AtomicLong)
     * but set a value from original object.
     * The method works via reflections so the warning will be thrown if any of referenced object is instance
     * of a class that shouldn't be accessed via reflection.
     * The method uses recursion so its depth is indirectly limited.
     * @param object object to be copied
     * @param isReplaceNonCopiedWithNull defines action on error during object copy creation:
     *                                   if true - set it as null if false - throw an error
     * @return copy of object with copies of all nested referenced objects
     * @throws ObjectCannotBeClonedException if any object can't be copied
     * @throws IllegalAccessException if any Field of any object is enforcing Java language access
     *                                   control and the underlying constructor is inaccessible
     */
    public static <T> T deepClone(T object, boolean isReplaceNonCopiedWithNull) throws ObjectCannotBeClonedException, IllegalAccessException {
        return new Copier(isReplaceNonCopiedWithNull).deepCopy(object);
    }

    private static class Copier {

        private final Map<Object, Object> oldToNewObjects = new HashMap<>();
        private final boolean isReplaceNonCopiedWithNull;

        private Copier(boolean isReplaceNonCopiedWithNull) {
            this.isReplaceNonCopiedWithNull = isReplaceNonCopiedWithNull;
        }


        private <T> T deepCopy(T object) throws ObjectCannotBeClonedException, IllegalAccessException {
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
            if (isReplaceNonCopiedWithNull && clone == null) {
                return null;
            }

            for (Field field : object.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);

                if(field.get(object) == null) {
                    field.set(clone, null);
                    continue;
                }

                if(field.getType().isPrimitive() || isReturnTheSameObject(field.getType())) {
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

        private Collection<Object> getCopiedCollection(Collection collectionChildObj) throws ObjectCannotBeClonedException, IllegalAccessException {
            Collection<Object> newCollection = (Collection<Object>) getObjectFromCacheOrCreateNew(collectionChildObj);

            for (Object o : collectionChildObj) {
                newCollection.add(deepCopy(o));
            }
            return newCollection;

        }

        private Map<Object, Object> getCopiedMap(Map<Object, Object> mapChildObj) throws ObjectCannotBeClonedException, IllegalAccessException {
            Map<Object, Object> newMap = (Map<Object, Object>) getObjectFromCacheOrCreateNew(mapChildObj);

            for (Map.Entry<Object, Object> o : mapChildObj.entrySet()) {
                newMap.put(deepCopy(o.getKey()), deepCopy(o.getValue()));
            }
            return newMap;

        }

        private Object getObjectFromCacheOrCreateNew(Object object) throws ObjectCannotBeClonedException {
            if (oldToNewObjects.get(object) != null) {
                return oldToNewObjects.get(object);
            }
            try {
                Object clone = object.getClass().getDeclaredConstructor().newInstance();
                oldToNewObjects.put(object, clone);
                return clone;
            }
            catch(InstantiationException ex) {
                throw new RuntimeException("Internal method error", ex);
            }
            catch (Exception ex) {
                if (isReplaceNonCopiedWithNull) {
                    return null;
                }
                throw new ObjectCannotBeClonedException(ex);
            }
        }

        private boolean isReturnTheSameObject(Class clazz) {
            return clazz.equals(String.class)
                    || (clazz.getSuperclass() != null && clazz.getSuperclass().equals(Number.class)
                        && !clazz.equals(AtomicInteger.class) && !clazz.equals(AtomicLong.class))
                    || clazz.equals(Boolean.class);
        }

    }

}
