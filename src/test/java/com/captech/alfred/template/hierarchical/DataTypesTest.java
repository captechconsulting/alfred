/*
 * Copyright 2018 CapTech Ventures, Inc.
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

package com.captech.alfred.template.hierarchical;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataTypesTest {

    @Test
    public void testDate() {
        String result = DataTypes.guessDataType("2002-10-10+13:00");
        assertEquals("date", result);
    }

    @Test
    public void testTime() {
        String result = DataTypes.guessDataType("13:20:00-05:00");
        assertEquals("timestamp", result);
    }

    @Test
    public void testDateTime() {
        String result = DataTypes.guessDataType("2017-06-01T12:30:04-04:00");
        assertEquals("timestamp", result);
    }

    @Test
    public void testInteger() {
        String result = DataTypes.guessDataType("123456");
        assertEquals("int", result);
    }

    @Test
    public void testDecimal() {
        String result = DataTypes.guessDataType("123.456");
        assertEquals("decimal", result);
    }

    @Test
    public void testFloat() {
        String result = DataTypes.guessDataType("1.234E5");
        assertEquals("float", result);
    }

    @Test
    public void testString() {
        String result = DataTypes.guessDataType("general junk");
        assertEquals("string", result);
    }

}
