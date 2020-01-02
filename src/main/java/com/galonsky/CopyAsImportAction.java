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


// TODO: probably don't need to extend from this anymore
public class CopyAsImportAction extends CopyReferenceAction {

    private final CopyAsImportHelper helper;

    CopyAsImportAction(CopyAsImportHelper helper) {
        this.helper = helper;
    }

    public CopyAsImportAction() {
        this(new CopyAsImportHelper());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // TODO more checks here that are done in actionPerformed
        DataContext dataContext = e.getDataContext();
        Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        // Only interested in elements, not paths for now
        boolean enabled = project != null && editor != null;
        if (!enabled) {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setVisible(false);
            return;
        }
        List<PsiElement> elements = helper.getElementsToCopy(editor, dataContext);
        enabled = elements.size() == 1;
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);

        e.getPresentation().setText("Copy as Import");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("copy as import called");

        DataContext dataContext = e.getDataContext();
        Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }
        List<PsiElement> elements = helper.getElementsToCopy(editor, dataContext);
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