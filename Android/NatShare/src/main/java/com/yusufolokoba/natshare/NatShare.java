package com.yusufolokoba.natshare;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import com.unity3d.player.UnityPlayer;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * NatShare
 * Created by yusuf on 4/16/18.
 */
public class NatShare {

    static {
        // Disable the FileUriExposedException from being thrown on Android 24+
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static boolean shareVideo (String path) {
        File file = new File(path);
        if (!file.exists()) return false;
        UnityPlayer.currentActivity.startActivity(Intent.createChooser(
                new Intent()
                        .setAction(Intent.ACTION_SEND)
                        .setType("video/mp4")
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)),
                "Share media"
        ));
        return true;
    }

    public static boolean saveVideoToCameraRoll (String path) {
        File file = new File(path);
        if (!file.exists()) return false;
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, file.getName());
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, path);
        UnityPlayer.currentActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        return true;
    }

    public static Object getThumbnail (String path, float time) {
        final class Thumbnail { ByteBuffer pixelBuffer; int width, height; boolean isLoaded () {return width > 0;} }
        // Load frame
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        Bitmap rawFrame = retriever.getFrameAtTime((long)(time * 1e+6f));
        retriever.release();
        if (rawFrame == null) return new Thumbnail();
        // Invert
        final Matrix invert = new Matrix();
        invert.postScale(1, -1, rawFrame.getWidth() / 2.f, rawFrame.getHeight() / 2.f);
        Bitmap frame = Bitmap.createBitmap(rawFrame, 0, 0, rawFrame.getWidth(), rawFrame.getHeight(), invert, true);
        rawFrame.recycle();
        // Extract pixel data
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.width = frame.getWidth();
        thumbnail.height = frame.getHeight();
        thumbnail.pixelBuffer = ByteBuffer.allocate(frame.getByteCount());
        frame.copyPixelsToBuffer(thumbnail.pixelBuffer);
        frame.recycle();
        return thumbnail;
    }
}