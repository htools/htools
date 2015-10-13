HTools
=========
This project contains a set of shared tools across projects:
- io: transparent reading/writing to local and Hadoop FS to allow the same application to run on both platforms (underlying implementations are for instance FSPth and HDFSPath)
- search: factory for fast search of regular expressions in byte arrays
- collection: many collection types that allow easy processing for specific purposes
- fcollection: many collection types that allow efficient processing for specific purposes
- extract: modular extraction pipeline that operates on raw byte arrays, for instance to remove noise, convert data, and extract tokens.
- compressed: support for compressed tar archives
- struct: support for writing structured files using records, e.g. xml, json, tsv, binary.
- web: simple multi-threaded crawler for web pages
- words: stemmers and stopword lists
- buffer: buffered reader and writer that transparently uses an in memory byte array or can be connected to a data stream, that supports reading an writing of a wide range of datatypes.
- hadoop: wraps hadoop classes with additional functionality
- hadoop.io: custom inputformat and outputformat to automatically setup jobs that read and write custom key-value classes
- hadoop.archivereader: configurable readers to process data stored in an archive format
- hadoop.backup: backup/update/restore a backup of files or folders on HDFS
- hbase: primitive HBase support
- lib: several (static) libraries with general purpose tools
