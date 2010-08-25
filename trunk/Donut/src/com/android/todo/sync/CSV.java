package com.android.todo.sync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.database.Cursor;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.android.todo.R;
import com.android.todo.data.ToDoDB;

/**
 * Class that deals with CSV imports and exports.
 */
public final class CSV {
  public static final String PATH = "/sdcard/Tag-ToDo_data/csv/";

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
        } catch (Exception e) {
          // continuing, just in case following lines are correct
          continue;
        }
      }
      reader.close();
      return R.string.import_CSV_success;
    } catch (Exception e) {
      return R.string.import_CSV_bad_structure;
    }
  }

  /**
   * 
   * @param f
   * @param dbHelper
   * @return
   */
  public final static int exportCSV(final File f, final ToDoDB dbHelper) {
    try {
      new File(CSV.PATH).mkdir();
      final FileWriter fw = new FileWriter(f);
      final CSVWriter writer = new CSVWriter(fw);
      final Cursor c = dbHelper.getTasks(null, -1, null);
      if (c.getCount() > 0) {
        c.moveToFirst();
        final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
        final int status = c.getColumnIndex(ToDoDB.KEY_STATUS);
        final int parent = c.getColumnIndex(ToDoDB.KEY_PARENT);
        writer.writeNext(new String[] { "TASK", "CHECKED", "TAG" });
        do {
          try{
          writer.writeNext(new String[] { c.getString(name),
              Integer.toString(c.getInt(status)), c.getString(parent) });
          }catch(Exception e){
            // continuing, just in case following lines are correct
            continue;
          }
        } while (c.moveToNext());
      }
      writer.close();
    } catch (Exception e) {
      return R.string.export_CSV_impossible;
    }

    return R.string.export_CSV_success;
  }
}
