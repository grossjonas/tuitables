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

    boolean quoted = false;
    var stringBuilder = new StringBuilder();
    var currentRow = new ArrayList<String>();

    int currentInt;
    while ((currentInt = pushbackInputStream.read()) != END_OF_STREAM_MARKER) {
      if (DOUBLE_QUOTE == currentInt) {
        if (!quoted) {
          quoted = true;
        } else {
          currentInt = pushbackInputStream.read();

          // two consecutive doube quotes
          if (DOUBLE_QUOTE == currentInt) {
            stringBuilder.append(currentInt);
            pushbackInputStream.unread(currentInt);
          } else {
            currentRow.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            quoted = false;
          }
        }
        continue;
      }

      if (quoted) {
        stringBuilder.append(Character.toChars(currentInt));
      } else {
        if (COMMA == currentInt) {
          currentRow.add(stringBuilder.toString());
          stringBuilder = new StringBuilder();
          continue;
        }
        if ( LF == currentInt) {
          currentInt = pushbackInputStream.read();
          if ( CR == currentInt ) {
            lines.add(currentRow);
            currentRow.clear();
          }
        }
      }
    }
    return lines;
  }
}
