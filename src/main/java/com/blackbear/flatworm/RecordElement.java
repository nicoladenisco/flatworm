/**
 * Flatworm - A Java Flat File Importer Copyright (C) 2004 James M. Turner Extended by James Lawrence - 2005
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
package com.blackbear.flatworm;

import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import java.util.HashMap;
import java.util.Map;
import org.commonlib5.utils.StringOper;

/**
 * Bean class used to store the values from the Record-Element XML tag
 */
public class RecordElement implements LineElement
{
  protected Integer fieldEnd;
  protected Integer fieldStart;
  protected Integer fieldLength;
  protected Integer spacerLength;
  protected char fieldType;
  protected String beanRef;
  protected String type;
  protected final Map<String, ConversionOption> conversionOptions = new HashMap<String, ConversionOption>();
  protected boolean optional = false;

  public RecordElement()
  {
    fieldEnd = null;
    fieldStart = null;
    fieldLength = null;
    spacerLength = null;
    fieldType = '\0';
    beanRef = null;
    type = null;
  }

  public boolean isFieldStartSet()
  {
    return fieldStart != null;
  }

  public boolean isFieldEndSet()
  {
    return fieldEnd != null;
  }

  public boolean isFieldLengthSet()
  {
    return fieldLength != null;
  }

  public int getFieldStart()
     throws FlatwormUnsetFieldValueException
  {
    if(fieldStart == null)
      throw new FlatwormUnsetFieldValueException("fieldStart is unset");
    else
      return fieldStart;
  }

  public void setFieldStart(int fieldStart)
  {
    this.fieldStart = fieldStart;
  }

  public int getFieldEnd()
     throws FlatwormUnsetFieldValueException
  {
    if(fieldEnd == null)
      throw new FlatwormUnsetFieldValueException("fieldEnd is unset");
    else
      return fieldEnd;
  }

  public void setFieldEnd(int fieldEnd)
  {
    this.fieldEnd = fieldEnd;
  }

  public int getFieldLength()
     throws FlatwormUnsetFieldValueException
  {
    if(fieldLength == null)
      if(!(isFieldStartSet() && isFieldEndSet()))
        throw new FlatwormUnsetFieldValueException("length is unset");
      else
        // Derive length from start and end position
        return fieldEnd - fieldStart;
    else
      return fieldLength;
  }

  public void setFieldLength(int fieldLength)
  {
    this.fieldLength = fieldLength;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public Map<String, ConversionOption> getConversionOptions()
  {
    return conversionOptions;
  }

  public void setConversionOptions(Map<String, ConversionOption> conversionOptions)
  {
    this.conversionOptions.clear();
    this.conversionOptions.putAll(conversionOptions);
  }

  public void addConversionOption(String name, ConversionOption option)
  {
    conversionOptions.put(name, option);
  }

  public void addConversionOption(String name, String value)
  {
    conversionOptions.put(name, new ConversionOption(name, value));
  }

  @Override
  public String getBeanRef()
  {
    return beanRef;
  }

  public void setBeanRef(String beanRef)
  {
    this.beanRef = beanRef;
  }

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public void saveBuiltInOptions()
  {
    addConversionOption("fieldEnd", StringOper.okStr(fieldEnd));
    addConversionOption("fieldStart", StringOper.okStr(fieldStart));
    addConversionOption("fieldLength", StringOper.okStr(fieldLength));
    addConversionOption("spacerLength", StringOper.okStr(spacerLength));
    addConversionOption("fieldType", StringOper.okStr(fieldType));
    addConversionOption("beanRef", StringOper.okStr(beanRef));
    addConversionOption("type", StringOper.okStr(type));
    addConversionOption("optional", StringOper.okStr(optional));
  }

  @Override
  public String toString()
  {
    return "RecordElement{" + beanRef + ", fieldStart=" + fieldStart + ", fieldEnd=" + fieldEnd + ", fieldLength=" + fieldLength + ", type=" + type + '}';
  }
}
