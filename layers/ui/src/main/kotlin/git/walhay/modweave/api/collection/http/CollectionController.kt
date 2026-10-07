package git.walhay.modweave.api.collection.http

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.http.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import java.net.URI
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/collections")
class CollectionController(
    private val collectionService: ICollectionService,
    private val modService: IModService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{collectionId}")
  fun getCollection(
      @PathVariable collectionId: UUID,
  ): CollectionResponseDto {
    logger.info { "GET /collections/$collectionId" }
    return collectionService.getCollectionById(CollectionId(collectionId)).let {
      CollectionResponseDto.fromCollection(it)
    }
  }

  @GetMapping("/{collectionId}/mods")
  fun getModsInCollection(
      @PathVariable collectionId: UUID,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["index"]) sort: Sort,
  ): Page<ModResponseDto> {
    logger.info { "GET /collections/$collectionId/mods - page: $page, size: $size" }
    collectionService.getCollectionById(CollectionId(collectionId))
    return modService.findModsInCollection(CollectionId(collectionId), page, size, sort).map {
      ModResponseDto.fromMod(it)
    }
  }

  @PostMapping(
      version = "1", consumes = ["application/x-www-form-urlencoded", "multipart/form-data"])
  fun createCollection(
      @Valid @ModelAttribute dto: CollectionCreateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): CollectionResponseDto {
    val username = requireUsername(user)
    logger.info { "POST /collections - creating collection: ${dto.name} for user: $username" }
    return collectionService
        .createCollection(UserId(username), dto.toCollectionCreateCommand())
        .let { CollectionResponseDto.fromCollection(it) }
  }

  @PostMapping(version = "2", consumes = ["application/x-www-form-urlencoded"])
  fun createCollectionV2(
      @Valid @ModelAttribute dto: CollectionCreateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): ResponseEntity<CollectionResponseDto> {
    val result = createCollection(dto, user)
    return ResponseEntity.created(URI.create("/api/v2/collections/${result.id}")).body(result)
  }

  @PutMapping("/{collectionId}", version = "1")
  fun addModToCollection(
      @PathVariable collectionId: UUID,
      @RequestParam modId: String,
      @RequestParam(required = false) @Min(0) index: Int?,
      @AuthenticationPrincipal user: UserDetails?,
  ): CollectionResponseDto {
    val username = requireUsername(user)
    logger.info { "PUT /collections/$collectionId - adding mod: $modId for user: $username" }
    return collectionService
        .addModToCollection(UserId(username), CollectionId(collectionId), ModId(modId), index)
        .let { CollectionResponseDto.fromCollection(it) }
  }

  @PostMapping("/{collectionId}/mods", version = "2")
  fun insertMod(
      @PathVariable collectionId: UUID,
      @RequestParam modId: String,
      @RequestParam(required = false) @Min(0) index: Int?,
      @AuthenticationPrincipal user: UserDetails?,
  ): CollectionResponseDto = addModToCollection(collectionId, modId, index, user)

  @DeleteMapping("/{collectionId}", version = "1")
  fun deleteCollection(
      @PathVariable collectionId: UUID,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val username = requireUsername(user)
    logger.info { "DELETE /collections/$collectionId for user: $username" }
    collectionService.deleteCollection(UserId(username), CollectionId(collectionId))
  }

  @DeleteMapping("/{collectionId}", version = "2")
  @ResponseStatus(NO_CONTENT)
  fun removeCollection(
      @PathVariable collectionId: UUID,
      @AuthenticationPrincipal user: UserDetails?,
  ) = deleteCollection(collectionId, user)

  @DeleteMapping("/{collectionId}/mods/{modId}", version = "1")
  fun deleteModFromCollection(
      @PathVariable collectionId: UUID,
      @PathVariable modId: String,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val username = requireUsername(user)
    logger.info { "DELETE /collections/$collectionId/mods/$modId for user: $username" }
    return collectionService.deleteModFromCollection(
        UserId(username), CollectionId(collectionId), ModId(modId))
  }

  @DeleteMapping("/{collectionId}/mods/{modId}", version = "2")
  @ResponseStatus(NO_CONTENT)
  fun removeCollectionMod(
      @PathVariable collectionId: UUID,
      @PathVariable modId: String,
      @AuthenticationPrincipal user: UserDetails?,
  ) = deleteModFromCollection(collectionId, modId, user)

  private fun requireUsername(user: UserDetails?): String =
      user?.username ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
