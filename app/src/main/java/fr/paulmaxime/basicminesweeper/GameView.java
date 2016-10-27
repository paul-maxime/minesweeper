package fr.paulmaxime.basicminesweeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {
    private static final int GAME_WIDTH = 10;
    private static final int GAME_HEIGHT = 10;
    private static final int TOTAL_MINES = 20;

    private Paint black_paint;
    private Paint white_paint;
    private Paint grey_paint;
    private Paint red_paint;
    private Paint green_paint;
    private Paint blue_paint;
    private Paint yellow_paint;

    private Rect square;

    private boolean[][] discovered_cells;
    private boolean[][] marked_cells;
    private int marked_cells_count;
    private boolean[][] mines;
    private int[][] adjacent_mines;
    private boolean is_game_over;
    private boolean is_marking_mode;

    private GameUpdateListener updateListener;

    public GameView(Context c) {
        super(c);
        init();
    }

    public GameView(Context c, AttributeSet as) {
        super(c, as);
        init();
    }

    public GameView(Context c, AttributeSet as, int default_style) {
        super(c, as, default_style);
        init();
    }

    private void init() {
        initColours();
        resetGame();
    }

    private void initColours() {
        black_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        white_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grey_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        red_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        green_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yellow_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        black_paint.setColor(0xFF000000);
        white_paint.setColor(0xFFFFFFFF);
        grey_paint.setColor(0xFF777777);
        red_paint.setColor(0xFFFF0000);
        green_paint.setColor(0xFF00FF00);
        blue_paint.setColor(0xFF0000FF);
        yellow_paint.setColor(0xFFFFFF00);
        black_paint.setTextSize(32.0f);
        blue_paint.setTextSize(32.0f);
        green_paint.setTextSize(32.0f);
        yellow_paint.setTextSize(32.0f);
        red_paint.setTextSize(32.0f);
    }

    public void setUpdateListener(GameUpdateListener listener) {
        updateListener = listener;
    }

    public void toggleMarkingMode() {
        is_marking_mode = !is_marking_mode;
    }

    public void resetGame() {
        discovered_cells = new boolean[GAME_WIDTH][GAME_HEIGHT];
        marked_cells = new boolean[GAME_WIDTH][GAME_HEIGHT];
        marked_cells_count = 0;
        placeMines();
        calculateAdjacentMines();
        is_game_over = false;
        if (updateListener != null) {
            updateListener.onGameUpdated();
        }
    }

    public int getTotalMines() {
        return TOTAL_MINES;
    }

    public int getMarkedMines() {
        return marked_cells_count;
    }

    public boolean isMarkingMode() {
        return is_marking_mode;
    }

    private void placeMines() {
        Random rand = new Random();
        mines = new boolean[GAME_WIDTH][GAME_HEIGHT];

        int remaining = TOTAL_MINES;
        while (remaining > 0) {
            int x = rand.nextInt(GAME_WIDTH);
            int y = rand.nextInt(GAME_HEIGHT);
            if (!mines[x][y]) {
                mines[x][y] = true;
                remaining--;
            }
        }
    }

    private void calculateAdjacentMines() {
        adjacent_mines = new int[GAME_WIDTH][GAME_HEIGHT];

        for (int x = 0; x < GAME_WIDTH; ++x) {
            for (int y = 0; y < GAME_HEIGHT; ++y) {
                if (!mines[x][y]) {
                    adjacent_mines[x][y] = getAdjacentMinesCount(x, y);
                }
            }
        }
    }

    private int getAdjacentMinesCount(int cellX, int cellY) {
        int count = 0;
        for (int x = cellX - 1; x <= cellX + 1; ++x) {
            for (int y = cellY - 1; y <= cellY + 1; ++y) {
                if (x >= 0 && x < GAME_WIDTH && y >= 0 && y < GAME_HEIGHT && mines[x][y]) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawCells(canvas);
        drawGrid(canvas);
    }

    private void drawCells(Canvas canvas) {
        for (int x = 0; x < GAME_WIDTH; ++x) {
            for (int y = 0; y < GAME_HEIGHT; ++y) {
                canvas.save();
                canvas.translate(x * square.width(), y * square.height());
                drawSingleCell(canvas, x, y);
                canvas.restore();
            }
        }
    }

    private void drawSingleCell(Canvas canvas, int x, int y) {
        if (discovered_cells[x][y]) {
            if (mines[x][y]) {
                canvas.drawRect(square, red_paint);
                canvas.drawText("M", 0.0f, square.height(), black_paint);
            } else if (adjacent_mines[x][y] > 0) {
                Paint color = getColorFromAdjacentCount(adjacent_mines[x][y]);
                canvas.drawRect(square, grey_paint);
                canvas.drawText(Integer.toString(adjacent_mines[x][y]), 0.0f, square.height(), color);
            } else {
                canvas.drawRect(square, grey_paint);
            }
        } else if (marked_cells[x][y]) {
            canvas.drawRect(square, yellow_paint);
        } else {
            canvas.drawRect(square, black_paint);
        }
    }

    private Paint getColorFromAdjacentCount(int count) {
        switch (count) {
            case 1:
                return blue_paint;
            case 2:
                return green_paint;
            case 3:
                return yellow_paint;
            default:
                return red_paint;
        }
    }

    private void drawGrid(Canvas canvas) {
        for (int x = 0; x < GAME_WIDTH; ++x) {
            canvas.save();
            canvas.translate(x * square.width(), 0);
            canvas.drawLine(0, 0, 0, square.height() * 10, white_paint);
            canvas.restore();
        }
        for (int y = 0; y < GAME_HEIGHT; ++y) {
            canvas.save();
            canvas.translate(0, y * square.height());
            canvas.drawLine(0, 0, square.width() * 10, 0, white_paint);
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int cellX = (int)event.getX() / square.width();
            int cellY = (int)event.getY() / square.height();
            if (cellX >= 0 && cellX < GAME_WIDTH && cellY >= 0 && cellY < GAME_HEIGHT) {
                discoverCell(cellX, cellY);
            }
        }

        return super.onTouchEvent(event);
    }

    private void discoverCell(int x, int y) {
        if (!is_game_over && !discovered_cells[x][y]) {
            if (is_marking_mode) {
                marked_cells[x][y] = !marked_cells[x][y];
                if (marked_cells[x][y]) {
                    marked_cells_count += 1;
                } else {
                    marked_cells_count -= 1;
                }
            } else {
                if (!marked_cells[x][y]) {
                    discovered_cells[x][y] = true;
                    if (mines[x][y]) {
                        is_game_over = true;
                    }
                }
            }
            if (updateListener != null) {
                updateListener.onGameUpdated();
            }
            invalidate();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        square = new Rect(0, 0, w / GAME_WIDTH, h / GAME_HEIGHT);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(minSize, minSize);
    }
}
