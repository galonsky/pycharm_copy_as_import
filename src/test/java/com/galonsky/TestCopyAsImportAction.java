package com.galonsky;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TestCopyAsImportAction {

    private DataContext dataContext;
    private Editor editor;
    private Project project;
    private Presentation presentation;
    private CopyAsImportHelper helper;
    private AnActionEvent event;


    @Before
    public void setup() {
        this.dataContext = mock(DataContext.class);
        this.editor = mock(Editor.class);
        this.project = mock(Project.class);
        this.presentation = mock(Presentation.class);

    }

    private void givenEditor(Editor editor) {
        this.editor = editor;
    }

    private void givenProject(Project project) {
        this.project = project;
    }

    private CopyAsImportAction givenAction() {
        final ActionManager actionManager = mock(ActionManager.class);

        when(dataContext.getData(CommonDataKeys.EDITOR)).thenReturn(editor);
        when(dataContext.getData(CommonDataKeys.PROJECT)).thenReturn(project);

        this.event = new AnActionEvent(null, dataContext, "place", presentation, actionManager, 0);
        this.helper = mock(CopyAsImportHelper.class);
        return new CopyAsImportAction(helper);
    }

    @Test
    public void testWhenNoEditorSetsDisabled() {
        givenEditor(null);
        var action = givenAction();
        action.update(event);
        verifyDisabled();
        verify(helper, never()).getElementsToCopy(any(), any());
    }

    @Test
    public void testWhenNoProjectSetsDisabled() {
        givenProject(null);
        var action = givenAction();
        action.update(event);
        verifyDisabled();
        verify(helper, never()).getElementsToCopy(any(), any());
    }

    private void verifyDisabled() {
        verify(presentation).setEnabled(false);
        verify(presentation).setVisible(false);
    }
}
