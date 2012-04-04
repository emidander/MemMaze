package org.memmaze.maze;

public class MemMazePersistenceException extends Exception {
  private static final long serialVersionUID = 1L;

  public MemMazePersistenceException(String description, Exception e) {
    super(description, e);
  }

}
