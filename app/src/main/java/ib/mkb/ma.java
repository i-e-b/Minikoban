package ib.mkb;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class ma extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // hide action bar
        ActionBar bar = this.getActionBar();
        if (bar != null) this.getActionBar().hide();

        // Make a new super-basic view and use it
        v = new vw(this);
        setContentView(v);
    }
    private vw v;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (v != null) return v.TouchEvent(event);
        return false;
    }
}