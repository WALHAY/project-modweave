package git.walhay.modweave.domain.category

class CategoryNotFoundException(
    categoryId: CategoryId,
) : RuntimeException("Category with id=\"$categoryId\" already exists")
