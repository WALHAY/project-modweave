package git.walhay.modweave.api.v1

import git.walhay.modweave.repositories.ModRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/mods")
class ModsController @Autowired constructor(private val modRepository: ModRepository) {

  @GetMapping
  fun getMods(@RequestParam page: Int, @RequestParam pageSize: Int) =
      modRepository.findAll(PageRequest.of(page, pageSize))

  @PostMapping fun uploadMod() {}
}
