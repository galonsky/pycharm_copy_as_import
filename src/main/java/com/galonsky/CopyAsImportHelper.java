package com.galonsky;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.ide.actions.QualifiedNameProviderUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CopyAsImportHelper {

    // Copy from CopyReferenceUtil.java, which had package private access :/
    PsiElement adjustElement(PsiElement element) {
        PsiElement adjustedElement = QualifiedNameProviderUtil.adjustElementToCopy(element);
        return adjustedElement != null ? adjustedElement : element;
    }


    @NotNull
    // Adapted from CopyReferenceUtil.java
    List<PsiElement> getElementsToCopy(final Editor editor, final DataContext dataContext) {
        List<PsiElement> elements = new ArrayList<>();
        PsiReference reference = TargetElementUtil.findReference(editor);
        if (reference != null) {
            ContainerUtil.addIfNotNull(elements, reference.getElement());
        }

        if (elements.isEmpty()) {
            PsiElement[] psiElements = LangDataKeys.PSI_ELEMENT_ARRAY.getData(dataContext);
            if (psiElements != null) {
                Collections.addAll(elements, psiElements);
            }
        }

        if (elements.isEmpty()) {
            ContainerUtil.addIfNotNull(elements, CommonDataKeys.PSI_ELEMENT.getData(dataContext));
        }

        return ContainerUtil.mapNotNull(elements, element -> element instanceof PsiFile && !((PsiFile)element).getViewProvider().isPhysical()
                ? null
                : adjustElement(element));
    }

    // Copy from CopyReferenceUtil.java, which had package private access :/
    void setStatusBarText(Project project, String message) {
        if (project != null) {
            final StatusBarEx statusBar = (StatusBarEx) WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                statusBar.setInfo(message);
            }
        }
    }
}
