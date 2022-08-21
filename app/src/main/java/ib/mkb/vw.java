package ib.mkb;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class vw extends View {

    private final Paint mPaint = new Paint();
    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    private int px,py; // player position
    private float vx,vy; // view position (should move toward player)
    private float moveX, moveY; // touch position when moving
    private float touchX, touchY; // touch start position
    private float upX, upY; // touch lift position

    private boolean touchDown; // are we currently touching the screen?
    private boolean didScroll; // did we scroll with this touch? Prevents move on lift.
    private boolean darkColors; // night mode if true.

    private int currentLevel = 1;
    private int levelWidth = 0;
    private int levelHeight = 0;
    private byte[/*row*/][/*col*/] level;
    private boolean levelComplete; // true once there are no tiles just box or just goal.

    // flags
    private static final byte fWall = 1;
    private static final byte fPlayer = 1<<1;
    private static final byte fGoal = 1<<2;
    private static final byte fBox = 1<<3;

    private static final int tileScale = 110; // size of tile grid (tune to fit font)
    private final AssetManager assets;

    public vw(final Context context, AssetManager assets) {
        super(context);
        this.assets = assets;
        loadLevel(currentLevel);


        // Check for dark mode.
        int uiMode = getResources().getConfiguration().uiMode;
        if ((uiMode & UI_MODE_NIGHT_YES) > 0){
            darkColors = true;
        } else if ((uiMode & UI_MODE_NIGHT_NO) > 0){
            darkColors = false;
        }

        levelComplete = false;
        touchDown = false;
        mPaint.setAntiAlias(true);
    }

    // read in a level, indexed from zero
    private void loadLevel(int levelNumber) {
        try {
        InputStream is = assets.open("levels.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        List<String> levelLines = new ArrayList<>();
        String readLine;
        int foundLevel=0;
        int maxWidth = 0;

            // While the BufferedReader readLine is not null
            while ((readLine = br.readLine()) != null) {
                if (foundLevel > levelNumber) break;
                if (readLine.equals("")){
                    foundLevel++;
                }
                else if (foundLevel == levelNumber) {
                    levelLines.add(readLine);
                    if (readLine.length() > maxWidth) maxWidth = readLine.length();
                }
            }

            // Close the InputStream and BufferedReader
            is.close();
            br.close();

        // build the array and populate
        levelWidth = maxWidth;
        levelHeight = levelLines.size();
        level = new byte[levelHeight][levelWidth];

        for (int i=0; i<levelHeight; i++)
        {
            loadLevel( i, levelLines.get(i));
        }
    } catch (Exception e) {
        levelWidth = 5; // bonus "broken" level
        levelHeight = 1;
        level = new byte[levelHeight][levelWidth];
        loadLevel( 0, "#@$.#");
    }
    }

    private void loadLevel(int row, String s) {
        for (int i = 0; i < s.length(); i++){
            byte f = charToFlags(s.charAt(i));
            level[row][i] = f;
            if ((f & fPlayer) > 0){
                px = i; py = row;
                vx=px; vy=py;
            }
        }
    }

    private byte charToFlags(char c) {
        switch (c){
            case '#': return fWall;
            case '@': return fPlayer;
            case '+': return fPlayer+fGoal;
            case '$': return fBox;
            case '*': return fBox+fGoal;
            case '.': return fGoal;
            default: return 0; // no flags is floor
        }
    }

    public void drawLevel(final Canvas canvas){
        mPaint.setTextSize(100);

        lastWidth = canvas.getWidth();
        lastHeight = canvas.getHeight();

        int cx = lastWidth / 2;
        int cy = lastHeight / 2;

        for (int y=0; y < levelHeight; y++){
            for (int x=0; x < levelWidth; x++){
                // draw level centred around the view point as grid coords
                // view will drift toward player over time
                float tx = cx + ((x - vx) * tileScale);
                float ty = cy + ((y - vy) * tileScale);
                drawStack(level[y][x], tx,ty, canvas);
            }
        }
    }

    private void drawStack(byte flags, float x, float y, Canvas canvas) {
        if ((flags&fGoal)>0) canvas.drawText("\uD83D\uDD73", x, y, mPaint);
        if ((flags&fBox)>0) canvas.drawText("\uD83D\uDCE6", x, y, mPaint);
        if ((flags&fPlayer)>0) canvas.drawText("\uD83E\uDDCD", x, y, mPaint);
        if ((flags&fWall)>0) canvas.drawText("\uD83E\uDDF1", x, y, mPaint);
    }

    @Override
    public void onDrawForeground(final Canvas canvas) {
        // clear background
        if (darkColors){
            canvas.drawARGB(255, 50,50,50);
        } else {
            canvas.drawARGB(255, 200,200,200);
        }

        drawLevel(canvas);

        // show crappy win screen. TODO: level transitions etc.
        if (levelComplete) {
            mPaint.setTextSize(200);
            canvas.drawText("WIN \uD83E\uDD38", 60, 200, mPaint);
        }

        // draw reset level button
        mPaint.setTextSize(150);
        canvas.drawText("\uD83D\uDD19", 0, 150, mPaint);

        // drift toward being centred on player if not dragging
        if ((!touchDown) && (Math.abs(px - vx) > 0.01f || Math.abs(py - vy) > 0.01f)) {
            // view is not aligned to player. drift in
            float dx = (px - vx) / 3.0f;
            float dy = (py - vy) / 3.0f;
            vx+=dx;
            vy+=dy;
            invalidate(); // draw a frame
        }
    }

    public boolean TouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown = true;
                didScroll = false;
                touchX = event.getAxisValue(0);
                touchY = event.getAxisValue(1);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getAxisValue(0);
                moveY = event.getAxisValue(1);
                touchMoved();
                break;
            case MotionEvent.ACTION_UP:
                touchDown = false;
                upX = event.getAxisValue(0);
                upY = event.getAxisValue(1);
                touchLift();
                break;
            default:
        }

        invalidate(); // draw a frame
        return true; // event handled
    }

    private void touchLift() {
        // if we didn't scroll, then see what side of the player we tapped. move, check state etc.
        if (didScroll) return;

        // split screen into 9 like #
        // centre and corners do nothing (to save ambiguous inputs)
        int xi = (int)((upX*3) / lastWidth);
        int yi = (int)((upY*3) / lastHeight);

        if (xi == 0 && yi == 1) movePlayer(-1, 0);
        if (xi == 2 && yi == 1) movePlayer(1, 0);

        if (xi == 1 && yi == 0) movePlayer(0, -1);
        if (xi == 1 && yi == 2) movePlayer(0, 1);

        // chop into fifths for small controls
        xi = (int)((upX*5) / lastWidth);
        yi = (int)((upY*5) / lastHeight);
        if (xi <= 0 && yi <= 0){
            // pressed reset button
            loadLevel(currentLevel);
        }
    }

    private void movePlayer(int dx, int dy) {
        int newX = px+dx;
        int newY = py+dy;

        if (newX < 0 || newY < 0 || newX >= levelWidth || newY >= levelHeight) {
            // would try to move off play field.
            // we bump the view to show we got the input, but we don't move
            vx = newX; vy = newY;
            return;
        }

        byte target = level[newY][newX];

        if ((target & fWall) > 0){
            // target is totally blocked. Bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        if ((target & fBox) == 0){
            // either empty floor or unfilled goal. Just move
            // we don't update the *view* position, so it will animate to catch up
            level[py][px] &= ~fPlayer; // remove from old position
            px+=dx; py+=dy;
            level[py][px] |= fPlayer; // add to new position
            return;
        }

        // here, we're bumping into a box.
        // need to check the box's target is empty or goal.
        // pushing two boxes isn't allowed

        int newBoxX = newX+dx;
        int newBoxY = newY+dy;

        if (newBoxX < 0 || newBoxY < 0 || newBoxX >= levelWidth || newBoxY >= levelHeight) {
            // would try to move box off the play field.
            // we bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        target = level[newBoxY][newBoxX]; // box's new space
        if ((target & (fWall|fBox)) > 0) {
            // there is a wall or another box behind the box.
            // bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        // YAY! we can push a box.
        // move the box and the player.
        // we don't update the *view* position, so it will animate to catch up
        level[py][px] &= ~fPlayer; // remove player from old position
        px+=dx; py+=dy;            // update player pointer
        level[py][px] |= fPlayer;  // add player to new position

        level[py][px] &= ~fBox;             // remove box from old position
        level[newBoxY][newBoxX] |= fBox;    // add box to new position

        checkLevelState();
    }

    private void checkLevelState() {
        levelComplete = true; // optimism! we'll turn it off if we see a lone box or goal
        for (int y=0; y < levelHeight; y++){
            for (int x=0; x < levelWidth; x++){
                byte tile = level[y][x];
                if (tile == fGoal || tile == fBox) { // a box on a goal would not equal either.
                    levelComplete = false;
                    return;
                }
            }
        }
    }

    // if it's a small move, ignore. Otherwise, look around
    private void touchMoved() {
        float dx = touchX-moveX;
        float dy = touchY-moveY;
        float dist = (float)Math.sqrt((dx*dx)+(dy*dy));

        if (!didScroll && dist < 100) return;

        // look around the player
        didScroll = true;
        vx = px + (dx / tileScale);
        vy = py + (dy / tileScale);
    }
}
