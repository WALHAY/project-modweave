package git.walhay.modweave.regression

import git.walhay.modweave.api.auth.AuthController
import git.walhay.modweave.api.common.ApiResourceAssembler
import git.walhay.modweave.api.config.*
import git.walhay.modweave.api.game.GameController
import git.walhay.modweave.api.mod.ModController
import git.walhay.modweave.api.security.*
import git.walhay.modweave.api.version.VersionController
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.common.paging.SortDirection
import git.walhay.modweave.domain.common.paging.SortOrder
import git.walhay.modweave.domain.game.GameService
import git.walhay.modweave.domain.mod.ModService
import git.walhay.modweave.domain.version.VersionService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(
    controllers =
        [
            GameController::class,
            ModController::class,
            VersionController::class,
            AuthController::class],
    properties =
        ["security.jwt.secret=ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA=="])
@Import(
    SecurityConfiguration::class,
    BearerProblemHandler::class,
    JwtConfiguration::class,
    CurrentUserJwtConverter::class,
    JwtService::class,
    WebConfiguration::class,
    ApiResourceAssembler::class)
@EnableConfigurationProperties(JwtProperties::class)
class ApiRoutingTest {
  @Autowired lateinit var mvc: MockMvc
  @MockitoBean lateinit var games: GameService
  @MockitoBean lateinit var mods: ModService
  @MockitoBean lateinit var versions: VersionService
  @MockitoBean lateinit var users: UserDetailsService

  @Test
  fun `versioned public endpoints allow guests and mutations require authentication`() {
    val modId = git.walhay.modweave.domain.mod.ModId("example")
    val page = PageRequest(0, 20, listOf(SortOrder("id", SortDirection.DESC)))
    `when`(versions.getModVersions(null, modId, page))
        .thenReturn(Page(emptyList(), page = 0, size = 20, totalElements = 0, totalPages = 0))
    mvc.perform(get("/api/v1/mods/example/versions").param("page", "0").param("size", "20"))
        .andExpect(status().isOk)
    mvc.perform(delete("/api/v1/games/example")).andExpect(status().isUnauthorized)
    mvc.perform(delete("/api/v1/mods/example/versions/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isUnauthorized)
    mvc.perform(
            post("/api/v1/auth/refresh")
                .contentType("application/x-www-form-urlencoded")
                .param("refreshToken", "invalid"))
        .andExpect(status().isUnauthorized)
  }

  @Test
  fun `HTTP pagination passes domain orders with a stable identity tie breaker`() {
    val modId = git.walhay.modweave.domain.mod.ModId("example")
    val page =
        PageRequest(
            page = 2,
            size = 5,
            sort =
                listOf(
                    SortOrder("name", SortDirection.DESC, ignoreCase = true),
                    SortOrder("uploadDate"),
                    SortOrder("id")))
    `when`(versions.getModVersions(null, modId, page))
        .thenReturn(Page(emptyList(), page = 2, size = 5, totalElements = 0, totalPages = 0))

    mvc.perform(
            get("/api/v2/mods/example/versions")
                .param("page", "2")
                .param("size", "5")
                .param("sort", "name,desc,ignorecase", "uploadDate,asc"))
        .andExpect(status().isOk)

    verify(versions).getModVersions(null, modId, page)
  }
}
