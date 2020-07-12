# gitbucket-csvtsv-plugin

[![Build Status](https://travis-ci.org/onukura/gitbucket-csvtsv-plugin.svg?branch=master)](https://travis-ci.org/onukura/gitbucket-csvtsv-plugin)

A GitBucket plugin for rendering csv file.  

## Screenshot

![screenshot](https://github.com/onukura/gitbucket-csvtsv-plugin/blob/assets/screenshot.png?raw=true)

## Install

1. Download *.jar from Releases.
2. Deploy it to `GITBUCKET_HOME/plugins`.
3. Restart GitBucket.

## Build from source

```sbt
sbt clean package
```

The built package is located at
`target/scala-2.13/gitbucket-csvtsv-plugin_2.13-{plugin-version}.jar`.

```sbt
sbt assembly
```

This makes the assembly package
`target/scala-2.13/gitbucket-csvtsv-plugin-{plugin-version}.jar`
for deployment.
