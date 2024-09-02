package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class OutlinedTextView extends AppCompatTextView {

    private int outlineColor = 0xFF000000; // Black outline
    private float outlineWidth = 2f; // Adjust this value to change the outline thickness

    public OutlinedTextView(Context context) {
        super(context);
    }

    public OutlinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setStyle(Paint.Style.FILL);

        int originalColor = getCurrentTextColor();
        String text = getText().toString();
        float x = getCompoundPaddingLeft();
        float y = getBaseline();

        // Draw the outline for ASCII and extended ASCII characters
        paint.setStrokeWidth(outlineWidth);
        paint.setColor(outlineColor);
        paint.setStyle(Paint.Style.STROKE);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                drawTextWithOutline(canvas, text, x + i * outlineWidth, y + j * outlineWidth, paint);
            }
        }

        // Draw the original text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(originalColor);
        canvas.drawText(text, x, y, paint);
    }

    private void drawTextWithOutline(Canvas canvas, String text, float x, float y, TextPaint paint) {
        StringBuilder outlineText = new StringBuilder();
        float[] textWidths = new float[text.length()];
        paint.getTextWidths(text, textWidths);

        float currentX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isAsciiOrExtendedAscii(c)) {
                outlineText.append(c);
            } else {
                if (outlineText.length() > 0) {
                    canvas.drawText(outlineText.toString(), currentX, y, paint);
                    outlineText.setLength(0);
                }
                currentX += textWidths[i];
            }
        }

        if (outlineText.length() > 0) {
            canvas.drawText(outlineText.toString(), currentX, y, paint);
        }
    }

    private boolean isAsciiOrExtendedAscii(char c) {
        return c <= 255;
    }

    public void setOutlineColor(int color) {
        this.outlineColor = color;
        invalidate();
    }

    public void setOutlineWidth(float width) {
        this.outlineWidth = width;
        invalidate();
    }
}