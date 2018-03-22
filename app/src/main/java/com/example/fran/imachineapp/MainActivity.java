package com.example.fran.imachineapp;

import android.Manifest;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.codekidlabs.storagechooser.StorageChooser;
import java.io.File;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    TextView path_chosen;
    String[] imagespath;
    String[] result;
    Vector<String> images = new Vector<>();
    Vector<String> vImages = new Vector<>();
    Vector<Integer> vClusters = new Vector<>();
    Vector<Integer> vClustersResult = new Vector<>();
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
              path_chosen.setText(path);

            }
        });
    }

    private void getAllFiles(File curDir){
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                getAllFiles(f);
            if(f.isFile()){
                if (f.getAbsolutePath().contains(".jpg") || f.getAbsolutePath().contains(".gif") || f.getAbsolutePath().contains(".bmp")
                        || f.getAbsolutePath().contains(".jpeg") || f.getAbsolutePath().contains(".tif") || f.getAbsolutePath().contains(".tiff")
                        || f.getAbsolutePath().contains(".png")){
                    images.add(f.getAbsolutePath());
                    if (images.size()==20){
                        break;
                    }
                }

            }
        }
    }

    public void procesarImagenes(View view) {
        File curDir = new File((String) path_chosen.getText());
        getAllFiles(curDir);
        imagespath = new String[images.size()];
        for (int i = 0; i<images.size(); i++){
            imagespath[i] = images.get(i);
        }
        result = imgProcess2(imagespath);
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
        setContentView(R.layout.activity_main);
    }
}
