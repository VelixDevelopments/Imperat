package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.type.ArrayParameterType;
import dev.velix.imperat.command.parameters.type.CollectionParameterType;
import dev.velix.imperat.command.parameters.type.MapParameterType;
import dev.velix.imperat.command.parameters.type.ParameterEnum;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ParamTypeRegistry<S extends Source> extends Registry<Type, Supplier<ParameterType>> {

    private final Registry<Type, Supplier<Collection<?>>> collectionInitializer = new Registry<>(LinkedHashMap::new);
    {
        // List implementations
        collectionInitializer.setData(ArrayList.class, ArrayList::new);
        collectionInitializer.setData(LinkedList.class, LinkedList::new);
        collectionInitializer.setData(Vector.class, Vector::new);
        collectionInitializer.setData(Stack.class, Stack::new);
        collectionInitializer.setData(CopyOnWriteArrayList.class, CopyOnWriteArrayList::new);

// Set implementations
        collectionInitializer.setData(HashSet.class, HashSet::new);
        collectionInitializer.setData(LinkedHashSet.class, LinkedHashSet::new);
        collectionInitializer.setData(TreeSet.class, TreeSet::new);
        collectionInitializer.setData(CopyOnWriteArraySet.class, CopyOnWriteArraySet::new);
        collectionInitializer.setData(ConcurrentSkipListSet.class, ConcurrentSkipListSet::new);

// Queue/Deque implementations
        collectionInitializer.setData(PriorityQueue.class, PriorityQueue::new);
        collectionInitializer.setData(ArrayDeque.class, ArrayDeque::new);
        collectionInitializer.setData(ConcurrentLinkedQueue.class, ConcurrentLinkedQueue::new);
        collectionInitializer.setData(ConcurrentLinkedDeque.class, ConcurrentLinkedDeque::new);
        collectionInitializer.setData(LinkedBlockingQueue.class, LinkedBlockingQueue::new);
        collectionInitializer.setData(PriorityBlockingQueue.class, PriorityBlockingQueue::new);
        collectionInitializer.setData(DelayQueue.class, DelayQueue::new);
        collectionInitializer.setData(SynchronousQueue.class, SynchronousQueue::new);
        collectionInitializer.setData(LinkedTransferQueue.class, LinkedTransferQueue::new);
    }


    private final Registry<Type, Function<Integer, Object[]>> arrayInitializer = new Registry<>(LinkedHashMap::new);
    {
        // Wrapped types array initializers with size parameter
        arrayInitializer.setData(Boolean.class, Boolean[]::new);
        arrayInitializer.setData(Byte.class, Byte[]::new);
        arrayInitializer.setData(Short.class, Short[]::new);
        arrayInitializer.setData(Integer.class, Integer[]::new);
        arrayInitializer.setData(Long.class, Long[]::new);
        arrayInitializer.setData(Float.class, Float[]::new);
        arrayInitializer.setData(Double.class, Double[]::new);
        arrayInitializer.setData(Character.class, Character[]::new);
        arrayInitializer.setData(String.class, String[]::new);
    }

    private final Registry<Type, Supplier<Map<?, ?>>> mapInitializer = new Registry<>(LinkedHashMap::new);
    {

        // Standard Map Implementations
        mapInitializer.setData(HashMap.class, HashMap::new);
        mapInitializer.setData(LinkedHashMap.class, LinkedHashMap::new);
        mapInitializer.setData(TreeMap.class, TreeMap::new);
        mapInitializer.setData(WeakHashMap.class, WeakHashMap::new);
        mapInitializer.setData(IdentityHashMap.class, IdentityHashMap::new);

        // Concurrent Map Implementations
        mapInitializer.setData(ConcurrentHashMap.class, ConcurrentHashMap::new);
        mapInitializer.setData(ConcurrentSkipListMap.class, ConcurrentSkipListMap::new);

        // Specialized Map Types
        mapInitializer.setData(EnumMap.class, () -> {
            throw new UnsupportedOperationException("EnumMap requires an enum type parameter");
        });

        // Sorted Map Interfaces
        mapInitializer.setData(SortedMap.class, TreeMap::new);
        mapInitializer.setData(NavigableMap.class, TreeMap::new);

    }

    private ParamTypeRegistry() {
        super();
        registerResolver(Boolean.class, ParameterTypes::bool);
        registerResolver(String.class, ParameterTypes::string);
        registerResolver(UUID.class, ParameterTypes::uuid);

    }

    public static <S extends Source> ParamTypeRegistry<S> createDefault() {
        return new ParamTypeRegistry<>();
    }

    public void registerResolver(Type type, Supplier<ParameterType> resolver) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) return;
        setData(type, resolver);
    }

    <E, C extends Collection<E>> Supplier<C> initializeNewCollection(TypeWrap<?> fullType) {
        var collectionType = fullType.getRawType();
        var data = collectionInitializer.getData(collectionType);
        if(data.isEmpty()) {
            return (Supplier<C>) collectionInitializer.search((ctype, supplier)-> TypeWrap.of(collectionType).isSupertypeOf(ctype))
                    .orElseThrow(()-> new IllegalArgumentException("Unknown collection-type detected '" + collectionType.getTypeName() + "'"));
        }else {
            return (Supplier<C>) data.get();
        }
    }

    Function<Integer, Object[]> initializeNewArray(TypeWrap<?> componentType) {
        var data = arrayInitializer.getData(componentType.getType());
        if(data.isEmpty()) {

            Function<Integer, Object[]> func = null;
            for(Type key : arrayInitializer.getKeys()) {
                if(componentType.isSupertypeOf(key)) {
                    func = arrayInitializer.getData(key).orElse(null);
                    if(func != null)
                        break;
                }
            }
            if(func == null) {
                throw new IllegalArgumentException("Unknown array-type detected '" + componentType.getType().getTypeName() + "'");
            }
            return func;
        }else {
            return data.get();
        }
    }

    <K, V, M extends Map<K, V>> Supplier<M> initializeNewMap(TypeWrap<?> fullType) {
        Type mapRawType = fullType.getRawType();
        var initializer = mapInitializer.getData(mapRawType);
        if(initializer.isEmpty()) {
            return (Supplier<M>) mapInitializer.search((ctype, supplier)-> TypeWrap.of(mapRawType).isSupertypeOf(ctype))
                    .orElseThrow(()-> new IllegalArgumentException("Unknown map-type detected '" + mapRawType.getTypeName() + "'"));
        }else {
            return (Supplier<M>) initializer.get();
        }
    }

    private <E, C extends Collection<E>> CollectionParameterType<S, E, C> getCollectionResolver(TypeWrap<?> type) {
        var parameterizedTypes = type.getParameterizedTypes();
        if(parameterizedTypes == null) {
            throw new IllegalArgumentException("NULL PARAMETERIZED TYPES");
        }
        TypeWrap<E> componentType = (TypeWrap<E>) TypeWrap.of(parameterizedTypes[0]);
        ParameterType<S, E> componentResolver = (ParameterType<S, E>) getResolver(componentType.getType()).orElseThrow(()-> new IllegalArgumentException("Unknown component-type detected '" + componentType.getType().getTypeName() + "'"));
        return new CollectionParameterType<>((TypeWrap<C>) type, initializeNewCollection(type), componentResolver);
    }

    private <E> ParameterType<S, E[]> getArrayResolver(TypeWrap<?> type) {
        var componentType = type.getComponentType();
        if(componentType == null) {
            throw new IllegalArgumentException("NULL COMPONENT TYPE");
        }
        ParameterType<S, E> componentResolver = (ParameterType<S, E>) getResolver(componentType.getType()).orElseThrow(()-> new IllegalArgumentException("Unknown component-type detected '" + componentType.getType().getTypeName() + "'"));
        return new ArrayParameterType<>((TypeWrap<E[]>) type, initializeNewArray(componentType), componentResolver);
    }

    private <K, V, M extends Map<K, V>> MapParameterType<S, K, V, M> getMapResolver(TypeWrap<?> type) {
        var parameterizedTypes = type.getParameterizedTypes();
        if(parameterizedTypes == null || parameterizedTypes.length == 0) {
            throw new IllegalArgumentException("Raw types are not allowed as parameters !");
        }
        TypeWrap<K> keyType = (TypeWrap<K>) TypeWrap.of(parameterizedTypes[0]);
        TypeWrap<V> valueType = (TypeWrap<V>) TypeWrap.of(parameterizedTypes[0]);

        ParameterType<S, K> keyResolver = (ParameterType<S, K>) getResolver(keyType.getType()).orElseThrow(()-> new IllegalArgumentException("Unknown component-type detected '" + keyType.getType().getTypeName() + "'"));
        ParameterType<S, V> valueResolver = (ParameterType<S, V>) getResolver(valueType.getType()).orElseThrow(()-> new IllegalArgumentException("Unknown component-type detected '" + valueType.getType().getTypeName() + "'"));

        return new MapParameterType<>((TypeWrap<M>) type, initializeNewMap(type), keyResolver, valueResolver);
    }

    public <E, C extends Collection<E>> void registerCollectionInitializer(Class<C> type, Supplier<C> initializerFunction) {
        collectionInitializer.setData(type, (Supplier<Collection<?>>) initializerFunction);
    }

    public void registerArrayInitializer(Type type, Function<Integer, Object[]> initializerFunction) {
        arrayInitializer.setData(type, initializerFunction);
    }

    public <K, V, M extends Map<K, V>>  void registerMapInitializer(Type type, Supplier<M> initializerFunction) {
        mapInitializer.setData(type, (Supplier<Map<?, ?>>) initializerFunction);
    }

    public <T> Optional<ParameterType<S, T>> getResolver(Type type) {
        var wrap = TypeWrap.of(type);
        if(wrap.isArray()) {
            //array type
            return Optional.of((ParameterType<S, T>) getArrayResolver(wrap));
        }else if(wrap.isSubtypeOf(Collection.class)) {
            //collection type
            return Optional.of((ParameterType<S, T>) this.getCollectionResolver(wrap));
        }
        else if(wrap.isSubtypeOf(Map.class)) {
            //map type
            return Optional.of((ParameterType<S, T>) this.getMapResolver(wrap));
        }


        if (TypeUtility.isNumericType(wrap))
            return Optional.of((ParameterType<S, T>) ParameterTypes.numeric((Class<? extends Number>) type));

        return Optional.ofNullable(getData(TypeUtility.primitiveToBoxed(type)).map(Supplier::get).orElseGet(() -> {
            if (TypeUtility.areRelatedTypes(type, Enum.class)) {
                ParameterEnum<S> preloadedEnumType = new ParameterEnum<>((TypeWrap<Enum<?>>) TypeWrap.of(type));
                registerResolver(type, ()-> preloadedEnumType);
                return preloadedEnumType;
            }

            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).map((s)-> ((ParameterType<S, T>)s.get()) ).orElse(null);
                }
            }
            return null;
        }));
    }

}
