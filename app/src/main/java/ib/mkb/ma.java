package ib.mkb;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class ma extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide action bar
        ActionBar titleBar = this.getActionBar();
        if (titleBar != null) this.getActionBar().hide();

        // Make a new super-basic view and use it
        vw basicView = new vw(this);
        setContentView(basicView);

        // Intercept all events
        final Window window = this.getWindow();
        if (window == null) return;
        th interactor = new th(window.getCallback(), basicView);
        window.setCallback(interactor);
    }
}