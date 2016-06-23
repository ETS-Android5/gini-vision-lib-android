package net.gini.android.vision.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.gini.android.vision.ActivityHelpers;
import net.gini.android.vision.GiniVisionCoordinator;
import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.analyse.AnalyseDocumentActivity;
import net.gini.android.vision.onboarding.OnboardingActivity;
import net.gini.android.vision.onboarding.OnboardingPage;
import net.gini.android.vision.reviewdocument.ReviewDocumentActivity;

import java.util.ArrayList;

/**
 * <p>
 * {@code ScannerActivity} is the main entry point to the Gini Vision Lib when using the Screen API.
 * </p>
 * <p>
 *     It shows a camera preview with tap-to-focus functionality and a trigger button. The camera preview also shows document corner guides to which the user should align the document.
 * </p>
 * <p>
 *     Start the {@code ScannerActivity} with {@link android.app.Activity#startActivityForResult(Intent, int)} to receive the original {@link Document} and the reviewed {@link Document} and also the {@link GiniVisionError}, if there was an error.
 * </p>
 * <p>
 *     These extras are mandatory:
 *     <ul>
 *         <li>{@link ScannerActivity#EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY} - use the {@link ScannerActivity#setReviewDocumentActivityExtra(Intent, Context, Class)} helper to set it. Must contain an explicit Intent to the {@link ReviewDocumentActivity} subclass from your application</li>
 *         <li>{@link ScannerActivity#EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY} - use the {@link ScannerActivity#setAnalyseDocumentActivityExtra(Intent, Context, Class)} helper to set it. Must contain an explicit Intent to the {@link AnalyseDocumentActivity} subclass from your application</li>
 *     </ul>
 * </p>
 * <p>
 *     Optional extras are:
 *     <ul>
 *         <li>{@link ScannerActivity#EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN} - if set and {@code true} the Onboarding Screen is shown the first time Gini Vision Lib is started</li>
 *         <li>{@link ScannerActivity#EXTRA_IN_ONBOARDING_PAGES} - custom pages for the Onboarding Screen as an {@link ArrayList} containing {@link OnboardingPage} objects</li>
 *     </ul>
 * </p>
 * <p>
 *     The following result codes need to be handled:
 *     <ul>
 *         <li>{@link ScannerActivity#RESULT_OK} - image of a document was taken, reviewed and analysed</li>
 *         <li>{@link ScannerActivity#RESULT_CANCELED} - image of document was not taken, user cancelled the Gini Vision Lib</li>
 *         <li>{@link ScannerActivity#RESULT_ERROR} - an error occured</li>
 *     </ul>
 * </p>
 * <p>
 *     Result extras returned by the {@code ScannerActivity}:
 *     <ul>
 *         <li>{@link ScannerActivity#EXTRA_OUT_ORIGINAL_DOCUMENT} - set when result is {@link ScannerActivity#RESULT_OK}, contains the unaltered image taken by the camera</li>
 *         <li>{@link ScannerActivity#EXTRA_OUT_DOCUMENT} - set when result is {@link ScannerActivity#RESULT_OK}, contains the reviewed image taken by the camera which should have been also uploaded to the Gini API</li>
 *         <li>{@link ScannerActivity#EXTRA_OUT_ERROR} - set when result is {@link ScannerActivity#RESULT_ERROR}, contains a {@link GiniVisionError} object detailing what went wrong</li>
 *     </ul>
 * </p>
 * <p>
 *     <b>Note:</b> It is important to retrieve the {@link Document} extras ({@link ScannerActivity#EXTRA_OUT_ORIGINAL_DOCUMENT} and {@link ScannerActivity#EXTRA_OUT_DOCUMENT}) to force unparceling of the {@link Document}s and removing of the references to their JPEG byte arrays from the memory cache. Failing to do so will lead to memory leaks.
 * </p>
 * <p>
 *     <b>Note:</b> For returning the extractions from the Gini API you can add your own extras in {@link ReviewDocumentActivity#onAddDataToResult(Intent)} or {@link AnalyseDocumentActivity#onAddDataToResult(Intent)}.
 * </p>
 */
public class ScannerActivity extends AppCompatActivity implements ScannerFragmentListener {

    /**
     * <p>
     * Mandatory extra which must contain an explicit Intent to the {@link ReviewDocumentActivity} subclass from your application.
     * </p>
     * <p>
     *     Use the {@link ScannerActivity#setReviewDocumentActivityExtra(Intent, Context, Class)} helper to set it.
     * </p>
     */
    public static final String EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY = "GV_EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY";
    /**
     * <p>
     * Mandatory extra which must contain an explicit Intent to the {@link AnalyseDocumentActivity} subclass from your application.
     * </p>
     * <p>
     *     Use the {@link ScannerActivity#setAnalyseDocumentActivityExtra(Intent, Context, Class)} helper to set it.
     * </p>
     */
    public static final String EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY = "GV_EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY";
    /**
     * <p>
     *     Optional extra which must contain an {@code ArrayList} with {@link OnboardingPage} objects.
     * </p>
     */
    public static final String EXTRA_IN_ONBOARDING_PAGES = "GV_EXTRA_IN_ONBOARDING_PAGES";
    /**
     * <p>
     *     Optional extra which must contain a boolean and shows the Onboarding Screen when the Gini Vision Lib is started for the first time, if it contains {@code true}.
     * </p>
     * <p>
     *     Default value is {@code true}.
     * </p>
     */
    public static final String EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN = "GV_EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN";

    /**
     * <p>
     *     Returned when the result code is {@link ScannerActivity#RESULT_OK} and contains the original image taken by the camera.
     * </p>
     * <p>
     *     <b>Note:</b> always retrieve this extra to force unparceling of the {@link Document} and removing of the reference to the JPEG byte array from the memory cache. Failing to do so will lead to memory leaks.
     * </p>
     */
    public static final String EXTRA_OUT_ORIGINAL_DOCUMENT = "GV_EXTRA_OUT_ORIGINAL_DOCUMENT";
    /**
     * <p>
     *     Returned when the result code is {@link ScannerActivity#RESULT_OK} and contains the reviewed image taken by the camera.
     * </p>
     * <p>
     *     <b>Note:</b> always retrieve this extra to force unparceling of the {@link Document} and removing of the reference to the JPEG byte array from the memory cache. Failing to do so will lead to memory leaks.
     * </p>
     */
    public static final String EXTRA_OUT_DOCUMENT = "GV_EXTRA_OUT_DOCUMENT";
    /**
     * <p>
     *     Returned when the result code is {@link ScannerActivity#RESULT_ERROR} and contains a {@link GiniVisionError} objects detailing what went wrong.
     * </p>
     */
    public static final String EXTRA_OUT_ERROR = "GV_EXTRA_OUT_ERROR";

    /**
     * <p>
     *     Returned result code, if something went wrong. You should retrieve the {@link ScannerActivity#EXTRA_OUT_ERROR} extra to find out what went wrong.
     * </p>
     */
    public static final int RESULT_ERROR = RESULT_FIRST_USER + 1;

    private static final int REVIEW_DOCUMENT_REQUEST = 1;
    private static final int ANALYSE_DOCUMENT_REQUEST = 2;

    private ArrayList<OnboardingPage> mOnboardingPages;
    private Intent mReviewDocumentActivityIntent;
    private Intent mAnalyseDocumentActivityIntent;
    private boolean mShowOnboardingAtFirstRun = true;
    private GiniVisionCoordinator mGiniVisionCoordinator;
    private Document mDocument;

    /**
     * <p>
     * Helper for setting the {@link ScannerActivity#EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY}.
     * </p>
     *
     * @param target your explicit {@link Intent} used to start the {@link ScannerActivity}
     * @param context {@link Context} used to create the explicit {@link Intent} for your {@link ReviewDocumentActivity} subclass
     * @param reviewPhotoActivityClass class of your {@link ReviewDocumentActivity} subclass
     * @param <T> type of your {@link ReviewDocumentActivity} subclass
     */
    public static <T extends ReviewDocumentActivity> void setReviewDocumentActivityExtra(Intent target,
                                                                                         Context context,
                                                                                         Class<T> reviewPhotoActivityClass) {
        ActivityHelpers.setActivityExtra(target, EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY, context, reviewPhotoActivityClass);
    }

    /**
     * <p>
     * Helper for setting the {@link ScannerActivity#EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY}.
     * </p>
     *
     * @param target your explicit {@link Intent} used to start the {@link ScannerActivity}
     * @param context {@link Context} used to create the explicit {@link Intent} for your {@link AnalyseDocumentActivity} subclass
     * @param reviewPhotoActivityClass class of your {@link AnalyseDocumentActivity} subclass
     * @param <T> type of your {@link AnalyseDocumentActivity} subclass
     */
    public static <T extends AnalyseDocumentActivity> void setAnalyseDocumentActivityExtra(Intent target,
                                                                                           Context context,
                                                                                           Class<T> reviewPhotoActivityClass) {
        ActivityHelpers.setActivityExtra(target, EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY, context, reviewPhotoActivityClass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_scanner);
        readExtras();
        createGiniVisionCoordinator();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGiniVisionCoordinator.onScannerStarted();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMemory();
    }

    private void readExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mOnboardingPages = extras.getParcelableArrayList(EXTRA_IN_ONBOARDING_PAGES);
            mReviewDocumentActivityIntent = extras.getParcelable(EXTRA_IN_REVIEW_DOCUMENT_ACTIVITY);
            mAnalyseDocumentActivityIntent = extras.getParcelable(EXTRA_IN_ANALYSE_DOCUMENT_ACTIVITY);
            mShowOnboardingAtFirstRun = extras.getBoolean(EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN, true);
        }
        checkRequiredExtras();
    }

    private void checkRequiredExtras() {
        if (mReviewDocumentActivityIntent == null) {
            throw new IllegalStateException("ScannerActivity requires a ReviewDocumentActivity class. Call setReviewDocumentActivityExtra() to set it.");
        }
        if (mAnalyseDocumentActivityIntent == null) {
            throw new IllegalStateException("ScannerActivity requires an AnalyseDocumentActivity class. Call setAnalyseDocumentActivityExtra() to set it.");
        }
    }

    private void createGiniVisionCoordinator() {
        mGiniVisionCoordinator = GiniVisionCoordinator.createInstance(this);
        mGiniVisionCoordinator
                .setShowOnboardingAtFirstRun(mShowOnboardingAtFirstRun)
                .setListener(new GiniVisionCoordinator.Listener() {
                    @Override
                    public void onShowOnboarding() {
                        startOnboardingActivity();
                    }
                });
    }

    /**
     * @exclude
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gv_scanner, menu);
        return true;
    }

    /**
     * @exclude
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.gv_action_show_onboarding) {
            startOnboardingActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startOnboardingActivity() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        if (mOnboardingPages != null) {
            intent.putParcelableArrayListExtra(OnboardingActivity.EXTRA_ONBOARDING_PAGES, mOnboardingPages);
        }
        startActivity(intent);
    }

    @Override
    public void onDocumentAvailable(Document document) {
        mDocument = document;
        // Start ReviewDocumentActivity
        mReviewDocumentActivityIntent.putExtra(ReviewDocumentActivity.EXTRA_IN_DOCUMENT, document);
        startActivityForResult(mReviewDocumentActivityIntent, REVIEW_DOCUMENT_REQUEST);
    }

    @Override
    public void onError(GiniVisionError error) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_ERROR, error);
        setResult(RESULT_ERROR, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REVIEW_DOCUMENT_REQUEST) {
            switch (resultCode) {
                case ReviewDocumentActivity.RESULT_PHOTO_WAS_REVIEWED:
                    if (data != null) {
                        Document document = data.getParcelableExtra(ReviewDocumentActivity.EXTRA_OUT_DOCUMENT);
                        mAnalyseDocumentActivityIntent.putExtra(AnalyseDocumentActivity.EXTRA_IN_DOCUMENT, document);
                        startActivityForResult(mAnalyseDocumentActivityIntent, ANALYSE_DOCUMENT_REQUEST);
                    }
                    break;
                case ReviewDocumentActivity.RESULT_PHOTO_WAS_REVIEWED_AND_ANALYZED:
                    if (data == null) {
                        data = new Intent();
                    }
                    if (mDocument != null) {
                        data.putExtra(EXTRA_OUT_ORIGINAL_DOCUMENT, mDocument);
                    }
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case ReviewDocumentActivity.RESULT_ERROR:
                    setResult(RESULT_ERROR, data);
                    finish();
                    break;
            }
        } else if (requestCode == ANALYSE_DOCUMENT_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    if (data == null) {
                        data = new Intent();
                    }
                    if (mDocument != null) {
                        data.putExtra(EXTRA_OUT_ORIGINAL_DOCUMENT, mDocument);
                    }
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case AnalyseDocumentActivity.RESULT_ERROR:
                    setResult(RESULT_ERROR, data);
                    finish();
                    break;
            }
        }
        clearMemory();
    }

    private void clearMemory() {
        mDocument = null;
    }
}
