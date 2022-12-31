package ib.mkb;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;

@SuppressLint("ViewConstructor")
public class Select extends BaseView {
    private final Paint mPaint = new Paint();
    private final Main parent;

    private int selectedLevel;

    private final boolean darkColors; // night mode if true.

    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    public Select(final Main context) {
        super(context);
        parent = context;

        selectedLevel = os.getLastLevel(parent);

        // Check for dark mode.
        darkColors = os.isDarkMode(this);

        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawForeground(final Canvas canvas) {
        lastWidth = canvas.getWidth();
        lastHeight = canvas.getHeight();

        int size = Math.min(400, lastHeight / 5);

        int c1 = 200, c3 = 70;
        // clear background
        if (darkColors){
            c1 = 50; c3 = 220;
        }
        canvas.drawARGB(255, c1,c1,c1);
        os.setGrey(mPaint, c3);

        Rect rect = new Rect();
        os.setSize(mPaint,size);

        // up arrow (previous level)
        String txt = "⬆️";
        os.measureText(mPaint, txt, rect);
        float h = rect.bottom - rect.top;
        float w = (lastWidth - rect.right - rect.left) / 2.0f;
        os.drawText(canvas,txt, w, h, mPaint);

        // Level number
        txt = ""+(selectedLevel+1);
        os.measureText(mPaint, txt, rect);
        h = (lastHeight - rect.bottom - rect.top) / 2.0f;
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        float subH = h + rect.height();
        os.drawText(canvas,txt, w, h, mPaint);

        // down arrow (next level)
        txt = "⬇️";
        os.measureText(mPaint, txt, rect);
        h = rect.bottom;
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        os.drawText(canvas,txt, w, lastHeight - h, mPaint);

        // get best score (if any)
        os.setSize(mPaint,80);
        int best = os.getScore(parent, selectedLevel);
        txt = "Not beaten yet";
        if (best > 0){
            txt = "Best score: "+best;
        }
        os.measureText(mPaint, txt, rect);
        w = (lastWidth - rect.right - rect.left) / 2.0f;
        os.drawText(canvas,txt, w, subH, mPaint);
    }

    @Override
    public boolean motionEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;

        float upX = event.getX();
        // touch lift position
        float upY = event.getY();
        float w3 = lastWidth / 3.0f;
        float h3 = lastHeight / 3.0f;

        if (!(upX >= w3) || !(upX <= 2 * w3)) return true;

        if (upY <= h3) levelUp();
        else if (upY >= 2*h3) levelDown();
        else select();

        return true; // event handled
    }

    @Override
    public boolean keyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) return true;
        int keyCode = event.getKeyCode();
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                select();
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:{
                levelDown();
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_UP:{
                levelUp();
                return true;
            }
        }
        return false;
    }

    private void levelUp(){
        selectedLevel--;
        if (selectedLevel < 0) selectedLevel = 0;
        if (selectedLevel > 96) selectedLevel = 96;
        os.setLastLevel(parent, selectedLevel);
        invalidate();
    }
    private void levelDown(){
        selectedLevel++;
        if (selectedLevel < 0) selectedLevel = 0;
        if (selectedLevel > 96) selectedLevel = 96;
        os.setLastLevel(parent, selectedLevel);
        invalidate();
    }
    private void select(){
        parent.switchToLevel(selectedLevel);
    }
}
