package git.walhay.modweave.regression

import git.walhay.modweave.api.category.CategoryController
import git.walhay.modweave.api.comment.CommentController
import git.walhay.modweave.api.common.apiLink
import java.net.URI
import java.net.URLDecoder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class ApiLinksTest {
  @Test
  fun `controller links include context path and encode path and query values`() {
    val request = MockHttpServletRequest("GET", "/gateway/api/v2/categories")
    request.contextPath = "/gateway"
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    try {
      val category = "Моды + утилиты"
      val self = apiLink(CategoryController::getCategory, category)
      val patch = apiLink(CategoryController::patchCategory, category)
      assertEquals("/gateway/api/v2/categories/$category", URI.create(self.href).path)
      assertEquals(self.href, patch.href)
      assertEquals("GET", self.method)
      assertEquals("PATCH", patch.method)
      assertTrue(self.href.contains("%2B"))

      val comments = apiLink(CommentController::getComments, "example + мод", 0, 20)
      val uri = URI.create(comments.href)
      assertEquals("/gateway/api/v2/comments", uri.path)
      val query =
          uri.rawQuery.split('&').associate {
            val parts = it.split('=', limit = 2)
            parts[0] to URLDecoder.decode(parts[1], Charsets.UTF_8)
          }
      assertEquals(mapOf("modId" to "example + мод", "page" to "0", "size" to "20"), query)
      assertFalse(comments.href.contains('{'))
    } finally {
      RequestContextHolder.resetRequestAttributes()
    }
  }
}
