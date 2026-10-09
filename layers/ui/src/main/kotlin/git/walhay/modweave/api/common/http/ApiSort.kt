package git.walhay.modweave.api.common.http

import org.springframework.data.domain.Sort

/** Reject undeclared properties before JPA and use a unique tie-breaker for stable pagination. */
fun Sort.forApi(vararg properties: String, identity: String = "id"): Sort {
  require(all { it.property in properties }) {
    "Unsupported sort property; allowed: ${properties.joinToString()}"
  }
  return if (getOrderFor(identity) != null) this else and(Sort.by(identity))
}
