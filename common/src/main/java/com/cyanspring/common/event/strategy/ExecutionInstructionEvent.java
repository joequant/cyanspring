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
package com.cyanspring.common.event.strategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.Clock;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.util.IdGenerator;

public class ExecutionInstructionEvent extends AsyncEvent {
	private Date time;
	private String id;
	private List<ExecutionInstruction> executionInstructions;

	public ExecutionInstructionEvent(String key,
			List<ExecutionInstruction> executionInstructions) {
		super(key);
		this.time = Clock.getInstance().now();
		this.executionInstructions = executionInstructions;
		this.id = IdGenerator.getInstance().getNextID() + "EI";
	}

	public String getId() {
		return id;
	}

	public List<ExecutionInstruction> getExecutionInstructions() {
		return executionInstructions;
	}
	
	public Date getTime() {
		return time;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		sb.append(", ");
		sb.append(new SimpleDateFormat("HH:mm:ss.SSS").format(this.time));
		sb.append(", ");
		for(ExecutionInstruction ei: executionInstructions) {
			sb.append("\n" + ei);
		}
		return sb.toString();
	}
}
