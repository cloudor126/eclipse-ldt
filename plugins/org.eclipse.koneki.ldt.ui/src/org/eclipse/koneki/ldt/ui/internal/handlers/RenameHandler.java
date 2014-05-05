package org.eclipse.koneki.ldt.ui.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.internal.core.SourceModule;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTModelUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Identifier;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.LuaExpression;
import org.eclipse.koneki.ldt.ui.internal.Activator;
import org.eclipse.koneki.ldt.ui.internal.editor.LuaEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class RenameHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		// Get selection.
		final ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!(selection instanceof TextSelection)) {
			return null;
		}
		final int selectionOffset = ((TextSelection) selection).getOffset();
		final int selectionLength = ((TextSelection) selection).getLength();

		// Get Lua editor.
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);

		if (!(editor instanceof LuaEditor)) {
			return null;
		}
		final LuaEditor luaEditor = (LuaEditor) editor;

		// Get source module.
		final IModelElement editorInput = luaEditor.getInputModelElement();
		if (!(editorInput instanceof SourceModule)) {
			return null;
		}
		final SourceModule sourceModule = (SourceModule) editorInput;

		// Get document.
		final IDocument document = luaEditor.getDocumentProvider().getDocument(luaEditor.getEditorInput());
		if (document == null) {
			return null;
		}

		// Get identifier under selection.
		final LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		final LuaExpression luaExpression = LuaASTUtils.getLuaExpressionAt(luaSourceRoot, selectionOffset, selectionOffset + selectionLength);
		if (luaExpression == null || !(luaExpression instanceof Identifier)) {
			return null;
		}
		final Identifier luaIdentifier = (Identifier) luaExpression;

		// Get its definition locally.
		final Item definition = luaIdentifier.getDefinition();
		if (definition == null) {
			return null;
		}

		// Get all the occurrences.
		final List<Identifier> occurrences = definition.getOccurrences();
		if (occurrences.isEmpty()) {
			return null;
		}

		try {
			final LinkedModeModel model = new LinkedModeModel();
			final LinkedPositionGroup group = new LinkedPositionGroup();

			// Always put Identifier we are renaming first, to keep focus on it
			group.addPosition(new LinkedPosition(document, luaIdentifier.sourceStart(), luaIdentifier.matchLength()));

			// Put others behind
			for (final Identifier occurrence : occurrences) {
				if (occurrence != luaIdentifier) {
					group.addPosition(new LinkedPosition(document, occurrence.sourceStart(), occurrence.matchLength()));
				}
			}

			model.addGroup(group);
			model.forceInstall();

			final LinkedModeUI ui = new LinkedModeUI(model, luaEditor.getScriptSourceViewer());
			ui.enter();

		} catch (final BadLocationException e) {
			Activator.logError("Unable to create the UI to rename a variable", e); //$NON-NLS-1$
		}
		return null;
	}
}
