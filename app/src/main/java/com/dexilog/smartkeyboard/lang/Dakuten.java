/*
 * Copyright (C) 2010-2017 Cyril Deguet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dexilog.smartkeyboard.lang;

import java.util.HashMap;

public class Dakuten {

    // Table for dakuten handling in T9
    private static final HashMap<Character, Character> DAKUTEN_TABLE = new HashMap<Character, Character>() {{
        put('\u3042', '\u3041'); put('\u3044', '\u3043'); put('\u3046', '\u3045'); put('\u3048', '\u3047'); put('\u304a', '\u3049');
        put('\u3041', '\u3042'); put('\u3043', '\u3044'); put('\u3045', '\u30f4'); put('\u3047', '\u3048'); put('\u3049', '\u304a');
        put('\u304b', '\u304c'); put('\u304d', '\u304e'); put('\u304f', '\u3050'); put('\u3051', '\u3052'); put('\u3053', '\u3054');
        put('\u304c', '\u304b'); put('\u304e', '\u304d'); put('\u3050', '\u304f'); put('\u3052', '\u3051'); put('\u3054', '\u3053');
        put('\u3055', '\u3056'); put('\u3057', '\u3058'); put('\u3059', '\u305a'); put('\u305b', '\u305c'); put('\u305d', '\u305e');
        put('\u3056', '\u3055'); put('\u3058', '\u3057'); put('\u305a', '\u3059'); put('\u305c', '\u305b'); put('\u305e', '\u305d');
        put('\u305f', '\u3060'); put('\u3061', '\u3062'); put('\u3064', '\u3063'); put('\u3066', '\u3067'); put('\u3068', '\u3069');
        put('\u3060', '\u305f'); put('\u3062', '\u3061'); put('\u3063', '\u3065'); put('\u3067', '\u3066'); put('\u3069', '\u3068');
        put('\u3065', '\u3064'); put('\u30f4', '\u3046');
        put('\u306f', '\u3070'); put('\u3072', '\u3073'); put('\u3075', '\u3076'); put('\u3078', '\u3079'); put('\u307b', '\u307c');
        put('\u3070', '\u3071'); put('\u3073', '\u3074'); put('\u3076', '\u3077'); put('\u3079', '\u307a'); put('\u307c', '\u307d');
        put('\u3071', '\u306f'); put('\u3074', '\u3072'); put('\u3077', '\u3075'); put('\u307a', '\u3078'); put('\u307d', '\u307b');
        put('\u3084', '\u3083'); put('\u3086', '\u3085'); put('\u3088', '\u3087');
        put('\u3083', '\u3084'); put('\u3085', '\u3086'); put('\u3087', '\u3088');
        put('\u308f', '\u308e');
        put('\u308e', '\u308f');
        put('\u309b', '\u309c');
        put('\u309c', '\u309b');
    }};

    public static char convertDakuten(char c) {
        Character result = DAKUTEN_TABLE.get(c);
        if (result != null) {
            return result;
        } else {
            return c;
        }
    }

}
