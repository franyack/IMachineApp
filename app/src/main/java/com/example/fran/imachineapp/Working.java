package com.example.fran.imachineapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Created by fran on 02/04/18.
 */

public class Working extends Activity {

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    String[] imagesPath;
    String[] result;

    int numberImages;
    Vector<String> vImages = new Vector<>();
    Vector<Integer> vClusters = new Vector<>();
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
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
        numberImages = bundle.getInt("imagesSize");

        initTensorFlowAndLoadModel();

    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE);
                    setParameters();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
    @Override
    protected synchronized void onResume() {
        super.onResume();
        setContentView(R.layout.working);
        TextView texto = findViewById(R.id.workingTexto);
        String setearTexto = "Procesando " + numberImages +  " imagenes, aguarde por favor…";
        //TODO: progressBar mientras va progesando
        texto.setText(setearTexto);
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void setParameters() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                TextView texto = findViewById(R.id.workingTexto);
//                String setearTexto = "Procesando " + numberImages +  " imagenes, aguarde por favor…";
//                //TODO: progressBar mientras va progesando
//                texto.setText(setearTexto);
                if (vImages.size()>0){
                    vImages.clear();
                }
                if (vClusters.size()>0){
                    vClusters.clear();
                }
//                Toast.makeText(getApplicationContext(),"TensorFlow Lite Working!", Toast.LENGTH_SHORT).show();
                procesarImagenes();
            }
        });
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
//        result = imgProcess2(imagesPath);
        result = new String[imagesPath.length];
        for (int i = 0; i< imagesPath.length; i++){
            Bitmap image = lessResolution(imagesPath[i],224,224);
            image = Bitmap.createScaledBitmap(image,224,224,false);
            final List<Classifier.Recognition> results = classifier.recognizeImage(image);
            int randomNum = ThreadLocalRandom.current().nextInt(1,11);
            result[i] = results.toString() + "->" + randomNum;
        }
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

    public static Bitmap lessResolution (String filePath, int width, int height){
        int reqHeight = height;
        int reqWidth = width;
        BitmapFactory.Options options = new BitmapFactory.Options();

        //First decode with inJustDecodeBounds = true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath,options);

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth){
            //Calculate ratios of height and width to requested heigth and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            //Choose the smallest ratio as inSampleSie value, this will guarantee
            //a final image with both dimensions larger than or equal to the requested
            //height and width
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}