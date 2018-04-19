package com.example.fran.imachineapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by fran on 02/04/18.
 */

public class Results extends Activity {

    ArrayList<String> vImages = new ArrayList<>();
    ArrayList<Integer> vClusters = new ArrayList<>();
    Vector<Integer> vClustersResult = new Vector<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Bundle bundle = getIntent().getExtras();

        vImages = (ArrayList<String>) getIntent().getSerializableExtra("vImages");
        vClusters = (ArrayList<Integer>) getIntent().getSerializableExtra("vClusters");

        obtenerClusters(vImages,vClusters);
    }

    private void obtenerClusters(ArrayList<String> vImages, ArrayList<Integer> vClusters) {
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
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
