package ib.mkb;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Main extends Activity {
    private BaseView view;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // hide action bar
        ActionBar bar = this.getActionBar();
        if (bar != null) this.getActionBar().hide();

        // Make a new super-basic view and use it
        showSelectionScreen();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        if (view != null) return view.motionEvent(ev);
        return false;
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev){
        if (view != null) return view.motionEvent(ev);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event){
        if (view != null) return view.keyEvent(event);
        return false;
    }

    public void switchToLevel(int level){
        Level v = new Level(this, getAssets(), level);
        setContentView(v);
        view = v;
    }

    public void showSelectionScreen() {
        Select v = new Select(this);
        setContentView(v);
        view = v;
    }
}