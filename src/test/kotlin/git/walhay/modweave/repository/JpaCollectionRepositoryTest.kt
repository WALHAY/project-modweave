package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.testutils.InteractionTest
import git.walhay.modweave.testutils.StateTransitionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JpaCollectionRepositoryTest {
  private val springData = mock<SpringDataCollectionRepository>()
  private val repository = JpaCollectionRepository(springData)

  @StateTransitionTest
  @Test
  fun `saves collection and maps entity`() {
    val collection = Collection("Favorites", "Saved mods", UserId("alice"))
    whenever(springData.save(any<CollectionEntity>()))
        .thenReturn(CollectionEntity.fromCollection(collection))

    assertEquals(collection, repository.save(collection))
    verify(springData).save<CollectionEntity>(any())
  }

  @InteractionTest
  @Test
  fun `finds collections by owner`() {
    val collection = Collection("Favorites", "Saved mods", UserId("alice"))
    whenever(
            springData.findAllByOwner(
                "alice", org.springframework.data.domain.PageRequest.of(0, 10)))
        .thenReturn(
            org.springframework.data.domain.PageImpl(
                listOf(CollectionEntity.fromCollection(collection))))

    val page =
        repository.findAllByUser(
            UserId("alice"), org.springframework.data.domain.PageRequest.of(0, 10))

    assertEquals(collection, page.content.single())
  }
}
