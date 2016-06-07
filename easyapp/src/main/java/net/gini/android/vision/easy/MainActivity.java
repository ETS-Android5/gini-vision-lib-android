package net.gini.android.vision.easy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import net.gini.android.ginivisiontest.R;
import net.gini.android.vision.scanner.ScannerActivity;

public class MainActivity extends AppCompatActivity {

    private Button mButtonStartScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        addInputHandlers();
    }

    private void addInputHandlers() {
        mButtonStartScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });
    }

    private void startScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivity(intent);
    }

    private void bindViews() {
        mButtonStartScanner = (Button) findViewById(R.id.button_start_scanner);
    }
}
