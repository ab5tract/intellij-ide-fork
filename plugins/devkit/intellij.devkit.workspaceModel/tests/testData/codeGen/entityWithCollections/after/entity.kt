package com.intellij.workspaceModel.test.api

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet

interface CollectionFieldEntity : WorkspaceEntity {
  val versions: Set<Int>
  val names: List<String>

  //region generated code
  @GeneratedCodeApiVersion(2)
  interface Builder : CollectionFieldEntity, WorkspaceEntity.Builder<CollectionFieldEntity> {
    override var entitySource: EntitySource
    override var versions: MutableSet<Int>
    override var names: MutableList<String>
  }

  companion object : EntityType<CollectionFieldEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(
      versions: Set<Int>,
      names: List<String>,
      entitySource: EntitySource,
      init: (Builder.() -> Unit)? = null,
    ): CollectionFieldEntity {
      val builder = builder()
      builder.versions = versions.toMutableWorkspaceSet()
      builder.names = names.toMutableWorkspaceList()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyEntity(
  entity: CollectionFieldEntity,
  modification: CollectionFieldEntity.Builder.() -> Unit,
): CollectionFieldEntity {
  return modifyEntity(CollectionFieldEntity.Builder::class.java, entity, modification)
}
//endregion
