package ib.mkb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class vw extends View {

    private final Paint mPaint = new Paint();
    private float mx,my;
    private boolean keys;

    private final char[] test = new char[10];

    public vw(final Context context) {
        super(context);

        test[0]='H';
        test[1]='e';
        test[2]='l';
        test[3]='l';
        test[4]='o';
        test[5]='W';
        test[6]='o';
        test[7]='r';
        test[8]='l';
        test[9]='d';

        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawForeground(final Canvas canvas) {
        final Paint paint = mPaint;

        String stand = "\uD83E\uDDCD";
        String brick = "\uD83E\uDDF1";
        String cartwheel = "\uD83E\uDD38";

        String run = "\uD83C\uDFC3";
        String fog = "\uD83C\uDF2B️";

        String up = "⬆️";
        String right = "➡️️";
        String down = "⬇️️️";
        String left = "⬅️️️️";

        String box = "\uD83D\uDCE6";
        String gem = "\uD83D\uDC8E";
        String diamond = "\uD83D\uDD37";
        String hole = "\uD83D\uDD73️";
        String walk = "\uD83D\uDEB6";
        String block = "\uD83D\uDEA7";
        String purple = "\uD83D\uDFEA";

        // draw a door
        paint.setARGB(255,0,0,127);
        paint.setTextSize(100.0f);
        canvas.drawText(String.valueOf(test), mx, my, paint);
        canvas.drawText(block+fog+box+stand+walk+run+cartwheel+brick, mx, my, paint);
        canvas.drawText(up+right+down+left+purple+diamond+hole+gem, mx, my + 110, paint);
    }

    public void TouchEvent(final MotionEvent event) {
        mx = event.getAxisValue(0);
        my = event.getAxisValue(1);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                test[0] = 'D';
                break;
            case MotionEvent.ACTION_MOVE:
                test[0] = 'M';
                break;
            case MotionEvent.ACTION_UP:
                test[0] = 'U';
                break;
            default:
        }

        invalidate();
    }
}
