import functions.proxygenerator.*

val TargetRoot = s"$ProjectRoot/tests-http4s-client-impl/src/main/generated"
deleteScalaFiles(TargetRoot)

generateCaller(generatorConfig,avroSerialization = true, jsonSerialization = true, http4sClientTransport = true)
    .generate(TargetRoot, ExportsCatsDep)
