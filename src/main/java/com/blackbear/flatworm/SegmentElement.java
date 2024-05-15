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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SegmentElement implements LineElement
{
    protected int fieldIdentStart = 0;
    protected int fieldIdentLength = 0;
    protected List<String> fieldIdentMatchStrings = new ArrayList<String>();
    protected int minCount;
    protected int maxCount;
    protected String name;
    protected String beanRef;
    protected String parentBeanRef;
    protected String addMethod;
    protected CardinalityMode cardinalityMode;
    protected List<LineElement> elements = new ArrayList<LineElement>();

    public int getFieldIdentStart()
    {
        return fieldIdentStart;
    }

    public void setFieldIdentStart(int fieldIdentStart)
    {
        this.fieldIdentStart = fieldIdentStart;
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
        return fieldIdentMatchStrings ;
    }

    public void setFieldIdentMatchStrings(List<String> fieldIdentMatchStrings)
    {
        this.fieldIdentMatchStrings = fieldIdentMatchStrings;
    }

    public void addFieldIdentMatchString(String s)
    {
        fieldIdentMatchStrings.add(s);
    }
    
    public boolean matchesId(String id)
    {
        return fieldIdentMatchStrings.contains(id);
    }

    public char getIdentTypeFlag()
    {
        return 'F';
    }

    public int getMinCount()
    {
        return minCount;
    }

    public void setMinCount(int minCount)
    {
        this.minCount = minCount;
    }

    public int getMaxCount()
    {
        return maxCount;
    }

    public void setMaxCount(int maxCount)
    {
        this.maxCount = maxCount;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getBeanRef()
    {
        return beanRef;
    }

    public void setBeanRef(String beanRef)
    {
        this.beanRef = beanRef;
    }

    public String getParentBeanRef()
    {
        return parentBeanRef;
    }

    public void setParentBeanRef(String parentBeanRef)
    {
        this.parentBeanRef = parentBeanRef;
    }

    public String getAddMethod()
    {
        return addMethod;
    }

    public void setAddMethod(String addMethod)
    {
        this.addMethod = addMethod;
    }

    public CardinalityMode getCardinalityMode()
    {
        return cardinalityMode;
    }

    public void setCardinalityMode(CardinalityMode cardinalityMode)
    {
        this.cardinalityMode = cardinalityMode;
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
}
