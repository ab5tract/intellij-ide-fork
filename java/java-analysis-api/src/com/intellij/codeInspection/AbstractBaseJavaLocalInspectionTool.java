// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInspection;

import com.intellij.java.analysis.JavaAnalysisBundle;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.pom.java.JavaFeature;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractBaseJavaLocalInspectionTool extends LocalInspectionTool {

  /**
   * @return set of the features required for a given inspection. The inspection will not be launched on the files where
   * the corresponding features are not available.
   */
  public @NotNull Set<@NotNull JavaFeature> requiredFeatures() {
    return Set.of();
  }

  @Override
  public boolean isAvailableForFile(@NotNull PsiFile file) {
    for (JavaFeature feature : requiredFeatures()) {
      if (!PsiUtil.isAvailable(feature, file)) return false;
    }
    return true;
  }

  @Override
  public HtmlChunk getDescriptionAddendum() {
    Set<JavaFeature> features = requiredFeatures();
    JavaFeature feature = ContainerUtil.getOnlyItem(features);
    if (feature != null) {
      return HtmlChunk.text(JavaAnalysisBundle.message("inspection.depends.on.the.java.feature", 
                                                       feature.getFeatureName(), feature.getMinimumLevel().getShortText()))
        .wrapWith("p");
    }
    return HtmlChunk.empty();
  }

  /**
   * Override this to report problems at method level.
   *
   * @param method     to check.
   * @param manager    InspectionManager to ask for ProblemDescriptors from.
   * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
   * @return {@code null} if no problems found or not applicable at method level.
   */
  public ProblemDescriptor @Nullable [] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
    return null;
  }

  /**
   * Override this to report problems at class level.
   *
   * @param aClass     to check.
   * @param manager    InspectionManager to ask for ProblemDescriptors from.
   * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
   * @return {@code null} if no problems found or not applicable at class level.
   */
  public ProblemDescriptor @Nullable [] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
    return null;
  }

  /**
   * Override this to report problems at field level.
   *
   * @param field      to check.
   * @param manager    InspectionManager to ask for ProblemDescriptors from.
   * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
   * @return {@code null} if no problems found or not applicable at field level.
   */
  public ProblemDescriptor @Nullable [] checkField(@NotNull PsiField field, @NotNull InspectionManager manager, boolean isOnTheFly) {
    return null;
  }

  @Override
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitMethod(@NotNull PsiMethod method) {
        super.visitMethod(method);
        addDescriptors(checkMethod(method, holder.getManager(), isOnTheFly));
      }

      @Override
      public void visitClass(@NotNull PsiClass aClass) {
        super.visitClass(aClass);
        addDescriptors(checkClass(aClass, holder.getManager(), isOnTheFly));
      }

      @Override
      public void visitField(@NotNull PsiField field) {
        super.visitField(field);
        addDescriptors(checkField(field, holder.getManager(), isOnTheFly));
      }

      @Override
      public void visitFile(@NotNull PsiFile file) {
        super.visitFile(file);
        addDescriptors(checkFile(file, holder.getManager(), isOnTheFly));
      }

      private void addDescriptors(final ProblemDescriptor[] descriptors) {
        if (descriptors != null) {
          for (ProblemDescriptor descriptor : descriptors) {
            holder.registerProblem(descriptor);
          }
        }
      }
    };
  }
}
