package net.gini.android.vision.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.volley.Cache;

import net.gini.android.DocumentTaskManager;
import net.gini.android.Gini;
import net.gini.android.SdkBuilder;
import net.gini.android.authorization.CredentialsStore;
import net.gini.android.authorization.SessionManager;
import net.gini.android.vision.Document;
import net.gini.android.vision.internal.camera.api.UIExecutor;
import net.gini.android.vision.network.model.GiniVisionSpecificExtraction;
import net.gini.android.vision.network.model.SpecificExtractionMapper;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Alpar Szotyori on 30.01.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

public class GiniVisionNetworkHandler implements GiniVisionNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(GiniVisionNetworkHandler.class);

    private final SingleDocumentAnalyzer mSingleDocumentAnalyzer;
    private final Gini mGiniApi;
    private final UIExecutor mUIExecutor = new UIExecutor();

    public static Builder builder(@NonNull final Context context) {
        return new Builder(context);
    }

    GiniVisionNetworkHandler(@NonNull final Gini giniApi,
            @NonNull final SingleDocumentAnalyzer singleDocumentAnalyzer) {
        mGiniApi = giniApi;
        mSingleDocumentAnalyzer = singleDocumentAnalyzer;
    }

    @Override
    public void analyze(@NonNull final Document document,
            @NonNull final Callback<AnalysisResult, Error> callback) {
        mSingleDocumentAnalyzer.analyzeDocument(document,
                new DocumentAnalyzer.Listener() {
                    @Override
                    public void onException(final Exception exception) {
                        callback.failure(new Error(exception.getMessage()));
                    }

                    @Override
                    public void onExtractionsReceived(
                            final Map<String, net.gini.android.models.SpecificExtraction> extractions) {
                        callback.success(new AnalysisResult(
                                mSingleDocumentAnalyzer.getGiniApiDocument().getId(),
                                SpecificExtractionMapper.mapToGVL(extractions)));
                    }
                });
    }

    @Override
    public void upload(@NonNull final Document document,
            @NonNull final Callback<Result, Error> callback) {

    }

    @Override
    public void cancel() {
        mSingleDocumentAnalyzer.cancelAnalysis();
    }

    @Override
    public void sendFeedback(@NonNull final Map<String, GiniVisionSpecificExtraction> extractions,
            @NonNull final Callback<Void, Error> callback) {
        final DocumentTaskManager documentTaskManager = mGiniApi.getDocumentTaskManager();

        final net.gini.android.models.Document document = mSingleDocumentAnalyzer.getGiniApiDocument();

        // We require the Gini API SDK's net.gini.android.models.Document for sending the feedback
        if (document != null) {
            try {
                documentTaskManager.sendFeedbackForExtractions(document,
                        SpecificExtractionMapper.mapToApiSdk(extractions))
                        .continueWith(new Continuation<net.gini.android.models.Document, Object>() {
                            @Override
                            public Object then(
                                    @NonNull final Task<net.gini.android.models.Document> task)
                                    throws Exception {
                                mUIExecutor.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (task.isFaulted()) {
                                            LOG.error("Feedback error", task.getError());
                                            String message = "unknown";
                                            if (task.getError() != null) {
                                                message = task.getError().getMessage();
                                            }
                                            callback.failure(new Error(message));
                                        } else {
                                            callback.success(null);
                                        }
                                    }
                                });
                                return null;
                            }
                        });
            } catch (final JSONException e) {
                LOG.error("Feedback not sent", e);
                callback.failure(new Error(e.getMessage()));
            }
        } else {
            callback.failure(new Error("Feedback not set: no Gini Api Document available"));
        }
    }

    public static class Builder {

        private final Context mContext;
        private String mClientId;
        private String mClientSecret;
        private String mEmailDomain;
        private String[] mCertificateAssetPaths;
        private SessionManager mSessionManager;
        private String mBaseUrl;
        private String mUserCenterBaseUrl;
        private Cache mCache;
        private CredentialsStore mCredentialsStore;
        private long mConnectionTimeout;
        private TimeUnit mConnectionTimeoutUnit;
        private int mMaxNumberOfRetries;
        private float mBackoffMultiplier;

        Builder(@NonNull final Context context) {
            mContext = context;
        }

        @NonNull
        public GiniVisionNetworkHandler build() {
            final SdkBuilder sdkBuilder;
            if (mCertificateAssetPaths != null) {
                sdkBuilder = new SdkBuilder(mContext, mClientId, mClientSecret,
                        mEmailDomain, mCertificateAssetPaths);
            } else if (mSessionManager != null) {
                sdkBuilder = new SdkBuilder(mContext, mSessionManager);
            } else {
                sdkBuilder = new SdkBuilder(mContext, mClientId, mClientSecret, mEmailDomain);
            }
            if (!TextUtils.isEmpty(mBaseUrl)) {
                sdkBuilder.setApiBaseUrl(mBaseUrl);
            }
            if (!TextUtils.isEmpty(mUserCenterBaseUrl)) {
                sdkBuilder.setUserCenterApiBaseUrl(mUserCenterBaseUrl);
            }
            if (mCache != null) {
                sdkBuilder.setCache(mCache);
            }
            if (mCredentialsStore != null) {
                sdkBuilder.setCredentialsStore(mCredentialsStore);
            }
            if (mConnectionTimeoutUnit != null) {
                sdkBuilder.setConnectionTimeoutInMs(
                        (int) TimeUnit.MILLISECONDS.convert(mConnectionTimeout,
                                mConnectionTimeoutUnit));
            }
            if (mMaxNumberOfRetries >= 0) {
                sdkBuilder.setMaxNumberOfRetries(mMaxNumberOfRetries);
            }
            if (mBackoffMultiplier >= 0) {
                sdkBuilder.setConnectionBackOffMultiplier(mBackoffMultiplier);
            }
            final Gini giniApi = sdkBuilder.build();
            final SingleDocumentAnalyzer singleDocumentAnalyzer = new SingleDocumentAnalyzer(
                    giniApi);
            return new GiniVisionNetworkHandler(giniApi, singleDocumentAnalyzer);
        }

        @NonNull
        public Builder setClientCredentials(@NonNull final String clientId,
                @NonNull final String clientSecret, @NonNull final String emailDomain) {
            mClientId = clientId;
            mClientSecret = clientSecret;
            mEmailDomain = emailDomain;
            return this;
        }

        @NonNull
        public Builder setCertificateAssetPaths(@NonNull final String[] certificateAssetPaths) {
            mCertificateAssetPaths = certificateAssetPaths;
            return this;
        }

        @NonNull
        public Builder setSessionManager(@NonNull final SessionManager sessionManager) {
            mSessionManager = sessionManager;
            return this;
        }

        @NonNull
        public Builder setBaseUrl(@NonNull final String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        @NonNull
        public Builder setUserCenterBaseUrl(@NonNull final String userCenterBaseUrl) {
            mUserCenterBaseUrl = userCenterBaseUrl;
            return this;
        }

        @NonNull
        public Builder setCache(@NonNull final Cache cache) {
            mCache = cache;
            return this;
        }

        @NonNull
        public Builder setCredentialsStore(@NonNull final CredentialsStore credentialsStore) {
            mCredentialsStore = credentialsStore;
            return this;
        }

        @NonNull
        public Builder setConnectionTimeout(final long connectionTimeout) {
            mConnectionTimeout = connectionTimeout;
            return this;
        }

        @NonNull
        public Builder setConnectionTimeoutUnit(@NonNull final TimeUnit connectionTimeoutUnit) {
            mConnectionTimeoutUnit = connectionTimeoutUnit;
            return this;
        }

        @NonNull
        public Builder setMaxNumberOfRetries(final int maxNumberOfRetries) {
            mMaxNumberOfRetries = maxNumberOfRetries;
            return this;
        }

        @NonNull
        public Builder setBackoffMultiplier(final float backoffMultiplier) {
            mBackoffMultiplier = backoffMultiplier;
            return this;
        }
    }

}
