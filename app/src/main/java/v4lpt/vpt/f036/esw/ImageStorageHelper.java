package v4lpt.vpt.f036.esw;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ImageStorageHelper {
    private static final String TAG = "ImageStorageHelper";

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            // Read the entire input stream into a byte array
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageData = byteBuffer.toByteArray();

            // Read the EXIF data
            ExifInterface exif = new ExifInterface(new ByteArrayInputStream(imageData));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Decode the image
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            // Rotate the bitmap if needed
            Bitmap rotatedBitmap = rotateBitmapIfNeeded(originalBitmap, orientation);

            String fileName = "event_image_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(context.getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Recycle bitmaps to free up memory
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            rotatedBitmap.recycle();

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    private static Bitmap rotateBitmapIfNeeded(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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