package utils;

import annotation.CopyConstructor;
import annotation.CopyFieldName;
import exception.ObjectCannotBeClonedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CopyUtils {

    /**
     * Deeply copy an object creating new instances of all nested references recursively.
     * <p>All referenced objects MUST have one of the following constructors to be created:
     * <ul>
     *  <li>public constructor without args</li>
     *  <li>a public constructor annotated with {@link CopyConstructor}</li>
     *  <li>only one public constructor</li>
     * </ul></p>
     * <p>All parameters for selected constructor with args MUST satisfy one of the conditions:
     * <ul>
     *  <li>parameter should be annotated with {@link CopyFieldName} with field name as annotation parameter</li>
     *  <li>only one object field should have the same type as constructor parameter has</li>
     * </ul></p>
     * <p>This method doesn't copy object with Number superclass (except AtomicInteger and AtomicLong)
     * but set a value from original object.</p>
     * <p>The method works via reflections so the warning will be thrown if any of referenced object is instance
     * of a class that shouldn't be accessed via reflection.</p>
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
     * <p>All referenced objects MUST have one of the following constructors to be created:
     * <ul>
     *  <li>public constructor without args</li>
     *  <li>a public constructor annotated with {@link CopyConstructor}</li>
     *  <li>only one public constructor</li>
     * </ul></p>
     * <p>All parameters for selected constructor with args MUST satisfy one of the conditions:
     * <ul>
     *  <li>parameter should be annotated with {@link CopyFieldName} with field name as annotation parameter</li>
     *  <li>only one object field should have the same type as constructor parameter has</li>
     * </ul></p>
     * <p>This method doesn't copy object with Number superclass (except AtomicInteger and AtomicLong)
     * but set a value from original object.</p>
     * <p>The method works via reflections so the warning will be thrown if any of referenced object is instance
     * of a class that shouldn't be accessed via reflection.</p>
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

            setFieldsForClone(object, clone);

            return (T) clone;
        }

        private void setFieldsForClone(Object object, Object clone) throws IllegalAccessException {
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
        }

        private Collection<Object> getCopiedCollection(Collection<Object> collectionChildObj) throws ObjectCannotBeClonedException, IllegalAccessException {
            Collection<Object> newCollection = (Collection<Object>) getObjectFromCacheOrCreateNew(collectionChildObj);
            if (newCollection == null) {
                return null;
            }
            for (Object o : collectionChildObj) {
                newCollection.add(deepCopy(o));
            }
            return newCollection;

        }

        private Map<Object, Object> getCopiedMap(Map<Object, Object> mapChildObj) throws ObjectCannotBeClonedException, IllegalAccessException {
            Map<Object, Object> newMap = (Map<Object, Object>) getObjectFromCacheOrCreateNew(mapChildObj);
            if (newMap == null) {
                return null;
            }
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
                Constructor<?> constructor = findConstructor(object);
                Object clone = createObjectWithConstructor(object, constructor);
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

        private Constructor<?> findConstructor(Object object) {
            Constructor<?>[] constructors = object.getClass().getDeclaredConstructors();
            Constructor<?> constructor;
            List<Constructor<?>> constructorList = Arrays.stream(constructors)
                    .filter(c -> Modifier.isPublic(c.getModifiers()))
                    .collect(Collectors.toList());
            if (constructorList.size() == 1) {
                constructor = constructorList.get(0);
            }
            else {
                List<Constructor<?>> copyConstructor = constructorList.stream()
                        .filter(c -> c.getAnnotation(CopyConstructor.class) != null)
                        .collect(Collectors.toList());
                if (copyConstructor.size() == 1) {
                    constructor = copyConstructor.get(0);
                }
                else if (copyConstructor.size() > 1) {
                    throw new ObjectCannotBeClonedException(
                            "Ambiguous constructor: there are more then 1 constructor with CopyConstructor annotation");
                }
                else {
                    constructor = constructorList.stream()
                            .filter(c -> c.getParameterCount() == 0)
                            .findFirst()
                            .orElse(null);
                    if (constructor == null) {
                        throw new ObjectCannotBeClonedException(
                                "Ambiguous constructor: for class " + object.getClass().getName() + " there are more than 1 constructor and there is neither empty constructor nor constructor annotated CopyConstructor");
                    }
                }
            }
            return constructor;
        }

        private Object createObjectWithConstructor(Object object, Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {

            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
            Field[] declaredFields = object.getClass().getDeclaredFields();

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            Object[] parameters = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                final int finalI = i;
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                CopyFieldName copyAnnotation = (CopyFieldName) Arrays.stream(parameterAnnotation)
                        .filter(a -> CopyFieldName.class.equals(a.annotationType()))
                        .findFirst()
                        .orElse(null);
                if (copyAnnotation != null) {
                    Optional<Field> field = Arrays.stream(declaredFields)
                            .filter(f -> f.getName().equals(copyAnnotation.value()))
                            .findFirst();
                    if (field.isPresent()) {
                        field.get().setAccessible(true);
                        parameters[finalI] = field.get().get(object);
                    }
                    else {
                        throw new ObjectCannotBeClonedException(
                                "Field with name " + copyAnnotation.value() + " can not be found for class " + object.getClass().getName());
                    }
                }
                else {
                    List<Field> objectFieldsWithTheSameType = Arrays.stream(declaredFields)
                            .filter(f -> f.getType().equals(parameterTypes[finalI]))
                            .collect(Collectors.toList());
                    if (objectFieldsWithTheSameType.size() != 1) {
                        throw new ObjectCannotBeClonedException(
                                "Ambiguous constructor field: " + i + "(" + parameterTypes[finalI].getName() + ") for class " + object.getClass().getName());
                    }
                    objectFieldsWithTheSameType.get(0).setAccessible(true);
                    parameters[finalI] = objectFieldsWithTheSameType.get(0).get(object);
                }
            }
            return constructor.newInstance(parameters);
        }

        private boolean isReturnTheSameObject(Class clazz) {
            return clazz.equals(String.class)
                    || (clazz.getSuperclass() != null && clazz.getSuperclass().equals(Number.class)
                        && !clazz.equals(AtomicInteger.class) && !clazz.equals(AtomicLong.class))
                    || clazz.equals(Boolean.class);
        }

    }

}
