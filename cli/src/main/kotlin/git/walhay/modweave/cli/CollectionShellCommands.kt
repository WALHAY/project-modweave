package git.walhay.modweave.cli

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CollectionShellCommands(
    private val collectionService: ICollectionService,
) : ShellCommandSupport() {
  @Command(name = ["collections", "get"], description = "Get collection by id.")
  fun collectionsGet(
      @Option(longName = "id") id: Long,
  ): Any = renderValue(collectionService.getCollectionById(CollectionId(id)))

  @Command(name = ["collections", "create"], description = "Create collection.")
  fun collectionsCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(
          collectionService.createCollection(UserId(userId), CollectionCreateCommand(name, description)),
      )

  @Command(name = ["collections", "delete"], description = "Delete collection.")
  fun collectionsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: Long,
  ): String {
    collectionService.deleteCollection(UserId(userId), CollectionId(id))
    return "Collection '$id' deleted."
  }

  @Command(name = ["collections", "add-mod"], description = "Add mod to collection.")
  fun collectionsAddMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "index", required = false) index: Int?,
  ): Any =
      renderValue(
          collectionService.addModToCollection(
              UserId(userId),
              CollectionId(collectionId),
              ModId(modId),
              index,
          ),
      )

  @Command(name = ["collections", "delete-mod"], description = "Delete mod from collection.")
  fun collectionsDeleteMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
  ): String {
    collectionService.deleteModFromCollection(UserId(userId), CollectionId(collectionId), ModId(modId))
    return "Mod '$modId' deleted from collection '$collectionId'."
  }
}
