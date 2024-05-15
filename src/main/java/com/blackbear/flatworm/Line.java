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

import com.blackbear.flatworm.errors.FlatwormConversionException;
import com.blackbear.flatworm.errors.FlatwormCreatorException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import com.blackbear.flatworm.errors.FlatwormInvalidRecordException;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean class used to store the values from the Line XML tag
 */
public class Line
{
  private static final Log log = LogFactory.getLog(Line.class);

  protected List<LineElement> elements = new ArrayList<LineElement>();
  protected String delimit = null;
  protected char chrQuote = '\0';
  protected ConversionHelper convHelper;
  protected Map<String, Object> beans;
  protected BeanMappingStrategy mappingStrategy = new PropertyUtilsMappingStrategy();

  // properties used for processing delimited input
  protected String[] delimitedFields;
  protected int currentField = 0;

  public Line()
  {
  }

  /**
   * <b>NOTE:</b> Only the first character in the string is considered.
   * @param quote
   */
  public void setQuoteChar(String quote)
  {
    chrQuote = quote.charAt(0);
  }

  public boolean isDelimeted()
  {
    return (null != delimit);
  }

  public void setDelimeter(String delimit)
  {
    this.delimit = delimit;
  }

  public String getDelimeter()
  {
    return delimit;
  }

  public List<LineElement> getElements()
  {
    return Collections.unmodifiableList(elements);
  }

  public void setElements(List<LineElement> recordElements)
  {
    this.elements.clear();
    this.elements.addAll(recordElements);
  }

  public void addElement(LineElement re)
  {
    elements.add(re);
  }

  @Override
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append(super.toString()).append("[");
    b.append("elements = ").append(elements);
    b.append("]");
    return b.toString();
  }

  /**
   *
   * @param inputLine A single line from file to be parsed into its corresponding bean
   * @param beans A Hashmap containing a collection of beans which will be populated with parsed data
   * @param convHelper A ConversionHelper which aids in the conversion of datatypes and string formatting
   * @param parent the value of parent record
   *
   * @throws FlatwormInputLineLengthException
   * @throws FlatwormConversionException
   * @throws FlatwormUnsetFieldValueException
   * @throws FlatwormInvalidRecordException
   * @throws FlatwormCreatorException
   */
  public void parseInput(String inputLine, Map<String, Object> beans, ConversionHelper convHelper, Record parent)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException,
     FlatwormInvalidRecordException, FlatwormCreatorException
  {
    this.convHelper = convHelper;
    this.beans = beans;

    // JBL - check for delimited status
    if(isDelimeted())
    {
      parseInputDelimited(inputLine);
      return;
    }

    int charPos = 0;
    boolean haveDummy = false;
    for(int i = 0; i < elements.size() && !haveDummy; i++)
    {
      LineElement le = (LineElement) elements.get(i);
      if(le instanceof RecordElement)
      {
        RecordElement re = (RecordElement) le;
        int start = charPos;
        int end = charPos;

        if(re.isFieldStartSet())
          start = re.getFieldStart();

        if(re.isFieldEndSet())
        {
          end = re.getFieldEnd();
          charPos = end;
        }

        if(re.isFieldLengthSet())
        {
          int flen = re.getFieldLength();

          if(flen == 0)
          {
            // una lunghezza zero significa leggi tutto fino a fine linea
            // si tratta di un campo dummy per formati a lunghezza variabile
            // questo campo dummy dovrebbe essere usato come ultimo campo
            end = inputLine.length();
            charPos = end;
            haveDummy = true;
          }
          else if(flen < 0)
          {
            // una lunghezza minore di zero significa leggi tutto fino a fine linea meno il valore specificato
            // si tratta di un campo dummy per formati a lunghezza variabile
            end = inputLine.length() - flen;
            charPos = end;
          }
          else
          {
            end = start + flen;
            charPos = end;
          }
        }

        if(end > inputLine.length())
        {
          if(!parent.isVariableLineLength() && !re.isOptional())
            throw new FlatwormInputLineLengthException(
               "In record " + parent.getName()
               + " looking for field " + re.getBeanRef() + " at pos " + start
               + ", end " + end + ", input length = " + inputLine.length());
        }
        else
        {
          String beanRef = re.getBeanRef();
          if(beanRef != null)
          {
            String fieldChars = inputLine.substring(start, end);

            // JBL - to keep from dup. code, moved this to a protected method
            mapField(fieldChars, re);
          }
        }
      }
      else if(le instanceof SegmentElement)
      {
        SegmentElement se = (SegmentElement) le;
        /* TODO - to be added. For now we only support delimited. But there really is no reason not to
                           support fixed-format as well
                int start = charPos;
                int end = charPos;
                if (se.isFieldStartSet())
                    start = se.getFieldStart();
                if (se.isFieldEndSet())
                {
                    end = se.getFieldEnd();
                    charPos = end;
                }
                if (se.isFieldLengthSet())
                {
                    end = start + se.getFieldLength();
                    charPos = end;
                }
                if (end > inputLine.length())
                    throw new FlatwormInputLineLengthException("Looking for field " + se.getBeanRef() + " at pos " + start
                            + ", end " + end + ", input length = " + inputLine.length());
                String beanRef = se.getBeanRef();
                if (beanRef != null)
                {
                    String fieldChars = inputLine.substring(start, end);

                    // JBL - to keep from dup. code, moved this to a protected method
                    mapField(convHelper, fieldChars, se, beans);

                }
         */
      }
    }
  }

  /**
   * Convert string field from file into appropriate type and set bean's value<br>
   *
   * @param fieldChars the raw string data read from the field
   * @param re the RecordElement, which contains detailed information about the field
   *
   * @throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException - wraps
   * IllegalAccessException,InvocationTargetException,NoSuchMethodException
   * @throws com.blackbear.flatworm.errors.FlatwormConversionException
   * @throws com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException
   */
  protected void mapField(String fieldChars, RecordElement re)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException
  {
    Object value = convHelper.convert(re.getType(), fieldChars, re.getConversionOptions(), re.getBeanRef());

    String beanRef = re.getBeanRef();
    int posOfFirstDot = beanRef.indexOf('.');
    String beanName = beanRef.substring(0, posOfFirstDot);
    String property = beanRef.substring(posOfFirstDot + 1);

    Object bean = beans.get(beanName);

    mappingStrategy.mapBean(bean, beanName, property, value, re.getConversionOptions());
  }

  /**
   * Convert string field from file into appropriate type and set bean's value. This is used for delimited files
   * only<br>
   *
   * @param inputLine the line of data read from the data file
   *
   * @throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException - wraps
   * IllegalAccessException,InvocationTargetException,NoSuchMethodException
   * @throws FlatwormInvalidRecordException
   * @throws FlatwormCreatorException
   */
  protected void parseInputDelimited(String inputLine)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException,
     FlatwormInvalidRecordException, FlatwormCreatorException
  {
    // JBL - gotcha, only 1st char of delimit is considered
    delimitedFields = Util.split(inputLine, delimit.charAt(0), chrQuote);
    currentField = 0;
    doParseDelimitedInput(elements);
  }

  protected void doParseDelimitedInput(List<LineElement> elements)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException,
     FlatwormCreatorException, FlatwormInvalidRecordException
  {
    for(int i = 0; i < elements.size(); ++i)
    {
      LineElement le = (LineElement) elements.get(i);
      if(le instanceof RecordElement)
      {
        try
        {
          parseDelimitedRecordElement((RecordElement) le, delimitedFields[currentField]);
          ++currentField;
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
          log.error("Ran out of data on field " + i + "\n(" + le + ")");
          throw new FlatwormInputLineLengthException("No data available for record-element " + i + "\n(" + le + ")");
        }
      }
      else if(le instanceof SegmentElement)
      {
        parseDelimitedSegmentElement((SegmentElement) le);
      }
    }
  }

  protected void parseDelimitedRecordElement(RecordElement re, String fieldStr)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException
  {
    String beanRef = re.getBeanRef();
    if(beanRef != null)
    {
      // JBL - to keep from dup. code, moved this to a protected method
      mapField(fieldStr, re);
    }
  }

  protected void parseDelimitedSegmentElement(SegmentElement segment)
     throws FlatwormCreatorException, FlatwormInputLineLengthException, FlatwormConversionException,
     FlatwormUnsetFieldValueException, FlatwormInvalidRecordException
  {
    int minCount = segment.getMinCount();
    int maxCount = segment.getMaxCount();
    if(maxCount <= 0)
    {
      maxCount = Integer.MAX_VALUE;
    }
    if(minCount < 0)
    {
      minCount = 0;
    }
    // TODO:  handle allowance for a single instance that is for a field rather than a list
    String beanRef = segment.getBeanRef();
    if(!segment.matchesId(delimitedFields[currentField]) && minCount > 0)
    {
      log.error("Segment " + segment.getName() + " with minimun required count of " + minCount + " missing.");
    }
    int cardinality = 0;
    try
    {
      while(currentField < delimitedFields.length && segment.matchesId(delimitedFields[currentField]))
      {
        if(beanRef != null)
        {
          ++cardinality;
          String parentRef = segment.getParentBeanRef();
          String addMethod = segment.getAddMethod();
          if(parentRef != null && addMethod != null)
          {
            Object instance = ParseUtils.newBeanInstance(beans.get(beanRef));
            beans.put(beanRef, instance);
            if(cardinality > maxCount)
            {
              if(segment.getCardinalityMode() == CardinalityMode.STRICT)
              {
                throw new FlatwormInvalidRecordException("Cardinality exceeded with mode set to STRICT");
              }
              else if(segment.getCardinalityMode() != CardinalityMode.RESTRICTED)
              {
                ParseUtils.invokeAddMethod(beans.get(parentRef), addMethod, instance);
              }
            }
            else
            {
              ParseUtils.invokeAddMethod(beans.get(parentRef), addMethod, instance);
            }
          }
          doParseDelimitedInput(segment.getElements());
        }
      }
    }
    finally
    {
      if(cardinality > maxCount)
      {
        log.error("Segment '" + segment.getName() + "' with maximum of " + maxCount + " encountered actual count of " + cardinality);
      }
    }
  }
}
