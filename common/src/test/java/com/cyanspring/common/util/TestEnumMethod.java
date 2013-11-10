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

import org.junit.Test;

import com.cyanspring.common.type.StrategyState;

public class TestEnumMethod {
	@Test
	public void test() {
		Object state = StrategyState.Paused;
		assertTrue(ReflectionUtil.isEnum(state));
		String[] fields = ReflectionUtil.getEnumStringValues(state);
		StrategyState[] states = StrategyState.values();
		assertTrue(states.length == fields.length);
		for(int i=0; i<states.length; i++) {
			assertTrue(states[i].toString().equals(fields[i]));
		}
		Object obj = ReflectionUtil.callStaticMethod(state.getClass(), "valueOf", new String[]{"Paused"});
		assertTrue(obj.equals(StrategyState.Paused));
	}
}
