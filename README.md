<h2 align="center" style="margin: 30px 0 30px; font-weight: bold;">spring-boot-starter-openapi-swagger2 V1.0.1</h2>

<h4 align="center">基于SpringBoot 3.x + springdoc-openapi 适配 swagger2.9.2 注解</h4>

### 简介
spring-boot-starter-openapi-swagger2 是一个基于 Spring Boot 3.x 框架的组件，旨在帮助开发者在新的 OpenAPI 标准下继续使用熟悉的 Swagger2 注解。随着 Swagger2 不再支持最新的 OpenAPI 开发规范，本组件提供了一种简便的方法，使现有的 Swagger2 注解能够在 OpenAPI 环境中正常工作，从而简化 API 文档的生成和维护。

### 主要特性
- **兼容性**：完全兼容 Spring Boot 3.x 及以上版本。
- **无缝迁移**：允许开发者在不改变现有 Swagger2 注解的情况下，轻松迁移到 OpenAPI 标准。
- **易于集成**：提供简单的配置选项，方便开发者快速集成到现有项目中。
- **安全性**：提供生产环境下的安全建议，帮助开发者避免常见的安全风险。

### 适配注解
|  swagger2   |  swagger3  |   是否适配   |
|  :-- | :--  |:--------:|
| @Api  | @Tag(name = “接口类描述”)| &#10003; |
| @ApiOperation  | @Operation(summary =“接口方法描述”)| &#10003; |
| @ApiImplicitParams  | @Parameters| &#10008; |
| @ApiImplicitParam  | @Parameter(description=“参数描述”)| &#10008; |
| @ApiParam  | @Parameter(description=“参数描述”)| &#10003; |
| @ApiIgnore  | @Parameter(hidden = true) <br/>或 @Operation(hidden = true) <br/>或 @Hidden| &#10003; |
| @ApiModel  | @Schema| &#10003; |
| @ApiModelProperty  | @Schema| &#10003; |


### 使用教程
- 添加依赖
```xml
<dependency>
    <groupId>io.github.bentomai</groupId>
    <artifactId>spring-boot-starter-openapi-swagger2</artifactId>
    <version>1.0.1</version>
</dependency>
```

- 新建SpringDocAutoConfiguration 配置类
```java

@ConditionalOnProperty(prefix = "springdoc.api-docs",name = "enabled", havingValue = "true")
public class SpringDocAutoConfiguration {

    @OpenAPIDefinition(
            servers = {
                    @Server(description = "开发环境服务器", url = "http://localhost:8080"),
            },
            externalDocs = @ExternalDocumentation(
                    description = "项目编译部署说明",
                    url = "http://localhost:8080/deploy/readme.md"
            )
    )
    @Configuration
    public static class SpringDocConfig {
        @Bean
        public OpenAPI openAPI() {
            return new OpenAPI()
                    // 配置接口文档基本信息
                    .info(this.getApiInfo());
        }
        private Info getApiInfo() {
            return new Info()
                    // 配置文档标题
                    .title("SpringBoot3集成Swagger3适配Swagger2注解")
                    // 配置文档描述
                    .description("SpringBoot3集成Swagger3适配Swagger2注解示例文档")
                    // 配置作者信息
                    .contact(new Contact().name("Bento Mai").url("https://www.xxxxx.cn").email("641298213@qq.com"))
                    // 配置License许可证信息
                    .license(new License().name("Apache 2.0").url("https://gitee.com/bento_mai/spring-boot-starter-openapi-swagger2/blob/master/LICENSE"))
                    .summary("SpringBoot3集成Swagger3适配Swagger2注解示例文档")
                    .termsOfService("https://www.xxxxx.cn")
                    // 配置版本号
                    .version("2.0");
        }
    }

}
```

- 添加 springdoc yml配置
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    tags-sorter: alpha
  group-configs:
    - group: 'test'
      display-name: '系统测试模块'
      paths-to-match: '/test/**'
      packages-to-scan: io.github.bentomai
```

### 在线体验
- 在线文档
- <a href="https://github.com/bentomai/spring-boot-starter-openapi-swagger2-example">示例项目</a>

### 使用须知
- **适用范围**：本组件适用于 Spring Boot 3.x 及以上版本的应用程序，旨在简化 OpenAPI 文档的生成与维护工作。
- **安全性**：尽管本组件已经过测试，但我们强烈建议在生产环境中谨慎使用，并采取适当的安全措施（例如，限制对 API 文档的访问）。
- **技术支持**：本组件按“原样”提供，没有明示或暗示的任何形式的保证。我们不对本组件的可用性、及时性、准确性或完整性作出任何承诺。对于因使用本组件而产生的任何直接或间接损失，我们不承担责任。
- **更新与维护**：我们将尽力保持本组件的稳定性和兼容性，但保留随时修改、暂停或终止本组件的权利，恕不另行通知。
- **第三方服务**：如果本组件中包含第三方软件或服务，这些第三方软件或服务的使用受各自独立的许可协议约束。

### 免责声明
本软件（以下简称“本组件”）是基于 Spring Boot 3.x 框架，旨在为开发者提供一个便捷的方式将 Swagger2 注解与 OpenAPI 标准相兼容的组件。本组件遵循 Apache License 2.0 开源协议发布。**对于因不当使用本组件而导致的数据泄露或其他安全问题，我们不承担任何责任**。

### 贡献与反馈
欢迎任何贡献和反馈！如果您发现任何问题或有改进建议，请在 [GitHub 仓库](https://gitee.com/bento_mai/spring-boot-starter-openapi-swagger2) 提交 Issue 或 Pull Request。

### 许可证
本项目遵循 Apache License 2.0 许可证。详情请参阅 [LICENSE](LICENSE) 文件。