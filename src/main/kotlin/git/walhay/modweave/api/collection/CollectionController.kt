package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.exception.CollectionCreationFailedException
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/collections")
class CollectionController(private val collectionService: ICollectionService) {

  @GetMapping("/{id}")
  fun getCollection(@PathVariable id: CollectionId): CollectionResponseDto =
      collectionService.getCollectionById(id).toCollectionResponseDto()

  @PostMapping
  fun createCollection(@Valid @ModelAttribute dto: CollectionCreateDto): CollectionResponseDto =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { collectionService.createCollection(UserId(it), dto) }
          ?.toCollectionResponseDto() ?: throw CollectionCreationFailedException()

  @PutMapping("/{id}")
  fun addModToCollection(@PathVariable id: CollectionId, @RequestParam modId: ModId) =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { collectionService.addModToCollection(UserId(it), id, modId) }
          ?.toCollectionResponseDto() ?: throw CollectionCreationFailedException()
}
