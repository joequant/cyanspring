package com.cyanspring.cstw.gui.command;

import java.io.File;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.cstw.business.Business;

public class SaveStrategyCommand extends AbstractHandler {
	private static final Logger log = LoggerFactory
			.getLogger(SaveStrategyCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		saveStrategy();
		return null;
	}

	public static void saveStrategy() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String selectedFileName = dialog.open();
		if (selectedFileName == null){
			return;
		}
		
		File selectedFile = new File(selectedFileName); 
		RemoteAsyncEvent event;
		try {
			event = (RemoteAsyncEvent)Business.getInstance().getXstream().fromXML(selectedFile);
			if(!(event instanceof RemoteAsyncEvent))
				throw new Exception("Object is not subclass of RemoteAsyncEvent");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			MessageDialog.openError(shell, "Error loading RemoteAsyncEvent", 
			e.getMessage());
			return;
		}
	}
}
