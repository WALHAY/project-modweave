package git.walhay.modweave.service

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.*
import git.walhay.modweave.api.version.exception.VersionExistsException
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import git.walhay.modweave.testutils.CombinatorialTest
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

@CombinatorialTest
class VersionServiceTest {
  private val repository = mock<VersionRepository>()
  private val files = mock<IFileService>()
  private val security = mock<AccessSecurity>()
  private val service = VersionService(repository, files, security)
  private val modService: IModService = mock()

  init {
    val field = VersionService::class.java.getDeclaredField("modService")
    field.isAccessible = true
    field.set(service, modService)
  }

  @Test
  fun `gets existing version`() {
    val version = TestFixtures.version()
    whenever(repository.findVersionById(version.id)).thenReturn(version)
    assertSame(version, service.getModVersion(version.id))
  }

  @Test
  fun `throws when version is missing`() {
    val id = TestFixtures.version().id
    whenever(repository.findVersionById(id)).thenReturn(null)

    assertThrows(VersionNotFoundException::class.java) { service.getModVersion(id) }
  }

  @Test
  fun `returns all versions for owner`() {
    val page = PageImpl(listOf(TestFixtures.version()))
    whenever(security.isModOwnerOrAdmin("alice", "sodium")).thenReturn(true)
    whenever(repository.findVersionsByModId(any(), any<Pageable>())).thenReturn(page)

    assertSame(
        page, service.getModVersions(UserId("alice"), ModId("sodium"), PageRequest.of(0, 10)))
    verify(repository).findVersionsByModId(ModId("sodium"), PageRequest.of(0, 10))
  }

  @Test
  fun `returns approved versions for another user`() {
    val page = PageImpl(listOf(TestFixtures.version()))
    whenever(security.isModOwnerOrAdmin("bob", "sodium")).thenReturn(false)
    whenever(repository.findVersionsByModIdAndStatus(any(), any(), any<Pageable>()))
        .thenReturn(page)

    assertSame(page, service.getModVersions(UserId("bob"), ModId("sodium"), PageRequest.of(0, 10)))
    verify(repository)
        .findVersionsByModIdAndStatus(
            ModId("sodium"), VersionStatus.APPROVED, PageRequest.of(0, 10))
  }

  @Test
  fun `creates version from mod and uploads files`() {
    val mod = TestFixtures.mod()
    val command =
        ModCreateCommand(
            mod.id,
            mod.name,
            null,
            TestFixtures.image(),
            versionName = "1.0.0",
            files = listOf(TestFixtures.archive()),
            gameId = mod.gameId)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    val result = service.createModVersion(mod, command)

    assertEquals("1.0.0", result.name)
    assertTrue(mod.versions.contains(result))
    verify(files).uploadVersionFiles(result, command.files)
  }

  @Test
  fun `creates version from id`() {
    val mod = TestFixtures.mod()
    val command =
        git.walhay.modweave.api.version.command.VersionCreateCommand(
            "2.0.0", "Changes", listOf(TestFixtures.archive()))
    whenever(modService.findModById(mod.id)).thenReturn(mod)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    val result = service.createModVersion(mod.id, command)

    assertEquals("2.0.0", result.name)
    verify(files).uploadVersionFiles(result, command.files)
  }

  @Test
  fun `rejects duplicate version name`() {
    val mod = TestFixtures.mod(versions = mutableListOf(TestFixtures.version("1.0.0")))
    val command =
        git.walhay.modweave.api.version.command.VersionCreateCommand("1.0.0", null, emptyList())
    whenever(modService.findModById(mod.id)).thenReturn(mod)

    assertThrows(VersionExistsException::class.java) { service.createModVersion(mod.id, command) }
    verify(repository, never()).save(any())
  }

  @Test
  fun `changes version status`() {
    val version = TestFixtures.version()
    whenever(repository.findVersionById(version.id)).thenReturn(version)
    whenever(repository.save(version)).thenReturn(version)

    val result = service.changeVersionStatus(UserId("admin"), version.id, VersionStatus.APPROVED)

    assertEquals(VersionStatus.APPROVED, result.status)
    verify(repository).save(version)
  }

  @Test
  fun `rejects changing status of missing version`() {
    val id = TestFixtures.version().id
    whenever(repository.findVersionById(id)).thenReturn(null)

    assertThrows(VersionNotFoundException::class.java) {
      service.changeVersionStatus(UserId("admin"), id, VersionStatus.APPROVED)
    }
    verify(repository, never()).save(any())
  }

  @Test
  fun `deletes version`() {
    val version = TestFixtures.version()
    whenever(repository.findVersionById(version.id)).thenReturn(version)

    service.deleteModVersion(version.id)

    verify(repository).delete(version.id)
  }

  @Test
  fun `rejects deleting missing version`() {
    val id = TestFixtures.version().id
    whenever(repository.findVersionById(id)).thenReturn(null)

    assertThrows(VersionNotFoundException::class.java) { service.deleteModVersion(id) }
    verify(repository, never()).delete(any())
  }
}
