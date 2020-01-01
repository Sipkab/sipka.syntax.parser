# sipka.syntax.parser

A parser library written in Java. The rules can be specified in a DSL for the library. The library parses the input and directly produces a statement tree. The library supports incrementally repairing the statement tree after it has been parsed. This makes it useful when used inside an IDE.

**NOTE**: The library is not well optimized, and generally slow when parsing the input. It uses regexes and sometimes algorithms with suboptimal complexity. It lacks features such as reporting the syntax errors.

We don't recommend using it in scenarios where quick feedback or performance matters.

## Build instructions

The library uses the [saker.build system](https://saker.build) for building. Use the following command to build the project:

```
java -jar path/to/saker.build.jar -bd build compile saker.build
```

## Documentation

Incomplete/non-existent at the moment.

## License

TBD TODO