package git.walhay.modweave.regression

import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.*
import git.walhay.modweave.api.version.command.VersionCreateCommand
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import git.walhay.modweave.config.properties.ModweaveProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class ServiceSecurityTest {
  @Configuration(proxyBeanMethods = false)
  @EnableMethodSecurity(proxyTargetClass = true)
  @EnableCaching(proxyTargetClass = true)
  @Import(VersionService::class, AccessSecurity::class)
  class Config {
    @Bean fun mods(): ModRepository = mock(ModRepository::class.java)

    @Bean fun versions(): VersionRepository = mock(VersionRepository::class.java)

    @Bean fun collections(): CollectionRepository = mock(CollectionRepository::class.java)

    @Bean fun comments(): CommentRepository = mock(CommentRepository::class.java)

    @Bean
    fun storage(): git.walhay.modweave.api.storage.ISimpleStorageService =
        mock(git.walhay.modweave.api.storage.ISimpleStorageService::class.java)

    @Bean fun files(): IFileService = mock(IFileService::class.java)

    @Bean fun modService(): IModService = mock(IModService::class.java)

    @Bean fun pageSizePolicy() = PageSizePolicy(ModweaveProperties())

    @Bean fun cacheManager(): CacheManager = ConcurrentMapCacheManager()
  }

  private lateinit var context: AnnotationConfigApplicationContext
  private lateinit var service: VersionService
  private lateinit var versions: VersionRepository
  private val modId = ModId("example")
  private lateinit var version: Version

  @BeforeEach
  fun setup() {
    context = AnnotationConfigApplicationContext(Config::class.java)
    service = context.getBean(VersionService::class.java)
    versions = context.getBean(VersionRepository::class.java)
    version = Version("1.0", null, modId)
    `when`(versions.findVersionById(version.id)).thenReturn(version)
    val mod =
        Mod(
            modId,
            "Example",
            imagePath = "image",
            publisherId = UserId("owner"),
            gameId = GameId("game"))
    `when`(context.getBean(ModRepository::class.java).findById(modId)).thenReturn(mod)
  }

  @AfterEach
  fun cleanup() {
    SecurityContextHolder.clearContext()
    context.close()
  }

  private fun login(username: String, role: String = "USER") {
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken(
            username, null, listOf(SimpleGrantedAuthority("ROLE_$role")))
  }

  @Test
  fun `unapproved version cannot be read from cache by another user`() {
    login("owner")
    assertEquals(version.id, service.getModVersion(version.id).id)
    login("stranger")
    assertThrows(AccessDeniedException::class.java) { service.getModVersion(version.id) }
  }

  @Test
  fun `guest can read approved version and cannot upload or delete`() {
    SecurityContextHolder.getContext().authentication =
        AnonymousAuthenticationToken(
            "key", "anonymousUser", listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")))
    version.status = VersionStatus.APPROVED
    assertEquals(version.id, service.getModVersion(version.id).id)
    assertThrows(AccessDeniedException::class.java) {
      service.createModVersion(modId, VersionCreateCommand("2.0", null, emptyList()))
    }
    assertThrows(AccessDeniedException::class.java) { service.deleteModVersion(modId, version.id) }
  }

  @Test
  fun `owner can delete but cannot moderate`() {
    login("owner")
    service.deleteModVersion(modId, version.id)
    verify(versions).delete(version.id)
    assertThrows(AccessDeniedException::class.java) {
      service.changeVersionStatus(UserId("owner"), modId, version.id, VersionStatus.REJECTED)
    }
  }

  @Test
  fun `admin can moderate only a version belonging to the path mod`() {
    login("admin", "ADMIN")
    `when`(versions.save(version)).thenReturn(version)
    assertEquals(
        VersionStatus.REJECTED,
        service
            .changeVersionStatus(UserId("admin"), modId, version.id, VersionStatus.REJECTED)
            .status)
    assertThrows(VersionNotFoundException::class.java) {
      service.deleteModVersion(ModId("other"), version.id)
    }
    assertThrows(IllegalArgumentException::class.java) {
      service.changeVersionStatus(UserId("admin"), modId, version.id, VersionStatus.APPROVED)
    }
  }

  @Test
  fun `guest list is restricted to approved versions and bounded pages`() {
    val bounded = PageRequest.of(0, 100)
    `when`(versions.findVersionsByModIdAndStatus(modId, VersionStatus.APPROVED, bounded))
        .thenReturn(PageImpl(emptyList()))
    assertTrue(service.getModVersions(null, modId, PageRequest.of(0, 500)).isEmpty)
    verify(versions).findVersionsByModIdAndStatus(modId, VersionStatus.APPROVED, bounded)
  }

  @Test
  fun `admin list includes pending and rejected versions`() {
    val bounded = PageRequest.of(0, 100)
    `when`(versions.findVersionsByModId(modId, bounded)).thenReturn(PageImpl(listOf(version)))

    login("admin", "ADMIN")

    assertEquals(
        listOf(version),
        service.getModVersions(UserId("admin"), modId, PageRequest.of(0, 500)).content)
    verify(versions).findVersionsByModId(modId, bounded)
  }
}
