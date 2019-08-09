package com.galonsky;

import com.google.common.base.Preconditions;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.ide.actions.QualifiedNameProviderUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.psi.*;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CopyAsImportAction extends CopyReferenceAction {

    // Copy from CopyReferenceUtil.java, which had package private access :/
    private static PsiElement adjustElement(PsiElement element) {
        PsiElement adjustedElement = QualifiedNameProviderUtil.adjustElementToCopy(element);
        return adjustedElement != null ? adjustedElement : element;
    }


    @NotNull
    // Adapted from CopyReferenceUtil.java
    private static List<PsiElement> getElementsToCopy(final Editor editor, final DataContext dataContext) {
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
    static void setStatusBarText(Project project, String message) {
        if (project != null) {
            final StatusBarEx statusBar = (StatusBarEx) WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                statusBar.setInfo(message);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // TODO more checks here that are done in actionPerformed
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        // Only interested in elements, not paths for now
        boolean enabled = project != null && editor != null;
        if (!enabled) {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setVisible(false);
        }
        List<PsiElement> elements = getElementsToCopy(editor, dataContext);
        enabled = elements.size() == 1;
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);

        e.getPresentation().setText("Copy as Import");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("copy as import called");

        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        List<PsiElement> elements = getElementsToCopy(editor, dataContext);
        Preconditions.checkState(elements.size() == 1);

        final PsiElement element = elements.get(0);
        if (!(element instanceof PsiNamedElement)) {
            return;
        }
        final PsiNamedElement namedElement = (PsiNamedElement) element;

        final PyElementGenerator generator = PyElementGenerator.getInstance(project);
        final LanguageLevel languageLevel = LanguageLevel.forElement(element);

        final String name = namedElement.getName();
        if (name == null) return;

        final QualifiedName importPath = QualifiedNameFinder.findCanonicalImportPath(element, element);
        if (importPath == null) return;

        final String path = importPath.toString();

        final PyFromImportStatement importStatement = generator.createFromImportStatement(languageLevel, path, name, null);
        final String importStr = importStatement.getText();

        CopyPasteManager.getInstance().setContents(new CopyAsImportTransferable(importStr));
//        setStatusBarText(project, IdeBundle.message("message.import.has.been.copied", importStr));
    }
}