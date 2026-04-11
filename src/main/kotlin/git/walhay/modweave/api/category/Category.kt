package git.walhay.modweave.api.category

import java.io.Serializable

data class Category(var name: CategoryId, var description: String? = null) : Serializable
