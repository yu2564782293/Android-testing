package com.example.gomoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private GomokuView boardView;
    private TextView statusView;
    private TextView scoreView;
    private Button undoButton;
    private int blackScore = 0;
    private int whiteScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(14), dp(18), dp(14), dp(14));
        root.setBackgroundColor(Color.rgb(255, 247, 230));

        TextView titleView = new TextView(this);
        titleView.setText("五子棋");
        titleView.setTextColor(Color.rgb(70, 43, 16));
        titleView.setTextSize(30);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(Gravity.CENTER);
        root.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        scoreView = new TextView(this);
        scoreView.setTextColor(Color.rgb(126, 78, 28));
        scoreView.setTextSize(16);
        scoreView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scoreParams.setMargins(0, dp(4), 0, 0);
        root.addView(scoreView, scoreParams);

        statusView = new TextView(this);
        statusView.setText("黑棋先手，请落子");
        statusView.setTextColor(Color.rgb(96, 63, 26));
        statusView.setTextSize(18);
        statusView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMargins(0, dp(8), 0, dp(12));
        root.addView(statusView, statusParams);

        boardView = new GomokuView(this, new GomokuView.GameListener() {
            @Override
            public void onTurnChanged(int currentPlayer) {
                statusView.setText(currentPlayer == GomokuView.BLACK ? "轮到黑棋" : "轮到白棋");
                updateControls();
            }

            @Override
            public void onGameOver(int winner) {
                if (winner == GomokuView.BLACK) {
                    blackScore++;
                } else {
                    whiteScore++;
                }
                updateScoreView();
                updateControls();

                String message = winner == GomokuView.BLACK ? "黑棋获胜！" : "白棋获胜！";
                statusView.setText(message);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("游戏结束")
                        .setMessage(message + "\n获胜五子已用红线标出。")
                        .setPositiveButton("再来一局", (dialog, which) -> resetGame())
                        .setNegativeButton("看看棋盘", null)
                        .show();
            }

            @Override
            public void onDrawGame() {
                statusView.setText("平局！棋盘已满");
                updateControls();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("游戏结束")
                        .setMessage("平局！棋盘已满")
                        .setPositiveButton("再来一局", (dialog, which) -> resetGame())
                        .setNegativeButton("看看棋盘", null)
                        .show();
            }

            @Override
            public void onInvalidMove(String message) {
                statusView.setText(message);
            }

            @Override
            public void onBoardChanged(int currentPlayer) {
                statusView.setText(currentPlayer == GomokuView.BLACK ? "轮到黑棋" : "轮到白棋");
                updateControls();
            }
        });
        root.addView(boardView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonRowParams.setMargins(0, dp(12), 0, 0);
        root.addView(buttonRow, buttonRowParams);

        undoButton = new Button(this);
        undoButton.setText("悔棋");
        undoButton.setAllCaps(false);
        undoButton.setTextSize(16);
        undoButton.setOnClickListener(v -> {
            if (boardView.undoLastMove()) {
                updateControls();
            }
        });
        LinearLayout.LayoutParams undoParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        undoParams.setMargins(0, 0, dp(6), 0);
        buttonRow.addView(undoButton, undoParams);

        Button resetButton = new Button(this);
        resetButton.setText("重新开始");
        resetButton.setAllCaps(false);
        resetButton.setTextSize(16);
        resetButton.setOnClickListener(v -> resetGame());
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        resetParams.setMargins(dp(6), 0, 0, 0);
        buttonRow.addView(resetButton, resetParams);

        updateScoreView();
        updateControls();
        setContentView(root);
    }

    private void resetGame() {
        boardView.reset();
        statusView.setText("黑棋先手，请落子");
        updateControls();
    }

    private void updateScoreView() {
        scoreView.setText("比分  黑棋 " + blackScore + " : " + whiteScore + " 白棋");
    }

    private void updateControls() {
        if (undoButton != null && boardView != null) {
            undoButton.setEnabled(boardView.canUndo());
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class GomokuView extends View {
        public static final int EMPTY = 0;
        public static final int BLACK = 1;
        public static final int WHITE = 2;
        private static final int BOARD_SIZE = 15;
        private static final int WIN_COUNT = 5;

        private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        private final List<Move> moveHistory = new ArrayList<>();
        private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint piecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint winPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final GameListener listener;

        private int currentPlayer = BLACK;
        private boolean gameOver = false;
        private float cellSize;
        private float boardLeft;
        private float boardTop;
        private float boardLength;
        private int moveCount = 0;
        private int lastRow = -1;
        private int lastCol = -1;
        private int winStartRow = -1;
        private int winStartCol = -1;
        private int winEndRow = -1;
        private int winEndCol = -1;

        public GomokuView(Activity context, GameListener listener) {
            super(context);
            this.listener = listener;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            boardPaint.setColor(Color.rgb(243, 195, 110));
            boardPaint.setStyle(Paint.Style.FILL);
            boardPaint.setShadowLayer(12f, 0f, 6f, Color.argb(70, 0, 0, 0));

            linePaint.setColor(Color.rgb(92, 57, 20));
            linePaint.setStrokeWidth(2f);

            starPaint.setColor(Color.rgb(92, 57, 20));
            starPaint.setStyle(Paint.Style.FILL);

            textPaint.setColor(Color.rgb(120, 78, 32));
            textPaint.setTextSize(24f);
            textPaint.setTextAlign(Paint.Align.CENTER);

            winPaint.setColor(Color.rgb(225, 56, 42));
            winPaint.setStrokeWidth(8f);
            winPaint.setStrokeCap(Paint.Cap.ROUND);
            winPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            calculateBoardArea();
            drawBoard(canvas);
            drawPieces(canvas);
            drawWinningLine(canvas);
        }

        private void calculateBoardArea() {
            float available = Math.min(getWidth() - getPaddingLeft() - getPaddingRight(),
                    getHeight() - getPaddingTop() - getPaddingBottom());
            boardLength = Math.max(0, available - 24f);
            cellSize = boardLength / (BOARD_SIZE - 1);
            boardLeft = (getWidth() - boardLength) / 2f;
            boardTop = (getHeight() - boardLength) / 2f;
        }

        private void drawBoard(Canvas canvas) {
            float padding = cellSize * 0.55f;
            canvas.drawRoundRect(boardLeft - padding, boardTop - padding,
                    boardLeft + boardLength + padding, boardTop + boardLength + padding,
                    24f, 24f, boardPaint);

            for (int i = 0; i < BOARD_SIZE; i++) {
                float pos = boardLeft + i * cellSize;
                canvas.drawLine(boardLeft, pos, boardLeft + boardLength, pos, linePaint);
                canvas.drawLine(pos, boardTop, pos, boardTop + boardLength, linePaint);
            }

            int[] starPoints = {3, 7, 11};
            for (int row : starPoints) {
                for (int col : starPoints) {
                    canvas.drawCircle(boardLeft + col * cellSize,
                            boardTop + row * cellSize,
                            Math.max(4f, cellSize * 0.10f), starPaint);
                }
            }
        }

        private void drawPieces(Canvas canvas) {
            float radius = cellSize * 0.38f;
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int piece = board[row][col];
                    if (piece == EMPTY) {
                        continue;
                    }
                    float cx = boardLeft + col * cellSize;
                    float cy = boardTop + row * cellSize;
                    if (piece == BLACK) {
                        piecePaint.setShader(new RadialGradient(cx - radius / 3f, cy - radius / 3f, radius,
                                Color.rgb(90, 90, 90), Color.rgb(10, 10, 10), Shader.TileMode.CLAMP));
                    } else {
                        piecePaint.setShader(new RadialGradient(cx - radius / 3f, cy - radius / 3f, radius,
                                Color.WHITE, Color.rgb(205, 205, 205), Shader.TileMode.CLAMP));
                    }
                    canvas.drawCircle(cx, cy, radius, piecePaint);
                    piecePaint.setShader(null);

                    if (row == lastRow && col == lastCol) {
                        piecePaint.setStyle(Paint.Style.STROKE);
                        piecePaint.setStrokeWidth(3f);
                        piecePaint.setColor(Color.rgb(220, 61, 45));
                        canvas.drawCircle(cx, cy, radius * 0.42f, piecePaint);
                        piecePaint.setStyle(Paint.Style.FILL);
                    }
                }
            }
        }

        private void drawWinningLine(Canvas canvas) {
            if (winStartRow < 0 || winEndRow < 0) {
                return;
            }
            float startX = boardLeft + winStartCol * cellSize;
            float startY = boardTop + winStartRow * cellSize;
            float endX = boardLeft + winEndCol * cellSize;
            float endY = boardTop + winEndRow * cellSize;
            canvas.drawLine(startX, startY, endX, endY, winPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP || cellSize <= 0) {
                return true;
            }
            if (gameOver) {
                listener.onInvalidMove("本局已结束，请重新开始");
                return true;
            }

            int col = Math.round((event.getX() - boardLeft) / cellSize);
            int row = Math.round((event.getY() - boardTop) / cellSize);
            if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                listener.onInvalidMove("请点击棋盘交叉点落子");
                return true;
            }
            if (board[row][col] != EMPTY) {
                listener.onInvalidMove("这里已经有棋子了");
                return true;
            }

            board[row][col] = currentPlayer;
            moveHistory.add(new Move(row, col, currentPlayer));
            lastRow = row;
            lastCol = col;
            moveCount++;

            if (hasFiveInLine(row, col, currentPlayer)) {
                gameOver = true;
                invalidate();
                listener.onGameOver(currentPlayer);
                return true;
            }

            if (moveCount == BOARD_SIZE * BOARD_SIZE) {
                gameOver = true;
                invalidate();
                listener.onDrawGame();
                return true;
            }

            currentPlayer = currentPlayer == BLACK ? WHITE : BLACK;
            listener.onTurnChanged(currentPlayer);
            invalidate();
            return true;
        }

        private boolean hasFiveInLine(int row, int col, int player) {
            return updateWinningLine(row, col, 1, 0, player)
                    || updateWinningLine(row, col, 0, 1, player)
                    || updateWinningLine(row, col, 1, 1, player)
                    || updateWinningLine(row, col, 1, -1, player);
        }

        private boolean updateWinningLine(int row, int col, int rowStep, int colStep, int player) {
            int startRow = row;
            int startCol = col;
            while (isSamePiece(startRow - rowStep, startCol - colStep, player)) {
                startRow -= rowStep;
                startCol -= colStep;
            }

            int endRow = row;
            int endCol = col;
            while (isSamePiece(endRow + rowStep, endCol + colStep, player)) {
                endRow += rowStep;
                endCol += colStep;
            }

            int total = Math.max(Math.abs(endRow - startRow), Math.abs(endCol - startCol)) + 1;
            if (total >= WIN_COUNT) {
                winStartRow = startRow;
                winStartCol = startCol;
                winEndRow = endRow;
                winEndCol = endCol;
                return true;
            }
            return false;
        }

        private boolean isSamePiece(int row, int col, int player) {
            return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == player;
        }

        public boolean undoLastMove() {
            if (!canUndo()) {
                return false;
            }
            Move lastMove = moveHistory.remove(moveHistory.size() - 1);
            board[lastMove.row][lastMove.col] = EMPTY;
            moveCount--;
            currentPlayer = lastMove.player;
            gameOver = false;
            clearWinningLine();

            if (moveHistory.isEmpty()) {
                lastRow = -1;
                lastCol = -1;
            } else {
                Move previousMove = moveHistory.get(moveHistory.size() - 1);
                lastRow = previousMove.row;
                lastCol = previousMove.col;
            }
            listener.onBoardChanged(currentPlayer);
            invalidate();
            return true;
        }

        public boolean canUndo() {
            return !gameOver && !moveHistory.isEmpty();
        }

        public void reset() {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    board[row][col] = EMPTY;
                }
            }
            currentPlayer = BLACK;
            gameOver = false;
            moveCount = 0;
            lastRow = -1;
            lastCol = -1;
            moveHistory.clear();
            clearWinningLine();
            invalidate();
        }

        private void clearWinningLine() {
            winStartRow = -1;
            winStartCol = -1;
            winEndRow = -1;
            winEndCol = -1;
        }

        private static class Move {
            final int row;
            final int col;
            final int player;

            Move(int row, int col, int player) {
                this.row = row;
                this.col = col;
                this.player = player;
            }
        }

        public interface GameListener {
            void onTurnChanged(int currentPlayer);
            void onGameOver(int winner);
            void onDrawGame();
            void onInvalidMove(String message);
            void onBoardChanged(int currentPlayer);
        }
    }
}
