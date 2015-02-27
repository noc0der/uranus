# uranus
## preface
想了一下，代码吧，不能总是这么变来变去，还是给自己留个底稿吧，以后就维护这个了

##feature

- [] entity management(it.meta -> entity)
    - entity model + field model
    - relation model
    - entity data service
    - entity analysis service
    - relation service
- [v] exception strategy
- [v] pojo
	- vo -> jo
	- jo -> johelper
- [?] ioc方面还是用javax的那一票吧，不用spring那一坨了
	- AutoWired -> Inject
	- Component -> Named
- [v] 持久层
	- tablemeta -> entitymodel(fieldmodel)
	- pagesize + pagenum ->enum paging
	- checkUnique -> assertTrue
	- sql expression
		- condition:
		- place holder:
- [v] jpa 2.0和jsonobject结构还是要纠结一下，不能都用，也不能都不用
    - basedao就有点悲剧了哈
- accesstoken+csrf一并搞个拦截器
	- base on rbac，研究一下shiro@apache
- sso(base on delayedque)
- abtest
- 基于mysql的读写分离,主从互备
- [v] business service
	service ->srv(alias)
- [?] log service
- [v] rule service
	- rule model需要增加是单一条件还是组合条件(一个条件被匹配上之后，继续执行)
- [v] dataset service
	- 需要加上安全策略
	
- bill service
	- 这个得从长计议
- dynamic freemarker
- schedule service(base on quartz)
	- 优先级没那么高
- bpmn service
- [?] cache service
	- 提供cache管理的服务，用于主动失效等
- [v] lock service
- [v] code style
	service:srv/{app}/{model}/{named}

## ask
- [?] vo vs jsonobject(vo.vo这种情况好像出来的是linkedmap(从mongo中出来))
	- vo vs bsonobject?
	- 目前没有明确的证据表明bson有优势，可以进行测试，参考温高铁的benchmark
		
- [v] 为了mongo store搞vo值得么？

```
没意义，mongo只是一部分
```

- [?] field能用枚举么？
	枚举 vs 普通类 有啥优势么？
	- [?] 解决存储的问题
- [?] 针对复杂情况的mongo+jsonobject读取 
- [?] sql processor中需要啥？sql执行时长？
