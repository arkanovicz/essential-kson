# Essential-kson

## Rationale

Essential-kson is a JSON library which:

- is *minimalistic*: only one source file
- doesn't use any reflection, validation, nor schema, or custom POJO field/class support
- is fully multiplatform
- doesn't wrap raw values (only object and array containers inherit the Json class)
- distinguish between mutable and immutable Json containers
- exposes Json container obects as extensible open classes
- provides a nice API with specialized getters and setters

The current multiplatform targets are jvm, js(IR) and linux/mingwx64 native. Other targets could easily be added.

## Usage

The `com.republicate.kson.Json` interface is implemented by its two inner classes `Json.Array` and `Json.Object`, which are in turn inherited with `Json.MutableArray` and`Json.MutableObject`.

`Json.Array` extends `List<Any?>` and `Json.Object` extends `Map<String, Any?>`.

### Inclusion in your project

Using Maven:

    <dependency>
        <groupId>com.republicate</groupId>
        <artifactId>essential-kson</artifactId>
        <version>2.1</version>
    </dependency>

Using Gradle:

    implementation 'com.republicate:essential-kson:2.1'

### Parsing JSON

The generic `Json.parse(string_or_stream)` method will return a `com.republicate.Json` value containing a `Json.Object` or `Json.Array` object (or throw a JsonException if parsing failed).

If you want to parse a content without knowing if it's a JSON container or a simple JSON value,
you will call the `Json.parseValue(string_or_stream)` method to get an `Any?`. 

    import com.republicate.kson.Json;
    ...
    val container = Json.parse(string_or_stream);
    // container will be a JSON object or a JSON array
    if (container.isObject())
    {
        Json.Object obj = container.asObject();
        ...
    }

    val value = Json.parseValue(string_or_reader);
    // value will either be a JSON container or a single Serializable value (or null)

### Rendering JSON

Containers `toString()` and `toString(stream)` methods will render JSON strings with proper quoting and encoding.

    import com.republicate.kson.Json;
    ...
    // getting a String
    val json = container.toString();

    // rendering towards a stream
    container.toString(stream);

### Converting to JSON

The reentrant method `Json.toJson(Any)` will try hard to convert any standard container to a JSON structure.

## Ktor content negociation integration

Use the following code to use essential-kson with ktor content negotiation:

```kotlin

object KsonConverter: ContentConverter {
    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val input = object: Json.Input {
            val reader = content.toInputStream().reader(charset)
            override fun read() = reader.read().toChar()
        }
        return Json.parseValue(input)
    }
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any
    ): OutgoingContent? = TextContent(value.toString(), contentType.withCharsetIfNeeded(charset))
}

// and in ktor configuration section:

    install(ContentNegotiation) {
        register(ContentType.Application.Json, KsonConverter)
    }


```

## References

+ [RFC 7159](https://tools.ietf.org/html/rfc7159)
+ [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite)
