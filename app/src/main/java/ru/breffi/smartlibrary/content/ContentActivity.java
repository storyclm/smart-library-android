package ru.breffi.smartlibrary.content;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.breffi.smartlibrary.R;
import ru.breffi.story.domain.models.PresentationEntity;

public class ContentActivity extends AppCompatActivity {
    public static final String PRESENTATION_ID = "PRESENTATION_ID";
    public static final String PRESENTATION = "PRESENTATION";
    public static final int REQUEST_CODE = 2019;
    ContentFragment contentFragment;

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
        contentFragment = ContentFragment.newInstance((PresentationEntity) getIntent().getSerializableExtra(PRESENTATION));
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, contentFragment, ContentFragment.TAG)
                .addToBackStack(ContentFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            if (contentFragment.isFinishedSession) {
                Intent intent = new Intent();
                intent.putExtra(PRESENTATION, getIntent().getSerializableExtra(PRESENTATION));
                setResult(RESULT_OK, intent);
                finish();
            } else {
                contentFragment.showFinishSessionDialog();
            }
        }else{
            super.onBackPressed();
        }
    }
}
