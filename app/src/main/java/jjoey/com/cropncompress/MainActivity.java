package jjoey.com.cropncompress;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView img_camera;
    private Button btnTakePicture;
    private Uri uri;
    private File file;

    private static final int CAM_CODE = 011;
    private static final int GAL_CODE = 101;
    private static final int CROP_CODE = 201;
    private static final int PERM_CODE = 303;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (permCheck == PackageManager.PERMISSION_DENIED) {
            askPermissions();
        }

        img_camera = findViewById(R.id.img_camera);

        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoDialog();
            }
        });

    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERM_CODE);
                return;
            }
        } else {
            photoDialog();
        }
    }

    private void photoDialog() {
        final CharSequence[] imgOptions = {"Take Photo", "Select from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Options");
        builder.setItems(imgOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (imgOptions[i].equals("Take Photo")) {
                    try {
                        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        //file = new File(Environment.getExternalStorageDirectory(), "tmp_" + String.valueOf(System.currentTimeMillis()));
                        file = new File(Environment.getExternalStorageDirectory(), "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                        Log.d("MainActivity", "File Saved in:\t" + file.getAbsolutePath().toString());

                        if (Build.VERSION.SDK_INT > 23) {
                            uri = FileProvider.getUriForFile(MainActivity.this, getString(R.string.file_prov_authority), file);
                        } else {
                            uri = Uri.fromFile(file);
                        }

                        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        camIntent.putExtra("return-data", "true");
                        startActivityForResult(camIntent, CAM_CODE);

                    } catch (ActivityNotFoundException ex) {
                        ex.printStackTrace();
                    }
                } else if (imgOptions[i].equals("Select from Gallery")) {
                    Intent galIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(galIntent, "Open With"), GAL_CODE);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAM_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Bitmap camBitmap = (Bitmap) bundle.get("data");

            //Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            img_camera.setImageBitmap(camBitmap);
            cropImage();
        }

        else if (requestCode == GAL_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                img_camera.setImageURI(uri);
                cropImage();
            }
        }

        else if (requestCode == CROP_CODE) {
            Bundle photo_extras = data.getExtras();
            Bitmap bitmap = photo_extras.getParcelable("data");

            img_camera.setImageBitmap(bitmap);

        }

    }

    private void cropImage() {

        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
//            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("outputX", 200);
            cropIntent.putExtra("outputY", 200);
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent, CROP_CODE);
        } catch (ActivityNotFoundException aex) {
            aex.printStackTrace();
            Toast.makeText(this, "Current Device Has No Crop Support", Toast.LENGTH_LONG).show();
        }

    }
}
