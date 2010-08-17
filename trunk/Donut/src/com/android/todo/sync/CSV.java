package com.android.todo.sync;

import java.io.File;
import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;

import com.android.todo.R;
import com.android.todo.data.ToDoDB;

/**
 * Class that deals with CSV imports and exports.
 */
public final class CSV {

  /**
   * 
   * @param f
   * @return
   */
  public final static int importCSV(final File f, final ToDoDB dbHelper) {
    try {
      final CSVReader reader = new CSVReader(new FileReader(f));
      String[] line;
      // not interested in the first line
      if (reader.readNext().length < 3) { // there are no tags, so we'll create
                                          // one
        dbHelper.createTag(f.getName());
      }
      while ((line = reader.readNext()) != null) {
        try {
          switch (line.length) {
            case 1:
              line[0] = ToDoDB.sanitize(line[0]);
              dbHelper.createTask(f.getName(), line[0]);
              break;
            case 2:
              line[0] = ToDoDB.sanitize(line[0]);
              dbHelper.createTask(f.getName(), line[0]);
              dbHelper.updateTask(line[0], Integer.valueOf(line[1]) == 1);
            case 3:
              line[0] = ToDoDB.sanitize(line[0]);
              line[2] = ToDoDB.sanitize(line[2]);
              dbHelper.createTag(line[2]);
              dbHelper.createTask(line[2], line[0]);
              dbHelper.updateTask(line[0], Integer.valueOf(line[1]) == 1);
          }
        } catch (Exception e2) {
          // continuing, just in case following lines are correct
          continue;
        }
      }
      return R.string.import_CSV_success;
    } catch (Exception e) {
      e.printStackTrace();
      return R.string.import_CSV_bad_structure;
    }
  }
}
