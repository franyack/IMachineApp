package com.example.fran.imachineapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import es.dmoral.toasty.Toasty;


import static com.example.fran.imachineapp.R.string.processing_toast;

public class MainActivity extends AppCompatActivity {

    TextView path_chosen;
    String[] imagespath;
    String[] result;
    Vector<String> images = new Vector<>();
    Vector<String> vImages = new Vector<>();
    Vector<Integer> vClusters = new Vector<>();
    Vector<Integer> vClustersResult = new Vector<>();
    String strPath = "";
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

    public native String[] imgProcess2(String[] images);

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
    public void prepararImagenes(){
        File curDir;
        CheckBox checkBox = findViewById(R.id.checkTodasLasImagenes);
        if (checkBox.isChecked()){
            curDir = new File("/storage/emulated/0");
        }else{
            curDir = new File((String) path_chosen.getText());
        }
        if (images.size()>0){
            images.clear();
        }
        getAllFiles(curDir);
        setContentView(R.layout.working);
        TextView texto = findViewById(R.id.workingTexto);
        String setearTexto = "Procesando " + images.size() +  " imagenes, aguarde por favor…";
        //TODO: progressBar mientras va progesando
        texto.setText(setearTexto);
        imagespath = new String[images.size()];
        for (int i = 0; i<images.size(); i++){
            imagespath[i] = images.get(i);
        }
    }


    public void procesarImagenes(View view) {
        //TODO: usar logger -> averiguar como se hace en android -> util.log
//        Toast.makeText(getApplicationContext(),R.string.processing_toast, Toast.LENGTH_LONG).show();
//        Toasty.info(getApplicationContext(), "Procesando las imagenes, aguerde un momento por favor...", Toast.LENGTH_L, true).show();
        prepararImagenes();

        if (vImages.size()>0){
            vImages.clear();
        }
        if (vClusters.size()>0){
            vClusters.clear();
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO: buscar syncronize
                synchronized (MainActivity.this){
                    result = imgProcess2(imagespath);
                }
            }
        };
        thread.start();
        while (result == null){continue;}
        for (String s:result){
            int positionOfCluster = s.lastIndexOf("->");
            String image = s.substring(0,positionOfCluster);
            Integer clust = Integer.parseInt(s.substring(positionOfCluster+2));
            vImages.add(image);
            vClusters.add(clust);
        }
        obtenerClusters(vImages,vClusters);
    }

    private void obtenerClusters(Vector<String> vImages, Vector<Integer> vClusters) {
        if (vClustersResult.size()>0){
            vClustersResult.clear();
        }
        while (!vClusters.isEmpty()) {
            int cant = 1;
            for (int i = 1; i < vClusters.size(); i++) {
                if (vClusters.get(i) == null) {
                    break;
                }
                if (vClusters.get(0) == vClusters.get(i)) {
                    cant++;
                    vClusters.remove(i);
                }
            }
            vClustersResult.add(cant);
            vClusters.remove(0);
        }
        mostrarResultados(vClustersResult);
    }

    private void mostrarResultados(Vector<Integer> vClustersResult) {
        String resu="";
        for (int i=0;i<vClustersResult.size();i++){
            resu=resu+"\n"+"Cluster "+i+": "+vClustersResult.get(i)+" image/s";
        }

        setContentView(R.layout.results);
        TextView res = findViewById(R.id.resultados);
        res.setText(resu);
    }

    public void volverMainActivity(View view) {
        vClustersResult.clear();
        setContentView(R.layout.activity_main);
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
