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

package com.dexilog.smartkeyboard.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** {@hide} */
public class XmlUtils
{

    public static void skipCurrentTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && (type != XmlPullParser.END_TAG
                       || parser.getDepth() > outerDepth)) {
        }
    }


    public static final boolean
    convertValueToBoolean(CharSequence value, boolean defaultValue)
    {
        boolean result = false;

        if (null == value)
            return defaultValue;

        if (value.equals("1")
        ||  value.equals("true")
        ||  value.equals("TRUE"))
            result = true;

        return result;
    }

    public static final int
    convertValueToInt(CharSequence charSeq, int defaultValue)
    {
        if (null == charSeq)
            return defaultValue;

        String nm = charSeq.toString();

        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!
        
        int sign = 1;
        int index = 0;
        int len = nm.length();
        int base = 10;

        if ('-' == nm.charAt(0)) {
            sign = -1;
            index++;
        }

        if ('0' == nm.charAt(index)) {
            //  Quick check for a zero by itself
            if (index == (len - 1))
                return 0;

            char    c = nm.charAt(index + 1);

            if ('x' == c || 'X' == c) {
                index += 2;
                base = 16;
            } else {
                index++;
                base = 8;
            }
        }
        else if ('#' == nm.charAt(index))
        {
            index++;
            base = 16;
        }

        return Integer.parseInt(nm.substring(index), base) * sign;
    }

    public static final int
    convertValueToUnsignedInt(String value, int defaultValue)
    {
        if (null == value)
            return defaultValue;

        return parseUnsignedIntAttribute(value);
    }

    public static final int
    parseUnsignedIntAttribute(CharSequence charSeq)
    {        
        String  value = charSeq.toString();

        int     index = 0;
        int     len = value.length();
        int     base = 10;
        
        if ('0' == value.charAt(index)) {
            //  Quick check for zero by itself
            if (index == (len - 1))
                return 0;
            
            char    c = value.charAt(index + 1);
            
            if ('x' == c || 'X' == c) {     //  check for hex
                index += 2;
                base = 16;
            } else {                        //  check for octal
                index++;
                base = 8;
            }
        } else if ('#' == value.charAt(index)) {
            index++;
            base = 16;
        }
        
        return (int) Long.parseLong(value.substring(index), base);
    }

 

    /**
     * Read a flattened object from an XmlPullParser.  The XML data could
     * previously have been written with writeMapXml(), writeListXml(), or
     * writeValueXml().  The XmlPullParser must be positioned <em>at</em> the
     * tag that defines the value.
     * 
     * @param parser The XmlPullParser from which to read the object.
     * @param name An array of one string, used to return the name attribute
     *             of the value's tag.
     * 
     * @return Object The newly generated value object.
     * 
     * @see #readMapXml
     * @see #readListXml
     * @see #writeValueXml
     */
    public static final Object readValueXml(XmlPullParser parser, String[] name)
    throws XmlPullParserException, java.io.IOException
    {
        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                return readThisValueXml(parser, name);
            } else if (eventType == XmlPullParser.END_TAG) {
                throw new XmlPullParserException(
                    "Unexpected end tag at: " + parser.getName());
            } else if (eventType == XmlPullParser.TEXT) {
                throw new XmlPullParserException(
                    "Unexpected text: " + parser.getText());
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
            "Unexpected end of document");
    }

    private static final Object readThisValueXml(XmlPullParser parser, String[] name)
    throws XmlPullParserException, java.io.IOException
    {
        final String valueName = parser.getAttributeValue(null, "name");
        final String tagName = parser.getName();

        //System.out.println("Reading this value tag: " + tagName + ", name=" + valueName);

        Object res;

        if (tagName.equals("null")) {
            res = null;
        } else if (tagName.equals("string")) {
            String value = "";
            int eventType;
            while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("string")) {
                        name[0] = valueName;
                        //System.out.println("Returning value for " + valueName + ": " + value);
                        return value;
                    }
                    throw new XmlPullParserException(
                        "Unexpected end tag in <string>: " + parser.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    value += parser.getText();
                } else if (eventType == XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(
                        "Unexpected start tag in <string>: " + parser.getName());
                }
            }
            throw new XmlPullParserException(
                "Unexpected end of document in <string>");
        } else if (tagName.equals("int")) {
            res = Integer.parseInt(parser.getAttributeValue(null, "value"));
        } else if (tagName.equals("long")) {
            res = Long.valueOf(parser.getAttributeValue(null, "value"));
        } else if (tagName.equals("float")) {
            res = new Float(parser.getAttributeValue(null, "value"));
        } else if (tagName.equals("double")) {
            res = new Double(parser.getAttributeValue(null, "value"));
        } else if (tagName.equals("boolean")) {
            res = Boolean.valueOf(parser.getAttributeValue(null, "value"));
        } else {
            throw new XmlPullParserException(
                "Unknown tag: " + tagName);
        }

        // Skip through to end tag.
        int eventType;
        while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(tagName)) {
                    name[0] = valueName;
                    //System.out.println("Returning value for " + valueName + ": " + res);
                    return res;
                }
                throw new XmlPullParserException(
                    "Unexpected end tag in <" + tagName + ">: " + parser.getName());
            } else if (eventType == XmlPullParser.TEXT) {
                throw new XmlPullParserException(
                "Unexpected text in <" + tagName + ">: " + parser.getName());
            } else if (eventType == XmlPullParser.START_TAG) {
                throw new XmlPullParserException(
                    "Unexpected start tag in <" + tagName + ">: " + parser.getName());
            }
        }
        throw new XmlPullParserException(
            "Unexpected end of document in <" + tagName + ">");
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG
                   && type != XmlPullParser.END_DOCUMENT) {
            ;
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        
        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }
    
    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG
                   && type != XmlPullParser.END_DOCUMENT) {
            ;
        }   
    }
}
