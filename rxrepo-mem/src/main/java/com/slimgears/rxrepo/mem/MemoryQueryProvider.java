package com.slimgears.rxrepo.mem;

import com.slimgears.rxrepo.encoding.MetaObjectResolver;
import com.slimgears.rxrepo.query.provider.AbstractEntityQueryProviderAdapter;
import com.slimgears.rxrepo.query.provider.EntityQueryProvider;
import com.slimgears.util.autovalue.annotations.HasMetaClassWithKey;
import com.slimgears.util.autovalue.annotations.MetaClassWithKey;
import com.slimgears.util.stream.Safe;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryQueryProvider extends AbstractEntityQueryProviderAdapter implements MetaObjectResolver {
    private final List<AutoCloseable> closeableList = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected <K, S extends HasMetaClassWithKey<K, S>> EntityQueryProvider<K, S> createProvider(MetaClassWithKey<K, S> metaClass) {
        MemoryEntityQueryProvider<K, S> provider = MemoryEntityQueryProvider.create(metaClass, this);
        closeableList.add(provider);
        return provider;
    }

    @Override
    protected Scheduler scheduler() {
        return Schedulers.computation();
    }

    @Override
    protected Completable dropAllProviders() {
        return Completable.complete();
    }

    @Override
    public <K, S extends HasMetaClassWithKey<K, S>> Maybe<S> resolve(MetaClassWithKey<K, S> metaClass, K key) {
        return ((MemoryEntityQueryProvider<K, S>)entities(metaClass)).find(key);
    }

    @Override
    public void close() {
        closeableList.stream()
                .map(Safe::ofClosable)
                .forEach(Safe.Closeable::close);
    }
}
