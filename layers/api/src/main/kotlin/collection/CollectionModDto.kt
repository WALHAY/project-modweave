package git.walhay.modweave.api.collection

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class CollectionModDto(
    @field:NotEmpty val modId: String,
    @param:JsonSetter(nulls = Nulls.FAIL) @field:Min(0) val index: Int? = null,
)
