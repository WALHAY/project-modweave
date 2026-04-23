package git.walhay.modweave.api.category.exception

import git.walhay.modweave.api.category.CategoryId

class CategoryNotFoundException(
    categoryId: CategoryId,
) : Exception("Category with id=\"$categoryId\" already exists")
