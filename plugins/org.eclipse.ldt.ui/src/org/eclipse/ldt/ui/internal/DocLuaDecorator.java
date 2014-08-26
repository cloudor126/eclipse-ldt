package org.eclipse.ldt.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

public class DocLuaDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final String DOCLUA_EXT = "doclua"; //$NON-NLS-1$

	private ImageDescriptor descriptor;

	public void decorate(Object element, IDecoration decoration) {
		if (shouldDecorate(element)) {
			// get descriptor if needed
			if (descriptor == null) {
				descriptor = Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.DOCLUA_OVERLAY);
			}
			// apply it
			decoration.addOverlay(descriptor);
		}
	}

	private boolean shouldDecorate(Object element) {
		if (element instanceof ISourceModule)
			return ((ISourceModule) element).getElementName().endsWith(DOCLUA_EXT);
		else if (element instanceof IFile)
			return DOCLUA_EXT.equals(((IFile) element).getFileExtension());
		return false;
	}
}
