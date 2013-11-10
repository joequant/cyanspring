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
package com.cyanspring.server.persistence;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TextObjectTest {

	@Test
	public void test() {
		int size = 10;
		
		String str1 = "";
		List<String> lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==1);
		assertTrue(lines.get(0).equals(str1));
		
		str1 = "01234567";
		lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==1);
		assertTrue(lines.get(0).equals(str1));
		
		str1 = "0123456789";
		lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==1);
		assertTrue(lines.get(0).equals(str1));
		
		str1 = "0123456789012345";
		lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==2);
		assertTrue(lines.get(0).equals("0123456789"));
		assertTrue(lines.get(1).equals("012345"));

		str1 = "01234567890123456789";
		lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==2);
		assertTrue(lines.get(0).equals("0123456789"));
		assertTrue(lines.get(1).equals("0123456789"));
		
		str1 = "012345678901234567890123";
		lines = TextObject.chop(str1, size);
		assertTrue(lines.size()==3);
		assertTrue(lines.get(0).equals("0123456789"));
		assertTrue(lines.get(1).equals("0123456789"));
		assertTrue(lines.get(2).equals("0123"));

	}
}
