package com.grossjonas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SimpleCSVReaderTest {

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.1
  @Test
  void test_2_1() throws Exception {
    final var testValue = "aaa,bbb,ccc\r\nzzz,yyy,xxx\r\n";
    final var expectedValue = List.of(
            List.of("aaa", "bbb", "ccc"),
            List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }


  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.2
  @Test
  void test_2_2() throws Exception {
    final var testValue = "aaa,bbb,ccc\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
            List.of("aaa", "bbb", "ccc"),
            List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.3
  @Test
  void test_2_3() throws Exception {
    final var testValue = "field_name,field_name,field_name\r\naaa,bbb,ccc\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
            List.of("field_name", "field_name", "field_name"),
            List.of("aaa", "bbb", "ccc"),
            List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.4
  @Test
  void test_2_4() throws Exception {
    final var testValue = "aaa,bbb,ccc\r\n";
    final var expectedValue = List.of(
            List.of("aaa", "bbb", "ccc")
    );

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.5
  @Test
  void test_2_5() throws Exception {
    final var testValue = "\"aaa\",\"bbb\",\"ccc\"\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
            List.of("aaa", "bbb", "ccc"),
            List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.6
  @Test
  void test_2_6() throws Exception {
    final var testValue = "\"aaa\",\"b\r\nbb\",\"ccc\"\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
        List.of("aaa", "b\r\nbb", "ccc"),
        List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }

  // https://www.rfc-editor.org/rfc/rfc4180 -> 2.7
  @Test
  void test_2_7() throws Exception {
    final var testValue = "\"aaa\",\"b\"\"bb\",\"ccc\"\r\nzzz,yyy,xxx";
    final var expectedValue = List.of(
            List.of("aaa", "b\"bb", "ccc"),
            List.of("zzz", "yyy", "xxx"));

    final var inputStream = new ByteArrayInputStream(testValue.getBytes(StandardCharsets.UTF_8));

    final var actualValue = new SimpleCSVReader()
            .read(inputStream);

    assertEquals(expectedValue, actualValue);
  }
}
