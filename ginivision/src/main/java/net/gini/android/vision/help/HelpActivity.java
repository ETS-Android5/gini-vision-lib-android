package net.gini.android.vision.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.gini.android.vision.GiniVisionFeatureConfiguration;
import net.gini.android.vision.R;

public class HelpActivity extends AppCompatActivity {

    public static final String EXTRA_IN_GINI_VISION_FEATURE_CONFIGURATION =
            "GV_EXTRA_IN_GINI_VISION_FEATURE_CONFIGURATION";
    private static final int PHOTO_TIPS_REQUEST = 1;
    private GiniVisionFeatureConfiguration mGiniVisionFeatureConfiguration;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_TIPS_REQUEST
                && resultCode == PhotoTipsActivity.RESULT_SHOW_CAMERA_SCREEN) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_help);
        readExtras();
        setUpHelpItems();
    }

    private void readExtras() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGiniVisionFeatureConfiguration = extras.getParcelable(
                    EXTRA_IN_GINI_VISION_FEATURE_CONFIGURATION);
            if (mGiniVisionFeatureConfiguration == null) {
                mGiniVisionFeatureConfiguration =
                        GiniVisionFeatureConfiguration.buildNewConfiguration().build();
            }
        }
    }

    private void setUpHelpItems() {
        final RecyclerView recyclerView = findViewById(R.id.gv_help_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HelpItemsAdapter(mGiniVisionFeatureConfiguration,
                new HelpItemsAdapter.HelpItemSelectedListener() {
                    @Override
                    public void onItemSelected(@NonNull final HelpItemsAdapter.HelpItem helpItem) {
                        launchHelpScreen(helpItem);
                    }
                }));
    }

    private void launchHelpScreen(final HelpItemsAdapter.HelpItem helpItem) {
        switch (helpItem) {
            case PHOTO_TIPS:
                launchPhotoTips();
                break;
            case FILE_IMPORT_GUIDE:
                launchFileImport();
                break;
            case SUPPORTED_FORMATS:
                launchSupportedFormats();
                break;
        }
    }

    private void launchPhotoTips() {
        final Intent intent = new Intent(this, PhotoTipsActivity.class);
        startActivityForResult(intent, PHOTO_TIPS_REQUEST);
    }

    private void launchFileImport() {
        final Intent intent = new Intent(this, FileImportActivity.class);
        startActivity(intent);
    }

    private void launchSupportedFormats() {
        final Intent intent = new Intent(this, SupportedFormatsActivity.class);
        intent.putExtra(SupportedFormatsActivity.EXTRA_IN_GINI_VISION_FEATURE_CONFIGURATION,
                mGiniVisionFeatureConfiguration);
        startActivity(intent);
    }
}
