package com.mad.poleato;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * This class is to used for image downloading from FireBase.
 * It can be called in another thread
 */
public class ImageDownloader implements Runnable {
    private String url;
    private Bitmap img;
    private ConditionVariable downloadFinished;




    public ImageDownloader(String url){
        this.url = url;
        this.img = null;
        downloadFinished = new ConditionVariable();
    }


    @Override
    public void run() {
        //StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        //StorageReference photoReference = FirebaseStorage.getInstance().getReference().child("R00/ProfileImage/img.jpg");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child("R00"+"/ProfileImage/img.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // set the downloaded image
                img = bmp;
                downloadFinished.open();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //set predefined image
                Log.d("matte", "onFailure() called in ImageDownloader");
                img = null;
                downloadFinished.open();
            }
        });
    }


    public Bitmap getValue() throws InterruptedException {
        downloadFinished.block();

        return this.img;
    }

}
