package com.drawingapp.tryit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;
import java.util.concurrent.Phaser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {


    ImageButton currPaint;
    @BindView(R.id.paint_colors)
    LinearLayout paintLayout;
    @BindView(R.id.canvas)
    CanvasView drawView;
    private float smallBrush, mediumBrush, largeBrush;
    private EventBus eventBus;

    @OnClick(R.id.new_canvas)
    void new_paper() {
        createNewCanvas();
    }

    @OnClick(R.id.erase)
    void erase() {
        brushSizeDialog(true);
    }

    @OnClick(R.id.brush)
    void draw() {
        brushSizeDialog(false);
    }

    @OnClick(R.id.save)
    void save() {
        saveImage();
    }

//    @OnClick(R.id.undo)
//    void undo() {
//        EventBus.getDefault().postSticky(new UndoEvent());
//    }
//
//    @OnClick(R.id.redo)
//    void redo() {
//        EventBus.getDefault().postSticky(new RedoEvent());
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        drawView.setColor(currPaint.getTag().toString());

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
    }


    public void paintClicked(View view) {
        if (view != currPaint) {
            ImageView imgView = (ImageView) view;
            String color = view.getTag().toString();

            drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }


    private void saveImage() {
        //save drawing
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle(R.string.save_drawing_title);
        saveDialog.setMessage(R.string.save_drawing_question);
        saveDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                drawView.setDrawingCacheEnabled(true);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                drawView.destroyDrawingCache();
                dialog.cancel();
            }
        });
        saveDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            "image-"+ UUID.randomUUID(), "drawing");

                    if (imgSaved != null) {
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                R.string.image_saved, Toast.LENGTH_SHORT);
                        savedToast.show();


                    } else {
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                R.string.image_saving_error, Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void createNewCanvas() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle(R.string.new_canvas_title);
        newDialog.setMessage(R.string.new_canvas_question);
        newDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                drawView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    private void brushSizeDialog(final boolean isErase) {
        final Dialog brushSizeDialog = new Dialog(this);

        brushSizeDialog.setContentView(R.layout.brush_chooser);
        ((TextView) brushSizeDialog.findViewById(R.id.title)).setText(isErase ? R.string.erase_size : R.string.brush_size);
        brushSizeDialog.show();

        ImageButton smallBtn = (ImageButton) brushSizeDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(isErase);
                drawView.setBrushSize(smallBrush);
                drawView.setLastBrushSize(smallBrush);
                brushSizeDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton) brushSizeDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(isErase);
                drawView.setBrushSize(mediumBrush);
                drawView.setLastBrushSize(mediumBrush);
                brushSizeDialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton) brushSizeDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(isErase);
                drawView.setBrushSize(largeBrush);
                drawView.setLastBrushSize(largeBrush);
                brushSizeDialog.dismiss();
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
//        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setContentView(R.layout.activity_main);
//            Log.e("On Config Change","PORTRAIT");
//        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            setContentView(R.layout.activity_main_landscape);
//            Log.e("On Config Change","LANDSCAPE");
//        }
    }


}
