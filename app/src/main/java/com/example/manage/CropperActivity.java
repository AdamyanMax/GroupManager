package com.example.manage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    String result;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        readIntent();

        String destinationUri = UUID.randomUUID().toString() + ".jpg";

        // Use mainly 'options.' for customization. More at: https://github.com/Yalantis/uCrop#customization
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropGridCornerColor(262626);

        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(), destinationUri)))
                .withOptions(options)
                .withAspectRatio(1, 1)
                .withMaxResultSize(400, 400)
                .start(CropperActivity.this);
    }

    private void readIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            result = intent.getStringExtra("DATA");
            fileUri = Uri.parse(result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            final Uri resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("RESULT", resultUri + "");
            setResult(-1, returnIntent);
            finish();
        } else if (requestCode == UCrop.RESULT_ERROR) {
            assert data != null;
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, cropError + "", Toast.LENGTH_SHORT).show();
        }


    }
}