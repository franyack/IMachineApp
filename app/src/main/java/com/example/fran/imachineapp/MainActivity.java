package com.example.fran.imachineapp;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codekidlabs.storagechooser.StorageChooser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {
    TextView path_chosen;
    Message messages;
    JSONArray images;
    int cont;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private static final String[] INITIAL_PERMS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int INITIAL_REQUEST = 1337;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int imgProcess(long inputMat, long imageGray);

    public void chooseGallery(View view) {
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(MainActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();

        // Show dialog whenever you want by
        chooser.show();

        // get path that the user has chosen
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
              path_chosen = (TextView) findViewById(R.id.path_chosen);
              path_chosen.setText(path);

            }
        });
    }

    private void getAllFiles(File curDir, JSONArray images){
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                getAllFiles(f, images);
            if(f.isFile()){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("img"+cont,f.getPath());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                images.put(jsonObject);
                cont++;
            }
        }
    }

    public void procesarImagenes(View view) {
        File curDir = new File((String) path_chosen.getText());
        cont = 0;
        images  = new JSONArray();
        getAllFiles(curDir,images);
        JSONObject imagesObj = new JSONObject();
        try {
            imagesObj.put("images",images);
            writeToFile(imagesObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("images.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void writeJson(View view){
//        IOHelper.writeToFile(this,"images.txt", images);
//    }
//
//    public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
//        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
//        writer.setIndent("  ");
//        writeMessagesArray(writer, messages);
//        writer.close();
//    }
//
//    public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
//        writer.beginArray();
//        for (Message message : messages) {
//            writeMessage(writer, images);
//        }
//        writer.endArray();
//    }
//
//    public void writeMessage(JsonWriter writer, String[] images) throws IOException {
//        writer.beginObject();
//        for (int i=0;i<images.length;i++){
//            writer.name("img"+i).value(images[i]);
//        }
//        writer.endObject();
//    }
}
