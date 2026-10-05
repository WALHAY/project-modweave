package git.walhay.modweave.regression

import git.walhay.modweave.api.auth.http.AuthController
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.http.GameController
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.http.ModController
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.http.VersionController
import git.walhay.modweave.config.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    JwtAuthenticationFilter::class,
    JwtService::class,
    WebConfiguration::class)
@EnableConfigurationProperties(JwtProperties::class)
class ApiRoutingTest {
  @Autowired lateinit var mvc: MockMvc
  @MockitoBean lateinit var games: IGameService
  @MockitoBean lateinit var mods: IModService
  @MockitoBean lateinit var versions: IVersionService
  @MockitoBean lateinit var users: UserDetailsService

  @Test
  fun `versioned public endpoints allow guests and mutations require authentication`() {
    val modId = git.walhay.modweave.api.mod.ModId("example")
    val page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
    `when`(versions.getModVersions(null, modId, page))
        .thenReturn(Page(emptyList(), page = 0, size = 20, totalElements = 0, totalPages = 0))
    mvc.perform(get("/api/v1/mods/example/versions").param("page", "0").param("size", "20"))
        .andExpect(status().isOk)
    mvc.perform(delete("/api/v1/games/example")).andExpect(status().isUnauthorized)
    mvc.perform(delete("/api/v1/mods/example/versions/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isUnauthorized)
    mvc.perform(post("/api/v1/auth/refresh").param("refreshToken", "invalid"))
        .andExpect(status().isUnauthorized)
  }
}
