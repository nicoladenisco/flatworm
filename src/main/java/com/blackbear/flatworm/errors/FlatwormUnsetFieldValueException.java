/**
 * Flatworm - A Java Flat File Importer Copyright (C) 2004 James M. Turner
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */

package com.blackbear.flatworm.errors;

/**
 * Exception thrown if code attempts to access the start, end or length of a field if it is not set.
 */

public class FlatwormUnsetFieldValueException extends FlatwormException
{

    public FlatwormUnsetFieldValueException(String s)
    {
        super(s);
    }
}