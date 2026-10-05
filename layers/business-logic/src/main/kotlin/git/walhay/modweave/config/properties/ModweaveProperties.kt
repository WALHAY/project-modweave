package git.walhay.modweave.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "modweave")
data class ModweaveProperties(
    val business: Business = Business(),
) {
  data class Business(
      val maxPageSize: Int = 100,
  ) {
    init {
      require(maxPageSize > 0) { "Maximum page size must be positive" }
    }
  }
}
