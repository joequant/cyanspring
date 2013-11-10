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
package com.cyanspring.common.staticdata;


public interface ITickTable {

	public double getRoundedPrice(double price, boolean up);
	public double tickUp(double price, boolean roundUp);
	public double tickDown(double price, boolean roundUp);
	public double tickUp(double price, int ticks, boolean roundUp);
	public double tickDown(double price, int ticks, boolean roundUp);
	public boolean validPrice(double price);
}
