package org.memmaze.rendering.canvas;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.memmaze.MemMazeApplication;
import org.memmaze.R;
import org.memmaze.SelectLevelActivity;
import org.memmaze.maze.Level;
import org.memmaze.maze.LevelNotFoundException;
import org.memmaze.maze.LevelResults;
import org.memmaze.maze.LevelTree;
import org.memmaze.maze.Tile;
import org.memmaze.rendering.InputObject;
import org.memmaze.rendering.TileSet;
import org.memmaze.rendering.resource.StringUtility;
import org.memmaze.rendering.util.TileSizeCalculator;
import org.memmaze.util.MemMazeDebugUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Main game thread. This thread is started by the View and will run for as long as the application is alive.
 * 
 * @author erikmidander
 */
public class MemMazeGameThread extends Thread {

	private static final int MILLIS_PER_FRAME = 32;		// 32 => 30 fps
	
	private SurfaceHolder surfaceHolder;
	private Context context;
	private MemMazeApplication memMazeApplication;

	private boolean running = true;
	private long lastFrameStartTime;
	private int profilingFrameCount = 0;
	private long profilingAggregatedTime = 0;
	private boolean surfaceHasChanged = true;
	private boolean drawingInitialized = false;
	
	protected float gridWidth;
	protected float gridHeight;
	protected float gridTop;
	protected float gridLeft;
	private DrawableTile[][] tiles;
	private float[] gridLines;
	
	private Level level = null;
	private TileSet tileSet = null;

	private int columnCount;
	private int rowCount;
	protected float tileWidth;
	protected float tileHeight;
	private float gridRight;
	private float gridBottom;

	private boolean[][] tileTouched;
	private int touchableTilesCount;

	private int gameState = GAME_STATE_SPLASH;
	private int gameStateBeforePause = GAME_STATE_PAUSED;
	private long countdownStarted;
	private boolean gameOver;
	private boolean success;

	private int currentTouchColumn = -1;
	private int currentTouchRow = -1;

	private static final long TOUCHTIME_NONE = 0;
	private static final float FADE_OUT_TIME = 1000;
	private static final long GAME_OVER_MINIMUM_WAIT_TIME = 500;
	private static final long GAME_OVER_MAXIMUM_WAIT_TIME = 3000;
	private static final int GAMEOVER_VIBRATE_TIME = 50;

	private static final int GAME_STATE_PAUSED = 0;
	private static final int GAME_STATE_MEMORIZE = 1;
	private static final int GAME_STATE_PLAY = 2;
	private static final int GAME_STATE_SPLASH = 3;
	private static final int GAME_STATE_ENDING = 4;
	private static final int GAME_STATE_FINAL_ENDING = 5;
	
	private static int SPLASH_HEADING_TOP = 50;
	private static int SPLASH_HEADING_BOTTOM_MARGIN = 30;

	private static final int SLEEP_TIME_WHEN_PAUSED = 200;

	private static final long REPORT_PROFILING_INTERVAL = 2000;
	private Rect gridGradientRect;
	private Rect gridRect;
	private long gameOverAt;
	
	private String splashHeading;
	private String splashText;
	private String textSuccess;
	private String textFailure;

	private Bitmap memorizeBackgroundBitmap;

	private static final String LOG_TAG_GAMETHREAD = "GameThread";
	private static final String LOG_TAG_PROFILING = "Profiling";

	private static final int LARGEST_ALLOWED_TILE_SIZE = 100;
	private static final int RESOURCE_TILE_SIZE = 50;
	private static final int ENDING_STATUS_MARGIN = 10;
	private static final int FINAL_ENDING_MARGIN = 10;

	private static final int INPUT_QUEUE_SIZE = 30;

	private final LevelTree levelTree;
	private float memorizeTime;
	private Typeface memMazeFont;

	private Paint endingStatusPaintRow1;
	private Paint endingStatusPaintRow2;
	private Rect rectTextBoundsStatusRow1;
	private Rect rectTextBoundsStatusRow2;
	private double levelHighscore;
	private float coverage;
	private Rect goToPremiumButtonRect;
	

	private int colorSuccess = Color.rgb(0, 225, 0);
	private int colorFailure = Color.rgb(225, 0, 0);

	private boolean hasTouchedScreenInPlayState;

	private Stack<String> playedLevelsStack = new Stack<String>();

	public MemMazeGameThread(SurfaceHolder holder, Context context, MemMazeApplication memMazeApplication, String levelId) throws LevelNotFoundException {
		this.surfaceHolder = holder;
		this.context = context;
		this.levelTree = memMazeApplication.getLevelTree();
		this.memMazeApplication = memMazeApplication;
		initWithLevel(levelTree.getLevel(levelId));
	}

	private void initWithLevel(Level level) {
		this.level = level;
		this.columnCount = level.getColumnCount();
		this.rowCount = level.getRowCount();
		this.splashHeading = new StringUtility(context.getResources()).getLevelName(levelTree.getLevelNumber(level.getLevelId()));
		this.splashText = level.getLevelText();
		this.memorizeTime = level.getMemorizeTime();
		this.tileTouched = new boolean[level.getRowCount()][level.getColumnCount()];
		this.touchableTilesCount = level.countTouchableTiles();
		
		this.countdownStarted = 0;
		this.gameOver = false;
		this.currentTouchColumn = -1;
		this.currentTouchRow = -1;		
	}
	
	@Override
	public void run() {
		
		setGameState(GAME_STATE_SPLASH);
		lastFrameStartTime = SystemClock.uptimeMillis();
		
		running = true;
		boolean profiling = MemMazeDebugUtil.appIsInDebugMode(context) && Log.isLoggable(LOG_TAG_PROFILING, Log.DEBUG);
		while (running) {

			processInput();
			
			final long frameStartTime = SystemClock.uptimeMillis();
			long timeDelta = frameStartTime - lastFrameStartTime;

			if (gameState == GAME_STATE_PAUSED) {
				try {
					Thread.sleep(SLEEP_TIME_WHEN_PAUSED);
				} catch (InterruptedException e) {
					// Doesn't matter. We are just being nice.
				}
			} else {
				if (timeDelta > MILLIS_PER_FRAME - 4) {
					lastFrameStartTime = frameStartTime;
					Canvas canvas = null;
					try {
						canvas = surfaceHolder.lockCanvas(null);
						if (canvas != null) {
							if (surfaceHasChanged || !drawingInitialized) {
								initializeDrawing(canvas);
							}
							switch (gameState) {
							case GAME_STATE_PAUSED:
								break;
							case GAME_STATE_MEMORIZE:
								drawFrameMemorize(canvas);
								break;
							case GAME_STATE_PLAY:
								drawFramePlay(canvas);
								break;
							case GAME_STATE_SPLASH:
								drawFrameSplash(canvas);
								break;
							case GAME_STATE_ENDING:
								drawFrameEnding(canvas);
								break;
							case GAME_STATE_FINAL_ENDING:
								drawFrameFinalEnding(canvas);
								break;
							}
						}
					} finally {
						if (canvas != null) {
							surfaceHolder.unlockCanvasAndPost(canvas);
						}
					}
					
	                long frameEndTime = SystemClock.uptimeMillis();
	                timeDelta = frameEndTime - frameStartTime;
	                
	                // profiling:
	                if (profiling) {
		                profilingFrameCount++;
		                profilingAggregatedTime += timeDelta;
		                if (profilingAggregatedTime > REPORT_PROFILING_INTERVAL) {
		                	final long averageFrameTime = (profilingAggregatedTime / profilingFrameCount);
		                	Log.d("Profiling", "FPS = " + (1000 / averageFrameTime));
		                	Log.d("Profiling", "Average frame time = " + averageFrameTime);
		                	
		                	profilingFrameCount = 0;
		                	profilingAggregatedTime = 0;
		                }
	                }
				} 
				
				if (timeDelta < MILLIS_PER_FRAME) {
	                try {
	                    Thread.sleep(MILLIS_PER_FRAME - timeDelta);
	                } catch (InterruptedException e) {
	                    // Interruptions here are no big deal.
	                }
				}
			}
		}
	}

	protected void initializeDrawing(Canvas canvas) {

        memMazeFont = Typeface.createFromAsset(context.getAssets(), "zeroes-one.ttf");  

		TileSizeCalculator tileSizeCalculator = new TileSizeCalculator(LARGEST_ALLOWED_TILE_SIZE);
		int tileSize = tileSizeCalculator.calculateTileSize(canvas.getWidth(), canvas.getHeight(), columnCount, rowCount);		
    	tileSet = new TileSet(this.context.getResources(), level.getTileSet(), tileSize, RESOURCE_TILE_SIZE);

    	// Grid
		tileWidth = tileSize;
		tileHeight = tileSize;
		gridWidth = (tileWidth * columnCount) + 1;
		gridHeight = (tileHeight * rowCount) + 1;

		gridLeft = (canvas.getWidth() - gridWidth) / 2;
		gridTop = (canvas.getHeight() - gridHeight) / 2;
		gridRight = gridLeft + gridWidth - 1;
		gridBottom = gridTop + gridHeight - 1;

		gridRect = new Rect((int)gridLeft, (int)gridTop, (int)gridRight, (int)(gridBottom));
		gridGradientRect = new Rect((int)gridLeft, (int)gridTop, (int)gridRight, (int)(gridTop + gridHeight / 3));
		
		// Tiles
		tiles = new DrawableTile[rowCount][columnCount];
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < columnCount; col++) {
				Tile tile = level.getTile(row, col);
				DrawableTile drawableTile = new DrawableTile();
				drawableTile.tile = tile;
				drawableTile.rect = new RectF(gridLeft + (col * tileWidth), gridTop + (row * tileHeight), gridLeft + ((col + 1) * tileWidth), gridTop + ((row + 1) * tileHeight));
				
				tiles[row][col] = drawableTile;
			}
		}
		
		// Grid lines
		gridLines = new float[4 * (rowCount + 1 + columnCount + 1)];
		int arrPos = 0;
		for (int row = 0; row <= rowCount; row++) {
			gridLines[arrPos++] = gridLeft; // x0
			gridLines[arrPos++] = gridTop + (row * tileHeight); // y0
			gridLines[arrPos++] = gridLeft + gridWidth; // x1
			gridLines[arrPos++] = gridTop + (row * tileHeight); // y1
		}
		for (int col = 0; col <= columnCount; col++) {
			gridLines[arrPos++] = gridLeft + (col * tileWidth); // x0
			gridLines[arrPos++] = gridTop; // y0
			gridLines[arrPos++] = gridLeft + (col * tileWidth); // x1
			gridLines[arrPos++] = gridTop + gridHeight; // y1
		}
		
		// Memorize
		memorizeBackgroundBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
		memorizeBackgroundBitmap.setDensity(canvas.getDensity());
		Canvas memorizeBackgroundCanvas = new Canvas(memorizeBackgroundBitmap);

		drawMemorizeBackground(memorizeBackgroundCanvas);

		// Finish
		textSuccess = context.getResources().getString(R.string.level_success);
		textFailure = context.getResources().getString(R.string.level_failure);
		
		endingStatusPaintRow1 = new Paint();
		endingStatusPaintRow1.setTypeface(memMazeFont);
		endingStatusPaintRow1.setTextAlign(Align.CENTER);
		endingStatusPaintRow1.setTextSize(getScaledFontSize(35));
		endingStatusPaintRow1.setAntiAlias(true);
		endingStatusPaintRow1.setColor(Color.BLACK);

		rectTextBoundsStatusRow1 = new Rect();
		endingStatusPaintRow1.getTextBounds(textSuccess + textFailure, 0, (textSuccess + textFailure).length(), rectTextBoundsStatusRow1);

		endingStatusPaintRow2 = new Paint();
		endingStatusPaintRow2.setTypeface(memMazeFont);
		endingStatusPaintRow2.setTextAlign(Align.CENTER);
		endingStatusPaintRow2.setTextSize(getScaledFontSize(20));
		endingStatusPaintRow2.setAntiAlias(true);
		endingStatusPaintRow2.setColor(Color.BLACK);

		rectTextBoundsStatusRow2 = new Rect();
		String textCompletion = getCoverageText(123456780.9);
		endingStatusPaintRow2.getTextBounds(textCompletion, 0, textCompletion.length(), rectTextBoundsStatusRow2);

		drawingInitialized = true;
		surfaceHasChanged = false;

	}

	private float getScaledFontSize(float size) {
		return size * context.getResources().getDisplayMetrics().density;
	}

	private String getCoverageText(double coveragePart) {
		return new StringUtility(context.getResources()).getLevelScoreText(coveragePart, (coverage > levelHighscore));
	}

	private void drawMemorizeBackground(Canvas canvas) {
		
		Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.rgb(245, 245, 245));
		canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundPaint);

		float backgroundGridLeft = gridLeft;
		int backgroundGridCol = 0;
		while (backgroundGridLeft > 0) { backgroundGridLeft -= tileWidth; backgroundGridCol--;}
		float backgroundGridTop = gridTop;
		int backgroundGridRow = 0;
		while (backgroundGridTop > 0) { backgroundGridTop -= tileHeight; backgroundGridRow--;}
		
		int tileCol = backgroundGridCol;
		for (float tileLeft = backgroundGridLeft; tileLeft < canvas.getWidth(); tileLeft += tileWidth, tileCol++) {
			int tileRow = backgroundGridRow;
			for (float tileTop = backgroundGridTop; tileTop < canvas.getHeight(); tileTop += tileHeight, tileRow++) {
				Tile tile = level.getTile(tileRow, tileCol);
				Bitmap bitmapForTileType = tileSet.getBitmapForTile(tile);
				if (bitmapForTileType != null) {
					canvas.drawBitmap(bitmapForTileType, tileLeft, tileTop, null);
				}
			}
		}

		drawGrid(canvas, Color.argb(128, 155, 155, 155));

	}

	private void drawFrameEnding(Canvas canvas) {

		 if (SystemClock.uptimeMillis() - gameOverAt > GAME_OVER_MAXIMUM_WAIT_TIME) {
			 restartPlayAfterEnding();
			 return;
		 }
		
		drawFramePlay(canvas);
		
		Paint endingStatusPaint = new Paint();
		endingStatusPaint.setStyle(Style.FILL);
		endingStatusPaint.setColor(Color.argb(205, 255, 255, 255));

		int statusHeight = rectTextBoundsStatusRow1.height() + 2 * ENDING_STATUS_MARGIN + (success ? rectTextBoundsStatusRow2.height() + ENDING_STATUS_MARGIN : 0);
		int statusTop = (canvas.getHeight() - statusHeight) / 2;
		Rect rectEndingStatus = new Rect(0, statusTop, canvas.getWidth(), statusTop + statusHeight);
		canvas.drawRect(rectEndingStatus, endingStatusPaint);
		
		canvas.drawText(success ? textSuccess : textFailure, canvas.getWidth() / 2, rectEndingStatus.top + rectTextBoundsStatusRow1.height() + ENDING_STATUS_MARGIN, endingStatusPaintRow1);	
		if (success) {
			canvas.drawText(getCoverageText(coverage), canvas.getWidth() / 2, rectEndingStatus.bottom - ENDING_STATUS_MARGIN, endingStatusPaintRow2);
		}
	}

	private void drawFrameSplash(Canvas canvas) {

		Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLACK);
		canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundPaint);

		// Heading
		Paint paintSplashHeading = new Paint();
		paintSplashHeading.setTypeface(memMazeFont);
		paintSplashHeading.setTextAlign(Align.LEFT);
		paintSplashHeading.setTextSize(getScaledFontSize(24));
		paintSplashHeading.setAntiAlias(true);
		paintSplashHeading.setColor(Color.WHITE);
		canvas.drawText(splashHeading, 0, SPLASH_HEADING_TOP, paintSplashHeading);

		Rect rectSplashHeading = new Rect();
		paintSplashHeading.getTextBounds(splashHeading, 0, splashHeading.length(), rectSplashHeading);

		
		// Level text
		TextPaint paintSplashText = new TextPaint();
		//paintSplashText.setTypeface(memMazeFont);
		paintSplashText.setTextAlign(Align.LEFT);
		paintSplashText.setTextSize(getScaledFontSize(18));
		paintSplashText.setAntiAlias(true);
		paintSplashText.setColor(Color.WHITE);
		
		StaticLayout splashTextLayout = new StaticLayout(splashText, 0, splashText.length(), paintSplashText, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
		canvas.save();
		canvas.translate(0, SPLASH_HEADING_TOP + rectSplashHeading.bottom + SPLASH_HEADING_BOTTOM_MARGIN);
		splashTextLayout.draw(canvas);
		canvas.restore();
		
		
		// GO! 
		TextPaint paintGoText = new TextPaint();
		paintGoText.setTypeface(memMazeFont);
		paintGoText.setTextAlign(Align.LEFT);
		paintGoText.setTextSize(getScaledFontSize(40));
		paintGoText.setAntiAlias(true);
		paintGoText.setColor(Color.GREEN);
		
		Rect rectGoText = new Rect();
		String goText = "GO!";
		paintGoText.getTextBounds(goText, 0, goText.length(), rectGoText);

		canvas.drawText(goText, (canvas.getWidth() - rectGoText.width()) / 2, (canvas.getHeight() + rectGoText.height()) / 2, paintGoText);		

		
		// Display the 'Replay Level X' text
		if (playedLevelsStack.size() > 0) {

			Paint paintReplayLastLevelText = new Paint();
			paintReplayLastLevelText.setTypeface(memMazeFont);
			paintReplayLastLevelText.setTextAlign(Align.LEFT);
			paintReplayLastLevelText.setTextSize(getScaledFontSize(16));
			paintReplayLastLevelText.setAntiAlias(true);
			paintReplayLastLevelText.setColor(Color.WHITE);

			String lastPlayedLevelId = playedLevelsStack.peek();
			String replayLastLevelText = new StringUtility(context.getResources()).getReplayLevelText(levelTree.getLevelNumber(lastPlayedLevelId ));

			Rect rectReplayLastLevelText = new Rect();
			paintReplayLastLevelText.getTextBounds(replayLastLevelText, 0, replayLastLevelText.length(), rectReplayLastLevelText);

			canvas.drawText(replayLastLevelText, 0, canvas.getHeight() - SPLASH_HEADING_BOTTOM_MARGIN, paintReplayLastLevelText);
			replayLastLevelTextTop = canvas.getHeight() - rectReplayLastLevelText.height() - SPLASH_HEADING_BOTTOM_MARGIN;
		
		}
		
	}

	private void drawFrameMemorize(Canvas canvas) {

		long millisCountedDown = SystemClock.uptimeMillis() - countdownStarted;

		float partDone = millisCountedDown / (float)memorizeTime;
		if (millisCountedDown >= memorizeTime) {
			setGameState(GAME_STATE_PLAY);
			return;
		}

		canvas.drawBitmap(memorizeBackgroundBitmap, 0, 0, null);
		
		int[] gradientColors = new int[] {Color.argb(0, 0, 0, 0), Color.argb(0, 0, 0, 0), Color.argb(255, 0, 0, 0)};	// mitten -> utkant
		GradientDrawable darknessGradient = new GradientDrawable(Orientation.LEFT_RIGHT, gradientColors);
		darknessGradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		darknessGradient.setDither(true);
		darknessGradient.setGradientCenter(0.5f, 0.5f);
		darknessGradient.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());		

		float smoothQuota = 1 - partDone * partDone * partDone;
		float gradientRadius = canvas.getHeight() * 2.0f / 2.0f * smoothQuota;
		darknessGradient.setGradientRadius(gradientRadius);
		darknessGradient.draw(canvas);
		
	}

	private void drawFramePlay(Canvas canvas) {

		Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(gameOver ? (success ? colorSuccess : colorFailure) : Color.BLACK);
		canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundPaint);

		long now = SystemClock.uptimeMillis();
		
		Paint paint = new Paint();
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < columnCount; col++) {
				DrawableTile tile = tiles[row][col];
				if (tile.lastTouchedTime == TOUCHTIME_NONE) {
					paint.setColor(Color.BLACK);
				} else {
					long fadeTime = now - tile.lastTouchedTime;
					if (fadeTime > FADE_OUT_TIME) {
						tile.lastTouchedTime = TOUCHTIME_NONE;
						paint.setColor(Color.BLACK);
					} else {
						paint.setColor(getTileFadeColor(fadeTime));
					}
				}
				canvas.drawRect(tile.rect, paint);
				
			}
		}

		drawGrid(canvas, Color.DKGRAY);
		
	}

	private void drawFrameFinalEnding(Canvas canvas) {

		Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLACK);
		canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundPaint);

		Paint paintHeading = new Paint();
		paintHeading.setTypeface(memMazeFont);
		paintHeading.setTextAlign(Align.LEFT);
		paintHeading.setTextSize(getScaledFontSize(32));
		paintHeading.setAntiAlias(true);
		paintHeading.setColor(Color.WHITE);
		String finalEndingHeading = context.getResources().getString(R.string.game_over_heading);
		canvas.drawText(finalEndingHeading, FINAL_ENDING_MARGIN, SPLASH_HEADING_TOP, paintHeading);

		Rect rectHeading = new Rect();
		paintHeading.getTextBounds(finalEndingHeading, 0, finalEndingHeading.length(), rectHeading);

		TextPaint paintText = new TextPaint();
		paintText.setTextAlign(Align.LEFT);
		paintText.setTextSize(getScaledFontSize(20));
		paintText.setAntiAlias(true);
		paintText.setColor(Color.WHITE);
		
		String finalEndingText = context.getResources().getString(memMazeApplication.isPremiumVersion() ? R.string.game_over_text_premium : R.string.game_over_text_free);
		StaticLayout splashTextLayout = new StaticLayout(finalEndingText, 0, finalEndingText.length(), paintText, canvas.getWidth() - 2 * FINAL_ENDING_MARGIN, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
		int finalEndingTextPosition = SPLASH_HEADING_TOP + rectHeading.bottom + SPLASH_HEADING_BOTTOM_MARGIN;
		canvas.save();
		canvas.translate(FINAL_ENDING_MARGIN, finalEndingTextPosition);
		splashTextLayout.draw(canvas);
		canvas.restore();
	
		if (!memMazeApplication.isPremiumVersion()) {
			LinearLayout layout = new LinearLayout(context);
			layout.setGravity(Gravity.CENTER);
			
			TextView goToPremiumButton =  new Button(context); 
			goToPremiumButton.setVisibility(View.VISIBLE);
			goToPremiumButton.setText(context.getResources().getString(R.string.go_to_premium));
			goToPremiumButton.setGravity(Gravity.CENTER);
			layout.addView(goToPremiumButton);
	
			int buttonLayoutTop = finalEndingTextPosition + splashTextLayout.getHeight() + SPLASH_HEADING_BOTTOM_MARGIN;
			layout.measure(canvas.getWidth(), canvas.getHeight() - buttonLayoutTop);
			layout.layout(0, buttonLayoutTop, canvas.getWidth(), canvas.getHeight());
	
			goToPremiumButtonRect = new Rect();
			goToPremiumButton.getHitRect(goToPremiumButtonRect);
			goToPremiumButtonRect.top += buttonLayoutTop;
			goToPremiumButtonRect.bottom += buttonLayoutTop;
			
			canvas.translate(0, buttonLayoutTop);
			layout.draw(canvas);
		}
	}

	private void drawGrid(Canvas c, int gridColor) {
		Paint linePaint = new Paint();
		linePaint.setColor(gridColor);
		c.drawLines(gridLines, linePaint);
		
		Paint borderPaint = new Paint();
		borderPaint.setColor(gridColor);
		borderPaint.setStrokeWidth(3);
		borderPaint.setStyle(Paint.Style.STROKE);
		c.drawRect(gridRect, borderPaint);
	}

	private int getTileFadeColor(long lastTouchedTime) {
		int colorValue = 255 - (int)(255 * lastTouchedTime / FADE_OUT_TIME);
		return Color.rgb(colorValue, colorValue, colorValue);
	}

	public void stopGameLoop() {
		running = false;
	}

	public void onSurfaceChanged(int width, int height) {
		surfaceHasChanged = true;
	}

	private void processMotionEvent(InputObject inputObject) {

		if (gameState == GAME_STATE_PLAY && !gameOver) {
					
			if (inputObject.x < gridLeft || inputObject.x >= gridRight || inputObject.y < gridTop || inputObject.y >= gridBottom) {
				return;
			}
	
			int newTouchColumn = (int)((inputObject.x - gridLeft) / tileWidth);
			int newTouchRow = (int)((inputObject.y - gridTop) / tileHeight);

			boolean sameTileAsLastTouch = newTouchColumn == currentTouchColumn && newTouchRow == currentTouchRow;
			boolean adjacent = currentTouchColumn == -1 || currentTouchRow == -1 ||
								(newTouchColumn == currentTouchColumn && Math.abs(newTouchRow - currentTouchRow) <= 1) ||
								(newTouchRow == currentTouchRow && Math.abs(newTouchColumn - currentTouchColumn) <= 1);
			
			currentTouchColumn = newTouchColumn;
			currentTouchRow = newTouchRow;

			tiles[currentTouchRow][currentTouchColumn].lastTouchedTime = SystemClock.uptimeMillis();

			tileTouched[currentTouchRow][currentTouchColumn] = true;
			
			Tile currentTile = tiles[currentTouchRow][currentTouchColumn].tile;
			if (inputObject.action == MotionEvent.ACTION_DOWN) {
				if (!(currentTile.isStart || sameTileAsLastTouch)) {
					setGameOver(false);
				} 
			} else {
				if (!adjacent) {
					setGameOver(false); 
				} else {
					if (currentTile.isBlock) {
						setGameOver(false);
					} else if (!hasTouchedScreenInPlayState && !currentTile.isStart) {
						setGameOver(false);
					} else if (currentTile.isGoal) {
						setGameOver(true);
					}
				}
			}
			
			hasTouchedScreenInPlayState = true;
			
		} else {
			if (gameState == GAME_STATE_ENDING) {
				 if (SystemClock.uptimeMillis() - gameOverAt > GAME_OVER_MINIMUM_WAIT_TIME) {
					 restartPlayAfterEnding();
				 }
			} else if (gameState == GAME_STATE_SPLASH) {
				if (inputObject.action == MotionEvent.ACTION_DOWN) {
					if (playedLevelsStack.size() > 0 && inputObject.y >= replayLastLevelTextTop) {
						// This means the user touched the "Replay last level" section						
						restartPlayAfterEnding(playedLevelsStack.pop());
					} else {
						countdownStarted = SystemClock.uptimeMillis();
						setGameState(GAME_STATE_MEMORIZE);
					}					
				}
			} else if (gameState == GAME_STATE_MEMORIZE) {
				if (inputObject.action == MotionEvent.ACTION_DOWN) {
					setGameState(GAME_STATE_PLAY);
					hasTouchedScreenInPlayState = false;
				}
			} else if (gameState == GAME_STATE_FINAL_ENDING) {
				if (inputObject.action == MotionEvent.ACTION_DOWN) {
					if (goToPremiumButtonRect.contains((int)inputObject.x, (int)inputObject.y)) {
						showPremiumAppInMarket();
					} else {
						restartPlayAfterEnding(levelTree.getFirstLevelId());
					}
				}
			}
		}

	}

	private void restartPlayAfterEnding() {
		String nextLevelId;
		if (success) {
			if (levelTree.isLastLevel(level.getLevelId())) {
				setGameState(GAME_STATE_FINAL_ENDING);
				return;
			} else {
				nextLevelId = levelTree.getNextLevelId(level.getLevelId());
			}
		} else {
			// Replay same level on failure
			nextLevelId = level.getLevelId();
		}
		
		restartPlayAfterEnding(nextLevelId);
	}
	
	private void restartPlayAfterEnding(String nextLevelId) {
		try {
			initWithLevel(levelTree.getLevel(nextLevelId));
		} catch (LevelNotFoundException e) {
			Log.e(LOG_TAG_GAMETHREAD, String.format("Unexpected level not found for level %s", level.getLevelId()), e);
            Intent intent = new Intent(context, SelectLevelActivity.class);
            context.startActivity(intent);
            return;
		}
		setGameState(GAME_STATE_SPLASH);
		drawingInitialized = false;				
	}
	
	private void setGameOver(boolean success) {
		long now = SystemClock.uptimeMillis();

		this.gameOver = true;
		this.success = success;
		this.gameOverAt = now;
		setGameState(GAME_STATE_ENDING);

		if (success) {
			if (this.playedLevelsStack.size() == 0 || !this.playedLevelsStack.peek().equals(level.getLevelId())) {
				this.playedLevelsStack.push(level.getLevelId());
			}

			coverage = (float)countTouchedTiles() / (float)touchableTilesCount;
			LevelResults levelResults = memMazeApplication.getLevelResults();
			levelHighscore = levelResults.getLevelResult(level.getLevelId());
			levelResults.setLevelResult(level.getLevelId(), coverage);
			memMazeApplication.setLevelResults(levelResults);
		} else {
			vibratePhone();
		}
		
		if (currentTouchRow != -1 && currentTouchColumn != -1) {
			tiles[currentTouchRow][currentTouchColumn].lastTouchedTime = now;
		}
		currentTouchColumn = -1;
		currentTouchRow = -1;
		
	}

	private int countTouchedTiles() {
		int touched = 0;
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < columnCount; col++) {
				if (tileTouched[row][col]) {
					touched++;
				}
			}
		}
		return touched;
	}

	private void setGameState(int newGameState) {
		this.gameState = newGameState;
	}

	private void vibratePhone() {
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(GAMEOVER_VIBRATE_TIME);
	}

	public void pauseGameThread() {
		Log.d(LOG_TAG_GAMETHREAD, String.format("Pausing from game state %d", gameState));
		synchronized (inputQueueMutex) {
			if (gameState == GAME_STATE_PAUSED) return;
			gameStateBeforePause = gameState;
			setGameState(GAME_STATE_PAUSED);
		}
	}

	public void resumeGameThread() {
		synchronized (inputQueueMutex) {
			Log.d(LOG_TAG_GAMETHREAD, String.format("Resuming to game state %d", gameStateBeforePause));
			setGameState(gameStateBeforePause);
		}
	}

	public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
		drawingInitialized = false;
	}

	//private ArrayBlockingQueue<InputObject> inputQueue = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
	private Queue<InputObject> inputQueue = new LinkedList<InputObject>();
	private Object inputQueueMutex = new Object();

	private float replayLastLevelTextTop;

	public void feedInput(InputObject inputObject) {
		synchronized (inputQueueMutex) {
			inputQueue.add(inputObject);
		}
	}

	private void processInput() {
		synchronized (inputQueueMutex) {
			while (!inputQueue.isEmpty()) {
				processMotionEvent(inputQueue.poll());
			}
		}
	}
	
	private void showPremiumAppInMarket() {
		Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.memmaze.premium"));
		marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(marketIntent);
	}
}
