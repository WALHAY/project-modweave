package git.walhay.modweave

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class DataConfig {
  @Bean
  fun dataSource() =
      DriverManagerDataSource().apply {
        username = "postgres"
        password = "postgres"
        schema = "modweave"
        catalog = "production"
        url = "jdbc:postgresql://localhost:5432/"
        setDriverClassName("org.postgresql.Driver")
      }
}
