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

package org.supercsv.exception;

import java.io.Serializable;

import org.supercsv.util.CSVContext;

/** If anything goes wrong, we throw one of these bad boys here */
public class SuperCSVException extends RuntimeException implements Serializable {
private static final long serialVersionUID = 1L;
private CSVContext csvContext;

public SuperCSVException(final String msg) {
	super(msg);
}

public SuperCSVException(final String msg, final CSVContext context) {
	super(msg);
	this.csvContext = context;
}

public SuperCSVException(final String msg, final CSVContext context, final Throwable t) {
	super(t.getMessage() + "\n" + msg, t);
	this.csvContext = context;
}

/**
 * The context may be null when exceptions are thrown before or after processing, such as in cell offendingProcessor's
 * <code>init()</code> methods.
 * 
 * @return null, or the context of the cvs file
 */
public CSVContext getCsvContext() {
	return csvContext;
}

/**
 * Think twice before invoking this...
 * 
 * @param csvContext
 *            the new context
 */
public void setCsvContext(final CSVContext csvContext) {
	this.csvContext = csvContext;
}

@Override
public String toString() {
	return String.format("%s context: %s", getMessage(), csvContext);
}
}
