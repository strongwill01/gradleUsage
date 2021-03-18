### 版本号自增
每次发布时，版本号自增加。但这样其实不能满足业务场景，对于当前我所涉及的产品来讲，是自增`versionCode`，而`versionName`是根据业务需求人为指定的。

当发布时获取当前配置的版本号追加当前时间作为新的版本号名称，明确知道打包的时间，便于后期的使用和查找等。

* 1、新建version.properties文件
内容如下：
```
  #Wed Mar 17 16:37:16 CST 2021
  VERSION_NAME=1.0.0
  VERSION_CODE=2
  VERSION_TIME=20210317163716
```

* 2、获取版本号、版本名称、当前时间
```
def vCode = getVersionCode()
def vName = getVersionName() + getVersionTime()// "1.0.0"

...


/**
 * 获取当前时间
 *
 * @return
 */
def releaseTime() {
    return new Date().format("yyyyMMddHHmmss", TimeZone.getTimeZone("GMT+08:00"))
}

/**
 * 获取当前应用版本号名称(这个由于业务原因在文件`version.properties`手动修改即可)
 *
 * @return
 */
def getVersionName() {
    def versionFile = file('version.properties')
    if (!versionFile.canRead()) {
        throw new GradleException("Could not find `version.properties`!")
    }

    Properties versionProps = new Properties()
    versionProps.load(new FileInputStream(versionFile))
    logger.log(LogLevel.INFO, "VERSION_NAME=" + versionProps['VERSION_NAME'])
    return versionProps['VERSION_NAME']

}

/**
 * 获取当前应用版本号(release下自增，也手动修改即可)
 *
 * @return
 */
def getVersionCode() {
    def versionFile = file('version.properties')
    if (!versionFile.canRead()) {
        throw new GradleException("Could not find `version.properties`!")
    }

    Properties versionProps = new Properties()
    versionProps.load(new FileInputStream(versionFile))

    Integer versionCode = versionProps['VERSION_CODE'].toInteger()

    def runTasks = gradle.startParameter.taskNames
    if ('assembleRelease' in runTasks) {
        // 仅在assembleRelease任务时在版本号自增
        versionProps['VERSION_CODE'] = Integer.toString(++versionCode)
        versionProps.store(versionFile.newWriter(), null)
    }
    logger.log(LogLevel.INFO, "VERSION_CODE=" + versionCode)

    return versionCode

}

/**
 * 获得版本生成的时间(追加在版本号之后，只有在release下生效)
 *
 * @return
 */
def getVersionTime() {
    def versionFile = file('version.properties')
    if (!versionFile.canRead()) {
        throw new GradleException("Could not find `version.properties`!")
    }

    Properties versionProps = new Properties()
    versionProps.load(new FileInputStream(versionFile))
    def runTasks = gradle.startParameter.taskNames
    if ('assembleRelease' in runTasks) {
        // 仅在assembleRelease任务时在版本号后追加.时间
        def versionTime = releaseTime()
        versionProps.setProperty("VERSION_TIME", versionTime)
        versionProps.store(versionFile.newWriter(), null)

        logger.log(LogLevel.INFO, "VERSION_TIME?=" + versionProps['VERSION_TIME'])
    } else {
        return "";
    }

    return "." + versionProps['VERSION_TIME']
}
```

* 4、打包修改apk名称
```
buildType{
...

applicationVariants.all { variant ->
            variant.outputs.all { output ->
                def outputFile = output.outputFile
                def fileName
                if (outputFile != null && outputFile.name.endsWith('.apk')) {
                    logger.error('\r\n==================================')
                    logger.error('ENV:' + variant.productFlavors[0].name)
                    if (variant.buildType.name.contains('release')) {
                        fileName = "oo" + "_v${vName}_${releaseTime()}_${variant.productFlavors[0].name}release.apk"
                    } else if (variant.buildType.name.contains('debug')) {
                        fileName = "oo" + "_v${vName}_${releaseTime()}_${variant.productFlavors[0].name}debug.apk"
                    }

                    logger.error('pkg name:' + outputFile.parent + File.separator + fileName.toString())
                    outputFileName = new File(fileName)
                }
            }
        }
        
}
```

具体内容详见`build.gradle`