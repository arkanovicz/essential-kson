# Essential-kson

## Rationale

Essential-kson is a JSON library which:

- is fully multiplatform
- is robust and performant
- is *minimalistic*: only one source file
- doesn't use any reflection, validation, nor schema, or custom POJO field/class support
- doesn't wrap raw values (only object and array containers inherit the Json class)
- distinguishes between mutable and immutable Json containers
- exposes Json container objects as extensible open classes
- provides a nice API with specialized getters and setters

Here is the list of currently supported targets:

- Jvm
- Js
- Desktop native targets: linuxX64, linuxArm64, mingwX64
- Android native targets: androidNativeX64, androidNativeX86, androidNativeArm32, androidNativeArm64
- WasmJs

Several iOS and macOS platforms are targeted as well, but they would need some help from a contributor proficient with Apple native kotlin code packaging (iosX64, iosArm64, iosSimulatorArm64, macosX64, macosArm64, tvosArm64, tvosSimulatorArm64, tvosX64, watchosArm64, watchosX64, watchosSimulatorArm64).

## Usage

The `com.republicate.kson.Json` interface is implemented by its two inner classes `Json.Array` and `Json.Object`, which are in turn inherited with `Json.MutableArray` and`Json.MutableObject`.

`Json.Array` extends `List<Any?>` and `Json.Object` extends `Map<String, Any?>`.

### Inclusion in your project

Using Maven:

    <dependency>
        <groupId>com.republicate.kson</groupId>
        <artifactId>essential-kson</artifactId>
        <version>2.5</version>
    </dependency>

Using Gradle:

    implementation 'com.republicate.kson:essential-kson:2.5'

### Parsing JSON

The generic `Json.parse(string_or_stream)` method will return a `com.republicate.kson.Json` value containing a `Json.Object` or `Json.Array` object (or throw a JsonException if parsing failed).

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

`toPrettyString()` methods will pretty-print the output.

### Converting to JSON

The reentrant method `Json.toJson(Any)` will try hard to convert any standard container to a JSON structure.

## Ktor content negotiation integration

### Ktor v1.x

Use the following code to use essential-kson with ktor 1.x content negotiation:

```kotlin

import com.republicate.kson.Json

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

### Ktor v2.x

Use the following code to use essential-kson with ktor 2.x content negotiation:

```kotlin

import com.republicate.kson.Json

object KsonConverter: ContentConverter {
    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val buffer = StringBuilder()
        var first = true
        while (true) {
            if (first) first = false
            else buffer.append('\n')
            if (!content.readUTF8LineTo(buffer)) break
        }
        return Json.parseValue(buffer.toString())
    }

    override suspend fun serializeNullable(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        if (value !is Json) throw IOException("content is not Json")
        return TextContent(value.toString(), ContentType.Application.Json)
    }
}

fun ContentNegotiationConfig.kson() {
    register(
        contentType = ContentType.Application.Json,
        converter = KsonConverter)
}


// and in ktor configuration section:

    install(ContentNegotiation) {
        kson()
    }


```

## References

+ [RFC 7159](https://tools.ietf.org/html/rfc7159)
+ [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite)
