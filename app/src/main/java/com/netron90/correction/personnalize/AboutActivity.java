package com.netron90.correction.personnalize;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView politique;
    private final String POLITIQUE_URL = "http://mighty-refuge-23480.herokuapp.com/termeuser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("A propos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        politique = (TextView) findViewById(R.id.app_politique);

        politique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                politiqueConfidentialitePage(POLITIQUE_URL);
            }
        });
    }

    private void politiqueConfidentialitePage(String urlPolitique) {

        Uri uriUrl = Uri.parse(urlPolitique);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(intent);
    }
}
