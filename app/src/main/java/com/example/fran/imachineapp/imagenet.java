package com.example.fran.imachineapp;

import android.content.res.AssetManager;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by fran on 02/05/18.
 */

public class imagenet {

    public List<wnidWords> wnidWordsList = new ArrayList<>();

    public List<HierarchyLookup> hierarchyLookupList = new ArrayList<>();



    public class wnidWords{
        String wnid;
        String words;

        public wnidWords(String wnid, String word){
            this.wnid = wnid;
            this.words = word;
        }

        public  String getWnid(){
            return wnid;
        }

        public String getWord() {
            return words;
        }
    }

    public List<wnidWords> loadWnIDWords(AssetManager assetManager,String wordsPath) throws IOException {
        List<wnidWords> wnidWordsList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(wordsPath)));
        String line;
        String wnid;
        String words;
        while ((line = reader.readLine()) != null) {
            wnid = line.substring(0,line.lastIndexOf("\t"));
            words = line.substring(line.lastIndexOf("\t")+1);
            wnidWordsList.add(new wnidWords(wnid,words));
        }
        reader.close();
        return wnidWordsList;
    }

    public List<wnidWords> getwniWordsList(){
        return wnidWordsList;
    }

    public class HierarchyLookup{
        String w1;
        String w2;

        public HierarchyLookup(String w1, String w2){
            this.w1 = w1;
            this.w2 = w2;
        }

        public String getW1() {
            return w1;
        }

        public String getW2() {
            return w2;
        }
    }

    public List<HierarchyLookup> loadHierarchy_lookup(AssetManager assetManager, String path_to_hierarchy) throws IOException {
        List<HierarchyLookup> hierarchyLookupList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(path_to_hierarchy)));
        String line;
        String w2;
        String w1;
        while ((line = reader.readLine()) != null) {
            w1 = line.substring(0,line.lastIndexOf(" "));
            w2 = line.substring(line.lastIndexOf(" ")+1);
            hierarchyLookupList.add(new HierarchyLookup(w1,w2));
        }
        reader.close();
        return hierarchyLookupList;
    }

    public String get_wnid_from_label(String label, List<wnidWords> wnidWordsList){
        String result="";
        for (int i=0;i<wnidWordsList.size();i++){
            if (label.equals(wnidWordsList.get(i).getWord())){
                result = wnidWordsList.get(i).getWnid();
                break;
            }
        }
        return result;
    }

    public ArrayList<String> get_full_hierarchy(String wnid, int depth, List<HierarchyLookup> hierarchyLookupList){
        ArrayList<String> results = new ArrayList<>();
        int d=0;
        results.add(wnid);
        while (d<depth){
            for (int i = 0;i<hierarchyLookupList.size();i++){
                if(wnid.equals(hierarchyLookupList.get(i).getW2())){
                    results.add(hierarchyLookupList.get(i).getW1());
                    wnid = hierarchyLookupList.get(i).getW1();
                    break;
                }
            }
            d += 1;
        }

//        double cr = new PearsonsCorrelation().correlation(y,x);

        return results;
    }
}
