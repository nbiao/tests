package com.hb.plantrecognition;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.hb.plantrecognition.update.Updater;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final float MAX_SIZE = 1024f;
    private static final int RETRY_COUNT = 3;
    private static final int RESULT_CAMERA = 100;
    private static final int RESULT_ALBUM = 200;
    private static final String BITMAP_FILE_NAME = "out_PlantRecognitionImage.jpg";
    private static final int imageResource[] = new int[]{
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d};

    private final Random random = new Random();
    private PlantRecognitionService plantRecognize;
    private ImageView imageView;
    private Uri imageUri;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_camera:
                getCameraImage();
                return true;
            case R.id.navigation_album:
                getAlbumImage();
                return true;
            case R.id.navigation_recognize:
                new PlantRecognizeTask().execute();
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(imageResource[random.nextInt(imageResource.length)]);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        plantRecognize = RetrofitFactory.getRetrofitService();
        Updater.getInstance().check(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_action_about:
                try {
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.logo)
                            .setTitle(R.string.title_about)
                            .setMessage("Programmed by LHL & HB.\nDesigned by LJL & MC & MJ & WRQ.\nGuided by LHL.\nV" +
                                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    private class PlantRecognizeTask extends
            AsyncTask<Void, Void, PlantRecognitionService.PlantInfo> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.recognizing));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(PlantRecognitionService.PlantInfo plantInfo) {
            progressDialog.dismiss();
            if (plantInfo == null || plantInfo.getResult().size() == 0) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_error)
                        .setMessage(R.string.recognize_failed)
                        .create()
                        .show();
                return;
            }

            StringBuilder sb = new StringBuilder();
            PlantRecognitionService.Result r = plantInfo.getResult().get(0);

            if (r.getScore() < 0.5f) sb.append(getResources().getString(R.string.may_be));
            sb.append(r.getName());

            String description = null;
            if (r.getBaike_info() != null) description = r.getBaike_info().getDescription();
            if (description == null) description = " ";

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(sb)
                    .setMessage(description)
                    .create()
                    .show();
        }

        @Override
        protected PlantRecognitionService.PlantInfo doInBackground(Void... voids) {
            return plantRecognize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        switch (requestCode) {
            case RESULT_CAMERA:
                try {
                    imageView.setImageBitmap(BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Get Camera Image Filed", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case RESULT_ALBUM:
                displayImage(data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getAlbumImage() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    private void displayImage(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, R.string.get_image_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_ALBUM);
    }

    private void getCameraImage() {
        try {
            File outputImage = new File(getExternalCacheDir(), BITMAP_FILE_NAME);
            if (outputImage.exists()) outputImage.delete();
            outputImage.createNewFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.hb.android.fileprovider", outputImage);
            } else {
                imageUri = Uri.fromFile(outputImage);
            }
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, RESULT_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Get Camera Image Filed", Toast.LENGTH_SHORT).show();
        }
    }

    private PlantRecognitionService.PlantInfo plantRecognize() {
        String img = null;
        try {
            Bitmap bitmap = scaleBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            img = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (img == null) return null;


        PlantRecognitionService.PlantInfo info = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                Call<PlantRecognitionService.PlantInfo> call = plantRecognize.plantRecognition(img);
                Response<PlantRecognitionService.PlantInfo> r = call.execute();
                info = r.body();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                info = null;
            }
        }
        if (info == null) return null;

        Collections.sort(info.getResult(), (o1, o2) -> (int) (o2.getScore() - o1.getScore()));

        return info;
    }

    private Bitmap scaleBitmap() {
        imageView.setDrawingCacheEnabled(true);
        Bitmap bm = imageView.getDrawingCache();
        int width = bm.getWidth();
        int height = bm.getHeight();
        int size = width > height ? width : height;
        float scale = 1f;
        if (size > MAX_SIZE) scale = MAX_SIZE / size;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        imageView.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
