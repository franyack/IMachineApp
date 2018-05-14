package com.example.fran.imachineapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.codekidlabs.storagechooser.StorageChooser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView path_chosen;
    String[] imagespath;

    Vector<String> images = new Vector<>();

    String strPath = "";
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
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "clusterResult");
        if (folder.exists()){
            try {
                FileUtils.deleteDirectory(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
    public void alert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Atención!");
        builder.setIcon(R.drawable.warning_black);
        builder.setMessage("Este proceso puede ocasionar que la pantalla se ponga en negro durante unos segundos.\n\nAguarde por favor.");
       final AlertDialog dialog = builder.create();
       dialog.show();
       Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    public void procesarImagenes(View view) {
        //TODO: usar logger -> averiguar como se hace en android -> util.log
        if (!prepararImagenes()){
            Toast.makeText(getApplicationContext(),"Debe seleccionar un directorio a procesar", Toast.LENGTH_SHORT).show();
            return;
        }
//        Toast.makeText(getApplicationContext(),R.string.processing_toast, Toast.LENGTH_LONG).show();
        alert();
        setContentView(R.layout.working);
        TextView texto = findViewById(R.id.workingTexto);
        String setearTexto = "Procesando " + images.size() +  " imagenes, aguarde por favor…";
        texto.setText(setearTexto);
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
