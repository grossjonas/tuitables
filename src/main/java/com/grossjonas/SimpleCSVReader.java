package com.grossjonas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SimpleCSVReader {

  private static final int CR = 13;
  private static final int LF = 10;
  private static final int COMMA = 44;
  private static final int DOUBLE_QUOTE = 34;

  private static final int END_OF_STREAM_MARKER = -1;

  public List<List<String>> read(final File file) throws FileNotFoundException, IOException {
    try (final var inputStream = new FileInputStream(file)) {
      return read(inputStream);
    }
  }

  // https://www.rfc-editor.org/rfc/rfc4180
  List<List<String>> read(InputStream inputStream) throws IOException {
    final var pushbackInputStream = new PushbackInputStream(inputStream, 2);

    // state:
    final var lines = new ArrayList<List<String>>();

    boolean quoteStarted = false;
    var stringBuilder = new StringBuilder();
    var currentRow = new ArrayList<String>();

    int currentByte;
    while ((currentByte = pushbackInputStream.read()) != END_OF_STREAM_MARKER) {
      if (DOUBLE_QUOTE == currentByte) {
        if (!quoteStarted) {
          quoteStarted = true;
        } else {
          currentByte = pushbackInputStream.read();

          // two consecutive doube quotes
          if (DOUBLE_QUOTE == currentByte) {
            stringBuilder.append(currentByte);
          } else {
            quoteStarted = false;
          }
        }
        continue;
      }

      if (quoteStarted) {
        stringBuilder.append(currentByte);
      } else {
        if (COMMA == currentByte) {
          currentRow.add(stringBuilder.toString());
          stringBuilder = new StringBuilder();
          continue;
        }
        if (CR == currentByte) {
          currentByte = pushbackInputStream.read();
          if (LF == currentByte) {
            lines.add(currentRow);
            currentRow.clear();
          }
        }
      }
    }
    return lines;
  }
}
