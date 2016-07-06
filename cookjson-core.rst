cookjson-core
=============

cookjson-core currently contains parsers and generators for
`Json <http://www.json.org/>`__ and `BSON <http://www.json.org/>`__ formats.

Maven
-----

.. code-block:: xml

	<dependency>
		<groupId>org.yuanheng.cookjson</groupId>
		<artifactId>cookjson-core</artifactId>
		<version>1.0</version>
	</dependency>

CookJsonProvider
----------------

`CookJsonProvider` can be used to create JSON or BSON parsers or generators
depending on the config settings.  The parsers and generators created without
config settings are JSON specific.

+----------------+----------+--------------------------------------------------+
| Property Name  | Value    | Description                                      |
+----------------+----------+--------------------------------------------------+
| `format`       | `json`   | Creates JSON parser / generator                  |
|                +----------+--------------------------------------------------+
|                | `bson`   | Creates BSON parser / generator                  |
+----------------+----------+--------------------------------------------------+
| `binaryFormat` | `base64` | Uses MIME Base64 encoding fo binary              |
|                +----------+--------------------------------------------------+
|                | `hex`    | Uses hexadecimal format                          |
+----------------+----------+--------------------------------------------------+
| `comment`      | `true`   | Enables comments in JSON                         |
+----------------+----------+--------------------------------------------------+
| `rootAsArray`  | `true`   | Treats the root Document as array in BSON        | 
+----------------+----------+--------------------------------------------------+
| `useDouble`    | `true`   | Stores BigDecimal / BigInteger as double in BSON | 
+----------------+----------+--------------------------------------------------+

Examples 1: Creating a JsonParser for a JSON Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: java

	CookJsonProvider provider = new CookJsonProvider ();
	JsonParser p = provider.createParser (new FileInputStream (srcFile));

Examples 2: Creating a JsonParser for a JSON Document with Comments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: java

	JsonProvider provider = new CookJsonProvider ();
	HashMap<String, Object> config = new HashMap<String, Object> ();
	// Either Boolean.TRUE or "true" can be used.
	config.put (CookJsonProvider.COMMENT, Boolean.TRUE);
	JsonParser p = provider.createParserFactory (config).createParser (new FileInputStream (srcFile));

Examples 3: Creating a JsonParser for a BSON Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: java

	JsonProvider provider = new CookJsonProvider ();
	HashMap<String, Object> config = new HashMap<String, Object> ();
	config.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
	JsonParser p = provider.createParserFactory (config).createParser (new FileInputStream (srcFile));

.. include::	JSON.txt
.. include::	BSON.txt
