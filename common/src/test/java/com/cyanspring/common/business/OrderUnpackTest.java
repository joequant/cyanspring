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
package com.cyanspring.common.business;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

public class OrderUnpackTest {

	@Test
	public void test() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ParentOrder order = null;
		try {
			order = new ParentOrder(map);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		// no exception raised
		assertTrue(order != null);
	}
}
