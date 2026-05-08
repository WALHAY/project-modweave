package git.walhay.modweave.api.category

import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType

@JvmInline
value class CategoryId(val value: String = "") {
  override fun toString(): String = value
}
