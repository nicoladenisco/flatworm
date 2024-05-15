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
package com.blackbear.flatworm;

import com.blackbear.flatworm.errors.FlatwormConversionException;
import com.blackbear.flatworm.errors.FlatwormCreatorException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import com.blackbear.flatworm.errors.FlatwormInvalidRecordException;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class used to store the values from the Record XML tag. Also aids in parsing and matching lines in the inputfile.
 */
public class Record
{
  private static final Log log = LogFactory.getLog(Record.class);

  protected String name;
  protected int lengthIdentMin;
  protected int lengthIdentMax;
  protected int fieldIdentStart;
  protected int fieldIdentLength;
  protected List<String> fieldIdentMatchStrings;
  protected char identTypeFlag;
  protected RecordDefinition recordDefinition;
  protected boolean variableLineLength = false;

  public Record()
  {
    lengthIdentMin = 0;
    lengthIdentMax = 0;
    fieldIdentStart = 0;
    fieldIdentLength = 0;
    fieldIdentMatchStrings = new ArrayList<String>();
    identTypeFlag = '\0';
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getLengthIdentMin()
  {
    return lengthIdentMin;
  }

  public void setLengthIdentMin(int lengthIdentMin)
  {
    this.lengthIdentMin = lengthIdentMin;
  }

  public int getLengthIdentMax()
  {
    return lengthIdentMax;
  }

  public void setLengthIdentMax(int lengthIdentMax)
  {
    this.lengthIdentMax = lengthIdentMax;
  }

  public int getFieldIdentLength()
  {
    return fieldIdentLength;
  }

  public void setFieldIdentLength(int fieldIdentLength)
  {
    this.fieldIdentLength = fieldIdentLength;
  }

  public List<String> getFieldIdentMatchStrings()
  {
    return fieldIdentMatchStrings;
  }

  public void setFieldIdentMatchStrings(List<String> fieldIdentMatchStrings)
  {
    this.fieldIdentMatchStrings = fieldIdentMatchStrings;
  }

  public void addFieldIdentMatchString(String s)
  {
    fieldIdentMatchStrings.add(s);
  }

  public char getIdentTypeFlag()
  {
    return identTypeFlag;
  }

  public void setIdentTypeFlag(char identTypeFlag)
  {
    this.identTypeFlag = identTypeFlag;
  }

  public RecordDefinition getRecordDefinition()
  {
    return recordDefinition;
  }

  public void setRecordDefinition(RecordDefinition recordDefinition)
  {
    this.recordDefinition = recordDefinition;
  }

  public int getFieldIdentStart()
  {
    return fieldIdentStart;
  }

  public void setFieldIdentStart(int fieldIdentStart)
  {
    this.fieldIdentStart = fieldIdentStart;
  }

  public boolean isVariableLineLength()
  {
    return variableLineLength;
  }

  public void setVariableLineLength(boolean variableLineLength)
  {
    this.variableLineLength = variableLineLength;
  }

  /**
   * Verifiy if this record match the input line.
   * @param line line of input from the file
   * @param ff not used at this time, for later expansion?
   * @return boolean does this line match according to the defined criteria?
   */
  public boolean matchesLine(String line, FileFormat ff)
  {
    switch(identTypeFlag)
    {
      // Recognition by value in a certain field
      // TODO: Will this work for delimited lines?
      case 'F':
        if(line.length() < fieldIdentStart + fieldIdentLength)
        {
          return false;
        }
        else
        {
          for(int i = 0; i < fieldIdentMatchStrings.size(); i++)
          {
            String s = (String) fieldIdentMatchStrings.get(i);
            if(line.regionMatches(fieldIdentStart, s, 0, fieldIdentLength))
            {
              return true;
            }
          }
        }
        return false;

      // Recognition by length of line
      case 'L':
        return line.length() >= lengthIdentMin && line.length() <= lengthIdentMax;

      // Ignore in auto match
      case 'I':
        return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append(super.toString()).append("[");
    b.append("name = ").append(getName());
    if(getIdentTypeFlag() == 'L')
      b.append(", identLength=(").append(getLengthIdentMin()).append(",").append(getLengthIdentMax()).append(")");
    if(getIdentTypeFlag() == 'F')
      b.append(", identField=(").append(getFieldIdentStart()).append(",").append(getFieldIdentLength()).append(",").append(getFieldIdentMatchStrings().toString()).append(")");
    if(getRecordDefinition() != null)
      b.append(", recordDefinition = ").append(getRecordDefinition().toString());
    b.append("]");
    return b.toString();
  }

  /**
   * Parse the record into the bean(s) <br>
   *
   * @param firstLine first line to be considered
   * @param in used to retrieve additional lines of input for parsing multi-line records
   * @param convHelper used to help convert datatypes and format strings
   * @return HashMap collection of beans populated with file data
   *
   * @throws FlatwormInputLineLengthException
   * @throws FlatwormConversionException
   * @throws FlatwormUnsetFieldValueException
   * @throws FlatwormInvalidRecordException
   * @throws FlatwormCreatorException
   */
  public Map<String, Object> parseRecord(String firstLine, BufferedReader in, ConversionHelper convHelper)
     throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException,
     FlatwormInvalidRecordException, FlatwormCreatorException
  {
    Map<String, Object> beans = new HashMap<String, Object>();

    try
    {
      Map<String, Bean> beanHash = recordDefinition.getBeansUsed();
      String beanName;
      Object beanObj;
      for(Iterator<String> bean_it = beanHash.keySet().iterator(); bean_it.hasNext();)
      {
        beanName = bean_it.next();
        Bean bean = (Bean) beanHash.get(beanName);
        beanObj = bean.getBeanObjectClass().newInstance();
        beans.put(beanName, beanObj);
      }

      List<Line> lines = recordDefinition.getLines();
      String inputLine = firstLine;
      for(int i = 0; i < lines.size(); i++)
      {
        Line line = lines.get(i);
        line.parseInput(inputLine, beans, convHelper, this);
        if(i + 1 < lines.size())
          inputLine = in.readLine();
      }
    }
    catch(SecurityException e)
    {
      log.error("Invoking method", e);
      throw new FlatwormConversionException("Couldn't invoke Method");
    }
    catch(IOException e)
    {
      log.error("Reading input", e);
      throw new FlatwormConversionException("Couldn't read line");
    }
    catch(InstantiationException e)
    {
      log.error("Creating bean", e);
      throw new FlatwormConversionException("Couldn't create bean");
    }
    catch(IllegalAccessException e)
    {
      log.error("No access to class", e);
      throw new FlatwormConversionException("Couldn't access class");
    }
    return beans;
  }

  protected String[] getFieldNames()
  {
    List<String> names = new ArrayList<String>();
    List<Line> lines = recordDefinition.getLines();
    for(int i = 0; i < lines.size(); i++)
    {
      Line l = lines.get(i);
      List<LineElement> el = l.getElements();
      for(int j = 0; j < el.size(); j++)
      {
        LineElement re = el.get(j);
        names.add(re.getBeanRef());
      }
    }

    String propertyNames[] = new String[names.size()];
    for(int i = 0; i < names.size(); i++)
      propertyNames[i] = (String) names.get(i);

    return propertyNames;
  }
}
