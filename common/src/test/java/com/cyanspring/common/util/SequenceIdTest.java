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
package com.cyanspring.common.util;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

public class SequenceIdTest {
	private HashMap<String, Object> map = new HashMap<String, Object>();
	
	private Runnable runnable = new Runnable() {
		public void run() {
			for(int i=0; i<100000; i++) {
				String id = IdGenerator.getInstance().getNextID();
//				System.out.println(Thread.currentThread().getId() + " : " + id);
				assertTrue(map.get(id)== null);
				map.put(id, new Object());
			}
			synchronized(SequenceIdTest.this) {
				SequenceIdTest.this.notify();
			}

		}
	};
	
	@Test
	public void test() throws InterruptedException {
		Thread t1 = new Thread(runnable);
		Thread t2 = new Thread(runnable);
		t1.start();
		t2.start();
		synchronized(this) {
			this.wait(1000);
		}

	}
}
