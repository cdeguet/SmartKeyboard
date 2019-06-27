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

import org.supercsv.util.CSVContext;

/**
 * Wraps the NullPouinterException to ensure only SuperCSVException's are thrown from cell processors.
 * <p>
 * 
 * @since 1.50
 * @author Kasper B. Graversen, (c) 2007
 * @author Dominique de Vito
 */
public class NullInputException extends SuperCSVException {
private static final long serialVersionUID = 1L;

public NullInputException(final String msg) {
	super(msg);
}

public NullInputException(final String msg, final CSVContext context, final Throwable t) {
	super(msg, context, t);
}

}
