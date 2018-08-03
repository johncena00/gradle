/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.changedetection.state;

import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.internal.Factory;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.resource.local.FileAccessTimeJournal;
import org.gradle.util.GUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Objects.firstNonNull;
import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;
import static org.gradle.internal.serialize.BaseSerializerFactory.FILE_SERIALIZER;
import static org.gradle.internal.serialize.BaseSerializerFactory.LONG_SERIALIZER;

public class DefaultFileAccessTimeJournal implements FileAccessTimeJournal, Stoppable {

    public static final String CACHE_KEY = "journal-1";
    public static final String FILE_ACCESS_CACHE_NAME = "file-access";
    public static final String FILE_ACCESS_PROPERTIES_FILE_NAME = FILE_ACCESS_CACHE_NAME + ".properties";
    public static final String INCEPTION_TIMESTAMP_KEY = "inceptionTimestamp";

    private final PersistentCache cache;
    private final PersistentIndexedCache<File, Long> store;
    private final long inceptionTimestamp;

    public DefaultFileAccessTimeJournal(CacheRepository cacheRepository, InMemoryCacheDecoratorFactory cacheDecoratorFactory) {
        cache = cacheRepository
            .cache(CACHE_KEY)
            .withCrossVersionCache(CacheBuilder.LockTarget.CacheDirectory)
            .withDisplayName("journal cache")
            .withLockOptions(mode(FileLockManager.LockMode.None)) // lock on demand
            .open();
        store = cache.createCache(PersistentIndexedCacheParameters.of(FILE_ACCESS_CACHE_NAME, FILE_SERIALIZER, LONG_SERIALIZER)
            .cacheDecorator(cacheDecoratorFactory.decorator(1000, true)));
        inceptionTimestamp = loadOrPersistInceptionTimestamp();
    }

    private Long loadOrPersistInceptionTimestamp() {
        return cache.useCache(new Factory<Long>() {
            @Override
            public Long create() {
                File propertiesFile = new File(cache.getBaseDir(), FILE_ACCESS_PROPERTIES_FILE_NAME);
                if (propertiesFile.exists()) {
                    Properties properties = GUtil.loadProperties(propertiesFile);
                    String inceptionTimestamp = properties.getProperty(INCEPTION_TIMESTAMP_KEY);
                    if (inceptionTimestamp != null) {
                        return Long.valueOf(inceptionTimestamp);
                    }
                }
                long inceptionTimestamp = System.currentTimeMillis();
                Properties properties = new Properties();
                properties.setProperty(INCEPTION_TIMESTAMP_KEY, String.valueOf(inceptionTimestamp));
                GUtil.saveProperties(properties, propertiesFile);
                return inceptionTimestamp;
            }
        });
    }

    @Override
    public void stop() {
        cache.close();
    }

    @Override
    public void setLastAccessTime(File file, long millis) {
        store.put(file, millis);
    }

    @Override
    public void deleteLastAccessTime(File file) {
        store.remove(file);
    }

    @Override
    public Snapshot createSnapshot() {
        return new DefaultSnapshot(store.createSnapshot());
    }

    private class DefaultSnapshot implements Snapshot {
        private final PersistentIndexedCache.Snapshot<File, Long> snapshot;

        public DefaultSnapshot(PersistentIndexedCache.Snapshot<File, Long> snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public long getLastAccessTime(File file) {
            return firstNonNull(snapshot.get(file), inceptionTimestamp);
        }

        @Override
        public void close() throws IOException {
            snapshot.close();
        }
    }
}