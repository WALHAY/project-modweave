package git.walhay.modweave.api.common

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links

/** Marks response DTOs that have a hypermedia representation in v2. */
interface ApiResource

data class ApiLink(@get:JsonIgnore val link: Link, val method: String) {
  val href: String
    get() = link.href
}

/** EntityModel unwraps the payload; this adapter preserves OpenAPI's href/method links. */
class ApiModel(resource: Any, @get:JsonProperty("_links") val resourceLinks: Map<String, ApiLink>) :
    EntityModel<Any>(resource, resourceLinks.map { (rel, link) -> link.link.withRel(rel) }) {
  @JsonIgnore override fun getLinks(): Links = super.getLinks()
}
