package git.walhay.modweave.cli

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.http.dto.fromCollection
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CollectionShellCommands(
    private val collectionService: ICollectionService,
) : ShellCommandSupport() {
  @Command(name = ["collection", "get"], description = "Get collection by id.")
  fun collectionGet(
      @Option(longName = "id") id: Long,
  ): Any = renderValue(CollectionResponseDto.fromCollection(collectionService.getCollectionById(CollectionId(id))))

  @Command(name = ["collection", "create"], description = "Create collection.")
  fun collectionCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(
          CollectionResponseDto.fromCollection(
              collectionService.createCollection(UserId(userId), CollectionCreateCommand(name, description)),
          ),
      )

  @Command(name = ["collection", "delete"], description = "Delete collection.")
  fun collectionDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: Long,
  ): String {
    collectionService.deleteCollection(UserId(userId), CollectionId(id))
    return "Collection '$id' deleted."
  }

  @Command(name = ["collection", "add-mod"], description = "Add mod to collection.")
  fun collectionAddMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "index", required = false) index: Int?,
  ): Any =
      renderValue(
          CollectionResponseDto.fromCollection(
              collectionService.addModToCollection(
                  UserId(userId),
                  CollectionId(collectionId),
                  ModId(modId),
                  index,
              ),
          ),
      )

  @Command(name = ["collection", "delete-mod"], description = "Delete mod from collection.")
  fun collectionDeleteMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
  ): String {
    collectionService.deleteModFromCollection(UserId(userId), CollectionId(collectionId), ModId(modId))
    return "Mod '$modId' deleted from collection '$collectionId'."
  }
}
