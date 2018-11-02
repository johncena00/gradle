The Gradle team is excited to announce Gradle 5.1.

This release features [Stricter validation with `validateTaskProperties`](userguide/java_gradle_plugin.html), [2](), ... [n](), and more.

We would like to thank the following community contributors to this release of Gradle:
[Mike Kobit](https://github.com/mkobit),
[Kent Fletcher](https://github.com/fletcher-sumglobal),
[Niklas Grebe](https://github.com/ThYpHo0n),
[Jonathan Leitschuh](https://github.com/JLLeitschuh),

## Stricter validation with `validateTaskProperties`

Cacheable tasks are validated stricter than non-cacheable tasks by the `validateTaskProperties` task, which is added automatically by the [`java-gradle-plugin`](userguide/java_gradle_plugin.html).
For example, all file inputs are required to have a normalization declared, like e.g. `@PathSensitive(RELATIVE)`.
This stricter validation can now be enabled for all tasks via [`validateTaskProperties.enableStricterValidation = true`](javadoc/org/gradle/plugin/devel/tasks/ValidateTaskProperties.html#setEnableStricterValidation-boolean-).

## Promoted features

Promoted features are features that were incubating in previous versions of Gradle but are now supported and subject to backwards compatibility.
See the User guide section on the “[Feature Lifecycle](userguide/feature_lifecycle.html)” for more information.

The following are the features that have been promoted in this Gradle release.

<!--
### Example promoted
-->

## Fixed issues

### Inherited configuration-wide dependency excludes are now published

Previously, only exclude rules directly declared on published configurations (e.g. `apiElements` and `runtimeElements` for the `java` component defined by the [Java Library Plugin](userguide/java_library_plugin.html#sec:java_library_configurations_graph)) were published in the Ivy descriptor and POM when using the [Ivy Publish Plugin](userguide/publishing_ivy.html) or [Maven Publish Plugins](userguide/publishing_maven.html), respectively.
Now, inherited exclude rules defined on extended configurations (e.g. `api` for Java libraries) are also taken into account.

## Known issues

## Deprecations

Features that have become superseded or irrelevant due to the natural evolution of Gradle become *deprecated*, and scheduled to be removed
in the next major Gradle version. See the User guide section on the “[Feature Lifecycle](userguide/feature_lifecycle.html)” for more information.

The following are the newly deprecated items in this Gradle release. If you have concerns about a deprecation, please raise it via the [Gradle Forums](https://discuss.gradle.org).

### Setters for `classes` and `classpath` on `ValidateTaskProperties`

There should not be setters for lazy properties like `ConfigurableFileCollection`s.
Use `setFrom` instead.

    validateTaskProperties.getClasses().setFrom(fileCollection)
    validateTaskProperties.getClasspath().setFrom(fileCollection)
    
### Breaking changes

<!-- add any notable changes here in a summary -->

See the [Gradle 5.x upgrade guide](userguide/upgrading_version_5.html) to learn about breaking changes and considerations for upgrading from Gradle 5.x.
    
## External contributions

We would like to thank the following community members for making contributions to this release of Gradle.

 - [Mike Kobit](https://github.com/mkobit) - Add missing `@Deprecated` annotations to `ProjectLayout` methods (gradle/gradle#7344)
 - [Kent Fletcher](https://github.com/fletcher-sumglobal) - Convert `WindowsResourceCompile` to use Provider API (gradle/gradle#7432)
 - [Niklas Grebe](https://github.com/ThYpHo0n) - Add more examples of dynamic versions to documentation (gradle/gradle#7417)
 - [Jonathan Leitschuh](https://github.com/JLLeitschuh) - Add Provider API types to `AbstractArchiveTask` task types (gradle/gradle#7435)

We love getting contributions from the Gradle community. For information on contributing, please see [gradle.org/contribute](https://gradle.org/contribute).

## Known issues

Known issues are problems that were discovered post release that are directly related to changes made in this release.
