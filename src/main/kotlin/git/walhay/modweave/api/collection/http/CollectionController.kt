package git.walhay.modweave.api.collection.http

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.http.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.http.dto.fromCollection
import git.walhay.modweave.api.collection.http.dto.toCollectionCreateCommand
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.fromMod
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/collections")
class CollectionController(
    private val collectionService: ICollectionService,
    private val modService: IModService,
) {
  @GetMapping("/{collectionId}")
  fun getCollection(
      @PathVariable collectionId: CollectionId,
  ): CollectionResponseDto =
      collectionService.getCollectionById(collectionId).let {
        CollectionResponseDto.fromCollection(it)
      }

  @GetMapping("/{collectionId}/mods")
  fun getModsInCollection(
      @PathVariable collectionId: Long,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["index"]) sort: Sort,
  ): Page<ModResponseDto> =
      modService.findModsInCollection(CollectionId(collectionId), page, size, sort).map {
        ModResponseDto.fromMod(it)
      }

  @PostMapping
  fun createCollection(
      @Valid @ModelAttribute dto: CollectionCreateDto,
      @AuthenticationPrincipal user: UserDetails,
  ): CollectionResponseDto =
      collectionService
          .createCollection(UserId(user.username), dto.toCollectionCreateCommand())
          .let { CollectionResponseDto.fromCollection(it) }

  @PutMapping("/{collectionId}")
  fun addModToCollection(
      @PathVariable collectionId: Long,
      @RequestParam modId: String,
      @RequestParam(required = false) index: Int?,
      @AuthenticationPrincipal user: UserDetails,
  ) =
      collectionService.addModToCollection(
          UserId(user.username), CollectionId(collectionId), ModId(modId), index)

  @DeleteMapping("/{collectionId}")
  fun deleteCollection(
      collectionId: Long,
      @AuthenticationPrincipal user: UserDetails,
  ) = collectionService.deleteCollection(UserId(user.username), CollectionId(collectionId))

  @DeleteMapping("/{collectionId}/mods/{modId}")
  fun deleteModFromCollection(
      @PathVariable collectionId: Long,
      @PathVariable modId: String,
      @AuthenticationPrincipal user: UserDetails,
  ) =
      collectionService.deleteModFromCollection(
          UserId(user.username), CollectionId(collectionId), ModId(modId))
}
