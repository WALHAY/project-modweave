package git.walhay.modweave.api.collection.http

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.http.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.http.dto.fromCollection
import git.walhay.modweave.api.collection.http.dto.toCollectionCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/collections")
class CollectionController(private val collectionService: ICollectionService) {

  @GetMapping("/{collectionId}")
  fun getCollection(@PathVariable collectionId: CollectionId): CollectionResponseDto =
      collectionService.getCollectionById(collectionId).let { CollectionResponseDto.fromCollection(it) }

  @PostMapping
  fun createCollection(@Valid @ModelAttribute dto: CollectionCreateDto, @AuthenticationPrincipal user: UserDetails): CollectionResponseDto =
      collectionService.createCollection(UserId(user.username), dto.toCollectionCreateCommand()).let { CollectionResponseDto.fromCollection(it) }

  @PutMapping("/{collectionId}")
  fun addModToCollection(
      @PathVariable collectionId: Long,
      @RequestParam modId: String,
      @RequestParam(required = false) index: Int?,
      @AuthenticationPrincipal user: UserDetails
  ) = collectionService.addModToCollection(UserId(user.username), CollectionId(collectionId), ModId(modId), index)
}
