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
import java.util.List;

import org.supercsv.exception.SuperCSVException;

/**
 * Supertype for all writers using lists
 * 
 * @author Kasper B. Graversen
 */
public interface ICsvListWriter extends ICsvWriter {
/**
 * Plain writing a list of strings. If the array is empty, an exception will be thrown to signal a possible error in the
 * user code
 * 
 * @since 1.0
 */
void write(List<? extends Object> content) throws IOException;


/**
 * Plain writing a list of Objects. Each object will be converted to a string by calling the <code>toString()</code>
 * on it. If the array is empty, an exception will be thrown to signal a possible error in the user code
 * 
 * @since 1.0
 */
void write(Object... content) throws IOException, SuperCSVException;

/**
 * Plain writing a list of strings If the array is empty, an exception will be thrown to signal a possible error in the
 * user code
 * 
 * @since 1.0
 */
void write(String... content) throws IOException, SuperCSVException;

}
