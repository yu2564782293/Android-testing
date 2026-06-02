package com.example.gomokuai;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private TextView statusText;
    private GomokuView gomokuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(255, 248, 225));
        root.setPadding(20, 24, 20, 20);

        TextView title = new TextView(this);
        title.setText("五子棋 AI 对战");
        title.setTextColor(Color.rgb(62, 39, 35));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        statusText = new TextView(this);
        statusText.setText("你执黑先手，点击棋盘落子");
        statusText.setTextColor(Color.rgb(93, 64, 55));
        statusText.setTextSize(17);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 10, 0, 12);
        root.addView(statusText, statusParams);

        gomokuView = new GomokuView(this, statusText);
        LinearLayout.LayoutParams boardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(gomokuView, boardParams);

        Button restart = new Button(this);
        restart.setText("重新开始");
        restart.setTextSize(16);
        restart.setAllCaps(false);
        restart.setOnClickListener(v -> gomokuView.resetGame());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 14, 0, 0);
        root.addView(restart, buttonParams);

        setContentView(root);
    }

    public static class GomokuView extends View {
        private static final int SIZE = 15;
        private static final int EMPTY = 0;
        private static final int HUMAN = 1;
        private static final int AI = 2;
        private static final int WIN_COUNT = 5;

        private final int[][] board = new int[SIZE][SIZE];
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextView statusText;
        private float cellSize;
        private float boardLeft;
        private float boardTop;
        private boolean gameOver = false;
        private boolean aiThinking = false;

        public GomokuView(Activity context, TextView statusText) {
            super(context);
            this.statusText = statusText;
            setFocusable(true);
        }

        public void resetGame() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    board[r][c] = EMPTY;
                }
            }
            gameOver = false;
            aiThinking = false;
            statusText.setText("你执黑先手，点击棋盘落子");
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            float boardSize = Math.min(width - getPaddingLeft() - getPaddingRight(), height - 10);
            cellSize = boardSize / (SIZE + 1);
            boardLeft = (width - cellSize * (SIZE - 1)) / 2f;
            boardTop = (height - cellSize * (SIZE - 1)) / 2f;

            drawBoardBackground(canvas, width, height);
            drawGrid(canvas);
            drawStarPoints(canvas);
            drawPieces(canvas);
        }

        private void drawBoardBackground(Canvas canvas, int width, int height) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(242, 200, 121));
            float padding = cellSize * 0.8f;
            canvas.drawRoundRect(boardLeft - padding, boardTop - padding,
                    boardLeft + cellSize * (SIZE - 1) + padding,
                    boardTop + cellSize * (SIZE - 1) + padding,
                    24, 24, paint);
        }

        private void drawGrid(Canvas canvas) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.rgb(93, 64, 55));
            for (int i = 0; i < SIZE; i++) {
                float p = boardTop + i * cellSize;
                canvas.drawLine(boardLeft, p, boardLeft + (SIZE - 1) * cellSize, p, paint);
                p = boardLeft + i * cellSize;
                canvas.drawLine(p, boardTop, p, boardTop + (SIZE - 1) * cellSize, paint);
            }
        }

        private void drawStarPoints(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(80, 50, 35));
            int[] points = {3, 7, 11};
            for (int r : points) {
                for (int c : points) {
                    canvas.drawCircle(boardLeft + c * cellSize, boardTop + r * cellSize, cellSize * 0.12f, paint);
                }
            }
        }

        private void drawPieces(Canvas canvas) {
            float radius = cellSize * 0.42f;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == EMPTY) continue;
                    float x = boardLeft + c * cellSize;
                    float y = boardTop + r * cellSize;
                    if (board[r][c] == HUMAN) {
                        paint.setShader(new RadialGradient(x - radius / 3, y - radius / 3, radius,
                                Color.rgb(95, 95, 95), Color.BLACK, Shader.TileMode.CLAMP));
                    } else {
                        paint.setShader(new RadialGradient(x - radius / 3, y - radius / 3, radius,
                                Color.WHITE, Color.rgb(210, 210, 210), Shader.TileMode.CLAMP));
                    }
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, radius, paint);
                    paint.setShader(null);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1.5f);
                    paint.setColor(board[r][c] == HUMAN ? Color.BLACK : Color.rgb(160, 160, 160));
                    canvas.drawCircle(x, y, radius, paint);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP || gameOver || aiThinking) return true;
            int col = Math.round((event.getX() - boardLeft) / cellSize);
            int row = Math.round((event.getY() - boardTop) / cellSize);
            if (!isInside(row, col) || board[row][col] != EMPTY) return true;

            board[row][col] = HUMAN;
            invalidate();
            if (checkWin(row, col, HUMAN)) {
                endGame("恭喜！你赢了！");
                return true;
            }
            if (isBoardFull()) {
                endGame("平局！棋盘已满");
                return true;
            }

            aiThinking = true;
            statusText.setText("AI 正在思考...");
            postDelayed(this::makeAiMove, 350);
            return true;
        }

        private void makeAiMove() {
            int[] move = findBestMove();
            if (move != null) {
                board[move[0]][move[1]] = AI;
                invalidate();
                if (checkWin(move[0], move[1], AI)) {
                    endGame("AI 获胜，再来一局吧！");
                } else if (isBoardFull()) {
                    endGame("平局！棋盘已满");
                } else {
                    statusText.setText("轮到你了：黑棋落子");
                }
            }
            aiThinking = false;
        }

        private int[] findBestMove() {
            int bestScore = Integer.MIN_VALUE;
            List<int[]> bestMoves = new ArrayList<>();
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] != EMPTY || !hasNeighbor(r, c)) continue;
                    int attack = evaluatePoint(r, c, AI);
                    int defend = evaluatePoint(r, c, HUMAN);
                    int score = Math.max(attack, defend + 8) + Math.min(attack, defend) / 2;
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear();
                        bestMoves.add(new int[]{r, c});
                    } else if (score == bestScore) {
                        bestMoves.add(new int[]{r, c});
                    }
                }
            }
            if (bestMoves.isEmpty()) return new int[]{SIZE / 2, SIZE / 2};
            return bestMoves.get((int) (Math.random() * bestMoves.size()));
        }

        private boolean hasNeighbor(int row, int col) {
            for (int dr = -2; dr <= 2; dr++) {
                for (int dc = -2; dc <= 2; dc++) {
                    int r = row + dr;
                    int c = col + dc;
                    if (isInside(r, c) && board[r][c] != EMPTY) return true;
                }
            }
            return false;
        }

        private int evaluatePoint(int row, int col, int player) {
            int[][] dirs = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
            int score = 0;
            for (int[] d : dirs) {
                int count = 1;
                int open = 0;
                count += countDirection(row, col, d[0], d[1], player);
                count += countDirection(row, col, -d[0], -d[1], player);
                if (isOpenEnd(row, col, d[0], d[1], player)) open++;
                if (isOpenEnd(row, col, -d[0], -d[1], player)) open++;
                score += patternScore(count, open);
            }
            return score;
        }

        private int countDirection(int row, int col, int dr, int dc, int player) {
            int count = 0;
            int r = row + dr;
            int c = col + dc;
            while (isInside(r, c) && board[r][c] == player) {
                count++;
                r += dr;
                c += dc;
            }
            return count;
        }

        private boolean isOpenEnd(int row, int col, int dr, int dc, int player) {
            int r = row + dr;
            int c = col + dc;
            while (isInside(r, c) && board[r][c] == player) {
                r += dr;
                c += dc;
            }
            return isInside(r, c) && board[r][c] == EMPTY;
        }

        private int patternScore(int count, int open) {
            if (count >= 5) return 1_000_000;
            if (count == 4 && open == 2) return 120_000;
            if (count == 4 && open == 1) return 25_000;
            if (count == 3 && open == 2) return 8_000;
            if (count == 3 && open == 1) return 1_500;
            if (count == 2 && open == 2) return 700;
            if (count == 2 && open == 1) return 120;
            if (count == 1 && open == 2) return 30;
            return 5;
        }

        private boolean checkWin(int row, int col, int player) {
            int[][] dirs = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
            for (int[] d : dirs) {
                int count = 1 + countDirection(row, col, d[0], d[1], player)
                        + countDirection(row, col, -d[0], -d[1], player);
                if (count >= WIN_COUNT) return true;
            }
            return false;
        }

        private boolean isBoardFull() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == EMPTY) return false;
                }
            }
            return true;
        }

        private boolean isInside(int row, int col) {
            return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
        }

        private void endGame(String message) {
            gameOver = true;
            statusText.setText(message);
        }
    }
}
