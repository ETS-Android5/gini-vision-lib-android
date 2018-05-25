package net.gini.android.vision.internal.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.LruCache;

import net.gini.android.vision.document.GiniVisionDocument;
import net.gini.android.vision.internal.AsyncCallback;

import java.util.List;

/**
 * Created by Alpar Szotyori on 16.03.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public class DocumentDataMemoryCache extends MemoryCache<GiniVisionDocument, byte[]> {

    private static final int RUNNING_WORKERS_LIMIT = 3;

    public DocumentDataMemoryCache() {
        super(RUNNING_WORKERS_LIMIT);
    }

    @Override
    protected LruCache<GiniVisionDocument, byte[]> createCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        return new LruCache<GiniVisionDocument, byte[]>(cacheSize) {
            @Override
            protected void entryRemoved(final boolean evicted, final GiniVisionDocument key,
                    final byte[] oldValue,
                    final byte[] newValue) {
                if (newValue == null) {
                    key.unloadData();
                }
            }

            @Override
            protected int sizeOf(final GiniVisionDocument key, final byte[] value) {
                return value.length / 1024;
            }
        };
    }

    @Override
    protected MemoryCache.Worker<GiniVisionDocument, byte[]> createWorker(
            @NonNull final List<MemoryCache.Worker<GiniVisionDocument, byte[]>> runningWorkers,
            @NonNull final GiniVisionDocument subject,
            @NonNull final AsyncCallback<byte[]> callback) {
        return new DocumentDataWorker(runningWorkers, subject, new AsyncCallback<byte[]>() {
            @Override
            public void onSuccess(final byte[] result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(final Exception exception) {
                callback.onError(exception);
            }
        });
    }

    private static class DocumentDataWorker extends MemoryCache.Worker<GiniVisionDocument, byte[]> {

        private DocumentDataWorker(
                @NonNull final List<MemoryCache.Worker<GiniVisionDocument, byte[]>> runningWorkers,
                @NonNull final GiniVisionDocument subject,
                @NonNull final AsyncCallback<byte[]> callback) {
            super(runningWorkers, subject, callback);
        }

        @Override
        protected void doExecute(@NonNull final Context context,
                @NonNull final GiniVisionDocument subject,
                @NonNull final AsyncCallback<byte[]> callback) {
            subject.loadData(context, new AsyncCallback<byte[]>() {
                @Override
                public void onSuccess(final byte[] result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onError(final Exception exception) {
                    callback.onError(exception);
                }
            });
        }
    }

}
