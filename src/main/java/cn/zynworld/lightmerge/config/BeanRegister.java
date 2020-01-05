package cn.zynworld.lightmerge.config;

import cn.zynworld.lightmerge.domain.GitProject;
import cn.zynworld.lightmerge.domain.Swimlane;
import cn.zynworld.lightmerge.domain.repository.GitProjectRepository;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author zhaoyuening
 * @date 19-8-3 下午5:07
 */
@Configuration
public class BeanRegister {

    @Bean
    public LightMergeConfig getConfig() {
        return LightMergeConfig.buildByConfig();
    }

    @Bean
    public GitProjectRepository projectRepository() {
        GitProjectRepository projectRepository = getConfig().getProjectRepository();
        projectRepository.getProjects().forEach(project -> project.setSafeConfig(getConfig().getSafeConfig()));
        return projectRepository;
    }


    // 配置跨域
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1允许任何域名使用
        corsConfiguration.addAllowedHeader("*"); // 2允许任何头
        corsConfiguration.addAllowedMethod("*"); // 3允许任何方法（post、get等）
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

}
