package com.example.fran.imachineapp;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.codekidlabs.storagechooser.StorageChooser;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView path_chosen;
    String[] imagespath;

    Vector<String> images = new Vector<>();

    String strPath = "";
//    private Application mClass;
//    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
//    private static final String LABEL_PATH = "labels.txt";
//    private static final int INPUT_SIZE = 224;
//    private Classifier classifier;
//    private Executor executor = Executors.newSingleThreadExecutor();
//    private Button btnChooseImage;
    private static final String[] INITIAL_PERMS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int INITIAL_REQUEST = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mClass = new Application();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }
//        btnChooseImage = findViewById(R.id.btnCarpetaProcesar);

//        initTensorFlowAndLoadModel();
    }

//    private void initTensorFlowAndLoadModel() {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    classifier = TensorFlowImageClassifier.create(
//                            getAssets(),
//                            MODEL_PATH,
//                            LABEL_PATH,
//                            INPUT_SIZE);
//                    makeButtonVisible();
//                } catch (final Exception e) {
//                    throw new RuntimeException("Error initializing TensorFlow!", e);
//                }
//            }
//        });
//    }

//    private void makeButtonVisible() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                btnChooseImage.setVisibility(View.VISIBLE);
//                Toast.makeText(getApplicationContext(),"TensorFlow Lite Working!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                classifier.close();
//            }
//        });
//    }
//
//    public Classifier getClassifier(){
//        return classifier;
//    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
//
//    public native int imgProcess(long inputMat, long imageGray);

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
              path_chosen = findViewById(R.id.path_chosen);
              strPath = path;
              path_chosen.setText(path);

            }
        });
    }

    private void getAllFiles(File curDir){
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory()) {
                getAllFiles(f);
            }else {
                if(f.isFile()){
                    if (images.size()>=50){
                        break;
                    }
                    //TODO: lower path
                    if ((f.getAbsolutePath().contains(".jpg") || f.getAbsolutePath().contains(".gif") || f.getAbsolutePath().contains(".bmp")
                            || f.getAbsolutePath().contains(".jpeg") || f.getAbsolutePath().contains(".tif") || f.getAbsolutePath().contains(".tiff")
                            || f.getAbsolutePath().contains(".png")) && !f.getAbsolutePath().contains("thumbnails")){
                        images.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    public boolean prepararImagenes(){
        File curDir;
        CheckBox checkBox = findViewById(R.id.checkTodasLasImagenes);
        if (path_chosen == null && !checkBox.isChecked()){
            return false;
        }
        if (checkBox.isChecked()){
            curDir = new File("/storage/emulated/0");
        }else{
            curDir = new File((String) path_chosen.getText());
        }
        if (images.size()>0){
            images.clear();
        }
        getAllFiles(curDir);
        imagespath = new String[images.size()];
        for (int i = 0; i<images.size(); i++){
            imagespath[i] = images.get(i);
        }
        return true;
    }


    public void procesarImagenes(View view) {
        //TODO: usar logger -> averiguar como se hace en android -> util.log
        if (!prepararImagenes()){
            Toast.makeText(getApplicationContext(),"Debe seleccionar un directorio a procesar", Toast.LENGTH_SHORT).show();
            return;
        }
//        Toast.makeText(getApplicationContext(),R.string.processing_toast, Toast.LENGTH_LONG).show();
//        setContentView(R.layout.working);
//        TextView texto = findViewById(R.id.workingTexto);
//        String setearTexto = "Procesando " + images.size() +  " imagenes, aguarde por favor…";
//        //TODO: progressBar mientras va progesando
//        texto.setText(setearTexto);
        Intent i = new Intent(this, Working.class);
        i.putExtra("imagesPath",imagespath);
        i.putExtra("imagesSize", images.size());
        startActivity(i);
    }


    public void checkBoxClick(View view) {
        final CheckBox checkBox = findViewById(R.id.checkTodasLasImagenes);
        Button btn = findViewById(R.id.btnCarpetaProcesar);
        TextView textView = findViewById(R.id.path_chosen);
        if (checkBox.isChecked()){
            btn.setEnabled(false);
            textView.setText("");
        }else{
            btn.setEnabled(true);
            textView.setText(strPath);
        }
    }

}
