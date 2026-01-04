package com.grossjonas;

import jexer.*;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import static com.grossjonas.Unchecked.runUncheckedIO;
import static com.grossjonas.Unchecked.getUncheckedIO;

interface App {

  class Application extends TApplication {
    private static final int MY_MENU_ID = 1025;

    public Application() throws UnsupportedEncodingException {
      super(BackendType.XTERM);

      final var i18n = ResourceBundle.getBundle(TApplication.class.getName());

      final var fileMenu = addMenu(i18n.getString("fileMenuTitle"));
      fileMenu.addDefaultItem(TMenu.MID_EXIT);

      // see "addTableMenu()"
      final var tableMenu = addMenu(i18n.getString("tableMenuTitle"));
      tableMenu.addDefaultItem(TMenu.MID_TABLE_FILE_OPEN_CSV);
      tableMenu.addItem(MY_MENU_ID, "My load csv file");
    }

    @Override
    protected boolean onMenu(TMenuEvent menuEvent) {
      return switch (menuEvent.getId()) {
        case TMenu.MID_TABLE_FILE_OPEN_CSV -> onFileOpenCsvSelected(this);
        case MY_MENU_ID -> onMyMenuIdSelected(this);
        default -> super.onMenu(menuEvent);
      };
    }

    static boolean onMyMenuIdSelected(Application application) {
      final var userHome = getUserHome();

      final var filename = getUncheckedIO(
          () -> application.fileOpenBox(userHome, TFileOpenBox.Type.OPEN, ".*\\.csv"));
      if (Objects.isNull(filename)) {
        application
            .messageBox("Error", "No file selected!")
            .show();
        return false;
      }

      final var file = new File(filename);
      if (!file.canRead()) {
        application
            .messageBox(
                "Error",
                "Can not read file: " + filename,
                TMessageBox.Type.OK)
            .show();
        return false;
      }

      final var window = new TWindow(
          application,
          "Table",
          application.getDesktop().getWidth(),
          application.getDesktop().getHeight());

      final var table = new TTable(
          window, 0, 0,
          application.getDesktop().getWidth() - 2,
          application.getDesktop().getHeight() - 2);

      clearTable(table);

      throw new UnsupportedOperationException("Unimplemented method 'onMyMenuIdSelected'");
    }

    static boolean onFileOpenCsvSelected(TApplication application) {
      final var userHome = getUserHome();

      final var filename = getUncheckedIO(
          () -> application.fileOpenBox(userHome, TFileOpenBox.Type.OPEN, ".*\\.csv"));
      if (Objects.isNull(filename)) {
        application
            .messageBox("Error", "No file selected!")
            .show();
        return false;
      }

      final var file = new File(filename);
      if (!file.canRead()) {
        application
            .messageBox(
                "Error",
                "Can not read file: " + filename,
                TMessageBox.Type.OK)
            .show();
        return false;
      }

      final var window = new TWindow(
          application,
          "Table",
          application.getDesktop().getWidth(),
          application.getDesktop().getHeight());

      final var table = new TTable(
          window, 0, 0,
          application.getDesktop().getWidth() - 2,
          application.getDesktop().getHeight() - 2);

      clearTable(table);

      runUncheckedIO(() -> loadCsvFile(table, file));

      useFirstRowAsHeader(table);

      return true;
    }

    static String getUserHome() {
      final var userHome = System.getProperty("user.home");
      if (Objects.isNull(userHome)) {
        throw new UncheckedIOException(
            new IOException("Could not determine java property 'user.home' to set as starting directory."));
      }
      return userHome;
    }

    static void clearTable(TTable table) {
      if (table.getRowCount() > 0) {
        do {
          table.deleteRow(table.getRowCount() - 1);
        } while (table.getRowCount() > 1);
      }
    }

    static void useFirstRowAsHeader(TTable table) {
      if (table.getRowCount() > 0) {
        IntStream
            .range(0, table.getColumnCount())
            .forEach(index -> {
              final var text = table.getCellText(index, 0);
              table.setColumnLabel(index, text);
            });
        table.deleteRow(0);
      }
    }

    // taken from jexer's TTable
    /**
     * Load contents from file in CSV format.
     *
     * @param csvFile a File referencing the CSV data
     * @throws IOException if a java.io operation throws
     */
    static void loadCsvFile(final TTable table, final File csvFile) throws IOException {
      BufferedReader reader = null;

      try {
        reader = new BufferedReader(new FileReader(csvFile));

        String line = null;
        boolean first = true;
        for (line = reader.readLine(); line != null; line = reader.readLine()) {

          List<String> list = fromCsv(line);
          if (list.size() == 0) {
            continue;
          }

          if (list.size() > table.getRowCount()) {
            int n = list.size() - table.getRowCount();
            for (int i = 0; i < n; i++) {
              table.setSelectedColumnNumber(table.getColumnCount() - 1);
              table.insertColumnRight(table.getSelectedColumnNumber());
            }
          }
          assert (list.size() == table.getColumnCount());

          if (first) {
            // First row: just replace what is here.
            table.setSelectedRowNumber(0);
            first = false;
          } else {
            // All other rows: append to the end.
            table.setSelectedRowNumber(table.getRowCount() - 1);
            table.insertRowBelow(table.getSelectedRowNumber());
            table.setSelectedRowNumber(table.getRowCount() - 1);
          }
          for (int i = 0; i < list.size(); i++) {
            table.setCellText(i, table.getSelectedRowNumber(), list.get(i));
          }
        }
      } finally {
        if (reader != null) {
          reader.close();
        }
      }

      // I am not sure, why this was set
      // left = 0;
      // top = 0;
      table.setSelectedCell(0, 0);

      for (int i = 0; i < table.getColumnCount(); i++) {
        table.setColumnWidthAuto(i, table.getWidth() / 2);
      }

      // alignGrid();
      // activate(columns.get(selectedColumn).get(selectedRow));
      table.activate();
    }

    // taken from jexer's StringUtils
    /**
     * Read a line of RFC4180 comma-separated values (CSV) into a list of
     * strings.
     *
     * @param line the CSV line, with or without without line terminators
     * @return the list of strings
     */
    public static List<String> fromCsv(final String line) {
      List<String> result = new ArrayList<String>();

      StringBuilder str = new StringBuilder();
      boolean quoted = false;
      boolean fieldQuoted = false;

      for (int i = 0; i < line.length(); i++) {
        char ch = line.charAt(i);

        /*
         * System.err.println("ch '" + ch + "' str '" + str + "' " +
         * " fieldQuoted " + fieldQuoted + " quoted " + quoted);
         */

        if (ch == ',') {
          if (fieldQuoted && quoted) {
            // Terminating a quoted field.
            result.add(str.toString());
            str = new StringBuilder();
            quoted = false;
            fieldQuoted = false;
          } else if (fieldQuoted) {
            // Still waiting to see the terminating quote for this
            // field.
            str.append(ch);
          } else if (quoted) {
            // An unmatched double-quote and comma. This should be
            // an invalid sequence. We will treat it as a quote
            // terminating the field.
            str.append('\"');
            result.add(str.toString());
            str = new StringBuilder();
            quoted = false;
            fieldQuoted = false;
          } else {
            // A field separator.
            result.add(str.toString());
            str = new StringBuilder();
            quoted = false;
            fieldQuoted = false;
          }
          continue;
        }

        if (ch == '\"') {
          if ((str.length() == 0) && (!fieldQuoted)) {
            // The opening quote to a quoted field.
            fieldQuoted = true;
          } else if (quoted) {
            // This is a double-quote.
            str.append('\"');
            quoted = false;
          } else {
            // This is the beginning of a quote.
            quoted = true;
          }
          continue;
        }

        // Normal character, pass it on.
        str.append(ch);
      }

      // Include the final field.
      result.add(str.toString());

      return result;
    }

  }

  static void main(String[] args) throws UnsupportedEncodingException {
    final var application = new Application();
    application.run();
  }

}
