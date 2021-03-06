// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.execution.impl

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.AnActionButton
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.labels.ActionLink
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

open class ProjectRunConfigurationConfigurable(project: Project, runDialog: RunDialogBase? = null) : RunConfigurable(project, runDialog) {
  override fun createLeftPanel(): JComponent {

    if (project.isDefault) {
      return ScrollPaneFactory.createScrollPane(tree)
    }

    val removeAction = MyRemoveAction()
    toolbarDecorator = ToolbarDecorator.createDecorator(tree)
      .setToolbarPosition(ActionToolbarPosition.TOP)
      .setPanelBorder(JBUI.Borders.empty())
      .setScrollPaneBorder(JBUI.Borders.empty())
      .setAddAction(toolbarAddAction).setAddActionName(ExecutionBundle.message("add.new.run.configuration.action2.name"))
      .setRemoveAction(removeAction).setRemoveActionUpdater(removeAction)
      .setRemoveActionName(ExecutionBundle.message("remove.run.configuration.action.name"))

      .addExtraAction(AnActionButton.fromAction(MyCopyAction()))
      .addExtraAction(AnActionButton.fromAction(MySaveAction()))
      .addExtraAction(AnActionButton.fromAction(MyEditTemplatesAction()))
      .addExtraAction(AnActionButton.fromAction(MyCreateFolderAction()))
      .addExtraAction(AnActionButton.fromAction(MySortFolderAction()))
      .setMinimumSize(JBDimension(200, 200))
      .setButtonComparator(ExecutionBundle.message("add.new.run.configuration.action2.name"),
                           ExecutionBundle.message("remove.run.configuration.action.name"),
                           ExecutionBundle.message("copy.configuration.action.name"),
                           ExecutionBundle.message("action.name.save.configuration"),
                           ExecutionBundle.message("run.configuration.edit.default.configuration.settings.text"),
                           ExecutionBundle.message("move.up.action.name"),
                           ExecutionBundle.message("move.down.action.name"),
                           ExecutionBundle.message("run.configuration.create.folder.text"))
      .setForcedDnD()
    val panel = toolbarDecorator!!.createPanel()
    initTree()
    return panel
  }

  override fun createTipPanelAboutAddingNewRunConfiguration(configurationType: ConfigurationType?): JPanel {
    if (!project.isDefault && DumbService.isDumb(project) &&
        (configurationType == null || !ConfigurationTypeUtil.isEditableInDumbMode(configurationType)))
      return JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.this.view.is.not.available.until.indices.are.built"))

    val messagePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    messagePanel.border = JBUI.Borders.empty(30, 0, 0, 0)
    messagePanel.add(JLabel(ExecutionBundle.message("empty.run.configuration.panel.text.label1")))

    val addIcon = ActionLink("", IconUtil.getAddIcon(), toolbarAddAction)
    addIcon.border = JBUI.Borders.empty(0, 3, 0, 3)
    messagePanel.add(addIcon)

    val configurationTypeDescription = when {
      configurationType != null -> configurationType.configurationTypeDescription
      else -> ExecutionBundle.message("run.configuration.default.type.description")
    }
    messagePanel.add(JLabel(ExecutionBundle.message("empty.run.configuration.panel.text.label3", configurationTypeDescription)))
    return messagePanel
  }

  override fun addRunConfigurationsToModel(model: DefaultMutableTreeNode) {
    for ((type, folderMap) in runManager.getConfigurationsGroupedByTypeAndFolder(true)) {
      val typeNode = DefaultMutableTreeNode(type)
      model.add(typeNode)
      for ((folder, configurations) in folderMap.entries) {
        val node: DefaultMutableTreeNode
        if (folder == null) {
          node = typeNode
        }
        else {
          node = DefaultMutableTreeNode(folder)
          typeNode.add(node)
        }

        for (it in configurations) {
          node.add(DefaultMutableTreeNode(it))
        }
      }
    }
  }
}