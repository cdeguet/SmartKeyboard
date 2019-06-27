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
import java.io.Reader;
import java.util.List;

import org.supercsv.prefs.CsvPreference;

/**
 * A simple reader, reading a line from a CSV file into a <code>List</code>. This low-level approach to CSV-reading
 * should be considered a last resort when the more elaborate schemes do not fit your purpose.
 * 
 * @author Kasper B. Graversen
 */
public class CsvListReader extends AbstractCsvReader implements ICsvListReader {
/**
 * Create a csv reader with a specific preference. Note that the <tt>reader</tt> provided in the argument will be
 * wrapped in a <tt>BufferedReader</tt> before accessed.
 */
public CsvListReader(final Reader reader, final CsvPreference preferences) {
	setPreferences(preferences);
	setInput(reader);
}

/**
 * {@inheritDoc}
 */
public List<String> read() throws IOException {
	if( tokenizer.readStringList(super.line) ) { return super.line; }
	return null;
}

}
