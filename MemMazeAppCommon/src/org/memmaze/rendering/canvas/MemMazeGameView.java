package org.memmaze.rendering.canvas;

import java.lang.Thread.State;

import org.memmaze.MemMazeApplication;
import org.memmaze.maze.LevelNotFoundException;
import org.memmaze.rendering.InputObject;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MemMazeGameView extends SurfaceView implements SurfaceHolder.Callback {

  private MemMazeGameThread gameThread = null;

  public MemMazeGameView(Context context, MemMazeApplication memMazeApplication, String levelId) throws LevelNotFoundException {
    super(context);

    // register our interest in hearing about changes to our surface
    SurfaceHolder holder = getHolder();
    holder.addCallback(this);

    holder.setFormat(PixelFormat.RGBA_8888);
    gameThread = new MemMazeGameThread(holder, this.getContext(), memMazeApplication, levelId);

    setFocusable(true); // make sure we get key events
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    gameThread.onSurfaceChanged(width, height);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    gameThread.setSurfaceHolder(holder);
    Log.d(VIEW_LOG_TAG, "Surface created!");
    if (gameThread.getState() == State.NEW) {
      gameThread.start();
    } else {
      gameThread.resumeGameThread();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(VIEW_LOG_TAG, "Surface destroyed!");
    if (gameThread != null) {
      gameThread.pauseGameThread();
    }
  }

  public void stopGameThread() {
    gameThread.stopGameLoop();
    try {
      gameThread.join();
    } catch (InterruptedException e) {
      ;
    }
    gameThread = null;
  }

  public void onPause() {
    gameThread.pauseGameThread();
  }

  public void onResume() {
    gameThread.resumeGameThread();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int historySize = event.getHistorySize();
    if (historySize > 0) {
      for (int historyIndex = 0; historyIndex < historySize; historyIndex++) {
        gameThread.feedInput(new InputObject(event.getAction(), event.getHistoricalX(historyIndex), event.getHistoricalY(historyIndex)));
      }
    }
    gameThread.feedInput(new InputObject(event.getAction(), event.getX(), event.getY()));
    try {
      Thread.sleep(16);
    } catch (InterruptedException e) {
      ;
    }
    return true;
  }

}
