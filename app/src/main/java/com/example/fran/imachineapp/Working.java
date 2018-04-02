package com.example.fran.imachineapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by fran on 02/04/18.
 */

public class Working extends Activity {

    String[] imagesPath;
    String[] result;
    boolean imgsProcessed;
    Vector<String> vImages = new Vector<>();
    Vector<Integer> vClusters = new Vector<>();

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    public native String[] imgProcess2(String[] images);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.working);

        Bundle bundle = getIntent().getExtras();
        imagesPath = bundle.getStringArray("imagesPath");

        int numberImages = bundle.getInt("imagesSize");

        TextView texto = findViewById(R.id.workingTexto);
        String setearTexto = "Procesando " + numberImages +  " imagenes, aguarde por favor…";
        //TODO: progressBar mientras va progesando
        texto.setText(setearTexto);

        imgsProcessed = false;

        if (vImages.size()>0){
            vImages.clear();
        }
        if (vClusters.size()>0){
            vClusters.clear();
        }

        procesarImagenes();

    }

    public void procesarImagenes() {

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (Working.this){
//                    result = imgProcess2(imagesPath);
//                    imgsProcessed = true;
//                }
//            }
//        });
//        thread.start();
        result = imgProcess2(imagesPath);
//        while (!imgsProcessed){continue;} // TODO: sleep para no comer tanto procesamiento?
        for (String s:result){
            int positionOfCluster = s.lastIndexOf("->");
            String image = s.substring(0,positionOfCluster);
            Integer clust = Integer.parseInt(s.substring(positionOfCluster+2));
            vImages.add(image);
            vClusters.add(clust);
        }
        Intent i = new Intent(this, Results.class);
        i.putExtra("vImages",vImages);
        i.putExtra("vClusters", vClusters);
        startActivity(i);
    }


}