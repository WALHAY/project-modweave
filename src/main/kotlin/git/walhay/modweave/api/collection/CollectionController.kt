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

  @GetMapping("/{collectionId}")
  fun getCollection(@PathVariable collectionId: CollectionId): CollectionResponseDto =
      collectionService.getCollectionById(collectionId).toCollectionResponseDto()

  @PostMapping
  fun createCollection(@Valid @ModelAttribute dto: CollectionCreateDto): CollectionResponseDto =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { collectionService.createCollection(UserId(it), dto) }
          ?.toCollectionResponseDto() ?: throw CollectionCreationFailedException()

  @PutMapping("/{collectionId}")
  fun addModToCollection(
      @PathVariable collectionId: CollectionId,
      @RequestParam modId: ModId,
      @RequestParam(required = false) index: Int?
  ) =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { collectionService.addModToCollection(UserId(it), collectionId, modId, index) }
          ?.toCollectionResponseDto() ?: throw CollectionCreationFailedException()
}
