package com.sociodyne;

public class LockingHolder<V> {

  private V value;
  private boolean locked = false;

  public LockingHolder() {
  }

  /**
   * Sets the starting value, but does not lock the value. Useful for setting
   * defaults that
   * may be overridden.
   */
  public LockingHolder(V value) {
    this.value = value;
  }

  public void set(V value) {
    if (locked) {
      throw new LockedException(value.toString());
    }

    this.value = value;
  }

  public void lock() {
    locked = true;
  }

  public synchronized void setAndLock(V value) {
    this.set(value);
    this.lock();
  }

  public void unlock() {
    locked = false;
  }

  public boolean isLocked() {
    return locked;
  }

  public V get() {
    return value;
  }

  public static <T> LockingHolder<T> of(T value) {
    return new LockingHolder<T>(value);
  }

  public static class LockedException extends IllegalStateException {

    private static final long serialVersionUID = 4978982546611675958L;

    public LockedException(String message, Throwable cause) {
      super(message, cause);
    }

    public LockedException(String message) {
      super(message);
    }

    public LockedException(Throwable cause) {
      super(cause);
    }
  }
}
