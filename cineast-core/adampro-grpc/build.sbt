//protobuf
import sbtassembly.AssemblyPlugin.autoImport._

name := "ADAMpro-grpc"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

resolvers += DefaultMavenRepository


libraryDependencies ++= Seq(
  "io.grpc" % "grpc-protobuf" % scalapb.compiler.Version.grpcJavaVersion,
  "io.grpc" % "grpc-stub" % scalapb.compiler.Version.grpcJavaVersion,
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "io.netty" % "netty-all" % "4.1.31.Final",
  "com.google.protobuf" % "protobuf-java" % "3.6.1",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
).map(
  _.excludeAll(
    ExclusionRule("org.scala-lang"),
    ExclusionRule("org.slf4j"),
    ExclusionRule("log4j")
  )
)

//assembly
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("io.netty.**" -> "adampro.grpc.shaded.io.netty.@1").inAll,
  ShadeRule.rename("com.fasterxml.**" -> "adampro.grpc.shaded.com.fasterxml.@1").inAll,
  ShadeRule.rename("org.apache.**" -> "adampro.grpc.shaded.org.apache.@1").inAll
)

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}