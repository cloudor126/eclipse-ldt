/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.lua.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ldt.lua.tests.internal.ast.models.api.APIModelTestSuite;
import org.eclipse.ldt.lua.tests.internal.ast.models.api.APIModelToHTMLTestSuite;
import org.eclipse.ldt.lua.tests.internal.ast.models.internal.InternalModelTestSuite;
import org.eclipse.ldt.lua.tests.internal.formatter.FourSpacesFormatterTestSuite;
import org.eclipse.ldt.lua.tests.internal.formatter.LineEndingTestSuite;
import org.eclipse.ldt.lua.tests.internal.formatter.MixedFormatterTestSuite;
import org.eclipse.ldt.lua.tests.internal.formatter.TabFormatterTestSuite;
import org.eclipse.ldt.lua.tests.internal.template.engine.JDMTemplateEngineTestSuite;
import org.eclipse.ldt.lua.tests.internal.template.engine.TemplateEngineTestSuite;

public class AllLuaIntegrationTests extends TestCase {

	public static Test suite() {
		final TestSuite suite = new TestSuite(AllLuaIntegrationTests.class.getName());
		final boolean ignore = true;
		suite.addTest(new APIModelTestSuite(ignore));
		suite.addTest(new APIModelToHTMLTestSuite(ignore));
		suite.addTest(new TabFormatterTestSuite(ignore));
		suite.addTest(new FourSpacesFormatterTestSuite(ignore));
		suite.addTest(new MixedFormatterTestSuite(ignore));
		suite.addTest(new LineEndingTestSuite(ignore));
		suite.addTest(new InternalModelTestSuite(ignore));
		suite.addTest(new TemplateEngineTestSuite(ignore));
		suite.addTest(new JDMTemplateEngineTestSuite(ignore));
		return suite;
	}
}
