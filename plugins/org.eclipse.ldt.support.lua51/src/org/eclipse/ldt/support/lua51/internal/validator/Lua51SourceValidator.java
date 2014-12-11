package org.eclipse.ldt.support.lua51.internal.validator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ldt.core.grammar.ILuaSourceValidator;
import org.eclipse.ldt.core.internal.ast.models.ModelFactory;
import org.eclipse.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.ldt.support.lua51.internal.Activator;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.eclipse.AbstractLuaModule;

public class Lua51SourceValidator extends AbstractLuaModule implements ILuaSourceValidator {

	private static final String VALIDATOR_PATH = "script/internal"; //$NON-NLS-1$
	private static final String MODULE_NAME = "lua51validator"; //$NON-NLS-1$
	private static final String VALIDATION_FUNCTION = "valid"; //$NON-NLS-1$

	private LuaState lua;

	public Lua51SourceValidator() {
	}

	@Override
	public String valid(String source, LuaSourceRoot root) {
		// Load function
		if (lua == null)
			lua = loadLuaModule();

		pushLuaModule(lua);
		lua.getField(-1, VALIDATION_FUNCTION);
		lua.pushString(source);
		lua.pushJavaObject(root);
		try {
			lua.call(2, 1);
		} catch (final LuaRuntimeException e) {
			Activator.logWarning("validation failed", e);
			return source;
		}

		// Error check
		// if (lua.isNil(-2))
		// throw new LuaFormatterException(lua.toString(-1));
		return lua.toString(-1);
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
		ModelFactory.registerModelFactory(l);
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

}
