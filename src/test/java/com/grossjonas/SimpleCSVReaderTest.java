package com.grossjonas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SimpleCSVReaderTest {

  @Test
  void test() throws Exception {
    // https://www.rfc-editor.org/rfc/rfc4180 -> 2.6
    final var testValue = "\"aaa\",\"b\r\nbb\",\"ccc\"\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
        List.of("aaa", "b\r\nbb", "ccc"),
        List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }
}
