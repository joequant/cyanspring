/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.data;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class XStreamTest 
{
	@Test
    public void testApp()
    {
        DataObject inputObj = new DataObject();
        DataObject innerObject = new DataObject();
        inputObj.put("Name", "Andy");
        Calendar cal = Calendar.getInstance();
        cal.set(1972, 10, 1);
        inputObj.put("DOB", cal.getTime());
        innerObject.put("weight", new Integer(100));
        innerObject.put("title", "Mr.");
        innerObject.put("xml", "<entry>string</entry>");
        inputObj.put("inner", innerObject);
        String xml = inputObj.toXML();
        //System.out.println(xml);
        DataObject outputObj = DataObject.fromString(DataObject.class, xml);
        assertTrue(outputObj.get(String.class, "Name").equals("Andy"));
        assertTrue(outputObj.get(Date.class, "DOB").equals(cal.getTime()));
        innerObject = outputObj.get(DataObject.class, "inner");
        assertTrue(innerObject.get(Integer.class, "weight").equals(100));
        assertTrue(innerObject.get(String.class, "title").equals("Mr."));
        assertTrue(innerObject.get(String.class, "xml").equals("<entry>string</entry>"));     
        
        Object obj = DataObject.fromString(Object.class, xml);
        assertTrue(obj instanceof DataObject);
    }
}
