# Contribution Guidelines

## Avoid Android Fragments and Activities
With [Flow](https://github.com/square/flow), the app achieves the maximum possible speed performance by using only Views. Avoid using Fragments whenever possible as explained [here](https://medium.com/square-corner-blog/advocating-against-android-fragments-81fd0b462c97) and [here](https://github.com/futurice/android-best-practices#activities-and-fragments). Views are preferred over Activities, as a complete Activity lifecycle is not needed in most use cases, and it is much slower to start an Activity using Intent than to attach a View.

## Stay within the dreadful DEX limit
The library dependencies used in the app are chosen carefully to avoid going beyond the DEX 65K limit. App cold-start time is thus reduced by as much as 50%.

## Be empathetic with fellow developers
Use [Lombok](https://projectlombok.org/), [AutoValue](https://github.com/google/auto/tree/master/value), [SpotBugs](https://spotbugs.github.io/) and [Checkstyle](http://checkstyle.sourceforge.net/) to help writing clean code and concise methods. Write as many tests as we can.

## Be open and/or free
Use open sourced technologies whenever possible. If not, use free services.
