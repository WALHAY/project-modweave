package git.walhay.modweave.api.category

import com.fasterxml.jackson.annotation.JsonProperty

data class CategoryPatchDto(@param:JsonProperty(required = true) val description: String?)
