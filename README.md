# adblock-coffee
[![Create release](https://github.com/breadrock1/Adblock-coffee/actions/workflows/release.yml/badge.svg)](https://github.com/breadrock1/Adblock-coffee/actions/workflows/release.yml)
[![Pipelines](https://img.shields.io/github/actions/workflow/status/breadrock1/Adblock-coffee/build.yml)](https://img.shields.io/github/actions/workflow/status/breadrock1/Adblock-coffee/build.yml)
[![Last release: ](https://img.shields.io/github/v/release/breadrock1/Adblock-coffee)](https://img.shields.io/github/v/release/breadrock1/Adblock-coffee)

This project is a simplest Java wrapper for the `adblock-rust` library, allowing you to use the powerful ad-blocking capabilities of `adblock-rust` in your Java applications.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)

## Introduction

`adblock-rust` is a high-performance ad-blocking library written in Rust. This project provides a Java wrapper around `adblock-rust`, enabling Java developers to integrate ad-blocking functionality into their applications seamlessly.

## Features

- High-performance ad-blocking using `adblock-rust`.
- Easy-to-use Java API.
- Cross-platform support.

## Installation

### Prerequisites

- Java Development Kit (JDK) 8 or higher.
- Rust and Cargo (for building the native library).

### Building the Native Library

First, you need to build the `adblock-rust` library and generate the shared library file (`.so`, `.dll`, or `.dylib` depending on your OS).

1. Clone the `adblock-rust` repository:
2. Build the library: 
```shell 
cargo build --release --manifest-path adblock-rs/Cargo.toml
mvn install
```

To build it for android platforms just use ndk (see installation on official site):
```shell
 cargo ndk -t aarch64-linux-android -o ./target build --release
 mv arm64-v8 release 
```

Go back to project root and enter:
```shell
mvn install
```

If you want to build library to another platform add option `--target` with needed platform like `aarch64-unknown-linux-gnu`, .
After that check that path of built library exists into `pom.xml` `maven-resources-plugin` section. As default `rustup` using current platform and creates `target/{debug,release}` directories.
After switching target platform by `rustup` these directories created as `targer/{target-platform}/{debug,release}` directories.

Example building library for `aarch64-unknown-linux-gnu` target: 
```shell
cargo build --release --target aarch64-unknown-linux-gnu --manifest-path adblock-rs/Cargo.toml
```

And you have to update `pom.xml` file:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-resources-plugin</artifactId>
  <version>3.2.0</version>
  <executions>
    <execution>
      <id>copy-native-libs</id>
      <phase>process-resources</phase>
      <goals>
        <goal>copy-resources</goal>
      </goals>
      <configuration>
        <outputDirectory>${project.basedir}/target/lib</outputDirectory>
        <resources>
          <resource>
            <directory>${project.basedir}/adblock-rs/target/aarch64-linux-android/debug/</directory>
            <includes>
              <include>**/*.dylib</include>
              <include>**/*.so</include>
              <include>**/*.dll</include>
            </includes>
          </resource>
          <resource>
            <directory>${project.basedir}/adblock-rs/target/aarch64-linux-android/release/</directory>
            <includes>
              <include>**/*.dylib</include>
              <include>**/*.so</include>
              <include>**/*.dll</include>
            </includes>
          </resource>
        </resources>
      </configuration>
    </execution>
  </executions>
</plugin>
```

And build jar:
```shell
mvn install
```

3. Locate the generated shared library file in the `target/release` directory.
```java
public class Main {    
    public static void main(String[] args) {
        List<String> rules = new ArrayList<>(List.of(
            "-advertisement-icon.",
            "-advertisement-management/",
            "-advertisement.",
            "-advertisement/script."
        ));
        
        AdvtBlocker blocker = AdvtBlocker.createInstance(rules);
    }
}
```

### Integrating with Your Java Project

1. Copy the shared library file to a directory accessible by your Java application.

2. Add the Java wrapper library to your project. You can do this by including the JAR file or adding the source code directly to your project.

## Usage

### Loading the Native Library

Before using the wrapper, you need to load the native library. This can be done using `System.loadLibrary` or `System.load`.

### Using the Wrapper

Here is an example of how to use the Java wrapper to block ads:

```java
import com.example.adblock.AdblockEngine;

public class Main {
    public static void main(String[] args) {
        List<String> rules = new ArrayList<>(List.of(
            "-advertisement-icon.",
            "-advertisement-management/",
            "-advertisement.",
            "-advertisement/script."
        ));
        
        AdvtBlocker blocker = AdvtBlocker.createInstance(rules);
        boolean result = blocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
        
        System.out.println(result);
    }
}
```
