package org.eclipse.ldt.ui.internal.properties;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ldt.core.internal.grammar.LuaGrammarManager;
import org.eclipse.ldt.ui.internal.grammar.GrammarContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GrammarPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private ComboViewer availableGrammarComboViewer;
	private IProject project;

	public GrammarPropertyPage() {
	}

	/**
	 * Initializes a ProjectReferencePage.
	 */
	private void initialize() {
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription(Messages.GrammarPropertyPage_page_description);
		if (project != null)
			setPreferenceStore(new ScopedPreferenceStore(new ProjectScope(project), LuaLanguageToolkit.getDefault().getPreferenceQualifier()));
	}

	@Override
	protected Control createContents(Composite parent) {
		// Initialize class
		initialize();

		// ----------------
		// CREATE CONTROL
		// Create container composite
		Composite containerComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(containerComposite);
		createDescriptionLabel(containerComposite);

		// Grammar combo viewer
		availableGrammarComboViewer = new ComboViewer(containerComposite, SWT.READ_ONLY | SWT.BORDER);
		availableGrammarComboViewer.setContentProvider(new GrammarContentProvider());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(availableGrammarComboViewer.getControl());

		// ----------------
		// Initialize UI component
		initializePage();
		return containerComposite;
	}

	private void initializePage() {
		if (availableGrammarComboViewer == null || availableGrammarComboViewer.getControl().isDisposed() || getPreferenceStore() == null)
			return;

		// Refresh list
		List<String> availableGrammars = LuaGrammarManager.getAvailableGrammars();
		availableGrammarComboViewer.setInput(availableGrammars);

		// Set default interpreter
		String defaultGrammar = getPreferenceStore().getString(PreferenceInitializer.GRAMMAR_DEFAULT_ID);
		availableGrammarComboViewer.setSelection(new StructuredSelection(defaultGrammar));
	}

	public String getSelectedGrammar() {
		ISelection selection = availableGrammarComboViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof String)
				return (String) firstElement;
		}
		return null;
	}

	@Override
	public boolean performOk() {
		// Get current values
		String oldGrammar = getPreferenceStore().getString(PreferenceInitializer.GRAMMAR_DEFAULT_ID);
		String newGrammar = getSelectedGrammar();

		// Change preference
		getPreferenceStore().setValue(PreferenceInitializer.GRAMMAR_DEFAULT_ID, newGrammar);

		// Get workbench Container
		IWorkbenchPreferenceContainer container = null;
		if (getContainer() instanceof IWorkbenchPreferenceContainer)
			container = (IWorkbenchPreferenceContainer) getContainer();

		// rebuild project if needed
		boolean needsBuild = oldGrammar != null && !oldGrammar.equals(newGrammar) && container != null;
		boolean doBuild = false;
		if (needsBuild) {
			MessageDialog dialog = new MessageDialog(getShell(), Messages.GrammarPropertyPage_rebuild_dialog_title, null,
					Messages.GrammarPropertyPage_rebuild_dialog_message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
			int res = dialog.open();
			if (res == 0) {
				doBuild = true;
			} else if (res != 1) {
				return false; // cancel pressed
			}
		}

		if (doBuild) { // post build
			Job job = CoreUtility.getBuildJob(project);
			// TODO we should not clear all the cache, but just the project one.
			SourceParserUtil.clearCache();
			container.registerUpdateJob(job);
		}
		return super.performOk();
	}
}
