// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.codeinsights.impl.base.quickFix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KtFirDiagnostic
import org.jetbrains.kotlin.idea.base.resources.KotlinBundle
import org.jetbrains.kotlin.idea.codeinsight.api.applicators.fixes.KotlinQuickFixFactory
import org.jetbrains.kotlin.idea.codeinsight.api.classic.quickfixes.KotlinPsiOnlyQuickFixAction
import org.jetbrains.kotlin.idea.codeinsight.api.classic.quickfixes.QuickFixesPsiBasedFactory
import org.jetbrains.kotlin.idea.codeinsight.api.classic.quickfixes.quickFixesPsiBasedFactory
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class ChangeVariableMutabilityFix(
    element: KtValVarKeywordOwner,
    private val makeVar: Boolean,
    private val actionText: String? = null,
    private val deleteInitializer: Boolean = false
) : KotlinPsiOnlyQuickFixAction<KtValVarKeywordOwner>(element) {

    override fun getText() = actionText ?: buildString {
        if (makeVar) append(KotlinBundle.message("change.to.var")) else append(KotlinBundle.message("change.to.val"))
        if (deleteInitializer) append(KotlinBundle.message("and.delete.initializer"))
    }

    override fun getFamilyName(): String = text

    override fun isAvailable(project: Project, editor: Editor?, file: KtFile): Boolean {
        val element = element ?: return false
        val valOrVar = element.valOrVarKeyword?.node?.elementType ?: return false
        return (valOrVar == KtTokens.VAR_KEYWORD) != makeVar
    }

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val element = element ?: return
        val factory = KtPsiFactory(project)
        val newKeyword = if (makeVar) factory.createVarKeyword() else factory.createValKeyword()
        element.valOrVarKeyword!!.replace(newKeyword)
        if (deleteInitializer) {
            (element as? KtProperty)?.initializer = null
        }
        if (makeVar) {
            (element as? KtModifierListOwner)?.removeModifier(KtTokens.CONST_KEYWORD)
        }
    }

    companion object {

        val VAL_WITH_SETTER_FACTORY: QuickFixesPsiBasedFactory<KtPropertyAccessor> =
            quickFixesPsiBasedFactory { psiElement: KtPropertyAccessor ->
                listOf(ChangeVariableMutabilityFix(psiElement.property, true))
            }

        val VAL_REASSIGNMENT = KotlinQuickFixFactory.IntentionBased { diagnostic: KtFirDiagnostic.ValReassignment ->
            val property = diagnostic.variable.psi as? KtValVarKeywordOwner
                ?: return@IntentionBased emptyList()
            listOf(
                ChangeVariableMutabilityFix(property, makeVar = true),
            )
        }


        val VAR_OVERRIDDEN_BY_VAL_FACTORY: QuickFixesPsiBasedFactory<PsiElement> =
            quickFixesPsiBasedFactory { psiElement: PsiElement ->
                when (psiElement) {
                    is KtProperty, is KtParameter -> listOf(ChangeVariableMutabilityFix(psiElement as KtValVarKeywordOwner, true))
                    else -> emptyList()
                }
            }

        val VAR_ANNOTATION_PARAMETER_FACTORY: QuickFixesPsiBasedFactory<KtParameter> =
            quickFixesPsiBasedFactory { psiElement: KtParameter ->
                listOf(ChangeVariableMutabilityFix(psiElement, false))
            }

        val LATEINIT_VAL_FACTORY: QuickFixesPsiBasedFactory<KtModifierListOwner> =
            quickFixesPsiBasedFactory { psiElement: KtModifierListOwner ->
                val property = psiElement as? KtProperty ?: return@quickFixesPsiBasedFactory emptyList()
                if (property.valOrVarKeyword.text != "val") {
                    emptyList()
                } else {
                    listOf(ChangeVariableMutabilityFix(property, makeVar = true))
                }
            }

        val CONST_VAL_FACTORY: QuickFixesPsiBasedFactory<PsiElement> =
            quickFixesPsiBasedFactory { psiElement: PsiElement ->
                if (psiElement.node.elementType as? KtModifierKeywordToken != KtTokens.CONST_KEYWORD) return@quickFixesPsiBasedFactory emptyList()
                val property = psiElement.getStrictParentOfType<KtProperty>() ?: return@quickFixesPsiBasedFactory emptyList()
                listOf(ChangeVariableMutabilityFix(property, makeVar = false))
            }

        val MUST_BE_INITIALIZED_FACTORY: QuickFixesPsiBasedFactory<PsiElement> =
            quickFixesPsiBasedFactory { psiElement: PsiElement ->
                val property = psiElement as? KtProperty ?: return@quickFixesPsiBasedFactory emptyList()
                val getter = property.getter ?: return@quickFixesPsiBasedFactory emptyList()
                if (!getter.hasBody()) return@quickFixesPsiBasedFactory emptyList()
                if (getter.hasBlockBody() && property.typeReference == null) return@quickFixesPsiBasedFactory emptyList()
                listOf(ChangeVariableMutabilityFix(property, makeVar = false))
            }
    }
}