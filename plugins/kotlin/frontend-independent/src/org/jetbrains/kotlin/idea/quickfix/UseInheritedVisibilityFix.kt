// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.quickfix

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import org.jetbrains.kotlin.idea.base.resources.KotlinBundle
import org.jetbrains.kotlin.idea.codeinsight.api.applicators.fixes.KotlinModCommandAction
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

class UseInheritedVisibilityFix(
    element: KtModifierListOwner,
    elementContext: ElementContext
) : KotlinModCommandAction<KtModifierListOwner, UseInheritedVisibilityFix.ElementContext>(element, elementContext) {
    override fun getFamilyName() = KotlinBundle.message("use.inherited.visibility")

    class ElementContext(val modifierType: KtModifierKeywordToken) : KotlinModCommandAction.ElementContext

    override fun invoke(context: ActionContext, element: KtModifierListOwner, elementContext: ElementContext, updater: ModPsiUpdater) {
        element.removeModifier(elementContext.modifierType)
    }

    companion object {
        fun createFix(element: KtModifierListOwner): List<UseInheritedVisibilityFix> {
            val modifierType = element.visibilityModifierType() ?: return emptyList()
            return listOf(UseInheritedVisibilityFix(element, ElementContext(modifierType)))
        }
    }
}