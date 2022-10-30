package ib.mkb;

import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Single points for Android system calls.
 */
public class os {
    public static boolean isDarkMode(View v){
        int uiMode = v.getResources().getConfiguration().uiMode;
        return ((uiMode & UI_MODE_NIGHT_YES) > 0);
    }

    public static void setSize(Paint mPaint, int s){
        mPaint.setTextSize(s);
    }
    public static void setGrey(Paint mPaint, int c2) {
        mPaint.setARGB(255, c2, c2, c2);
    }
    public static void measureText(Paint mPaint, String txt, Rect rect){
        mPaint.getTextBounds(txt, 0,txt.length(), rect);
    }

    public static void measureChr(Paint mPaint, String txt, Rect rect) {
        mPaint.getTextBounds(txt, 0,2, rect);
    }

    private static SharedPreferences getPref(Context c){
        return c.getSharedPreferences("scores", Context.MODE_PRIVATE);
    }

    public static int getLastLevel(Context c){
        return getPref(c).getInt("lastLvl", 0);
    }

    public static void setLastLevel(Context c, int level){
        SharedPreferences.Editor e = getPref(c).edit();
        e.putInt("lastLvl", level);
        e.apply();
    }

    public static int getScore(Context c, int level){
        String levelName = ""+level;
        return getPref(c).getInt(levelName, 0);
    }

    public static void setScore(Context c, int level, int score){
        String levelKey = ""+level;
        SharedPreferences pref = getPref(c);
        int best = pref.getInt(levelKey, 0);

        if (best < 1 || score < best) { // yay! Best score.
            // save the preference
            SharedPreferences.Editor e = pref.edit();
            e.putInt(levelKey, score);
            e.apply();
        }
    }

    public static void drawText(Canvas canvas, String text, float x, float y, Paint mPaint) {
        canvas.drawText(text, x, y, mPaint);
    }

    public static void drawRect(Paint p, Canvas canvas, float l, float t, float r, float b) {
        canvas.drawRect(l,t,r,b, p);
    }
}
