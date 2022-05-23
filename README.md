# jprocessor
Library that makes working with ASM and classes easier

# mappings
```java
        // load the mapping file
        MappingManager mappingManager = JProcessor.Mapping.load(MAPPINGS_FILE, MappingType.PROGUARD);

        // load the jar into memory
        MemoryJar memoryJar = JProcessor.Jar.load(JAR_FILE);

        // remap the jar
        memoryJar.reMap(mappingManager);

        // save the jar to the disk
        memoryJar.save(JAR_OUT_FILE);
```

# runtime jar editing
```java
        // load the jar into memory
        MemoryJar memoryJar = JProcessor.Jar.load(JAR_FILE);

        // check that the jar is loaded
        assert !memoryJar.isLoaded();

        // inject a test class into the jar
        MemoryClass cls = memoryJar.createClass(
                Opcodes.V1_8, Opcodes.ACC_PUBLIC, "me/mat/jprocessor/TestClass",
                null, null, null
        );

        // inject a string field into the injected class
        cls.addField(
                Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "testString", "Ljava/lang/String;",
                null, "Hello, World!"
        );

        // inject a method into the injected class
        cls.addMethod(
                Opcodes.ACC_PROTECTED, "testMethod", "()V",
                null, null
        );

        // save the jar to the output file
        memoryJar.save(JAR_OUT_FILE);
```

# adding the library to your project

**Gradle**

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	dependencies {
            implementation 'com.github.Mat1337:jprocessor:Tag'
	}
```

**Maven**

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

    <dependency>
        <groupId>com.github.Mat1337</groupId>
        <artifactId>jprocessor</artifactId>
        <version>Tag</version>
    </dependency>
```