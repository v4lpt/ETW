package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ImageStorageHelper {
    private static final String TAG = "ImageStorageHelper";

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            String fileName = "event_image_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(context.getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap loadImageFromInternalStorage(String path) {
        return BitmapFactory.decodeFile(path);
    }

    public static void deleteImageFromInternalStorage(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}