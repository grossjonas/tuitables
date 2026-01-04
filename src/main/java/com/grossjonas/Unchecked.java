package com.grossjonas;

import java.io.IOException;
import java.io.UncheckedIOException;

interface Unchecked {
  interface IOThrowingSupplier<T> {
    T get() throws IOException;
  }

  static <T> T getUncheckedIO(IOThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (IOException ioException) {
      throw new UncheckedIOException(ioException);
    }
  }

  interface IOThrowingRunnable {
    void run() throws IOException;
  }

  static void runUncheckedIO(IOThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (IOException ioException) {
      throw new UncheckedIOException(ioException);
    }
  }
}
