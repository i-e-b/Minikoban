package ib.mkb;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("ViewConstructor")
public class Select extends View {
    private final Paint mPaint = new Paint();
    private final Main parent;

    private float upX, upY; // touch lift position

    private int selectedLevel;

    private boolean darkColors; // night mode if true.

    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    public Select(final Main context) {
        super(context);
        parent = context;

        SharedPreferences pref = parent.getSharedPreferences("scores", Context.MODE_PRIVATE);
        selectedLevel = pref.getInt("lastLvl", 0);

        // Check for dark mode.
        int uiMode = getResources().getConfiguration().uiMode;
        if ((uiMode & UI_MODE_NIGHT_YES) > 0){
            darkColors = true;
        } else if ((uiMode & UI_MODE_NIGHT_NO) > 0){
            darkColors = false;
        }

        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawForeground(final Canvas canvas) {
        lastWidth = canvas.getWidth();
        lastHeight = canvas.getHeight();


        int c1 = 200, c3 = 70;
        // clear background
        if (darkColors){
            c1 = 50; c3 = 220;
        }
        canvas.drawARGB(255, c1,c1,c1);
        mPaint.setARGB(255, c3,c3,c3);

        Rect rect = new Rect();
        mPaint.setTextSize(400);

        // up arrow (previous level)
        String txt = "⬆️";
        mPaint.getTextBounds(txt, 0,txt.length(), rect);
        float h = rect.bottom - rect.top;
        float w = (lastWidth - rect.right - rect.left) / 2.0f;
        canvas.drawText(txt, w, h, mPaint);

        // Level number
        txt = ""+(selectedLevel+1);
        mPaint.getTextBounds(txt, 0, txt.length(), rect);
        h = (lastHeight - rect.bottom - rect.top) / 2.0f;
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        float subH = h + rect.height();
        canvas.drawText(txt, w, h, mPaint);

        // down arrow (next level)
        txt = "⬇️";
        mPaint.getTextBounds(txt, 0,txt.length(), rect);
        h = rect.bottom;
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        canvas.drawText(txt, w, lastHeight - h, mPaint);

        // get best score (if any)
        mPaint.setTextSize(80);
        SharedPreferences pref = parent.getSharedPreferences("scores", Context.MODE_PRIVATE);
        String levelName = ""+selectedLevel;
        int best = pref.getInt(levelName, 0);
        txt = "Not beaten yet";
        if (best > 0){
            txt = "Best score: "+best;
        }
        mPaint.getTextBounds(txt, 0, txt.length(), rect);
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        canvas.drawText(txt, w, subH, mPaint);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            upX = event.getX();
            upY = event.getY();
            touchLift();
        }

        invalidate(); // draw a frame
        return true; // event handled
    }


    private void touchLift() {
        float w3 = lastWidth / 3.0f;
        float h3 = lastHeight / 3.0f;

        if (upX >= w3 && upX <= 2*w3) {
            if (upY <= h3) selectedLevel--;
            else if (upY >= 2*h3) selectedLevel++;
            else parent.switchToLevel(selectedLevel);
        }
        if (selectedLevel < 0) selectedLevel = 0;
        if (selectedLevel > 96) selectedLevel = 96;

        // save the preference
        SharedPreferences pref = parent.getSharedPreferences("scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = pref.edit();
        e.putInt("lastLvl", selectedLevel);
        e.apply();
    }
}
