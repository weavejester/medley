# Medley

[![Build Status](https://travis-ci.org/weavejester/medley.svg?branch=master)](https://travis-ci.org/weavejester/medley)

Medley is a lightweight Clojure/ClojureScript library of useful,
*mostly* pure functions that are "missing" from clojure.core.

Medley has a tighter focus than [flatland/useful][1] and
[Prismatic/plumbing][2], and limits itself to a small set of
general-purpose functions.

[1]: https://github.com/flatland/useful
[2]: https://github.com/prismatic/plumbing

## Installation

To install, add the following to your project `:dependencies`:

    [medley "0.7.4"]

## Breaking Changes

In 0.7.0 the minimum Clojure version was changed from 1.5.1 to 1.7.0
to take advantage of [reader conditionals][3]. The `update` function
has also been removed, as it is now present in `clojure.core`.

In 0.6.0 the type signature of `greatest` and `least` was changed to be more
like `max` and `min` in Clojure core. If you're upgrading from a version prior
to 0.6.0, please update your usage of `greatest` and `least`.

[3]: http://dev.clojure.org/display/design/Reader+Conditionals

## Documentation

* [API Docs](http://weavejester.github.io/medley/medley.core.html)

## License

Copyright © 2016 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
