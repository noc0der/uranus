# common module
## feature list
- service
	- op log service
		- [x] 需要修改一下，不能用write-concern="FSYNC_SAFE"，性能太低
	- lock service
	- abstract business service
- entity model
	- field model
- entity wrapper helper
	- 提供bean的反射功能(base on asm)