package git.walhay.modweave.api.collection.http

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.http.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import mu.KLogger
import mu.KotlinLogging
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
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{collectionId}")
  fun getCollection(
      @PathVariable collectionId: CollectionId,
  ): CollectionResponseDto {
    logger.info { "GET /collections/$collectionId" }
    return collectionService.getCollectionById(collectionId).let {
      CollectionResponseDto.fromCollection(it)
    }
  }

  @GetMapping("/{collectionId}/mods")
  fun getModsInCollection(
      @PathVariable collectionId: Long,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["index"]) sort: Sort,
  ): Page<ModResponseDto> {
    logger.info { "GET /collections/$collectionId/mods - page: $page, size: $size" }
    return modService.findModsInCollection(CollectionId(collectionId), page, size, sort).map {
      ModResponseDto.fromMod(it)
    }
  }

  @PostMapping
  fun createCollection(
      @Valid @ModelAttribute dto: CollectionCreateDto,
      @AuthenticationPrincipal user: UserDetails,
  ): CollectionResponseDto {
    logger.info {
      "POST /collections - creating collection: ${dto.name} for user: ${user.username}"
    }
    return collectionService
        .createCollection(UserId(user.username), dto.toCollectionCreateCommand())
        .let { CollectionResponseDto.fromCollection(it) }
  }

  @PutMapping("/{collectionId}")
  fun addModToCollection(
      @PathVariable collectionId: Long,
      @RequestParam modId: String,
      @RequestParam(required = false) index: Int?,
      @AuthenticationPrincipal user: UserDetails,
  ): CollectionResponseDto {
    logger.info { "PUT /collections/$collectionId - adding mod: $modId for user: ${user.username}" }
    return collectionService
        .addModToCollection(UserId(user.username), CollectionId(collectionId), ModId(modId), index)
        .let { CollectionResponseDto.fromCollection(it) }
  }

  @DeleteMapping("/{collectionId}")
  fun deleteCollection(
      collectionId: Long,
      @AuthenticationPrincipal user: UserDetails,
  ) {
    logger.info { "DELETE /collections/$collectionId for user: ${user.username}" }
    collectionService.deleteCollection(UserId(user.username), CollectionId(collectionId))
  }

  @DeleteMapping("/{collectionId}/mods/{modId}")
  fun deleteModFromCollection(
      @PathVariable collectionId: Long,
      @PathVariable modId: String,
      @AuthenticationPrincipal user: UserDetails,
  ) {
    logger.info { "DELETE /collections/$collectionId/mods/$modId for user: ${user.username}" }
    return collectionService.deleteModFromCollection(
        UserId(user.username), CollectionId(collectionId), ModId(modId))
  }
}
