package git.walhay.modweave.service

import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.config.properties.ModweaveProperties
import org.springframework.data.domain.Sort

abstract class ServiceTestSupport {
  protected val pagePolicy = PageSizePolicy(ModweaveProperties())
  protected val sort = Sort.by("name")
}
