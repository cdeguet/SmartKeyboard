/*
 * Copyright 2007 Kasper B. Graversen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.supercsv.io;

import java.io.IOException;

import org.supercsv.prefs.CsvPreference;

/**
 * Super Type for all csv writers.
 * 
 * @author Kasper B. Graversen
 */
public interface ICsvWriter {
/**
 * close the stream *
 * 
 * @since 1.0
 */
void close() throws IOException;

/**
 * return the number of lines written so far. The first line is 1 *
 * 
 * @since 1.0
 */
int getLineNumber();

/**
 * Determine how the writer writes the destination. *
 * 
 * @since 1.0
 */
ICsvWriter setPreferences(CsvPreference preference);

/**
 * Write a string array as a header. The elements of the array cannot be null.
 * 
 * @throws IOException
 *             When in IO exception occur
 * @param header
 * @since 1.0
 */
void writeHeader(String... header) throws IOException;

}
