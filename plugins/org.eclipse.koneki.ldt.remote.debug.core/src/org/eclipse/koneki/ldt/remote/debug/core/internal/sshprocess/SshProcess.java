/*******************************************************************************
 * Copyright (c) 2012, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.remote.debug.core.internal.sshprocess;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.koneki.ldt.remote.debug.core.internal.Activator;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Implementation of {@link org.eclipse.debug.core.model.IProcess} based on ssh connection
 */
public class SshProcess implements IProcess {

	public static final char ARGUMENT_SEPARATOR = ' ';

	private ILaunch launch;
	private ChannelExec channelExec;
	private StreamsProxy sshStreamProxy;
	private String label;

	/**
	 * Create and launch a process corresponding to the given command throw an ssh connection<br>
	 * <b>/!\ Session must be connected !</b>
	 * 
	 * @throws CoreException
	 *             if exec channel can not be created, or session is down
	 */
	public SshProcess(Session session, ILaunch launch, String workingDirectoryPath, String command, Map<String, String> envVars) throws CoreException {
		this.launch = launch;

		// open exec channel
		Channel channel;
		try {
			channel = session.openChannel("exec"); //$NON-NLS-1$
		} catch (JSchException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create SShProcess", e)); //$NON-NLS-1$
		}
		if (!(channel instanceof ChannelExec))
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create SShProcess")); //$NON-NLS-1$
		channelExec = (ChannelExec) channel;

		this.label = command;

		// create composed command
		String composedCommand = createLaunchCommand(workingDirectoryPath, command, envVars);
		channelExec.setCommand(composedCommand);
		channelExec.setPty(true);
	}

	/**
	 * Escapes a string so it became a valid Shell token.
	 * 
	 * @param s
	 * @return
	 */
	public static String escapeShell(String s) {
		// TODO: there is still some sequences to escape
		return "\"" + s.replaceAll("\"", Matcher.quoteReplacement("\\\"")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * start the process <br>
	 * don't forget to call Terminate if an error is raised.
	 * 
	 * @throws CoreException
	 *             if an error occured (session is down, channel is not open,IO Problem on channel's stream)
	 */
	public void start() throws CoreException {
		// start stream monitoring
		try {
			sshStreamProxy = new StreamsProxy(channelExec.getInputStream(), channelExec.getErrStream(), channelExec.getOutputStream(), null, null);
		} catch (IOException e) {
			this.terminate();
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to start SShProcess", e)); //$NON-NLS-1$
		}

		// start connection
		try {
			channelExec.connect();
		} catch (JSchException e) {
			this.terminate();
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to start SShProcess", e)); //$NON-NLS-1$
		}
		fireCreationEvent();

		// monitor channel state
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean connected;
				do {
					connected = channelExec.isConnected();
					try {
						Thread.sleep(100);
						// CHECKSTYLE:OFF
					} catch (InterruptedException e) {
						// nothing to do
						// CHECKSTYLE:ON
					}
				} while (connected);
				sshStreamProxy.kill();
				fireTerminateEvent();
			}
		}).start();
	}

	/**
	 * create one command from workingdir, envpath and command
	 */
	private String createLaunchCommand(String workingDirectoryPath, String command, Map<String, String> envVars) {
		// TODO : should works only on linux...

		StringBuilder composedCommand = new StringBuilder();
		// add : move to the working directory
		composedCommand.append("cd "); //$NON-NLS-1$
		composedCommand.append(escapeShell(workingDirectoryPath));
		composedCommand.append(" && "); //$NON-NLS-1$

		// add : set env path
		for (Entry<String, String> entrySet : envVars.entrySet()) {
			composedCommand.append("export "); //$NON-NLS-1$
			composedCommand.append(entrySet.getKey());
			composedCommand.append("="); //$NON-NLS-1$
			composedCommand.append(escapeShell(entrySet.getValue()));
			composedCommand.append(" && "); //$NON-NLS-1$
		}
		composedCommand.append(command);

		return composedCommand.toString();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return !isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return channelExec.isClosed();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		channelExec.disconnect();
		sshStreamProxy.kill();
		fireTerminateEvent();
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	@Override
	public IStreamsProxy getStreamsProxy() {
		return sshStreamProxy;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String key, String value) {
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String key) {
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	@Override
	public int getExitValue() throws DebugException {
		return channelExec.getExitStatus();
	}

	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 * 
	 * @param event
	 *            debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fires a change event.
	 */
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}
}
