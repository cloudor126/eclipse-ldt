package org.eclipse.ldt.support.lua51.internal.validator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ldt.core.grammar.ILuaSourceValidator;
import org.eclipse.ldt.support.lua51.internal.Activator;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.eclipse.AbstractLuaModule;

public class Lua51SourceValidator extends AbstractLuaModule implements ILuaSourceValidator {

	private static final String VALIDATOR_PATH = "script/internal"; //$NON-NLS-1$
	private static final String MODULE_NAME = "lua51validator"; //$NON-NLS-1$
	private static final String VALIDATION_FUNCTION = "valid"; //$NON-NLS-1$

	private LuaState lua;

	private String cleanedSource;
	private String errorMessage;
	private int lineIndex;

	public Lua51SourceValidator() {
	}

	@Override
	public boolean valid(String source) {
		// Load function
		if (lua == null)
			lua = loadLuaModule();

		pushLuaModule(lua);
		lua.getField(-1, VALIDATION_FUNCTION);
		lua.pushString(source);
		try {
			lua.call(1, 3);
		} catch (final LuaRuntimeException e) {
			Activator.logWarning("validation 5.1 failed", e); //$NON-NLS-1$
			cleanedSource = null;
			errorMessage = "Unexpected error ..."; //$NON-NLS-1$
			lineIndex = 0;
			return false;
		}

		cleanedSource = lua.toString(-3);
		errorMessage = lua.toString(-2);
		lineIndex = Math.max(lua.toInteger(-1) - 1, 0);

		return errorMessage == null;
	}

	/**
	 * @see com.naef.jnlua.eclipse.AbstractLuaModule#getLuaSourcePaths()
	 */
	@Override
	protected List<String> getLuaSourcePaths() {
		ArrayList<String> sourcepaths = new ArrayList<String>();
		sourcepaths.add(VALIDATOR_PATH);
		return sourcepaths;
	}

	/**
	 * @see com.naef.jnlua.eclipse.AbstractLuaModule#getLuacSourcePaths()
	 */
	@Override
	protected List<String> getLuacSourcePaths() {
		return null;
	}

	/**
	 * @see com.naef.jnlua.eclipse.AbstractLuaModule#createLuaState()
	 */
	@Override
	protected LuaState createLuaState() {
		LuaState l = new LuaState();
		l.openLibs();
		return l;
	}

	/**
	 * @see com.naef.jnlua.eclipse.AbstractLuaModule#getPluginID()
	 */
	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	/**
	 * @see com.naef.jnlua.eclipse.AbstractLuaModule#getModuleName()
	 */
	@Override
	protected String getModuleName() {
		return MODULE_NAME;
	}

	/**
	 * @see org.eclipse.ldt.core.grammar.ILuaSourceValidator#getCleanedSource()
	 */
	@Override
	public String getCleanedSource() {
		return cleanedSource;
	}

	/**
	 * @see org.eclipse.ldt.core.grammar.ILuaSourceValidator#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @see org.eclipse.ldt.core.grammar.ILuaSourceValidator#getLineIndex()
	 */
	@Override
	public int getLineIndex() {
		return lineIndex;
	}

}
