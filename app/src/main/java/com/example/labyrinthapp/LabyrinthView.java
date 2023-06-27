package com.example.labyrinthapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import java.text.AttributedCharacterIterator;

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

    public LabyrinthView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init();
    }

    private void init() {
        characterTexture = BitmapFactory.decodeResource(getResources(), R.drawable.character);

        currentPlayerXpos = 0;
        currentPlayerYpos = 1;
    }

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
                Log.d("s", "INVALID LEVEL");
        }

        PlayerController.getInstance().setLabyrinth(labyrinth);
        PlayerController.getInstance().setLabyrinthView(this);
    }

    public void setPlayerIndex(int x, int y) {
        currentPlayerXpos = x;
        currentPlayerYpos = y;
    }

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
            //TODO maybe in init
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
    private void drawPlayer(int labX, int labY) {
        float left = labY * cellSize + (getWidth() / 8);
        float top = labX * cellSize;
        float right = left + cellSize;
        float bottom = top + cellSize;
        canvas.drawBitmap(characterTexture, null, new RectF(left, top, right, bottom), null);
    }
}
