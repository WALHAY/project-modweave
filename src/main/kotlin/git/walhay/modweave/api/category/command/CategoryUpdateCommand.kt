package git.walhay.modweave.api.category.command

import git.walhay.modweave.api.category.CategoryId

data class CategoryUpdateCommand(val name: CategoryId, val description: String?)
