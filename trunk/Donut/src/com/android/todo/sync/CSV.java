package com.android.todo.sync;

import java.io.File;
import java.io.FileReader;

import com.android.todo.R;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Class that deals with CSV imports and exports.
 */
public final class CSV {

  /**
   * 
   * @param f
   * @return
   */
  public final static int importCSV(final File f) {
    try {
      final CSVReader reader = new CSVReader(new FileReader(f));
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        // nextLine[] is an array of values from the line
        System.out.println(nextLine[0] + nextLine[1] + "etc...");
      }
      return R.string.import_CSV_success;
    } catch (Exception e) {
      return R.string.import_CSV_bad_structure;
    }
  }
}
