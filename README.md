[![Build Status](https://travis-ci.com/Thomsch/metric-history.svg?token=kEZ3SvFYosMEzwAWUkVz&branch=master)](https://travis-ci.com/Thomsch/metric-history)
# MetricHistory
MetricHistory is an extensible tool designed to collect and process software
measurements across mutiple versions of a code base. The measurement itself is
modular and executed by a third party tool. The default analyzer is
[SourceMeter](https://www.sourcemeter.com/) which offers more than 52 metrics at
the project, package, class, and method level.

Features:
* Automated collection of measurements for multiple version of a project.
* Computation of the difference in metrics between two version of a project.

On top of these features, MetricHistory offers a collection of optional
utilities:
* Conversion of native analysis results into a easily readable reference format (RAW format).
* Retrieval of the parent versions of a list of versions.
* [Incubating] Exports to mongodb database.

## Running from command line
MetricHistory comes with a rich command line interface. Every command is
explained in the `help` command (`./metric-history help`).

### Example: Collecting the metrics of a list of versions with SourceMeter
`./metric-history collect versions.txt path/to/sourcemeter/executable myproject/ results/ myproject`
This command will analyse all the versions (and their parents) of _myproject_ using the 
analysis software specified (_analysis-software.exe_) specified in _versions.txt_ which contains simply a list of
commit ids for a GIT based project and store the results in the specified folder (_results/_) under a directory with
the name of the project (_myproject_).

## Using the API
Inspire yourself from the example in `org.metrichistory.cmd.*`, they all use the public API!

## Building
We use Gradle to build the project and resolve dependencies automatically.

## Contributing
Suggestions are welcomed! Try creating a new issue or submitting a pull request!
