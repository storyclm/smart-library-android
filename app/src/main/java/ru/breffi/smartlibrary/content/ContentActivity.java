package ru.breffi.smartlibrary.content;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import ru.breffi.smartlibrary.R;
import ru.breffi.story.domain.models.PresentationEntity;

public class ContentActivity extends AppCompatActivity {
    public static final String PRESENTATION_ID = "PRESENTATION_ID";
    public static final String PRESENTATION = "PRESENTATION";
    public static final int REQUEST_CODE = 2019;

    public static Intent getIntent(final Context context, PresentationEntity presentationEntity) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(PRESENTATION, presentationEntity);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        showContent();
    }

    private void showContent() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, ContentFragment.newInstance((PresentationEntity) getIntent().getSerializableExtra(PRESENTATION)), ContentFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(PRESENTATION, getIntent().getSerializableExtra(PRESENTATION));
        setResult(RESULT_OK, intent);
        finish();
    }
}
