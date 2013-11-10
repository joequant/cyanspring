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

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.common.SysSettings;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	final private static Logger log = LoggerFactory.getLogger(Activator.class); 

	// The plug-in ID
	public static final String PLUGIN_ID = "CSTW"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	    URL confURL = getBundle().getEntry(SysSettings.CONF_PATH + "log4j.xml");     
	   // DOMConfigurator.configure( FileLocator.toFileURL(confURL).getFile());     
	    //PropertyConfigurator.configure(FileLocator.toFileURL(confURL).getFile());     
	    DOMConfigurator.configure(confURL);     
	    log.info("Logging using log4j and configuration " + FileLocator.toFileURL(confURL).getFile());     

	    confURL = getBundle().getEntry(SysSettings.CONF_PATH);
	    String confPath = FileLocator.toFileURL(confURL).getFile();
	    Business.getInstance().setConfigPath(confPath);
	    log.info("Setting configuration path: " + confPath); 
	    
		ApplicationContext spring = new FileSystemXmlApplicationContext("classpath*:cstw.xml");
		BeanHolder holder = (BeanHolder)spring.getBean("beanHolder");
		BeanHolder.setInstance(holder);
		log.debug("Initiated bean holder");
		Business.getInstance().init();
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
        super.initializeImageRegistry(registry);
	    log.debug("initializeImageRegistry: " + registry);
        
		for (ImageID id: ImageID.values()) {
		    URL confURL = getBundle().getEntry(id.value());    
		    log.debug("URL value: " + confURL);
		    log.debug("Image value: " + id.toString() + " : " + id.value());
	        ImageDescriptor image = ImageDescriptor.createFromURL(confURL);
		    log.debug("Imag: " + image);
	        registry.put(id.toString(), image);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
