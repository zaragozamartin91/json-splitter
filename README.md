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
  - [Pre-Sorting Keys Before Split](#pre-sorting-keys-before-split)
  - [Combining Options](#combining-options)
- [API Reference](#api-reference)
- [License](#license)

---

## Features

- **Multiple Split Strategies**: Split by entry count, equal distribution, or byte size
- **Flattening Support**: Automatically flatten nested JSON structures before splitting
- **Key Pre-Sorting**: Sort keys alphabetically before applying split logic
- **Preserved Structure**: Each output part is a valid JSON object
- **Unflatten Capability**: Reconstruct original nested structure from flattened parts
- **Java 8+ Compatible**: Works with Java 11 and newer versions

---

## Installation

Add JsonSplitter as a dependency in your Maven project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.zaragozamartin91.splitter</groupId>
    <artifactId>json-splitter</artifactId>
    <version>3.0</version>
</dependency>
```

For Gradle projects, add to your `build.gradle`:

```groovy
implementation 'io.github.zaragozamartin91.splitter:json-splitter:3.0'
```

---

## Quick Start

Here's a minimal example demonstrating the basic usage:

```java
import io.github.zaragozamartin91.splitter.*;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Create a JsonSource from a JSON string
        JsonSource source = JsonSource.fromString(
            "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}"
        );

        // 2. Define a split strategy (split by entry count)
        SplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2);

        // 3. Split the JSON
        JsonSplitter splitter = new JsonSplitter();
        SplitJson result = splitter.split(source, strategy);

        // 4. Access the parts
        for (FlatJson part : result.getParts()) {
            System.out.println(part.unflattenAsString());
        }
    }
}
```

**Output:**
```json
{"key1":"value1","key2":"value2"}
{"key3":"value3"}
```

---

## Core Concepts

### JsonSource
Represents the input JSON data. Can be created from:
- JSON strings
- JSON files (file paths)
- Java Maps

### SplitStrategy
Defines how the JSON should be split. Use `DynamicSplitStrategy` for built-in strategies:
- `byEntryCount(n)` - Each part contains at most `n` entries
- `equally(n)` - Split into exactly `n` parts
- `bySize(context)` - Each part is at most `n` bytes

### SplitJson
Container holding all split parts. Access parts via:
- `getPart(int index)` - Get a specific part by index
- `getParts()` - Get all parts as a List

### FlatJson
Represents a single split part with flattened keys. Provides:
- `unflattenAsMap()` - Reconstruct nested structure as a Java Map
- `unflattenAsString()` - Reconstruct nested structure as a JSON string
- `getKeySet()` - Get all keys in this part

---

## Usage Examples

### Split by Entry Count

Split JSON so each part contains at most `n` key-value pairs:

```java
JsonSource source = JsonSource.fromString(
    "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}"
);

SplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2);

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);

List<FlatJson> parts = splitJson.getParts();
// Part 0: {"key1": "value1", "key2": "value2"}
// Part 1: {"key3": "value3"}
```

**Verification:**
```java
FlatJson part0 = parts.get(0);
FlatJson part1 = parts.get(1);

assertEquals(2, part0.getKeySet().size());
assertEquals(1, part1.getKeySet().size());
```

---

### Split Equally into N Parts

Distribute JSON entries evenly across a specified number of parts:

```java
String largeJson = "{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5,\"f\":6}";
JsonSource source = JsonSource.fromString(largeJson);

// Split into exactly 2 parts (3 entries each)
SplitStrategy strategy = DynamicSplitStrategy.equally(2);

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);

List<FlatJson> parts = splitJson.getParts();
// Part 0: 3 entries
// Part 1: 3 entries
```

---

### Split by Size (Bytes)

Ensure each part does not exceed a specific byte size:

```java
String jsonData = TestUtil.utf8FileText("/sample-data.json");
JsonSource source = JsonSource.fromString(jsonData);

// Each part will be at most 128 bytes
int sizeLimit = 128;
GroupBySizeContext context = new GroupBySizeContext(sizeLimit);
SplitStrategy strategy = DynamicSplitStrategy.bySize(context);

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);

// Verify all parts are under the size limit
for (FlatJson part : splitJson.getParts()) {
    String json = part.unflattenAsString();
    assert json.getBytes().length <= sizeLimit;
}
```

---

### Flattening Nested JSON

When working with deeply nested JSON, use `.flatten()` to split on leaf entries:

**Input JSON:**
```json
{
  "key1": "value1",
  "key2": "value2",
  "parentKey": {
    "nestedKey0": "nestedValue0",
    "nestedKey1": "nestedValue1",
    "nestedKey2": "nestedValue2"
  }
}
```

**Flattened representation:**
```json
{
  "key1": "value1",
  "key2": "value2",
  "parentKey.nestedKey0": "nestedValue0",
  "parentKey.nestedKey1": "nestedValue1",
  "parentKey.nestedKey2": "nestedValue2"
}
```

**Example:**
```java
JsonSource source = JsonSource.fromString(
    "{\"key1\": \"value1\", \"key2\": \"value2\", \"parentKey\": {" +
    "\"nestedKey0\": \"nestedValue0\", \"nestedKey1\": \"nestedValue1\", " +
    "\"nestedKey2\": \"nestedValue2\"}}"
);

// Flatten before splitting by entry count
SplitStrategy strategy = DynamicSplitStrategy
    .byEntryCount(3)
    .flatten();

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);

List<FlatJson> parts = splitJson.getParts();
// Part 0: 3 entries (key1, key2, parentKey.nestedKey0)
// Part 1: 2 entries (parentKey.nestedKey1, parentKey.nestedKey2)
```

**Rehydrating the nested structure:**
```java
FlatJson part0 = parts.get(0);
Map<String, Object> restored = part0.unflattenAsMap();
// restored = {
//   "key1": "value1",
//   "key2": "value2",
//   "parentKey": {"nestedKey0": "nestedValue0"}
// }
```

---

### Pre-Sorting Keys Before Split

Sort keys alphabetically before applying the split strategy:

```java
JsonSource source = JsonSource.fromString(
    "{\"zebra\": 1, \"apple\": 2, \"mango\": 3}"
);

// Sort keys in ascending order before splitting
SplitStrategy strategy = DynamicSplitStrategy
    .byEntryCount(2)
    .preSortByKey(SortFunction.SortOrder.ASCENDING);

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);

// Part 0: {"apple": 2, "mango": 3}
// Part 1: {"zebra": 1}
```

---

### Combining Options

Chain multiple modifiers for complex splitting behavior:

```java
JsonSource source = JsonSource.fromString(largeNestedJson);

// Flatten -> Sort keys -> Split equally into 2 parts
SplitStrategy strategy = DynamicSplitStrategy
    .equally(2)
    .flatten()
    .preSortByKey(SortFunction.SortOrder.ASCENDING);

JsonSplitter splitter = new JsonSplitter();
SplitJson splitJson = splitter.split(source, strategy);
```

---

## API Reference

### DynamicSplitStrategy Factory Methods

| Method | Description |
|--------|-------------|
| `byEntryCount(int n)` | Each part contains at most `n` entries |
| `equally(int n)` | Split into exactly `n` parts |
| `bySize(GroupBySizeContext ctx)` | Each part is at most `n` bytes |

### DynamicSplitStrategy Modifiers

| Method | Description |
|--------|-------------|
| `flatten()` | Flatten nested JSON before splitting |
| `preSortByKey(SortOrder order)` | Sort keys before splitting (ASCENDING/DESCENDING) |

### FlatJson Output Methods

| Method | Description |
|--------|-------------|
| `unflattenAsMap()` | Reconstruct nested JSON as `Map<String, Object>` |
| `unflattenAsString()` | Reconstruct nested JSON as JSON string |
| `getKeySet()` | Get all keys in this part |

### SplitJson Accessor Methods

| Method | Description |
|--------|-------------|
| `getPart(int index)` | Get part at specified index |
| `getParts()` | Get all parts as `List<FlatJson>` |

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). See the LICENSE file for details.

---

## Support

- **GitHub Repository**: https://github.com/zaragozamartin91/json-splitter
- **Issues**: https://github.com/zaragozamartin91/json-splitter/issues
- **Maven Central**: https://central.sonatype.com/artifact/io.github.zaragozamartin91.splitter/json-splitter
