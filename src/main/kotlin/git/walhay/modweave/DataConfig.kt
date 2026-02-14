package git.walhay.modweave

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class DataConfig {
    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()

        dataSource.username = "postgres"
        dataSource.password = "postgres"
        dataSource.schema = "modweave"
        dataSource.catalog = "production"
        dataSource.url = "jdbc:postgresql://localhost:5432/"
        dataSource.setDriverClassName("org.postgresql.Driver")

        return dataSource
    }
}