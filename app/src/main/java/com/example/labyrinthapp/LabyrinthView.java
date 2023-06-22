package com.example.labyrinthapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import java.text.AttributedCharacterIterator;

public class LabyrinthView extends AppCompatImageView {
    private int[][] labyrinth;
    private int cellSize;
    private Paint wallPaint;
    private Paint pathPaint;
    private Paint entrancePaint;
    private Paint exitPaint;
    private Paint boundaryPaint;
    int currentPlayerXpos;
    int currentPlayerYpos;
    Canvas canvas;

    public LabyrinthView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init();
    }

    private void init() {
        pathPaint = new Paint();
        pathPaint.setColor(Color.WHITE);
        pathPaint.setStyle(Paint.Style.FILL);

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStyle(Paint.Style.FILL);

        boundaryPaint = new Paint();
        boundaryPaint.setColor(Color.BLACK);
        boundaryPaint.setStyle(Paint.Style.FILL);

        entrancePaint = new Paint();
        entrancePaint.setColor(Color.RED);
        entrancePaint.setStyle(Paint.Style.FILL);

        exitPaint = new Paint();
        exitPaint.setColor(Color.GREEN);
        exitPaint.setStyle(Paint.Style.FILL);

        currentPlayerXpos = 0;
        currentPlayerYpos = 1;
    }

    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
        PlayerController.getInstance().setLabyrinth(labyrinth);
        PlayerController.getInstance().setPlayerPaint(entrancePaint);
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

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        cellSize = Math.min(viewWidth, viewHeight) / labyrinth.length;

        if (labyrinth == null) {
            return;
        }

        int expandedRows = labyrinth.length;
        int expandedCols = labyrinth[0].length;

        for(int i = 0; i < expandedRows; i++) {
            for (int j = 0; j < expandedCols; j++) {

                float left = j * cellSize;
                float top = i * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                if(labyrinth[i][j] == 0) {
                    canvas.drawRect(left + (viewWidth / 8), top, right + (viewWidth / 8), bottom, pathPaint);
                }
                else {
                    canvas.drawRect(left + (viewWidth / 8), top, right + (viewWidth / 8), bottom, wallPaint);
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
        canvas.drawRect(left, top, right, bottom, entrancePaint);
    }
}
