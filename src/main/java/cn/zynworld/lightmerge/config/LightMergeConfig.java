package cn.zynworld.lightmerge.config;

import cn.zynworld.lightmerge.common.Constants;
import cn.zynworld.lightmerge.common.FileUtils;
import cn.zynworld.lightmerge.common.JsonUtils;
import cn.zynworld.lightmerge.domain.GitProject;
import cn.zynworld.lightmerge.domain.Swimlane;
import cn.zynworld.lightmerge.domain.repository.GitProjectRepository;
import lombok.Data;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhaoyuening
 * @date 2019/8/3.
 */
@Data
public class LightMergeConfig implements Serializable {
	private static final long serialVersionUID = 7685144889767962346L;

	// 默认私钥位置
	private SafeConfig safeConfig;

	private GitProjectRepository projectRepository;

	public static LightMergeConfig buildByConfig() {
		String configJson = FileUtils.readFileContent(Constants.PROJECT_CONFIG_FILE);
		return JsonUtils.jsonToPojo(configJson, LightMergeConfig.class);
	}

	public static void test() {
		LightMergeConfig lightMergeConfig = new LightMergeConfig();

		GitProjectRepository projectRepository = new GitProjectRepository();
		GitProject projectNameProject = new GitProject(new LightMergeConfig().getSafeConfig());
		projectNameProject.setProjectName("projectName");
		projectNameProject.setRemoteAddress("");

		// 配置泳道
		Swimlane projectNameRdSwimlane = new Swimlane();
		projectNameRdSwimlane.setSwimlaneName("rd");
		projectNameRdSwimlane.setRemotePushBranchName("rd");
		projectNameProject.addSwimlane(projectNameRdSwimlane);

		Swimlane projectNameQaSwimlane = new Swimlane();
		projectNameQaSwimlane.setSwimlaneName("qa");
		projectNameQaSwimlane.setRemotePushBranchName("qa");
		projectNameProject.addSwimlane(projectNameQaSwimlane);

		projectRepository.addProject(projectNameProject);
		lightMergeConfig.setSafeConfig(new SafeConfig());
		lightMergeConfig.setProjectRepository(projectRepository);
		System.out.println(JsonUtils.objectToJson(lightMergeConfig));
	}

	public static void main(String[] args) throws IOException {
		LightMergeConfig lightMergeConfig = LightMergeConfig.buildByConfig();

		System.out.println(lightMergeConfig);
	}
}
