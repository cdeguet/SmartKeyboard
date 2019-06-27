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
import java.io.Writer;
import java.util.List;

import org.supercsv.prefs.CsvPreference;

/**
 * The writer class capable of writing arrays and lists to a CSV file. Notice that the cell processors can also be
 * utilized when writing (using the <code>org.bestcsv.util</code>). They can help ensure that only numbers are written
 * in numeric columns, that numbers are unique or the output does not contain certain characters or exceed specified
 * string lengths.
 * 
 * @author Kasper B. Graversen
 */
public class CsvListWriter extends AbstractCsvWriter implements ICsvListWriter {

/**
 * Create a CSV writer. Note that the <tt>writer</tt> provided in the argument will be wrapped in a
 * <tt>BufferedWriter</tt> before accessed.
 * 
 * @param stream
 *            Stream to write to
 * @param preference
 *            defines separation character, end of line character, etc.
 */
public CsvListWriter(final Writer stream, final CsvPreference preference) {
	super(stream, preference);
}


/**
 * {@inheritDoc}
 */
@Override
public void write(final List<?> content) throws IOException {
	super.write(content);
}

/**
 * {@inheritDoc}
 */
@Override
public void write(final Object... content) throws IOException {
	super.write(content);
}

/**
 * {@inheritDoc}
 */
@Override
public void write(final String... content) throws IOException {
	super.write(content);
}
}
