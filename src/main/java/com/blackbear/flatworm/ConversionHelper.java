/**
 * Flatworm - A Java Flat File Importer Copyright (C) 2004 James M. Turner Extended by James Lawrence 2005
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonlib5.utils.StringOper;

/**
 * The <code>ConversionHelper</code> was created to separate formatting responsibility into a separate class.
 * This class also makes writing your own converter more of a reality by separating type conversion from string
 * formatting.
 * String formatting has moved to a separate class called Util.
 */
public class ConversionHelper
{
  private static final Log log = LogFactory.getLog(ConversionHelper.class);

  protected Map<String, Converter> converters;
  protected Map<Converter, Method> converterMethodCache;
  protected Map<Converter, Method> converterToStringMethodCache;
  protected Map<String, Object> converterObjectCache;

  public ConversionHelper()
  {
    converters = new HashMap<String, Converter>();
    converterMethodCache = new HashMap<Converter, Method>();
    converterToStringMethodCache = new HashMap<Converter, Method>();
    converterObjectCache = new HashMap<String, Object>();
  }

  /**
   * Converte il valore stringa nel tipo specificato.
   * @param type The name of the converter from the xml configuration file
   * @param fieldChars The value of the field as read from the input file
   * @param options Map of ConversionOptions (if any) for this field
   * @param beanRef "class.property", used for more descriptive exception messages, should something go wrong
   *
   * @throws FlatwormConversionException - if problems are encountered during the conversion process (wraps other
   * exceptions)
   * @return Java type corresponding to the field type, post conversion
   */
  public Object convert(String type, String fieldChars, Map<String, ConversionOption> options, String beanRef)
     throws FlatwormConversionException
  {
    Object value = null;

    try
    {
      Object object = getConverterObject(type);
      Method method = getConverterMethod(type);

      fieldChars = transformString(fieldChars, options, 0);
      value = method.invoke(object, fieldChars, options);
    }
    catch(IllegalAccessException | InvocationTargetException | IllegalArgumentException e)
    {
      log.error("While running convert method for " + beanRef, e);
      throw new FlatwormConversionException("Converting field " + beanRef + " with value '" + fieldChars + "'");
    }

    return value;
  }

  /**
   * Converte il valore specificato in stringa.
   * @param type The name of the converter from the xml configuration file
   * @param obj il valore da convertire in stringa
   * @param options Map of ConversionOptions (if any) for this field
   * @param beanRef "class.property", used for more descriptive exception messages, should something go wrong
   * @return la stringa corrispondente al valore specificato
   * @throws FlatwormConversionException
   */
  public String convert(String type, Object obj, Map<String, ConversionOption> options, String beanRef)
     throws FlatwormConversionException
  {
    try
    {
      Object converter = getConverterObject(type);
      Method method = getToStringConverterMethod(type);
      String result = (String) method.invoke(converter, obj, options);
      return result;
    }
    catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
    {
      log.error("While running toString convert method for " + beanRef, e);
      throw new FlatwormConversionException("Converting field " + beanRef + " to string for value '" + obj + "'");
    }
  }

  /**
   * Handles the processing of the Conversion-Options from the flatworm XML file
   *
   * @param fieldChars The string to be transformed
   * @param options Collection of ConversionOption objects
   * @param length Used in justification to ensure proper formatting
   *
   * @return The transformed string
   */
  public String transformString(String fieldChars, Map<String, ConversionOption> options, int length)
  {
    // JBL - Implement iteration of conversion-options
    // Iterate over conversion-options, that way, the xml file
    // can drive the order of conversions, instead of having them
    // hard-coded like in 'removePadding' (old way)
    Set<String> keys = options.keySet();
    for(Iterator<String> it = keys.iterator(); it.hasNext();)
    {
      ConversionOption conv = options.get(it.next());

      switch(StringOper.okStr(conv.getName()).toLowerCase())
      {
        case "justify":
          fieldChars = Util.justify(fieldChars, conv.getValue(), options, length);
          break;
        case "strip-chars":
          fieldChars = Util.strip(fieldChars, conv.getValue(), options);
          break;
        case "substring":
          fieldChars = Util.substring(fieldChars, conv.getValue(), options);
          break;
        case "default-value":
          fieldChars = Util.defaultValue(fieldChars, conv.getValue(), options);
          break;
      }
    }

    if(length > 0)
    {
      // Never request string to be zero length
      if(fieldChars.length() > length) // too long, chop it off
        fieldChars = fieldChars.substring(0, length);
      else if(fieldChars.length() < length) // too short, add spaces
        fieldChars = StringOper.GetFixedString(fieldChars, length);
    }

    return fieldChars;
  }

  /**
   * Facilitates the storage of multiple converters used by the <code>convert</code> method during processing
   *
   * @param converter The converter to be added
   */
  public void addConverter(Converter converter)
  {
    converters.put(converter.getName(), converter);
  }

  public Converter getConverter(String name)
  {
    Converter result = null;
    Converter convert = converters.get(name);
    if(convert != null)
    {
      result = new Converter();
      result.setConverterClass(convert.getConverterClass());
      result.setMethod(convert.getMethod());
      result.setName(convert.getName());
      result.setReturnType(convert.getReturnType());
    }

    return result;
  }

  /**
   * @param type The name of the converter. Used for lookup
   * @return Java reflection Object used to represent the conversion method
   * @throws com.blackbear.flatworm.errors.FlatwormConversionException
   */
  protected Method getConverterMethod(String type)
     throws FlatwormConversionException
  {
    Converter c = (Converter) converters.get(type);
    if(converterMethodCache.get(c) != null)
      return (Method) converterMethodCache.get(c);

    try
    {
      Class<? extends Object> cl = Class.forName(c.getConverterClass());
      Method meth = cl.getMethod(c.getMethod(), String.class, Map.class);

      converterMethodCache.put(c, meth);
      return meth;
    }
    catch(NoSuchMethodException e)
    {
      log.error("Finding method", e);
      throw new FlatwormConversionException("Couldn't Find Method");
    }
    catch(ClassNotFoundException e)
    {
      log.error("Finding class", e);
      throw new FlatwormConversionException("Couldn't Find Class");
    }
  }

  protected Method getToStringConverterMethod(String type)
     throws FlatwormConversionException
  {
    Converter c = (Converter) converters.get(type);
    if(converterToStringMethodCache.get(c) != null)
      return (Method) converterToStringMethodCache.get(c);

    try
    {
      Class<? extends Object> cl = Class.forName(c.getConverterClass());
      Method meth = cl.getMethod(c.getMethod(), Object.class, Map.class);

      converterToStringMethodCache.put(c, meth);
      return meth;
    }
    catch(NoSuchMethodException e)
    {
      log.error("Finding method", e);
      throw new FlatwormConversionException("Couldn't Find Method 'String " + c.getMethod()
         + "(Object, HashMap)'");
    }
    catch(ClassNotFoundException e)
    {
      log.error("Finding class", e);
      throw new FlatwormConversionException("Couldn't Find Class");
    }
  }

  /**
   * @param type The name of the converter. Used for lookup
   * @return An instance of the conversion class
   * @throws FlatwormConversionException if there is no Converter registered with the specified name.
   */
  protected Object getConverterObject(String type)
     throws FlatwormConversionException
  {
    Converter c = (Converter) converters.get(type);
    if(c == null)
      throw new FlatwormConversionException("type '" + type + "' not registered");

    if(converterObjectCache.get(c.getConverterClass()) != null)
      return converterObjectCache.get(c.getConverterClass());

    try
    {
      Class<? extends Object> cl = Class.forName(c.getConverterClass());
      Class args[] = new Class[0];
      Object objArgs[] = new Object[0];
      Object o = cl.getConstructor(args).newInstance(objArgs);

      converterObjectCache.put(c.getConverterClass(), o);
      return o;
    }
    catch(NoSuchMethodException e)
    {
      log.error("Finding method", e);
      throw new FlatwormConversionException("Couldn't Find Method");
    }
    catch(IllegalAccessException e)
    {
      log.error("No access to class", e);
      throw new FlatwormConversionException("Couldn't access class");
    }
    catch(InvocationTargetException e)
    {
      log.error("Invoking method", e);
      throw new FlatwormConversionException("Couldn't invoke method");
    }
    catch(InstantiationException e)
    {
      log.error("Instantiating", e);
      throw new FlatwormConversionException("Couldn't instantiate converter");
    }
    catch(ClassNotFoundException e)
    {
      log.error("Finding class", e);
      throw new FlatwormConversionException("Couldn't Find Class");
    }
  }
}
