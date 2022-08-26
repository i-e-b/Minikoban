package ib.mkb;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Level extends View {

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
    private final Main parent;

    public Level(final Main context, AssetManager assets, int selectedLevel) {
        super(context);
        this.assets = assets;
        parent = context;
        currentLevel = selectedLevel;
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

    @Override
    public void onDrawForeground(final Canvas canvas) {
        lastWidth = canvas.getWidth();
        lastHeight = canvas.getHeight();

        // clear background
        if (darkColors){
            canvas.drawARGB(255, 50,50,50);
            mPaint.setARGB(255, 70,70,70);
        } else {
            canvas.drawARGB(255, 200,200,200);
            mPaint.setARGB(255, 220,220,220);
        }

        drawMotionHints(canvas);
        drawLevel(canvas);
        drawGeneralControls(canvas);

        if (darkColors){
            mPaint.setARGB(255, 220,220,220);
        } else {
            mPaint.setARGB(255, 70,70,70);
        }

        if (levelComplete) {
            mPaint.setTextSize(200);
            Rect rect = new Rect();
            mPaint.getTextBounds("WIN \uD83E\uDD38", 0,2, rect);
            float h = (lastHeight - rect.bottom - rect.top) / 2.0f;
            float w = (lastWidth - rect.right - rect.left) / 2.0f;
            canvas.drawText("WIN \uD83E\uDD38", w, h, mPaint);
        }


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

    private void drawGeneralControls(Canvas canvas){
        mPaint.setTextSize(150);
        Rect rect = new Rect();

        // draw reset level button
        mPaint.getTextBounds("\uD83D\uDD19", 0,2, rect);
        float h = rect.bottom - rect.top;
        canvas.drawText("\uD83D\uDD19", 0, h, mPaint);

        // level select button
        mPaint.getTextBounds("\uD83D\uDD22", 0,2, rect);
        h = rect.bottom - rect.top;
        canvas.drawText("\uD83D\uDD22", lastWidth-rect.right, h, mPaint);
    }

    private void drawMotionHints(Canvas canvas) {
        float w3 = lastWidth / 3.0f;
        float h3 = lastHeight / 3.0f;
        canvas.drawRect(0, h3, w3,2*h3, mPaint); // L
        canvas.drawRect(2*w3, h3, 3*w3,2*h3, mPaint); // R
        canvas.drawRect(w3, 0, 2*w3,h3, mPaint); // U
        canvas.drawRect(w3, 2*h3, 2*w3,3*h3, mPaint); // D
    }

    public void drawLevel(final Canvas canvas){
        mPaint.setTextSize(100);

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


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown = true;
                didScroll = false;
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                touchMoved();
                break;
            case MotionEvent.ACTION_UP:
                touchDown = false;
                upX = event.getX();
                upY = event.getY();
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

        float w3 = lastWidth / 3.0f;
        float h3 = lastHeight / 3.0f;
        // split screen into 9 like #
        // centre and corners do nothing (to save ambiguous inputs)

        if (upY >= h3 && upY <= 2*h3) {
            if (upX <= w3) movePlayer(-1, 0);
            if (upX >= 2*w3) movePlayer(1, 0);
        }

        if (upX >= w3 && upX <= 2*w3) {
            if (upY <= h3) movePlayer(0, -1);
            if (upY >= 2*h3) movePlayer(0, 1);
        }

        // chop into fifths for small controls
        float minDim = Math.min(lastWidth, lastHeight) / 5.0f;
        if (upX <= minDim && upY <= minDim){
            // pressed reset button
            loadLevel(currentLevel);
        }

        if (upX >= lastWidth - minDim && upY <= minDim){
            // pressed level select button
            parent.showSelectionScreen();
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
