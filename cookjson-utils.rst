cookjson-utils
==============

``cookjson-utils`` currently contains two command line utilities.

convertjson
-----------

``convertjson`` converts from a JSON format to a target format.  It
recognizes file types by extension.

* ``.json`` is JSON format.
* ``.bson`` is BSON format.

If the output format is BSON, the length information for Document / Array
are correctly generated as well so it can be safely dumped by bsondump.

Syntax
~~~~~~

.. code-block:: none

	usage: convertjson [options]
	  -a,--array         treat BSON root document as array.
	  -d,--double        use double for BSON to store BigDecimal / BigInteger.
	  -f,--from <file>   from file
	  -h,--help          print this message.
	  -n,--nofix         disable fixing of BSON lengths.
	  -p,--pretty        pretty output for JSON format.
	  -t,--to <file>     to file
	  -x,--hex           use hexadecimal instead of base64 to represent binary
	                     data.

Examples
~~~~~~~~

* Converting from JSON to BSON

.. code-block:: bash

	convertjson -f mydata.json -t mydata.bson

* Converting from BSON to JSON

.. code-block:: bash

	convertjson -f mydata.bson -t mydata.json

* Converting from BSON to JSON, treating the root of BSON as an array 

.. code-block:: bash

	convertjson -f mydata.bson -t mydata.json -a

fixbson
-------

This utility basically calculates and updates the length information
for ``Document`` and ``Array`` types in a BSON document.

Syntax
~~~~~~

.. code-block:: none

	usage: fixbson [options] [file]
	  -h,--help   print this message.

