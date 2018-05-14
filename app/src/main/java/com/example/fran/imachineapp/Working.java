package com.example.fran.imachineapp;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.ejml.data.DMatrixRMaj;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final String LABEL_PATH = "labels2.txt";
    private static final String WORDS_PATH = "words.txt";
    private static final String HIERARCHY_PATH = "wordnet.is_a.txt";
    private static final int INPUT_SIZE = 224;
    imagenet wnid_lookup = new imagenet();
    List<String> label_lookup = new ArrayList<>();
    String[] imagesPath;
    String[] result;
    List<Predictions> predictions = new ArrayList<Predictions>();
    List<Top_Predictions> top_predictions = new ArrayList<>();
    double[][] g_aff_matrix;
    float[] predic = new float[1000];
    int numberImages;
    Vector<String> vImages = new Vector<>();
    Vector<Integer> vClusters = new Vector<>();
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    public class Predictions{
        String img_path;
        float[] probs = new float[1000];

        public Predictions(String img_path, float[] probs){
            this.img_path = img_path;
            this.probs = probs;
        }

        public String getImgPath(){
            return img_path;
        }

        public float[] getProbs() {
            return probs;
        }
    }

    public class Top_Predictions{
        String img_path;
        List<wnIdPredictions> result;

        public Top_Predictions(String s, List<wnIdPredictions> wnIdPredictions) {
            img_path = s;
            result = wnIdPredictions;
        }


        public String getImg_path() {
            return img_path;
        }

        public List<wnIdPredictions> getResult() {
            return result;
        }
    }

    public class wnIdPredictions{
        String wnId;
        float prediction;
        public wnIdPredictions(String wnId, float prediction){
            this.wnId = wnId;
            this.prediction = prediction;
        }

        public String getWnId() {
            return wnId;
        }

        public float getPrediction() {
            return prediction;
        }

        public void setPrediction(float prediction) {
            this.prediction = prediction;
        }
    }

    public native String[] imgProcess2(String[] images);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.working);

        Bundle bundle = getIntent().getExtras();
        imagesPath = bundle.getStringArray("imagesPath");
        numberImages = bundle.getInt("imagesSize");

        try {
            wnid_lookup.wnidWordsList = wnid_lookup.loadWnIDWords(getAssets(),WORDS_PATH);
            label_lookup = TensorFlowImageClassifier.loadLabelList(getAssets(),LABEL_PATH);
            wnid_lookup.hierarchyLookupList = wnid_lookup.loadHierarchy_lookup(getAssets(),HIERARCHY_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
//        setContentView(R.layout.working);
//        TextView texto = findViewById(R.id.workingTexto);
//        String setearTexto = "Procesando " + numberImages +  " imagenes, aguarde por favor…";
//        //TODO: progressBar mientras va progesando
//        texto.setText(setearTexto);
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
//            for (int j=0; j<1000;j++){
//                predic[i] = 0;
//            }
//            for (int j=0; j<results.size(); j++){
//                predic[Integer.parseInt(results.get(j).getId())] = results.get(j).getConfidence();
//            }
//            predictions.add(new Predictions(imagesPath[i],predic));
            List<wnIdPredictions> wnIdPredictionsList;
            wnIdPredictionsList = process_top_predictions(results,4,0.5);
            top_predictions.add(new Top_Predictions(imagesPath[i], wnIdPredictionsList));


//            int randomNum = ThreadLocalRandom.current().nextInt(1,11);
//            result[i] = results.toString() + "->" + randomNum;
        }
        g_aff_matrix = get_grammatical_affinity(top_predictions);

        DMatrixRMaj cluster_matrix = new DMatrixRMaj(g_aff_matrix);

        int maxIt = 100;
        int expPow = 2;
        int infPow = 2;
        double epsConvergence = 1e-3;
        double threshPrune = 0.01;
        int n = 100;
        int seed = 1234;

        MCLDenseEJML mcl = new MCLDenseEJML(maxIt, expPow, infPow, epsConvergence, threshPrune);


        cluster_matrix = mcl.run(cluster_matrix);

        ArrayList<ArrayList<Integer>> clusters = mcl.getClusters(cluster_matrix);


//        while (!imgsProcessed){continue;} // TODO: sleep para no comer tanto procesamiento?
//        for (String s:result){
//            int positionOfCluster = s.lastIndexOf("->");
//            String image = s.substring(0,positionOfCluster);
//            Integer clust = Integer.parseInt(s.substring(positionOfCluster+2));
//            vImages.add(image);
//            vClusters.add(clust);
//        }
        for (int i=0;i<imagesPath.length;i++){
            vImages.add(imagesPath[i]);
            for (int j=0;j<clusters.size();j++){
                for (int k=0;k<clusters.get(j).size();k++){
                    if(clusters.get(j).get(k) == i){
                        vClusters.add(j);
                        break;
                    }
                }
            }
        }
        Intent i = new Intent(this, Results.class);
        i.putExtra("vImages",vImages);
        i.putExtra("vClusters", vClusters);
        startActivity(i);
    }

    private double[][] get_grammatical_affinity(List<Top_Predictions> top_predictions) {
        double[][] result = new double[top_predictions.size()][top_predictions.size()];
        List<String> dictionary = new ArrayList<>();
        boolean add;
        int d;
        for (int i=0;i<top_predictions.size();i++){
            for (int j=0; j<top_predictions.get(i).getResult().size();j++){
               add=true;
               if(dictionary.size() == 0){
                   dictionary.add(top_predictions.get(i).getResult().get(j).getWnId());
               }else{
                   d=0;
                   while (d<dictionary.size()){
                       if(dictionary.get(d).equals(top_predictions.get(i).getResult().get(j).getWnId())){
                          add=false;
                          break;
                       }
                       d+=1;
                   }
                   if(add){
                       dictionary.add(top_predictions.get(i).getResult().get(j).getWnId());
                   }
               }
            }
        }



        double[] aff_row;
        double[] v1,v2;
        double v1_s,v2_s;
        double corr;
        for(int i =0;i<top_predictions.size();i++){
            aff_row = new double[top_predictions.size()];
            corr=0;
            v1 = new double[dictionary.size()];
            for(int j=0;j<top_predictions.get(i).getResult().size();j++){
                d=0;
                while(d<dictionary.size()){
                    if(dictionary.get(d).equals(top_predictions.get(i).getResult().get(j).getWnId())){
                        v1[d]=top_predictions.get(i).getResult().get(j).getPrediction();
                    }
                    d+=1;
                }
            }
            v1_s=0;
            for (int r=0;r<v1.length;r++){
                v1_s += v1[r];
            }
            for (int r=0;r<v1.length;r++){
                v1[r] = v1[r]/v1_s;
            }
            for(int k=0;k<top_predictions.size();k++){
                v2= new double[dictionary.size()];
                for(int j=0;j<top_predictions.get(k).getResult().size();j++){
                    d=0;
                    while(d<dictionary.size()){
                        if(dictionary.get(d).equals(top_predictions.get(k).getResult().get(j).getWnId())){
                            v2[d]=top_predictions.get(k).getResult().get(j).getPrediction();
                        }
                        d+=1;
                    }
                }
                v2_s=0;
                for (int r=0;r<v2.length;r++){
                    v2_s += v2[r];
                }
                for (int r=0;r<v2.length;r++){
                    v2[r] = v2[r]/v2_s;
                }
                corr = new PearsonsCorrelation().correlation(v2,v1);
                corr = (corr + 1)/2.0; //Normalize output
                if(corr>0.65){
                    corr = corr;
                }else{
                    corr = 0;
                }
                aff_row[k]=corr;
            }
            for(int j=0;j<top_predictions.size();j++){
                result[i][j]=aff_row[j];
            }
        }
        return result;
    }

    private List<wnIdPredictions> process_top_predictions(List<Classifier.Recognition> results, int depth, double thresh_prob) {
        List<wnIdPredictions> predictions = new ArrayList<>();
        String wnid;
        ArrayList<String> full_hiearchy;
        for (int i = 0; i< results.size(); i++){
            wnid = wnid_lookup.get_wnid_from_label(results.get(i).getTitle(),wnid_lookup.wnidWordsList);
            full_hiearchy = wnid_lookup.get_full_hierarchy(wnid,4,wnid_lookup.hierarchyLookupList);
            for (int j=0;j<full_hiearchy.size();j++){
                predictions.add(new wnIdPredictions(full_hiearchy.get(j), results.get(i).getConfidence()));
            }
        }
        List<wnIdPredictions> result = new ArrayList<>();

        result.add(new wnIdPredictions(predictions.get(0).getWnId(),predictions.get(0).getPrediction()));

        for (int i=1; i<predictions.size();i++){
            int d = 0;
            boolean add = true;
            while (d<result.size()){
               if(predictions.get(i).getWnId().equals(result.get(d).getWnId())){
                   result.get(d).setPrediction(result.get(d).getPrediction()+predictions.get(i).getPrediction());
                   add = false;
                   break;
               }
               d+=1;
            }
            if(add){
                result.add(new wnIdPredictions(predictions.get(i).getWnId(),predictions.get(i).getPrediction()));
            }
        }

        return result;
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