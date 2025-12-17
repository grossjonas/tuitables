package com.grossjonas;

import jexer.*;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.ResourceBundle;

interface App {
    class Application extends TApplication {

        public Application() throws UnsupportedEncodingException {
            super(BackendType.XTERM);

            final var i18n = ResourceBundle.getBundle(TApplication.class.getName());

            final var fileMenu = addMenu(i18n.getString("fileMenuTitle"));
            fileMenu.addDefaultItem(TMenu.MID_EXIT);

            // see "addTableMenu()"
            final var tableMenu = addMenu(i18n.getString("tableMenuTitle"));
            tableMenu.addDefaultItem(TMenu.MID_TABLE_FILE_OPEN_CSV);
        }

        @Override
        protected boolean onMenu(TMenuEvent menuEvent) {
            return switch (menuEvent.getId()){
                case TMenu.MID_TABLE_FILE_OPEN_CSV -> {
                    final var userHome = System.getProperty("user.home");
                    if(Objects.isNull(userHome)){
                        throw new UncheckedIOException(new IOException("Could not determine java property 'user.home' to set as starting directory."));
                    }

                    final String filename;
                    try {
                        filename = fileOpenBox(userHome, TFileOpenBox.Type.OPEN, ".*\\.csv");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    if (filename != null) {
                        final var file = new File(filename);
                        if(file.canRead()) {
                            final var window = new TWindow(this, "Table", this.getDesktop().getWidth(), this.getDesktop().getHeight());

                            final var table = new TTable(window, 0, 0, this.getDesktop().getWidth() - 2, this.getDesktop().getHeight() - 2);

                            try {
                                table.loadCsvFile(file);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }

                            yield true;
                        }
                    }

                    yield false;
                }
                default -> super.onMenu(menuEvent);
            };
        }
    }


    static void main(String[] args) throws UnsupportedEncodingException {
        final var application = new Application();
        application.run();
    }
}
