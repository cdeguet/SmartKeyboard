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

package org.supercsv.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.supercsv.exception.SuperCSVException;

/**
 * A utility class for various list/map operations. May be of use to the public as well as to BestCSV
 * 
 * @author Kasper B. Graversen
 */
public abstract class Util {

/**
 * Convert a map to a list
 * 
 * @param source
 *            the map to create a list from
 * @param nameMapping
 *            the keys of the map whose values will constitute the list
 * @return a list
 */
public static List<? extends Object> map2List(final Map<String, ? extends Object> source, final String[] nameMapping) {
	final List<? super Object> result = new ArrayList<Object>(nameMapping.length);
	for( final String element : nameMapping ) {
		result.add(source.get(element));
	}
	return result;
}

/**
 * A function to convert a list to a map using a namemapper for defining the keys of the map.
 * 
 * @param destination
 *            the resulting map instance. The map is cleared before populated
 * @param nameMapper
 *            cannot contain duplicate names
 * @param values
 *            A list of values TODO: see if using an iterator for the values is more efficient
 */
public static <T> void mapStringList(final Map<String, T> destination, final String[] nameMapper, final List<T> values) {
	if( nameMapper.length != values.size() ) {
		throw new SuperCSVException(
			"The namemapper array and the value list must match in size. Number of columns mismatch number of entries for your map.");
	}
	destination.clear();
	
	// map each element of the array
	for( int i = 0; i < nameMapper.length; i++ ) {
		final String key = nameMapper[i];
		
		// null's in the name mapping means skip column
		if( key == null ) {
			continue;
		}
		
		// only perform safe inserts
		if( destination.containsKey(key) ) {
			throw new SuperCSVException("nameMapper array contains duplicate key \"" + key
				+ "\" cannot map the list");
		}
		
		destination.put(key, values.get(i));
	}
}

/**
 * Convert a map to an array of objects
 * 
 * @param values
 *            the values
 * @param nameMapping
 *            the mapping defining the order of the value extract of the map
 * @return the map unfolded as an array based on the nameMapping
 */
public static Object[] stringMap(final Map<String, ? extends Object> values, final String[] nameMapping) {
	final Object[] res = new Object[nameMapping.length];
	int i = 0;
	for( final String name : nameMapping ) {
		res[i++] = values.get(name);
	}
	return res;
}
}
