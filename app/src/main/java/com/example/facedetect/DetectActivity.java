package com.example.facedetect;


import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.PointF;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.FaceDetector;
import com.mindorks.paracamera.Camera;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS;
import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;

public class DetectActivity extends AppCompatActivity {

    Camera camera;
    float xpos,ypos,width,height;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        Button btn = (Button) findViewById(R.id.btn);
       // dispatchTakePictureIntent();


        //Face Detection API



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Build the camera
                camera = new Camera.Builder()
                        .resetToCorrectOrientation(true)// it will rotate the camera bitmap to the correct orientation from meta data
                        .setTakePhotoRequestCode(1)
                        .setDirectory("pics")
                        .setName("ali_" + System.currentTimeMillis())
                        .setImageFormat(Camera.IMAGE_JPEG)
                        .setCompression(75)
                        .setImageHeight(1000)// it will try to achieve this height as close as possible maintaining the aspect ratio;
                        .build(DetectActivity.this);

                try {
                    camera.takePicture();
                }catch (Exception e){
                    e.printStackTrace();
                }



                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable=true;
                Bitmap myBitmap = BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        R.drawable.test,
                        options);*/






            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            Bitmap myBitmap = camera.getCameraBitmap();
            if(myBitmap != null) {

                final ImageView imageView2 = (ImageView) findViewById(R.id.test);

                //imageView2.setImageBitmap(myBitmap);
                Paint myRectPaint = new Paint();
                myRectPaint.setStrokeWidth(5);
                myRectPaint.setColor(Color.RED);
                myRectPaint.setStyle(Paint.Style.STROKE);

                Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(true).setClassificationType(ALL_CLASSIFICATIONS)
                        .setMode(FAST_MODE)
                        .build();
                if(!faceDetector.isOperational()){
                    return;
                }


                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);

                for(int i=0; i<faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    float smile = thisFace.getIsSmilingProbability();
                    float lefteye = thisFace.getIsLeftEyeOpenProbability();
                    float righteye = thisFace.getIsRightEyeOpenProbability();


                    TextView textView = findViewById(R.id.details);
                    String details = "Smiling Probability: "+ smile + "\n   Right Eye Open Probability "+ righteye + "\n    Left Eye Open Probability: "+ lefteye;
                    textView.setText(details);

                    //for POST
                     xpos = x1;
                     ypos = y1;
                     width = x2;
                     height = y2;

                    tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);


                }
                imageView2.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
            //end of detection process


            }else{
                Toast.makeText(this.getApplicationContext(),"Picture not taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menudetect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("POST Details");
            // set the custom layout
            final View customLayout = getLayoutInflater().inflate(R.layout.detailsdialog, null);
            builder.setView(customLayout);

            builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO SEND JSON POST REQUEST
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.show();
            
        }
        return super.onOptionsItemSelected(item);
    }

}
