package ib.mkb;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

// TODO: base class for the two views that does touch and draw common stuff.

public class Main extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // hide action bar
        ActionBar bar = this.getActionBar();
        if (bar != null) this.getActionBar().hide();

        // Make a new super-basic view and use it
        showSelectionScreen();
    }

    public void switchToLevel(int level){
        Level v = new Level(this, getAssets(), level);
        setContentView(v);
    }

    public void showSelectionScreen() {
        Select v = new Select(this);
        setContentView(v);
    }
}