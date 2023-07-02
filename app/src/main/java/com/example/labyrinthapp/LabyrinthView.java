package com.example.labyrinthapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Custom view for displaying a labyrinth.
 */
public class LabyrinthView extends AppCompatImageView {
    private int[][] labyrinth;
    private int cellSize;
    private Bitmap pathTexture;
    private Bitmap wallTexture;
    private Bitmap entranceTexture;
    private Bitmap characterTexture;
    int currentPlayerXpos;
    int currentPlayerYpos;
    boolean firstDrawInLevel = true;
    Canvas canvas;

    /**
     * Constructs a new labyrinthView and sets character to starting position.
     * @param context The context.
     * @param attrSet The attribute set.
     */
    public LabyrinthView(Context context, AttributeSet attrSet) {
        super(context, attrSet);

        characterTexture = BitmapFactory.decodeResource(getResources(), R.drawable.character);
        currentPlayerXpos = 0;
        currentPlayerYpos = 1;
    }

    /**
     * Sets the given labyrinth and sets the path, wall and entrance
     * textures based on the current level.
     * @param labyrinth The labyrinth which should be drawn.
     */
    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;

        switch (PlayerController.getInstance().getLevel()) {
            case 1:
                pathTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage1_path);
                wallTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage1_wall);
                entranceTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage1_entrance);
                break;
            case 2:
                pathTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage2_path);
                wallTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage2_wall);
                entranceTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage2_entrance);
                break;
            case 3:
                pathTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage3_path);
                wallTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage3_wall);
                entranceTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage3_entrance);
                break;
            case 4:
                pathTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage4_path);
                wallTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage4_wall);
                entranceTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage4_entrance);
                break;
            case 5:
                pathTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage5_path);
                wallTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage5_wall);
                entranceTexture = BitmapFactory.decodeResource(getResources(), R.drawable.stage5_entrance);
                break;
            default:
                Log.d("LabyrinthView", "Invalid Level.");
        }

        PlayerController.getInstance().setLabyrinth(labyrinth);
        PlayerController.getInstance().setLabyrinthView(this);
    }

    /**
     * Sets the current player position/index.
     * @param x Current x-position (row) of the player.
     * @param y Current y-position (column) of the player.
     */
    public void setPlayerIndex(int x, int y) {
        currentPlayerXpos = x;
        currentPlayerYpos = y;
    }

    /**
     * Draws the labyrinth on the canvas and calls the drawPlayer method.
     * Calculates the size of the quads which then form the labyrinth.
     * @param canvas Canvas to be drawn on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;

        if (labyrinth == null) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        cellSize = Math.min(viewWidth, viewHeight) / labyrinth.length;

        if(firstDrawInLevel) {
            firstDrawInLevel = false;
            pathTexture = Bitmap.createScaledBitmap(pathTexture, cellSize, cellSize, false);
            wallTexture = Bitmap.createScaledBitmap(wallTexture, cellSize, cellSize, false);
            entranceTexture = Bitmap.createScaledBitmap(entranceTexture, cellSize, cellSize, false);
            characterTexture = Bitmap.createScaledBitmap(characterTexture, cellSize, cellSize, false);
        }

        int expandedRows = labyrinth.length;
        int expandedCols = labyrinth[0].length;

        for(int i = 0; i < expandedRows; i++) {
            for (int j = 0; j < expandedCols; j++) {

                float left = (j * cellSize) ;
                float top = i * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                if(labyrinth[i][j] == 0) {
                    canvas.drawBitmap(pathTexture, null, new RectF(left + (viewWidth / 8), top, right + (viewWidth / 8), bottom), null);
                }
                else if(labyrinth[i][j] == 1) {
                    canvas.drawBitmap(wallTexture, null, new RectF(left + (viewWidth / 8), top, right + (viewWidth / 8), bottom), null);
                }
                else {
                    canvas.drawBitmap(entranceTexture, null, new RectF(left + (viewWidth / 8), top, right + (viewWidth / 8), bottom), null);
                }
            }
        }
        drawPlayer(currentPlayerXpos, currentPlayerYpos);
        invalidate();
    }

    /**
     * Draws the player at the given position.
     * @param labX X-value (row) of the player in the labyrinth.
     * @param labY Y-value (column) of the player in the labyrinth.
     */
    private void drawPlayer(int labX, int labY) {
        float left = labY * cellSize + (getWidth() / 8);
        float top = labX * cellSize;
        float right = left + cellSize;
        float bottom = top + cellSize;
        canvas.drawBitmap(characterTexture, null, new RectF(left, top, right, bottom), null);
    }
}
