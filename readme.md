# LightMerge

![image-20200928185653623](https://tva1.sinaimg.cn/large/007S8ZIlgy1gj6kd3p88yj31jj0u0jwr.jpg)

## 目的

提高协同开发过程中分支合并部署效率及敏捷度，所谓敏捷度在LightMerge中主要体现在一下功能：

1. 可以轻松选择需要部署分支
2. 可以从已经部署分支中剔除不需要分支
3. 可见已经有哪些分支部署
4. 冲突指名分支

## 职责范围

LightMerge 不等于自动构建、不等于自动部署、他处于这些步骤的上游，能让我们自由控制对分支的部署，能基于开发分支为粒度自由组合部署。

![image-20200928172616053](https://tva1.sinaimg.cn/large/007S8ZIlgy1gj6hqsyq52j31c20qcdm9.jpg)

## 快速使用

### 前置条件

为了确保LightMerge能适用于没有Git仓库API权限的团队也能使用LM的功能，LM是基于用户角度SSH拉取合并推送代码的，故你需要确保：

- 部署的机器ssh公钥已经被添加到你的Git账号内
- 关联公钥的Git账号具备所要控制项目的推送权限

最好的验证就是你可以在该机器，通过ssh协议进行对项目的拉取推送。

### 配置

[配置文件](https://github.com/ZhaoYueNing/lightmerge/blob/master/config.json)存放在 `~/.lightmerge/config.json`

```json
# 为注释，真实配置请移除注视
{
	"safeConfig": {
        	# 由于采用ssh访问git仓库，这里填入你的私钥位置
		# 由于私钥头的区别，请使用 ssh-keygen -m PEM -t rsa -b 4096 命令来生成私钥并将填入此
		"privateKeyPosition": "~/.ssh/id_rsa"
	},
	"projectRepository": {
        	# 项目配置，每一个都是一个项目
		"projects": {
			"yourProjectName": {
				# 项目名称
				"projectName": "yourProjectName",
				# 项目的ssh地址
				"remoteAddress": "git@github.com:ZhaoYueNing/lightmerge.git",
				# 泳道，这里的泳道实际指一个远程目标分支
				"swimlaneMap": {
				    # qa 代表选中分支会被合并到原创 qa
				    "qa": {
					# 与key保持同名
					"swimlaneName": "qa",
					# 远程目标分支名称与Key保持同名
					"remotePushBranchName": "qa"
				    },
				    "rd": {
					"swimlaneName": "rd",
					"remotePushBranchName": "rd"
				    }
				}
			}
		}
	}
}
```

### 启动

- 下载 [lightmerge-x.jar](https://github.com/ZhaoYueNing/lightmerge/releases)

- `java -jar light-merge-*.jar --server.port=9000` 
- docker镜像 `buynow/lightmerge` 
- `docker run -p 8099:8099 -v {你本地.lightmerge 的配置目录}:/root/.lightmerge/ buynow/lightmerge  java -jar /app/light-merge.jar --server-port=8090`
## 开发
- 前端 @Jarvan
- 后端 @ZhaoYueNing



