# Medley

[![Build Status](https://img.shields.io/circleci/project/weavejester/medley/master.svg)](https://circleci.com/gh/weavejester/medley/tree/master)

Medley is a lightweight Clojure/ClojureScript library of useful,
*mostly* pure functions that are "missing" from clojure.core.

Medley has a tighter focus than [flatland/useful][] or [Plumbing][],
and limits itself to a small set of general-purpose functions.

[flatland/useful]: https://github.com/flatland/useful
[plumbing]:        https://github.com/plumatic/plumbing

## Installation

To install, add the following to your project `:dependencies`:

    [medley "0.8.2"]

## Breaking Changes

In 0.7.0 the minimum Clojure version was changed from 1.5.1 to 1.7.0
to take advantage of [reader conditionals][]. The `update` function
has also been removed, as it is now present in `clojure.core`.

In 0.6.0 the type signature of `greatest` and `least` was changed to
be more like `max` and `min` in Clojure core. If you're upgrading from
a version prior to 0.6.0, please update your usage of `greatest` and
`least`.

[reader conditionals]: http://dev.clojure.org/display/design/Reader+Conditionals

## Documentation

* [API Docs](http://weavejester.github.io/medley/medley.core.html)

## License

Copyright © 2016 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
