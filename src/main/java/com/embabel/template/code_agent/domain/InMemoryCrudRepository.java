package com.embabel.template.code_agent.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Spring Data CrudRepository with in memory storage.
 * Not itself intended for production usage, but can be used
 * in demos to minimize dependencies, and ultimately swapped out
 * for serious use.
 */
public class InMemoryCrudRepository<T> implements CrudRepository<T, String> {

    private final Function<T, String> idGetter;
    private final BiFunction<T, String, T> idSetter;
    private final ConcurrentHashMap<String, T> storage = new ConcurrentHashMap<>();

    public InMemoryCrudRepository(Function<T, String> idGetter, BiFunction<T, String, T> idSetter) {
        this.idGetter = idGetter;
        this.idSetter = idSetter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> S save(S entity) {
        S savedEntity = entity;
        String existingId = idGetter.apply(entity);
        String id = existingId != null ? existingId : UUID.randomUUID().toString();
        if (existingId == null) {
            savedEntity = (S) idSetter.apply(savedEntity, id);
        }
        storage.put(id, savedEntity);
        return savedEntity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    @Override
    public Iterable<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Iterable<T> findAllById(Iterable<String> ids) {
        List<T> result = new ArrayList<>();
        for (String id : ids) {
            T entity = storage.get(id);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    @Override
    public void delete(T entity) {
        String id = idGetter.apply(entity);
        if (id != null) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        for (String id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }
}