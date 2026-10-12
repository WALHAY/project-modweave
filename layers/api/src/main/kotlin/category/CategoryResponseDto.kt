package git.walhay.modweave.api.category

import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.category.Category

data class CategoryResponseDto(
    val name: String,
    val description: String? = null,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): CategoryResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  val self = link("/categories/{name}", name)
                  put("self", self)
                  put("collection", link("/categories"))
                  if (isAdmin) {
                    put("update", self.copy(method = "PATCH"))
                    put("delete", self.copy(method = "DELETE"))
                  }
                })
      }

  companion object {
    fun fromCategory(category: Category): CategoryResponseDto =
        CategoryResponseDto(
            name = category.name.value,
            description = category.description,
        )
  }
}

data class CategoryCollectionDto(
    val items: List<CategoryResponseDto>,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): CategoryCollectionDto =
      with(context) {
        copy(
            items = items.map { it.withLinks(context) },
            links =
                buildMap {
                  put("self", link("/categories"))
                  if (isAdmin) put("create", link("/categories", method = "POST"))
                })
      }
}
