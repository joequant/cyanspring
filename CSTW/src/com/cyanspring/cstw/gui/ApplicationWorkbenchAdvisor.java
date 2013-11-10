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
package com.cyanspring.cstw.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.cstw.business.Business;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	final private static Logger log = LoggerFactory.getLogger(ApplicationWorkbenchAdvisor.class); 

	private static final String PERSPECTIVE_ID = "CSTW.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
	}

	@Override
	public void postStartup() {
		try {
//			This does NOT work
//			ApplicationContext spring = new FileSystemXmlApplicationContext(Business.getInstance().getConfigPath() + "cstw.xml");
//			SysSettings sys = (SysSettings)spring.getBean("sysSettings");
//			log.debug("SysSettings: " + sys.CONF_PATH);
			Business.getInstance().start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Error",
					e.getMessage());

			PlatformUI.getWorkbench().close();
		}
		super.preStartup();
	}
	
	@Override
	public boolean preShutdown() {
		Business.getInstance().stop();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		return super.preShutdown();
	}
}
