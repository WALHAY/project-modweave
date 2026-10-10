package git.walhay.modweave.domain.category

class CategoryExistsException(
    categoryId: CategoryId,
) : RuntimeException("Category with id=\"$categoryId\" already exists")
