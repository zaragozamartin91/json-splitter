# JsonSplitter - Java JSON Splitter Library


JsonSplitter is a Java library that provides functionality to split JSON data into multiple parts based on a specified strategy. Each part remains a valid json object. The library is designed to be flexible and easy to use, allowing developers to split JSON data in a variety of ways.

## Getting Started

To use JsonSplitter in your project, you need to add the library as a dependency in your build configuration. The library is available on Maven Central, so you can add the following dependency to your pom.xml file:

```xml
<dependency>
    <groupId>io.github.zaragozamartin91.splitter</groupId>
    <artifactId>json-splitter</artifactId>
    <version>{latestVersion}</version>
</dependency>
```

## Usage

To split JSON data using JsonSplitter, you need to follow these steps:

1. Create a JsonSource object from your JSON data. You can create a JsonSource object from a JSON string, a JSON map, or a file path.
2. Create a SplitStrategy object that defines how the JSON data should be split. You can create a SplitStrategy object using the DynamicSplitStrategy class, which provides a variety of built-in splitting strategies.
3. Call the split method on the JsonSplitter object, passing in the JsonSource object and the SplitStrategy object. This method will return a SplitJson object, which contains the split JSON data.

Here's an example using the DynamicSplitStrategy class to split JSON data by entry count:

```java
// Create a JsonSource object from a JSON string
JsonSource source = JsonSource.fromString(
    "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}"
);

// Create a DynamicSplitStrategy object to split the JSON data by entry count
DynamicSplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2);

// Create a JsonSplitter object and split the JSON data
JsonSplitter splitter = new JsonSplitter(source);
SplitJson splitJson = splitter.split(strategy);

// Get the split JSON data
List<FlatJson> flatJsons = splitJson.getParts();
```


In this example, the JSON data is split into parts each one containing 2 or less keys from the original JSON data.


This will hold true:
```java
FlatJson part0 = flatJsons.get(0);
FlatJson part1 = flatJsons.get(1);

assertEquals(2, part0.getKeySet().size());
assertEquals(1, part1.getKeySet().size());
```

The split parts are "flat jsons". They can be "rehydrated" via any of its utility functions:

- unflattenAsMap()
- unflattenAsString() 

```java
HashMap<String, Object> expectedMap0 = new HashMap<String, Object>() {{
    put("key1", "value1");
    put("key2", "value2");
}};

HashMap<String, Object> expectedMap1 = new HashMap<String, Object>() {{
    put("key3", "value3");
}};

assertEquals(expectedMap0, part0.unflattenAsMap());
assertEquals(expectedMap1, part1.unflattenAsMap());
```

You can also customize the splitting strategy by chaining methods on the  DynamicSplitStrategy object. For example, you can specify a pre-sort function to sort the JSON entries before splitting:

```java
// Create a DynamicSplitStrategy object to split the JSON data by entry count and pre-sort the entries by key
DynamicSplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2)
                                                    .preSortByKey(SortOrder.ASCENDING);
```

