package net.gini.android.vision.document;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.gini.android.vision.Document;
import net.gini.android.vision.internal.AsyncCallback;
import net.gini.android.vision.internal.camera.photo.ParcelableMemoryCache;
import net.gini.android.vision.internal.util.UriReaderAsyncTask;
import net.gini.android.vision.util.IntentHelper;

import java.util.UUID;

/**
 * @exclude
 */
public class GiniVisionDocument implements Document {

    /**
     * @exclude
     */
    public static final Creator<GiniVisionDocument> CREATOR = new Creator<GiniVisionDocument>() {
        @Override
        public GiniVisionDocument createFromParcel(final Parcel in) {
            return new GiniVisionDocument(in);
        }

        @Override
        public GiniVisionDocument[] newArray(final int size) {
            return new GiniVisionDocument[size];
        }
    };
    private final String mUniqueId;
    private final Intent mIntent;
    private final Uri mUri;
    private final boolean mIsImported;
    private final boolean mIsReviewable;
    private final Type mType;
    private final String mMimeType;
    private byte[] mData;

    GiniVisionDocument(@NonNull final Type type,
            @NonNull final String mimeType,
            @Nullable final byte[] data,
            @Nullable final Intent intent,
            @Nullable final Uri uri,
            final boolean isReviewable,
            final boolean isImported) {
        this(generateUniqueId(), type, mimeType, data, intent, uri, isReviewable, isImported);
    }

    GiniVisionDocument(
            @Nullable final String uniqueId,
            @NonNull final Type type,
            @NonNull final String mimeType,
            @Nullable final byte[] data,
            @Nullable final Intent intent,
            @Nullable final Uri uri,
            final boolean isReviewable,
            final boolean isImported) {
        mUniqueId = uniqueId != null ? uniqueId : generateUniqueId();
        mType = type;
        mMimeType = mimeType;
        mData = data;
        mIntent = intent;
        mUri = uri;
        mIsReviewable = isReviewable;
        mIsImported = isImported;
    }

    private static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    GiniVisionDocument(final Parcel in) {
        mUniqueId = in.readString();
        final ParcelableMemoryCache cache = ParcelableMemoryCache.getInstance();
        final ParcelableMemoryCache.Token token = in.readParcelable(
                ParcelableMemoryCache.Token.class.getClassLoader());
        if (token != null) {
            mData = cache.getByteArray(token);
            cache.removeByteArray(token);
        }
        mType = (Type) in.readSerializable();
        mMimeType = in.readString();
        mIntent = in.readParcelable(Intent.class.getClassLoader());
        mUri = in.readParcelable(Uri.class.getClassLoader());
        mIsReviewable = in.readInt() == 1;
        mIsImported = in.readInt() == 1;
    }

    @Override
    @NonNull
    public String getId() {
        return mUniqueId;
    }

    /**
     * @exclude
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @exclude
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mUniqueId);
        if (mData != null) {
            final ParcelableMemoryCache cache = ParcelableMemoryCache.getInstance();
            final ParcelableMemoryCache.Token token = cache.storeByteArray(mData);
            dest.writeParcelable(token, flags);
        } else {
            dest.writeParcelable(null, flags);
        }

        dest.writeSerializable(mType);
        dest.writeString(mMimeType);
        dest.writeParcelable(mIntent, flags);
        dest.writeParcelable(mUri, flags);
        dest.writeInt(mIsReviewable ? 1 : 0);
        dest.writeInt(mIsImported ? 1 : 0);
    }

    @Deprecated
    @NonNull
    @Override
    public byte[] getJpeg() {
        final byte[] data = getData();
        return data != null ? data : new byte[]{};
    }

    @Deprecated
    @Override
    public int getRotationForDisplay() {
        return 0;
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Nullable
    @Override
    public synchronized byte[] getData() {
        return mData;
    }

    private synchronized void setData(final byte[] data) {
        mData = data;
    }

    @Nullable
    @Override
    public Intent getIntent() {
        return mIntent;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public boolean isImported() {
        return mIsImported;
    }

    @Override
    public boolean isReviewable() {
        return mIsReviewable;
    }

    @Override
    public String toString() {
        return "GiniVisionDocument{" +
                "mUniqueId='" + mUniqueId + '\'' +
                ", mIntent=" + mIntent +
                ", mUri=" + mUri +
                ", mIsImported=" + mIsImported +
                ", mIsReviewable=" + mIsReviewable +
                ", mType=" + mType +
                ", mMimeType='" + mMimeType + '\'' +
                ", mData=" + mData +
                '}';
    }

    public synchronized void loadData(@NonNull final Context context,
            @NonNull final AsyncCallback<byte[]> callback) {
        if (mData != null) {
            callback.onSuccess(mData);
            return;
        }
        Uri uri = mUri;
        if (uri == null) {
            if (mIntent == null) {
                callback.onError(new IllegalStateException("No Intent to load the data from"));
                return;
            }
            uri = IntentHelper.getUri(mIntent);
        }
        if (uri == null) {
            callback.onError(new IllegalStateException("No Uri to load the data from"));
            return;
        }
        final UriReaderAsyncTask asyncTask = new UriReaderAsyncTask(context,
                new AsyncCallback<byte[]>() {
                    @Override
                    public void onSuccess(final byte[] result) {
                        setData(result);
                        callback.onSuccess(mData);
                    }

                    @Override
                    public void onError(final Exception exception) {
                        callback.onError(exception);
                    }
                });
        asyncTask.execute(uri);
    }

    public synchronized void unloadData() {
        mData = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GiniVisionDocument that = (GiniVisionDocument) o;

        if (mIsImported != that.mIsImported) {
            return false;
        }
        if (mIsReviewable != that.mIsReviewable) {
            return false;
        }
        if (!mUniqueId.equals(that.mUniqueId)) {
            return false;
        }
        if (mIntent != null ? !mIntent.equals(that.mIntent) : that.mIntent != null) {
            return false;
        }
        if (mUri != null ? !mUri.equals(that.mUri) : that.mUri != null) {
            return false;
        }
        if (mType != that.mType) {
            return false;
        }
        return mMimeType.equals(that.mMimeType);
    }

    @Override
    public int hashCode() {
        int result = mUniqueId.hashCode();
        result = 31 * result + (mIntent != null ? mIntent.hashCode() : 0);
        result = 31 * result + (mUri != null ? mUri.hashCode() : 0);
        result = 31 * result + (mIsImported ? 1 : 0);
        result = 31 * result + (mIsReviewable ? 1 : 0);
        result = 31 * result + mType.hashCode();
        result = 31 * result + mMimeType.hashCode();
        return result;
    }
}
