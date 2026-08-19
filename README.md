# JsonSplitter - Java JSON Splitter Library

[![Maven Central](https://img.shields.io/maven-central/v/io.github.zaragozamartin91/json-splitter?gav=io.github.zaragozamartin91:json-splitter)](https://central.sonatype.com/artifact/io.github.zaragozamartin91/json-splitter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**JsonSplitter** is a lightweight Java library designed to split large JSON documents into smaller, manageable chunks while preserving JSON validity. Each resulting part can be independently processed, transmitted, or stored, making this library ideal for scenarios involving:

- Batch processing of large datasets
- Chunking JSON for APIs with payload size limits
- Parallel processing pipelines
- Memory-efficient handling of large JSON files
- Data partitioning for distributed systems

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [Usage Examples](#usage-examples)
  - [Split by Entry Count](#split-by-entry-count)
  - [Split Equally into N Parts](#split-equally-into-n-parts)
  - [Split by Size (Bytes)](#split-by-size-bytes)
  - [Flattening Nested JSON](#flattening-nested-json)
- [API Reference](#api-reference)
- [License](#license)

---

## Features

- **Multiple Split Strategies**: Split by entry count, equal distribution, or byte-size chunks.
- **Flattening Support**: Recursively flatten nested JSON structures before splitting to enable splitting on leaf nodes.
- **Preserved Structure**: Each output part remains a valid JSON object or array.
- **Java 8+ Compatible**: Lightweight and dependency-efficient.

---

## Installation

Add JsonSplitter as a dependency in your Maven project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.zaragozamartin91.splitter</groupId>
    <artifactId>json-splitter</artifactId>
    <version>{latestVersion}</version>
</dependency>
```

For Gradle projects, add to your `build.gradle`:

```groovy
implementation 'io.github.zaragozamartin91.splitter:json-splitter:{latestVersion}'
```

---

## Quick Start

Here's a minimal example demonstrating the basic usage:

```java
import io.github.zaragozamartin91.splitter.*;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Define configuration (e.g., split by entry count)
        JsonSplitterConfig config = JsonSplitterConfig.splitByEntryCount(2);
        
        // 2. Initialize the splitter with the config
        JsonSplitter splitter = new JsonSplitter(config);
        
        // 3. Create a source and apply the split
        JsonSource source = new JsonSource("{\"k1\":\"v1\", \"k2\":\"v2\", \"k3\":\"v3\"}");
        SplitJson result = splitter.apply(source);
        
        // 4. Process the resulting parts
        for (JsonPart part : result.getParts()) {
            System.out.println(part.jsonMap());
        }
    }
}
```

**Output:**
```json
{"k1":"v1","k2":"v2"}
{"k3":"v3"}
```

---

## Core Concepts

### JsonSource
Represents the input JSON data. It wraps the raw JSON string or source to be processed by the splitter.

### JsonSplitterConfig
A configuration object that defines how the splitting should occur. It is created via static factory methods (e.g., `splitByEntryCount`) and supports fluent modifiers like `.withFlatten()`.

### JsonSplitter
The main engine of the library. It is initialized with a `JsonSplitterConfig` and provides the `apply(JsonSource)` method to execute the splitting process.

### SplitJson
A container holding the result of a split operation. It provides access to the resulting parts as a list.

### JsonPart
Represents a single split segment of the original JSON. It provides access to the underlying data via:
- `jsonMap()`: Returns the part as a `Map<String, Object>` if the part is a JSON object.
- `jsonArray()`: Returns the part as a `List<Object>` if the part is a JSON array.

### JsonPartWriter
A utility class for serializing `JsonPart` objects back into their original JSON string or byte array representation.


---

## Usage Examples

### Split by Entry Count

Split JSON so that each part contains at most `n` key-value pairs:

```java
JsonSource source = new JsonSource("{\"k1\":\"v1\", \"k2\":\"v2\", \"k3\":\"v3\"}");
JsonSplitterConfig config = JsonSplitterConfig.splitByEntryCount(2);
JsonSplitter splitter = new JsonSplitter(config);

SplitJson result = splitter.apply(source);
List<JsonPart> parts = result.getParts();
// Part 0: 2 entries
// Part 1: 1 entry
```

### Split Equally into N Parts

Distribute JSON entries as evenly as possible across a specified number of parts:

```java
String json = "{\"k1\":\"v1\", \"k2\":\"v2\", \"k3\":\"v3\", \"k4\":\"v4\"}";
JsonSource source = new JsonSource(json);
JsonSplitterConfig config = JsonSplitterConfig.splitByNumberOfParts(2);
JsonSplitter splitter = new JsonSplitter(config);

SplitJson result = splitter.apply(source);
List<JsonPart> parts = result.getParts();
// Result: 2 parts with 2 entries each
```

### Split by Size (Bytes)

Ensure each part's size falls within a specific byte range (min to max):

```java
JsonSource source = new JsonSource("..."); // Large JSON data
JsonSplitterConfig config = JsonSplitterConfig.splitByChunkSize(128, 256);
JsonSplitter splitter = new JsonSplitter(config);

SplitJson result = splitter.apply(source);
for (JsonPart part : result.getParts()) {
    // Each part is within the 128-256 byte range
    System.out.println(part.jsonMap());
}
```

### Flattening Nested JSON

When working with deeply nested JSON, use `.withFlatten(true)` to split on leaf entries rather than top-level keys.

**Input JSON:**
```json
{
  "key1": "value1",
  "parentKey": {
    "nestedKey0": "nestedValue0",
    "nestedKey1": "nestedValue1"
  }
}
```

**Example:**
```java
JsonSource source = new JsonSource("..."); // Nested JSON
JsonSplitterConfig config = JsonSplitterConfig.splitByEntryCount(2)
                                             .withFlatten(true);
JsonSplitter splitter = new JsonSplitter(config);

SplitJson result = splitter.apply(source);
List<JsonPart> parts = result.getParts();
// Each part contains leaf entries (e.g., "parentKey.nestedKey0")
// The results are automatically expanded back to original nested structure
```

### Serializing Parts

Serialize a `JsonPart` back into a JSON string or byte array for storage or transmission:

```java
JsonPart part = ...; // a part obtained from SplitJson.getParts()
JsonPartWriter writer = new JsonPartWriter();

// 1. Serialize to string with a specific charset
String jsonString = writer.writeText(part, StandardCharsets.UTF_8);

// 2. Serialize to bytes
byte[] jsonBytes = writer.writeBytes(part);
```

---

## API Reference

### JsonSplitterConfig Factory Methods

| Method | Description |
| :--- | :--- |
| `splitByEntryCount(int n)` | Each part contains at most `n` entries. |
| `splitByNumberOfParts(int n)` | Splits the JSON into exactly `n` parts. |
| `splitByChunkSize(long min, long max)` | Splits into parts within the specified byte size range. |

### JsonSplitterConfig Modifiers

| Method | Description |
| :--- | :--- |
| `withFlatten(boolean enable)` | If true, recursively flattens nested structures before splitting. |

### JsonPart Data Access

| Method | Description |
| :--- | :--- |
| `jsonMap()` | Returns the part as a `Map<String, Object>` (for JSON objects). |
| `jsonArray()` | Returns the part as a `List<Object>` (for JSON arrays). |

### SplitJson Results

| Method | Description |
| :--- | :--- |
| `getParts()` | Returns the list of all resulting `JsonPart` segments. |

### JsonPartWriter Methods

| Method | Description |
| :--- | :--- |
| `writeText(JsonPart jsonPart, Charset charset)` | Serializes the JSON part to a string using the specified charset. |
| `writeBytes(JsonPart jsonPart)` | Serializes the JSON part to a byte array. |

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). See the LICENSE file for more details.

---

## Support

- **GitHub Repository**: https://github.com/zaragozamartin91/json-splitter
- **Issues**: https://github.com/zaragozamartin91/json-splitter/issues
- **Maven Central**: https://central.sonatype.com/artifact/io.github.zaragozamartin91.splitter/json-splitter
